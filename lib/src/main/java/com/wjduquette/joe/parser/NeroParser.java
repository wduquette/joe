package com.wjduquette.joe.parser;

import com.wjduquette.joe.nero.*;
import com.wjduquette.joe.scanner.Token;

import java.util.*;
import java.util.function.Supplier;

import static com.wjduquette.joe.nero.RuleEngine.*;
import static com.wjduquette.joe.scanner.TokenType.*;

/**
 * A parser for Nero, which is embedded in the Joe parser.
 * Intentionally package-private.
 */
class NeroParser extends EmbeddedParser {
    private static final String DEFINE = "define";
    private static final String TRANSIENT = "transient";

    //-------------------------------------------------------------------------
    // Types

    /**
     * Whether Nero is parsed as a standalone language, or as embedded in Joe.
     */
    public enum Mode {
        /** Mode */ STANDALONE,
        /** Mode */ EMBEDDED
    }

    // A parsed relation.  Includes the token, as location info, and the
    // name, which might include a '!'.
    private record Relation(Token token, String name) { }

    // An atom together with its relation token.
    private record AtomPair(Token token, Atom atom, String text) {
        String relation() { return atom.relation(); }
        Set<String> getVariableNames() { return atom.getVariableNames(); }
    }

    // The context in which atoms and terms are being parsed.  HEAD indicates
    // either an axiom or a rule head; we don't know which it will be
    // until after the atom is fully parsed.
    private enum Context {
        HEAD("axiom or head atom"),
        BODY("body atom"),
        CONSTRAINT("constraint");

        private final String place;

        Context(String place) { this.place = place; }

        String place() { return place; }
    }


    //-------------------------------------------------------------------------
    // Instance Variables

    // Standalone or embedded
    private final Mode mode;
    private Schema schema = null;

    //-------------------------------------------------------------------------
    // Constructor

    public NeroParser(Parser parent, Mode mode) {
        super(parent);
        this.mode = mode;
    }

    //-------------------------------------------------------------------------
    // API

    /**
     * Parses Nero code, using an appropriate condition for returning control
     * to the client.
     * @return The rule set
     */
    public NeroRuleSet parse() {
        this.schema = new Schema();
        return doParse();
    }

    //-------------------------------------------------------------------------
    // The Parser

    @SuppressWarnings("Convert2MethodRef")
    private NeroRuleSet doParse() {
        Supplier<Boolean> endCondition = mode == Mode.STANDALONE
            ? () -> scanner.isAtEnd()
            : () -> scanner.match(RIGHT_BRACE);
        Set<Atom> axioms = new HashSet<>();
        Set<Rule> rules = new HashSet<>();

        while (!endCondition.get()) {
            try {
                // define
                if (scanner.matchIdentifier(DEFINE)) {
                    defineDeclaration();
                    continue;
                }

                // transient
                if (scanner.matchIdentifier(TRANSIENT)) {
                    transientDeclaration();
                    continue;
                }

               var head = atom(Context.HEAD, false);

                if (scanner.match(SEMICOLON)) {
                    if (RuleEngine.isBuiltIn(head.relation())) {
                        throw errorSync(head.token(),
                            "found built-in predicate in axiom.");
                    }
                    if (!schema.hasRelation(head.relation())) {
                        throw errorSync(head.token(), "undefined relation in axiom.");
                    }
                    if (!schema.check(head.atom())) {
                        error(head.token(),
                            "schema mismatch, expected shape compatible with '" +
                            schema.get(head.relation()).toSpec() +
                            "', got: '" + head.text() + "'.");
                    }
                    axioms.add(axiom(head));
                } else if (scanner.match(COLON_MINUS)) {
                    if (RuleEngine.isBuiltIn(head.relation())) {
                        throw errorSync(head.token(),
                            "found built-in predicate in rule head.");
                    }
                    if (!schema.hasRelation(head.relation())) {
                        throw errorSync(head.token(),
                            "undefined relation in rule head.");
                    }
                    if (!schema.check(head.atom())) {
                        error(head.token(),
                            "schema mismatch, expected shape compatible with '" +
                                schema.get(head.relation()).toSpec() +
                                "', got: '" + head.text() + "'.");
                    }
                    rules.add(rule(head));
                } else {
                    scanner.advance();
                    throw errorSync(scanner.previous(),
                        "expected axiom or rule.");
                }
            } catch (Parser.ErrorSync error) {
                synchronize();
            }
        }

        return new NeroRuleSet(schema, axioms, rules);
    }

