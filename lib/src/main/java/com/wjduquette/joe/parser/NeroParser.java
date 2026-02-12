package com.wjduquette.joe.parser;

import com.wjduquette.joe.nero.*;
import com.wjduquette.joe.scanner.Token;

import java.util.*;
import java.util.function.Supplier;

import static com.wjduquette.joe.scanner.TokenType.*;

/**
 * A parser for Nero, which is embedded in the Joe parser.
 * Intentionally package-private.
 */
class NeroParser extends EmbeddedParser {
    private static final String DEFINE = "define";
    private static final String TRANSIENT = "transient";
    private static final List<String> DEFAULT_FIELD_NAMES = List.of(
        "a", "b", "c", "d", "e", "f", "g", "h", "i"
    );

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
                    axioms.add(axiom(head));
                } else if (scanner.match(COLON_MINUS)) {
                    rules.add(rule(head));
                } else {
                    scanner.advance();
                    throw errorSync(scanner.previous(),
                        "expected declaration, axiom, or rule.");
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
        } else if (scanner.match(NUMBER)) {
            var token = scanner.previous();
            var max = DEFAULT_FIELD_NAMES.size();
            var num = (Double)token.literal();
            if (token.lexeme().contains(".") || num < 1 || num > max) {
                throw errorSync(scanner.previous(),
                    "expected integer arity in range 1..." + max + ".");
            }
            var names = DEFAULT_FIELD_NAMES.subList(0, num.intValue());
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
        // FIRST, do checks that apply to both axioms and rule heads.
        checkAxiomOrHead(head, "axiom");

        // An axiom relation must be a normal relation.
        if (RuleEngine.isBuiltIn(head.relation())) {
            throw errorSync(head.token(),
                "found built-in predicate in axiom.");
        }

        // Axioms may not contain aggregation functions or variables.
        if (head.atom().getAllTerms().stream().anyMatch(t -> t instanceof Aggregate)) {
            error(head.token(), "found aggregation function in axiom.");
        } else if (!head.getVariableNames().isEmpty()) {
            error(head.token(), "found variable in axiom.");
        }

        // Return the atom.
        return head.atom();
    }

    private Rule rule(AtomPair head) {
        // FIRST, check the rule head, insofar as we can at this point.
        checkRuleHead(head);

        // NEXT, parse the rule's body atoms.
        var pairs = new ArrayList<AtomPair>();
        do {
            pairs.add(atom(Context.BODY, scanner.match(NOT)));
        } while (scanner.match(COMMA));

        // NEXT, do global checks on the body atoms and head.
        var bodyVars = checkBodyAtoms(head, pairs);

        // NEXT, parse and check the constraints.
        var constraints = new ArrayList<Constraint>();
        if (scanner.match(WHERE)) {
            do {
                constraints.add(constraint(bodyVars));
            } while (scanner.match(COMMA));
        }

        scanner.consume(SEMICOLON, "expected ';' after rule body.");

        // FINALLY, return the parsed rule.
        return new Rule(
            head.atom(),
            pairs.stream().map(AtomPair::atom).toList(),
            constraints);
    }

    // Performs checks for both axioms and rule heads
    private void checkAxiomOrHead(AtomPair head, String where) {
        if (RuleEngine.isBuiltIn(head.relation())) {
            throw errorSync(head.token(),
                "found built-in predicate in " + where + ".");
        }

        if (!schema.hasRelation(head.relation())) {
            switch (head.atom()) {
                case ListAtom a -> {
                    if (a.terms().size() > DEFAULT_FIELD_NAMES.size()) {
                        throw errorSync(head.token(),
                            "cannot infer shape, atom with undefined relation has too many fields.");
                    }

                    schema.add(new Shape(head.relation(),
                        DEFAULT_FIELD_NAMES.subList(0, a.terms().size())));
                }
                case MapAtom a -> {
                    schema.add(new Shape(head.relation()));
                }
            }
            return;
        }

        if (!schema.check(head.atom())) {
            error(head.token(),
                "schema mismatch, expected shape compatible with '" +
                    schema.get(head.relation()).toSpec() +
                    "', got: '" + head.text() + "'.");
        }
    }

    // Verify that there is at most one aggregate, and that it shares no
    // variable names with other head atoms.
    private void checkRuleHead(AtomPair head) {
        // FIRST, do checks that apply to both axioms and rule heads.
        checkAxiomOrHead(head, "rule head");

        // NEXT, check any aggregation functions in the head: no more than one,
        // and an aggregated variable can appear only in the function.
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

    private Set<String> checkBodyAtoms(AtomPair head, List<AtomPair> pairs) {
        // FIRST, loop over the atoms, checking left-to-right binding.
        var bodyVars = new HashSet<String>();
        var defaultedVars = new HashSet<String>();

        for (var pair : pairs) {
            // No update atoms in body of non-updating rule
            if (hasBang(pair.relation()) && !hasBang(head.relation())) {
                error(pair.token(),
                    "found update marker '!' in body atom of non-updating rule.");
            }

            if (pair.atom().hasDefaults()) {
                if (RuleEngine.isBuiltIn(pair.relation())) {
                    error(pair.token(),
                        "found variable with default value in built-in predicate.");
                } else if (pair.atom().isNegated()) {
                    error(pair.token(),
                        "found variable with default value in negated atom.");
                } else {
                    for (var t1 : pair.atom().getAllTerms()) {
                        if (t1 instanceof VariableWithDefault vwd) {
                            defaultedVars.add(vwd.variable().name());

                            if (vwd.value() instanceof Variable v) {
                                if (!bodyVars.contains(v.name())) {
                                    error(pair.token(),
                                        "default value has unbound variable: '" +
                                            v.name() + "'.");
                                }
                            }
                        }
                    }
                }
            }

            // No unbound variables in negated atoms.
            if (pair.atom().isNegated()) {
                for (var name : pair.getVariableNames()) {
                    if (!bodyVars.contains(name)) {
                        error(pair.token(),
                            "negated atom contains unbound variable: '" +
                            name + "'.");
                    }
                }
            }

            // Check built-in predicate term modes.
            if (RuleEngine.isBuiltIn(pair.relation())) {
                checkBuiltIn(pair.token(), bodyVars, pair.atom());
            }

            // Save this atom's variables.
            bodyVars.addAll(pair.getVariableNames());
        }

        // NEXT, verify that defaulted variables appear only once in the
        // body atoms, at the point of definition.
        var seen = new HashSet<String>();
        for (var pair : pairs) {
            for (var t : pair.atom().getAllTerms()) {
                if (t instanceof VariableWithDefault vwd) {
                    var name = vwd.variable().name();
                    if (seen.contains(name)) {
                        error(pair.token(),
                            "variable has multiple default values: '"
                                + name + "'.");
                    }
                    seen.add(name);
                    t = vwd.value();
                }

                for (var v : Term.getVariableNames(t)) {
                    if (defaultedVars.contains(v)) {
                        error(pair.token(),
                            "defaulted variable is referenced in body atom(s): '" +
                            v + "'.");
                    }
                }
            }
        }

        // NEXT, Verify that all head variables are bound in the body.
        if (!bodyVars.containsAll(head.getVariableNames())) {
            error(head.token(), "found unbound variable(s) in rule head.");
        }

        return bodyVars;
    }

    // Verify that this is a valid built-in.
    private void checkBuiltIn(Token token, Set<String> bodyVars, Atom predicate) {
        assert predicate instanceof ListAtom;
        var atom = (ListAtom)predicate;

        var builtIn = RuleEngine.getBuiltIn(atom.relation());
        if (builtIn == null) {
            throw new IllegalStateException(
                "Unexpected built-in-predicate: '" + atom.relation() + "'.");
        }

        // Check shape
        if (!Shape.conformsTo(atom, builtIn.shape())) {
            error(token, "expected " + builtIn.shape().toSpec() + ", got: '" +
                atom + "'.");
            return;
        }

        // Check TermModes
        var modes = builtIn.modes();
        for (var i = 0; i < modes.size(); i++) {
            if (modes.get(i) == TermMode.IN) {
                var term = atom.terms().get(i);
                if (term instanceof Constant) continue;
                if (term instanceof Variable v && bodyVars.contains(v.name())) continue;
                error(token,
                    "expected bound variable or constant for term '" +
                    builtIn.shape().names().get(i) +
                    "', got: '" + term + "'.");
            }
        }
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
                var variable = new Variable(name.lexeme());

                if (scanner.match(PIPE)) {
                    var pipe = scanner.previous();
                    if (ctx == Context.BODY) {
                        var token = scanner.peek();
                        var value = term(ctx);

                        if (!(value instanceof Variable) &&
                            !(value instanceof Constant)
                        ) {
                            error(token, "expected variable or constant as default value.");
                        }
                        return new VariableWithDefault(variable, value);
                    } else {
                        throw errorSync(pipe,
                            "found default variable syntax in " + ctx.place() + ".");
                    }
                }

                return variable;
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