    private Relation relation(String message) {
        scanner.consume(IDENTIFIER, message);
        var token = scanner.previous();
        var name = scanner.match(BANG) ? token.lexeme() + "!" : token.lexeme();
        return new Relation(token, name);
    }

    private boolean hasBang(String name) {
        return name.endsWith("!");
    }

    private void defineDeclaration() {
        var transience = scanner.matchIdentifier(TRANSIENT);

        var relation = relation("expected relation after 'define [transient]'.");

        if (RuleEngine.isBuiltIn(relation.token().lexeme())) {
            throw errorSync(relation.token(),
                "found built-in predicate in 'define' declaration.");
        }


        scanner.consume(SLASH, "expected '/' after relation.");

        Shape shape;

        if (scanner.match(DOT_DOT_DOT)) {
            shape = new Shape(relation.name());
        } else if (scanner.check(IDENTIFIER)) {
            var names = new ArrayList<String>();
            do {
                scanner.consume(IDENTIFIER, "expected field name.");
                var name = scanner.previous().lexeme();
                if (names.contains(name)) {
                    error(scanner.previous(), "duplicate field name.");
                }
                names.add(name);
            } while (scanner.match(COMMA));

            shape = new Shape(relation.name(), names);
        } else {
            scanner.advance();
            throw errorSync(scanner.previous(),
                "expected arity, '...', or field names.");
        }
        scanner.consume(SEMICOLON, "expected ';' after definition.");

        if (schema.hasRelation(shape.relation())) {
            if (!schema.check(shape)) {
                error(relation.token(), "definition clashes with earlier entry.");
            }
        } else {
            schema.add(shape);
        }

        if (transience) schema.setTransient(relation.name(), true);
    }

    private void transientDeclaration() {
        var relation = relation("expected relation after 'transient'.");

        if (RuleEngine.isBuiltIn(relation.token().lexeme())) {
            throw errorSync(relation.token(),
                "found built-in predicate in 'transient' declaration.");
        }

        scanner.consume(SEMICOLON, "expected ';' after relation.");

        schema.setTransient(relation.name(), true);
    }

    private Atom axiom(AtomPair head) {

        if (head.atom().getAllTerms().stream().anyMatch(t -> t instanceof Aggregate)) {
            error(head.token(), "found aggregation function in axiom.");
        } else if (!head.getVariableNames().isEmpty()) {
            error(head.token(), "found variable in axiom.");
        }
        return head.atom();
    }

    private Rule rule(AtomPair head) {
        var pairs = new ArrayList<AtomPair>();
        var bodyAtoms = new ArrayList<Atom>();
        var constraints = new ArrayList<Constraint>();
        var bodyVars = new HashSet<String>();

        // FIRST, parse the rule's body atoms.
        do {
            var negated = scanner.match(NOT);
            var token = scanner.peek();
            var pair = atom(Context.BODY, negated);
            pairs.add(pair);

            if (hasBang(pair.relation()) && !hasBang(head.relation())) {
                error(token, "found update marker '!' in body atom of non-updating rule.");
            }

            if (negated) {
                for (var name : pair.getVariableNames()) {
                    if (!bodyVars.contains(name)) {
                        error(token,
                            "negated body atom contains unbound variable: '" +
                            name + "'.");
                    }
                }
            } else {
                if (RuleEngine.isBuiltIn(pair.relation())) {
                    checkBuiltIn(token, bodyVars, pair.atom());
                }
                bodyVars.addAll(pair.getVariableNames());
            }
            bodyAtoms.add(pair.atom());
        } while (scanner.match(COMMA));

        // NEXT, do global checks on the body atoms and head.
        // TODO

        // Verify that all head variables are bound in the body.
        if (!bodyVars.containsAll(head.getVariableNames())) {
            error(head.token(), "found unbound variable(s) in rule head.");
        }

        // Check any aggregators in the rule's head.
        checkAggregates(head);

        // NEXT, parse and check the constraints.
        if (scanner.match(WHERE)) {
            do {
                constraints.add(constraint(bodyVars));
            } while (scanner.match(COMMA));
        }

        scanner.consume(SEMICOLON, "expected ';' after rule body.");

        // FINALLY, return the parsed rule.
        return new Rule(head.atom(), bodyAtoms, constraints);
    }

    // Verify that there is at most one aggregate, and that it shares no
    // variable names with other head atoms.
    private void checkAggregates(AtomPair head) {
        var aggVars = new HashSet<String>();
        var others = new HashSet<String>();
        var count = 0;
        for (var term : head.atom().getAllTerms()) {
            if (term instanceof Aggregate a) {
                ++count;
                aggVars.addAll(a.names());
            } else {
                others.addAll(Term.getVariableNames(term));
            }
        }

        if (count > 1) {
            error(head.token(), "rule head contains more than one aggregation function.");
        } else if (count == 1) {
            aggVars.retainAll(others);
            if (!aggVars.isEmpty()) {
                error(head.token(), "aggregated variable(s) found elsewhere in rule head.");
            }
        }
    }

    // Verify that this is a valid built-in.
    private void checkBuiltIn(Token token, Set<String> bodyVars, Atom atom) {
        var shape = RuleEngine.getBuiltInShape(atom.relation());
        assert shape != null;
        if (!Shape.conformsTo(atom, shape)) {
            error(token, "expected " + shape.toSpec() + ", got: '" +
                atom + "'.");
            return;
        }

        switch (atom.relation()) {
            case MEMBER ->
                requireBound(token, bodyVars, atom, 1);
            case INDEXED_MEMBER, KEYED_MEMBER ->
                requireBound(token, bodyVars, atom, 2);
            case MAPS_TO -> {
                requireHasKnownValue(token, bodyVars, atom, 0);
                requireHasKnownValue(token, bodyVars, atom, 1);
            }
            default -> throw new IllegalStateException(
                "Unexpected built-in-predicate: '" + atom.relation() + "'.");
        }
    }

    // Checks whether the index'th term in the atom is a bound variable.
    private void requireBound(
        Token relation,
        Set<String> bodyVars,
        Atom atom,
        int index
    ) {
        // We've checked the shape of the atom, and it conforms to a
        // built-in predicate; therefore it is an OrderedAtom, and it
        // has the expected number of terms.
        assert atom instanceof ListAtom;
        var a = (ListAtom)atom;
        var term = a.terms().get(index);
        if (term instanceof Variable v && bodyVars.contains(v.name())) return;
        error(relation, "expected bound variable as term " + index +
            ", got: '" + term + "'.");
    }

    // Checks whether the index'th term in the atom is either a bound variable
    // or a constant.
    private void requireHasKnownValue(
        Token relation,
        Set<String> bodyVars,
        Atom atom,
        int index
    ) {
        // We've checked the shape of the atom, and it conforms to a
        // built-in predicate; therefore it is an OrderedAtom, and it
        // has the expected number of terms.
        assert atom instanceof ListAtom;
        var a = (ListAtom)atom;
        var term = a.terms().get(index);
        if (term instanceof Constant ||
            (term instanceof Variable v && bodyVars.contains(v.name()))
        ) {
            return;
        }
        error(relation, "expected bound variable or constant as term " + index +
            ", got: '" + term + "'.");
    }

    private Constraint constraint(Set<String> bodyVar) {
        var term = term(Context.CONSTRAINT);
        Constraint.Op op;
        Variable a = null;

        switch (term) {
            case Constant ignored ->
                error(scanner.previous(), "expected bound variable.");
            case Variable v -> {
                a = v;
                if (!bodyVar.contains(v.name())) {
                    error(scanner.previous(), "expected bound variable.");
                }
            }
            case Wildcard ignored ->
                error(scanner.previous(), "expected bound variable.");
            default -> throw new UnsupportedOperationException("TODO");
        }

        if (scanner.match(BANG_EQUAL)) {
            op = Constraint.Op.NE;
        } else if (scanner.match(EQUAL_EQUAL)) {
            op = Constraint.Op.EQ;
        } else if (scanner.match(GREATER)) {
            op = Constraint.Op.GT;
        } else if (scanner.match(GREATER_EQUAL)) {
            op = Constraint.Op.GE;
        } else if (scanner.match(LESS)) {
            op = Constraint.Op.LT;
        } else if (scanner.match(LESS_EQUAL)) {
            op = Constraint.Op.LE;
        } else {
            scanner.advance();
            throw errorSync(scanner.previous(), "expected comparison operator.");
        }

        var b = term(Context.CONSTRAINT);

        if (b instanceof Variable v) {
            if (!bodyVar.contains(v.name())) {
                error(scanner.previous(),
                    "expected bound variable or constant.");
            }
        } else if (b instanceof Wildcard) {
            error(scanner.previous(), "expected bound variable or constant.");
        }

        return new Constraint(a, op, b);
    }

    private AtomPair atom(Context ctx, boolean negated) {
        // NEXT, parse the atom.
        var token = scanner.peek();
        var start = token.span().start();
        var relation = relation("expected relation.");
        scanner.consume(LEFT_PAREN, "expected '(' after relation.");

        var atom = scanner.checkTwo(IDENTIFIER, COLON)
            ? mapAtom(ctx, negated, relation.name())
            : listAtom(ctx, negated, relation.name());
        var end = scanner.previous().span().end();
        var text = parent.source().span(start, end).text();
        return new AtomPair(token, atom, text);
    }

    private Atom listAtom(Context ctx, boolean negated, String relation) {
        var terms = new ArrayList<Term>();

        do {
            terms.add(term(ctx));
        } while (scanner.match(COMMA));

        scanner.consume(RIGHT_PAREN, "expected ')' after terms.");

        return new ListAtom(negated, relation, terms);
    }

    private MapAtom mapAtom(Context ctx, boolean negated, String relation) {
        var terms = new LinkedHashMap<String,Term>();

        do {
            scanner.consume(IDENTIFIER, "expected field name.");
            var name = scanner.previous();
            scanner.consume(COLON, "expected ':' after field name.");
            terms.put(name.lexeme(), term(ctx));
        } while (scanner.match(COMMA));

        scanner.consume(RIGHT_PAREN, "expected ')' after terms.");

        return new MapAtom(negated, relation, terms);
    }

    private Term term(Context ctx) {
        if (ctx == Context.HEAD && scanner.checkTwo(IDENTIFIER, LEFT_PAREN)) {
            return aggregate();
        } else if (ctx == Context.BODY && scanner.checkTwo(IDENTIFIER, LEFT_PAREN)) {
            return patternTerm();
        } else if (ctx == Context.BODY && scanner.checkTwo(IDENTIFIER, AT)) {
            return patternTerm();
        } else if (scanner.match(IDENTIFIER)) {
            var name = scanner.previous();

            if (!name.lexeme().startsWith("_")) {
                return new Variable(name.lexeme());
            } else {
                if (ctx != Context.BODY) {
                    error(name, "found wildcard in " + ctx.place() + ".");
                }
                return new Wildcard(name.lexeme());
            }
        } else if (scanner.match(MINUS)) {
            scanner.consume(NUMBER, "expected number after '-'.");
            var number = (Double)scanner.previous().literal();
            return new Constant(-number);
        } else if (scanner.match(TRUE)) {
            return new Constant(true);
        } else if (scanner.match(FALSE)) {
            return new Constant(false);
        } else if (scanner.match(NULL)) {
            return new Constant(null);
        } else if (scanner.match(KEYWORD, NUMBER, STRING)) {
            return new Constant(scanner.previous().literal());
        } else if (ctx == Context.HEAD && scanner.match(LEFT_BRACKET)) {
            return listTerm();
        } else if (ctx == Context.BODY && scanner.check(LEFT_BRACKET)) {
            return patternTerm();
        } else if (scanner.match(LEFT_BRACKET)) {
            throw errorSync(scanner.previous(),
                "found collection literal in " + ctx.place() + ".");
        } else if (ctx == Context.HEAD && scanner.match(LEFT_BRACE)) {
            return setOrMapTerm(ctx);
        } else if (ctx == Context.BODY && scanner.check(LEFT_BRACE)) {
            return patternTerm();
        } else if (scanner.match(LEFT_BRACE)) {
            throw errorSync(scanner.previous(),
                "found collection literal in " + ctx.place() + ".");
        } else {
            scanner.advance();
            throw errorSync(scanner.previous(), "expected term.");
        }
    }

    private Term aggregate() {
        scanner.advance(); // Past name
        var name = scanner.previous();
        var aggregator = Aggregator.find(name.lexeme());
        scanner.advance(); // Past '('

        if (aggregator == null) {
            throw errorSync(name, "unknown aggregation function.");
        }

        var names = new ArrayList<String>();
        do {
            scanner.consume(IDENTIFIER, "expected aggregation variable name.");
            names.add(scanner.previous().lexeme());
        } while (scanner.match(COMMA));
        scanner.consume(RIGHT_PAREN, "expected ')' after aggregation variable name(s).");

        if (names.size() != aggregator.arity()) {
            error(name, "expected " + aggregator.arity() + " variable name(s).");
        }
        return new Aggregate(aggregator, names);
    }

    private Term listTerm() {
        // '[' has already been matched.
        var list = new ArrayList<Term>();

        do {
            if (scanner.check(RIGHT_BRACKET)) break;
            list.add(term(Context.HEAD));

        } while (scanner.match(COMMA));
        scanner.consume(RIGHT_BRACKET, "expected ']' after list items.");

        return new ListTerm(list);
    }

    // Matches a destructuring pattern and returns it as a PatternTerm.
    // The pattern will not be a Pattern.Constant, Pattern.Variable, or
    // Pattern.Wildcard, as these are parsed as the equivalent Nero terms.
    // Further, the pattern will not be or contain any Pattern.Expressions,
    // as Nero does not support them.
    private Term patternTerm() {
        var patternParser = new PatternParser(parent, PatternParser.Mode.NERO);
        var ast = patternParser.parse();
        return new PatternTerm(ast.getPattern());
    }

    private Term setOrMapTerm(Context ctx) {
        // Empty terms
        if (scanner.match(RIGHT_BRACE)) {
            return new SetTerm(List.of());
        } else if (scanner.match(COLON)) {
            scanner.consume(RIGHT_BRACE,
                "expected '}' after empty map literal.");
            return new MapTerm(List.of());
        }

        var first = term(ctx);

        if (scanner.match(COLON)) {
            return mapTerm(first);
        } else {
            return setTerm(first);
        }

    }

    private Term setTerm(Term first) {
        var list = new ArrayList<Term>();
        list.add(first);

        while (scanner.match(COMMA)) {
            if (scanner.check(RIGHT_BRACE)) break;
            list.add(term(Context.HEAD));

        }
        scanner.consume(RIGHT_BRACE, "expected '}' after set items.");

        return new SetTerm(list);
    }

    private Term mapTerm(Term first) {
        var list = new ArrayList<Term>();
        list.add(first);
        // Colon is already matched
        list.add(term(Context.HEAD));

        while (scanner.match(COMMA)) {
            if (scanner.check(RIGHT_BRACE)) break;
            list.add(term(Context.HEAD));
            scanner.consume(COLON, "expected ':' after key term.");
            list.add(term(Context.HEAD));
        }
        scanner.consume(RIGHT_BRACE, "expected '}' after map items.");

        return new MapTerm(list);
    }

    // Discard tokens until we are at the beginning of the next statement.
    // For Nero code, that just means whatever follows the next semicolon.
    private void synchronize() {
        try {
            parent.setSynchronizing(true);

            // Discard this token
            scanner.advance();

            while (!scanner.isAtEnd()) {
                // If we see we just completed a statement, return.
                if (scanner.previous().type() == SEMICOLON) return;

                // Discard this token.
                scanner.advance();
            }
        } finally {
            parent.setSynchronizing(false);
        }
    }

}
