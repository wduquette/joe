package com.wjduquette.joe.nero;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.Keyword;
import com.wjduquette.joe.patterns.Matcher;
import com.wjduquette.joe.types.ListValue;
import com.wjduquette.joe.types.MapValue;
import com.wjduquette.joe.types.SetValue;
import com.wjduquette.joe.util.Bindings;

import java.util.*;
import java.util.function.Function;

/**
 * The Nero inference engine.  Given a {@link NeroRuleSet} and a set of
 * input {@link Fact Facts}, the engine will infer the facts implied by
 * the two.
 */
public class RuleEngine {
    /**
     * The variable name used in Bindings for the result of an aggregation
     * function.  Intentionally package-private.
     */
    static final String AGGREGATE = "*aggregate*";

    /**
     * A sentinel value used when map(k,v) aggregation finds a duplicate key
     * with two different values.
     */
    public static Object DUPLICATE_KEY = new Keyword("duplicateKey");

    //-------------------------------------------------------------------------
    // Static Built-In Predicate Schema

    private interface BuiltInFunction {
        Set<Fact> compute(BindingContext bc, Atom builtIn);
    }

    private static final Map<String,Shape> BUILT_INS = new HashMap<>();

    /** Name of the "member" built-in predicate. */
    public static final String MEMBER = "member";

    /** Name of the "indexedMember" built-in predicate. */
    public static final String INDEXED_MEMBER = "indexedMember";

    /** Name of the "keyedMember" built-in predicate. */
    public static final String KEYED_MEMBER = "keyedMember";

    /** Name of the "equivalent" built-in predicate. */
    public static final String EQUIVALENT = "equivalent";

    /** Name of the "str2num" equivalence. */
    public static final Keyword STR2NUM = new Keyword("str2num");

    static {
        builtIn(MEMBER, List.of("item", "collection"));
        builtIn(INDEXED_MEMBER, List.of("index", "item", "list"));
        builtIn(KEYED_MEMBER, List.of("key", "value", "map"));
        builtIn(EQUIVALENT, List.of("equivalence", "a", "b"));
    }

    private static void builtIn(String name, List<String> fields) {
        var shape = new Shape.PairShape(name, fields);
        BUILT_INS.put(shape.relation(), shape);
    }

    /**
     * Returns true if the relation names a built-in predicate, and false
     * otherwise.  All built-in predicates have names with an
     * initial lowercase letter.
     * @param relation The relation
     * @return true or false
     */
    public static boolean isBuiltIn(String relation) {
        return BUILT_INS.get(relation) != null;
    }

    /**
     * Gets the relation's shape from the built-in predicate's schema.
     * @param relation The relation
     * @return The shape or null.
     */
    public static Shape getBuiltInShape(String relation) {
        return BUILT_INS.get(relation);
    }

    //-------------------------------------------------------------------------
    // Instance Variables

    //
    // Constructor Data
    //

    // The Joe instance, for `Pattern` matching
    private final Joe joe;

    // The Nero rule set, i.e., the compiled Nero program.
    private final NeroRuleSet ruleset;

    // Map from head relation to rules with that head.
    private final Map<String,List<Rule>> ruleMap = new HashMap<>();

    // The built-in predicate functions, by predicate name
    private final Map<String,BuiltInFunction> builtIns;

    //
    // Configuration Data
    //

    private final Map<Keyword, Equivalence> equivalences =
        new HashMap<>();

    // Debug Flag
    private boolean debug = false;

    //
    // Working Data
    //

    // The set of known facts, initialized by the constructor
    private final FactSet knownFacts;

    // Whether the infer() method has been called or not.  We only do
    // inference once per instance of the engine.
    private boolean inferenceComplete = false;

    // Facts inferred from the rule set's axioms and rules (aka,
    // the "intensional database" in Datalog jargon).
    private final FactSet inferredFacts = new FactSet();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a new RuleEngine with the given ruleset and initial fact
     * database, which may be empty.  <b>The infer() method will update the
     * fact database in place;</b> make an explicit copy if this is not wanted.
     *
     * <p><b>Note:</b> this constructor is intended for internal use only;</p>
     * see {@link Nero} for the public API.
     * @param joe The related Joe interpreter.
     * @param ruleset The ruleset
     * @param db The database of facts
     */
    RuleEngine(Joe joe, NeroRuleSet ruleset, FactSet db) {
        this.joe = joe;
        this.ruleset = ruleset;
        this.knownFacts = db;
        this.builtIns = Map.of(
            MEMBER,         this::_member,
            INDEXED_MEMBER, this::_indexedMember,
            KEYED_MEMBER,   this::_keyedMember,
            EQUIVALENT,     this::_equivalent
        );

        // Define the predefined equivalences.
        this.equivalences.put(STR2NUM, new LambdaEquivalence(
            STR2NUM, this::string2number, this::number2string
        ));

        // NEXT, Categorize the rules by head relation
        for (var rule : ruleset.rules()) {
            var head = rule.head().relation();
            var list = ruleMap.computeIfAbsent(head, k -> new ArrayList<>());
            list.add(rule);
        }
    }

    //-------------------------------------------------------------------------
    // Configuration

    /**
     * Gets whether debugging is enabled or not.
     * @return true or false
     */
    @SuppressWarnings("unused")
    public boolean isDebug() {
        return debug;
    }

    /**
     * Enables/disables debugging.  When debugging is enabled, Nero will
     * dump a great deal of debugging information that can be used to
     * debug the rule set, and also Nero itself.
     * @param debug true or false
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    //-------------------------------------------------------------------------
    // Inference

    /**
     * Executes the inference algorithm, updating the given fact set in place.
     * @return The inferred facts.
     * @throws JoeError if the rule set is not stratified.
     */
    public FactSet infer() {
        // FIRST, check stratification
        if (!ruleset.isStratified()) {
            throw new JoeError("Rule set is not stratified.");
        }

        // NEXT, check for rule/inputs relation collision
        for (var relation : knownFacts.getRelations()) {
            if (ruleMap.containsKey(relation)) {
                throw new JoeError(
                    "Rule head relation collides with input fact relation: '" +
                        relation + "'.");
            }
        }

        // NEXT, only do inference once.
        if (inferenceComplete) return inferredFacts;
        inferenceComplete = true;

        // NEXT, infer all axioms.
        for (var axiom : ruleset.axioms()) {
            var fact = axiom2fact(axiom);
            knownFacts.add(fact);
            inferredFacts.add(fact);
        }

        // NEXT, execute the rules.
        if (debug) {
            System.out.println("Rule Strata: " +
                ruleset.strata());
        }

        for (var i = 0; i < ruleset.strata().size(); i++) {
            inferStratum(i, ruleset.strata().get(i));
        }

        // NEXT, drop the transient relations.
        for (var name : ruleset.schema().getTransients()) {
            knownFacts.drop(name);
            inferredFacts.drop(name);
        }

        // NEXT, handle update semantics
        for (var name : ruleset.getRelations()) {
            if (!name.endsWith("!")) continue;

            var oldName = name.substring(0, name.length() - 1);
            knownFacts.drop(oldName);
            knownFacts.rename(name, oldName);
            inferredFacts.drop(oldName);
            inferredFacts.rename(name, oldName);
        }

        return inferredFacts;
    }

    private void inferStratum(int stratum, List<String> heads) {
        int count = 0;
        boolean gotNewFact;
        do {
            gotNewFact = false;
            if (debug) System.out.println("Iteration " + stratum + "." + (++count) + ":");
            for (var head : heads) {
                for (var rule : ruleMap.get(head)) {
                    if (matchRule(rule)) gotNewFact = true;
                }
            }
        } while (gotNewFact);
    }

    // Matches the rule against the known facts, adding any new facts and
    // returning true if there were any and false otherwise.
    private boolean matchRule(Rule rule) {
        if (debug) System.out.println("  Rule: " + rule);

        // FIRST, match the rule against the known facts and find matches.
        var bc = new BindingContext(rule,
            ruleset.schema().get(rule.head().relation()));

        matchNextBodyAtom(bc, 0);

        if (bc.matches.isEmpty()) return false;

        // NEXT, do any aggregation
        var aggregatedMatches = aggregate(bc);

        // NEXT, convert the matches into facts, and see if we've got anything
        // new
        var gotNew = false;
        for (var bindings : aggregatedMatches) {
            bc.bindings = bindings;
            var newFact = createFact(bc);
            if (knownFacts.add(newFact)) {
                inferredFacts.add(newFact);
                gotNew = true;
            }
        }
        return gotNew;
    }

    // Matches the rule's index-th body atom against the relevant facts.
    private void matchNextBodyAtom(BindingContext bc, int index) {
        var atom = bc.rule.body().get(index);
        Set<Fact> facts = factsForAtom(bc, atom);

        // FIRST, Save the current bindings, as we will begin with them for each
        // fact.
        var givenBindings = bc.bindings;

        for (var fact : facts) {
            // FIRST, Copy the bindings for this fact.
            bc.bindings = new Bindings(givenBindings);

            // NEXT, if it doesn't match, go on to the next fact.
//            System.out.println("Checking: " + atom + " matches " + fact);
            if (!matchAtom(atom, fact, bc)) {
//                System.out.println("  No match");
                continue;
            }

            // NEXT, it matches.  If there's another body atom, check it and
            // then go on to the next fact.
            if (index + 1 < bc.rule.body().size()) {
                matchNextBodyAtom(bc, index + 1);
                continue;
            }

            // NEXT, we've matched all body atoms.  Check the bindings against
            // the constraints.  If they are not met, continue with the
            // next fact.
            if (!constraintsMet(bc)) continue;

            // NEXT, check each negation.
            if (!checkNegations(bc)) continue;

            // NEXT, the rule has matched.  Build the inferred fact, and see
            // if it's actually new.
            bc.matches.add(bc.bindings);
        }
    }

    private Set<Fact> factsForAtom(BindingContext bc, Atom atom) {
        if (isBuiltIn(atom.relation())) {
            // The NeroParser ensures that atom conforms to the built-in's shape.
            return builtIns.get(atom.relation()).compute(bc, atom);
        } else {
            return knownFacts.relation(atom.relation());
        }
    }

    // Attempts to match the atom and the fact, given the current bindings.
    // Returns true on success and false on failure.  Any new bindings are
    // added to the current bindings.
    private boolean matchAtom(
        Atom bodyAtom,
        Fact fact,
        BindingContext bc
    ) {
         switch (bodyAtom) {
             case NamedAtom atom -> {
                 for (var e : atom.termMap().entrySet()) {
                     var name = e.getKey();

                     if (!fact.getFieldMap().containsKey(name)) {
                         return false;
                     }
                     var f = fact.getFieldMap().get(name);
                     var t = e.getValue();
                     if (!matchTerm(t, f, bc)) return false;
                 }
                 return true;
             }
             case OrderedAtom atom -> {
                 if (!fact.isOrdered()) {
                     throw new JoeError(
                         "'" + atom.relation() +
                             "' in rule '" + bc.rule +
                             "' requires ordered fields, but a provided " +
                             "fact is not ordered.");
                 }

                 var n = atom.terms().size();
                 if (fact.getFields().size() != n) return false;

                 for (var i = 0; i < atom.terms().size(); i++) {
                     var t = atom.terms().get(i);
                     var f = fact.getFields().get(i);

                     if (!matchTerm(t, f, bc)) return false;
                 }
                 return true;
             }
         }
    }

    private boolean matchTerm(
        Term term,
        Object value,
        BindingContext bc
    ) {
        return switch (term) {
            case Constant c -> {
                if (Objects.equals(value, c.value())) {
                    yield true;
                }
                if (value instanceof Enum<?> e &&
                    c.value() instanceof Keyword k) {
                    yield e.name().equalsIgnoreCase(k.name());
                }
                yield false;
            }
            case PatternTerm pt ->
                Matcher.matchWith(joe, pt.pattern(), value, null, bc.bindings);
            case Variable v -> {
                var bound = bc.bindings.get(v.name());

                if (bound == null) {
                    bc.bindings.bind(v.name(), value);
                    yield true;
                } else {
                    yield Objects.equals(bound, value);
                }
            }
            case Wildcard ignored -> true;
            default -> throw new IllegalStateException(
                "Unexpected term type in body atom: '" + term + "'.");
        };
    }

    private boolean constraintsMet(BindingContext bc) {
        for (var constraint : bc.rule.constraints()) {
            if (!constraintMet(constraint, bc.bindings)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkNegations(BindingContext bc) {
        for (var atom : bc.rule.negations()) {
            for (var fact : factsForAtom(bc, atom)) {
                if (matchAtom(atom, fact, bc)) {
                    return false;
                }
            }
        }
        return true;
    }

    private Fact axiom2fact(Atom axiom) {
        return switch (axiom) {
            case NamedAtom atom -> {
                var termMap = new HashMap<String,Object>();

                for (var e : atom.termMap().entrySet()) {
                    termMap.put(e.getKey(), Term.toValue(e.getValue(), null));
                }

                yield new MapFact(atom.relation(), termMap);
            }
            case OrderedAtom atom -> {
                var shape = ruleset.schema().get(atom.relation());
                var terms = new ArrayList<>();

                for (var term : atom.terms()) {
                    terms.add(Term.toValue(term, null));
                }
                if (shape instanceof Shape.PairShape ps) {
                    yield new PairFact(atom.relation(), ps.fieldNames(), terms);
                } else {
                    yield new ListFact(atom.relation(), terms);
                }
            }
        };
    }

    private Fact createFact(BindingContext bc) {
        return switch (bc.rule.head()) {
            case NamedAtom atom -> {
                var terms = new HashMap<String,Object>();

                for (var e : atom.termMap().entrySet()) {
                    terms.put(e.getKey(), term2value(e.getValue(), bc));
                }

                yield new MapFact(atom.relation(), terms);
            }
            case OrderedAtom atom -> {
                var terms = new ArrayList<>();

                for (var term : atom.terms()) {
                    terms.add(term2value(term, bc));
                }
                if (bc.shape instanceof Shape.PairShape ps) {
                    yield new PairFact(atom.relation(), ps.fieldNames(), terms);
                } else {
                    yield new ListFact(atom.relation(), terms);
                }
            }
        };
    }

    private Object term2value(Term term, BindingContext bc) {
        return Term.toValue(term, bc.bindings);
    }

    private boolean constraintMet(
        Constraint constraint,
        Bindings bindings
    ) {
        var a = Term.toValue(constraint.a(), bindings);
        var b = Term.toValue(constraint.b(), bindings);

        return switch (constraint.op()) {
            case EQ -> Objects.equals(a, b);
            case NE -> !Objects.equals(a, b);
            case GT -> {
                if (a instanceof Double d1 && b instanceof Double d2) {
                    yield d1 > d2;
                } else if (a instanceof String s1 && b instanceof String s2) {
                    yield s1.compareTo(s2) > 0;
                } else {
                    yield false;
                }
            }
            case GE -> {
                if (a instanceof Double d1 && b instanceof Double d2) {
                    yield d1 >= d2;
                } else if (a instanceof String s1 && b instanceof String s2) {
                    yield s1.compareTo(s2) >= 0;
                } else {
                    yield false;
                }
            }
            case LT -> {
                if (a instanceof Double d1 && b instanceof Double d2) {
                    yield d1 < d2;
                } else if (a instanceof String s1 && b instanceof String s2) {
                    yield s1.compareTo(s2) < 0;
                } else {
                    yield false;
                }
            }
            case LE -> {
                if (a instanceof Double d1 && b instanceof Double d2) {
                    yield d1 <= d2;
                } else if (a instanceof String s1 && b instanceof String s2) {
                    yield s1.compareTo(s2) <= 0;
                } else {
                    yield false;
                }
            }
        };
    }

    //-------------------------------------------------------------------------
    // Built-In Predicates

    // member/item,collection
    private Set<Fact> _member(BindingContext bc, Atom atom) {
        var coll = extractVar(bc, atom, 1);

        var facts = new HashSet<Fact>();
        if (coll instanceof Collection<?> c) {
            for (var item : c) {
                facts.add(new ListFact(MEMBER, List.of(item, c)));
            }
        }

        return facts;
    }

    // indexedMember/index,item,list
    private Set<Fact> _indexedMember(BindingContext bc, Atom atom) {
        var coll = extractVar(bc, atom, 2);

        var facts = new HashSet<Fact>();
        if (coll instanceof List<?> list) {
            int index = 0;
            for (var item : list) {
                facts.add(new ListFact(INDEXED_MEMBER,
                    List.of((double)index, item, list)));
                ++index;
            }
        }

        return facts;
    }

    // keyedMember/key,value,map
    private Set<Fact> _keyedMember(BindingContext bc, Atom atom) {
        var coll = extractVar(bc, atom, 2);
        var facts = new HashSet<Fact>();

        if (coll instanceof Map<?,?> map) {
            for (var e : map.entrySet()) {
                facts.add(new ListFact(KEYED_MEMBER,
                    List.of(e.getKey(), e.getValue(), map)));
            }
        }

        return facts;
    }

    // equivalent/equivalence,a,b
    private Set<Fact> _equivalent(BindingContext bc, Atom theAtom) {
        assert theAtom instanceof OrderedAtom;
        var atom = (OrderedAtom)theAtom;
        var facts = new HashSet<Fact>();
        if (debug) System.out.println("booyah!");

        // FIRST, Get the equivalence.  It is constrained to be a
        // bound variable or a constant.
        var equiv = extractEquivalence(atom.terms().get(0), bc);
        if (equiv == null) {
            var name = term2value(atom.terms().get(0), bc);
            throw joe.expected("keyword of known equivalence", name);
        }
//        System.out.println("Equivalence: " + name);

        // NEXT, get the A and B terms.  They will be constants or
        // variables; if variables, bound or unbound.
        var termA = atom.terms().get(1);
        var termB = atom.terms().get(2);
//        System.out.println("termA: " + termA);
//        System.out.println("termB: " + termB);

        // CASE 1: both terms have known values.
        if (hasValue(termA, bc) && hasValue(termB, bc)) {
            var a = term2value(termA, bc);
            var b = term2value(termB, bc);
//            System.out.println("Case 1: a=" + a + " b=" + b);
            if (a == null || b == null) return facts; // null never matches

            // If a and b are not equivalent, no match.
            if (equiv.isEquivalent(a, b)) {
                facts.add(new ListFact(EQUIVALENT, List.of(equiv.keyword(), a, b)));
            }
            return facts;
        }

        // CASE 2: a has a value but b doesn't.
        if (hasValue(termA, bc)) {
            var a = term2value(termA, bc);
            var b = equiv.a2b(a);
//            System.out.println("Case 2: a=" + a + " b=" + b);
            if (a == null || b == null) return facts; // null never matches
            facts.add(new ListFact(EQUIVALENT, List.of(equiv.keyword(), a, b)));
            return facts;
        }

        // CASE 3: b has a value but a doesn't
        if (hasValue(termB, bc)) {
            var b = term2value(termB, bc);
            var a = equiv.b2a(b);
//            System.out.println("Case 3: a=" + a + " b=" + b);
            if (a == null || b == null) return facts; // null never matches
            facts.add(new ListFact(EQUIVALENT, List.of(equiv.keyword(), a, b)));
            return facts;
        }

        // CASE 4: neither term has a value; no match.
        return facts;
    }

    // Converts the term into an Equivalence value.
    private Equivalence extractEquivalence(Term term, BindingContext bc) {
        var name = term2value(term, bc);
        if (name instanceof Keyword k) {
            return equivalences.get(k);
        } else {
            return null;
        }
    }

    // Given a term that is either a constant or a variable, checks whether
    // it has a value.
    private boolean hasValue(Term term, BindingContext bc) {
        return term instanceof Constant ||
            (term instanceof Variable v && bc.bindings.hasBinding(v.name()));
    }

    private Object extractVar(BindingContext bc, Atom atom, int index) {
        assert atom instanceof OrderedAtom;
        var a = (OrderedAtom)atom;
        var term = a.terms().get(index);
        assert term instanceof Variable;
        var theVar = (Variable)term;
        return bc.bindings.get(theVar.name());
    }

    //-------------------------------------------------------------------------
    // Aggregation

    private List<Bindings> aggregate(BindingContext bc) {
        // FIRST, get the Aggregate term, if any.
        var term = getAggregate(bc.rule.head());
        if (term == null) return bc.matches;

        // NEXT, aggregate using the function
        return switch (term.aggregator()) {
            case INDEXED_LIST -> aggregateIndexedList(term.names(), bc.matches);
            case LIST -> aggregateList(term.names(), bc.matches);
            case MAP -> aggregateMap(term.names(), bc.matches);
            case MAX -> aggregateMax(term.names(), bc.matches);
            case MIN -> aggregateMin(term.names(), bc.matches);
            case SET -> aggregateSet(term.names(), bc.matches);
            case SUM -> aggregateSum(term.names(), bc.matches);
        };
    }

    private Aggregate getAggregate(Atom head) {
        for (var term : head.getAllTerms()) {
            if (term instanceof Aggregate a) return a;
        }
        return null;
    }

    private List<Bindings> aggregateIndexedList(
        List<String> names,
        List<Bindings> matches
    ) {
        // FIRST, aggregate the lists by group
        var indexVar = names.get(0);
        var itemVar = names.get(1);
        var groups = new HashMap<Bindings,ArrayList<Pair>>();

        for (var match : matches) {
            var index = match.get(indexVar);
            var item = match.get(itemVar);
            match.unbindAll(names);
            var list = groups.computeIfAbsent(match, g -> new ArrayList<>());
            list.add(new Pair(index, item));
        }

        return aggregates(groups, this::pairs2items);
    }

    private ListValue pairs2items(ArrayList<Pair> pairs) {
        return new ListValue(pairs.stream()
            .sorted(this::comparePairs)
            .map(Pair::item)
            .toList());
    }

    private int comparePairs(Pair a, Pair b) {
        if (a.index() instanceof Double da &&
            b.index() instanceof Double db
        ) {
            return da.compareTo(db);
        } else if (
            a.index() instanceof String sa &&
            b.index() instanceof String sb
        ) {
            return sa.compareTo(sb);
        } else {
            return Integer.compare(Objects.hashCode(a), Objects.hashCode(b));
        }
    }

    private List<Bindings> aggregateList(
        List<String> names,
        List<Bindings> matches
    ) {
        // FIRST, aggregate the lists by group
        var varName = names.getFirst();
        var groups = new HashMap<Bindings,ListValue>();

        for (var match : matches) {
            var o = match.get(varName);
            match.unbindAll(names);
            var list = groups.computeIfAbsent(match, g -> new ListValue());
            list.add(o);
        }

        return aggregates(groups);
    }

    private List<Bindings> aggregateMap(
        List<String> names,
        List<Bindings> matches
    ) {
        // FIRST, aggregate the lists by group
        var kVar = names.get(0);
        var vVar = names.get(1);
        var groups = new HashMap<Bindings, MapValue>();

        for (var match : matches) {
            var k = match.get(kVar);
            var v = match.get(vVar);
            match.unbindAll(names);
            var map = groups.computeIfAbsent(match, g -> new MapValue());

            // If a key has multiple values, set the value to duplicate key.
            // DUPLICATE_KEY.  This allows the client to handle the error
            // as desired without raising an exception, rather like returning
            // `NaN` or `Infinity` from a bad numeric computation.
            if (map.containsKey(k)) {
                if (!map.get(k).equals(v)) {
                    map.put(k, DUPLICATE_KEY);
                }
            } else {
                map.put(k, v);
            }
        }

        return aggregates(groups);
    }

    private List<Bindings> aggregateMax(
        List<String> names,
        List<Bindings> matches
    ) {
        // FIRST, aggregate the max by group, ignoring non-numeric values.
        // If there are no non-numeric values then there is no match.
        var varName = names.getFirst();
        var groups = new HashMap<Bindings,DoubleCell>();

        for (var match : matches) {
            var o = match.get(varName);
            match.unbindAll(names);

            // Only create a group for a numeric match.
            if (o instanceof Double d) {
                var cell = groups.computeIfAbsent(match, g -> new DoubleCell(d));
                cell.value = Math.max(cell.value, d);
            }
        }

        return aggregates(groups, DoubleCell::value);
    }

    private List<Bindings> aggregateMin(
        List<String> names,
        List<Bindings> matches
    ) {
        // FIRST, aggregate the max by group, ignoring non-numeric values.
        // If there are no non-numeric values then there is no match.
        var varName = names.getFirst();
        var groups = new HashMap<Bindings,DoubleCell>();

        for (var match : matches) {
            var o = match.get(varName);
            match.unbindAll(names);

            // Only create a group for a numeric match.
            if (o instanceof Double d) {
                var cell = groups.computeIfAbsent(match, g -> new DoubleCell(d));
                cell.value = Math.min(cell.value, d);
            }
        }

        return aggregates(groups, DoubleCell::value);
    }

    private List<Bindings> aggregateSet(
        List<String> names,
        List<Bindings> matches
    ) {
        // FIRST, aggregate the sets by group
        var varName = names.getFirst();
        var groups = new HashMap<Bindings, SetValue>();

        for (var match : matches) {
            var o = match.get(varName);
            match.unbindAll(names);
            var set = groups.computeIfAbsent(match, g -> new SetValue());
            set.add(o);
        }

        return aggregates(groups);
    }

    private List<Bindings> aggregateSum(
        List<String> names,
        List<Bindings> matches
    ) {
        // FIRST, aggregate the sum by group, ignoring non-numeric values.
        // If there are no non-numeric values, the sum is 0.
        var varName = names.getFirst();
        var groups = new HashMap<Bindings,DoubleCell>();

        for (var match : matches) {
            var o = match.get(varName);
            match.unbindAll(names);
            var cell = groups.computeIfAbsent(match, g -> new DoubleCell(0));

            if (o instanceof Double d) {
                cell.value += d;
            }
        }

        // NEXT, produce the results
        return aggregates(groups, DoubleCell::value);
    }

    private <V> List<Bindings> aggregates(
        Map<Bindings,V> groups,
        Function<V,Object> converter
    ) {
        var result = new ArrayList<Bindings>();
        for (var e : groups.entrySet()) {
            var bindings = e.getKey();
            bindings.bind(AGGREGATE, converter.apply(e.getValue()));
            result.add(bindings);
        }
        return result;
    }

    private List<Bindings> aggregates(Map<Bindings,?> groups) {
        var result = new ArrayList<Bindings>();
        for (var e : groups.entrySet()) {
            var bindings = e.getKey();
            bindings.bind(AGGREGATE, e.getValue());
            result.add(bindings);
        }
        return result;
    }

    //-------------------------------------------------------------------------
    // Helpers

    private Object string2number(Object a) {
        try {
            if (a instanceof String s) return Double.parseDouble(s);
        } catch (Exception ex) {
            // Nothing to do.
        }
        return null;
    }

    private Object number2string(Object b) {
        if (b instanceof Double d) {
            return joe.stringify(d);
        } else {
            return null;
        }
    }

    // The context for the recursive matchBodyAtom method.
    private static class BindingContext {
        private final Rule rule;
        private final Shape shape;
        private final List<Bindings> matches = new ArrayList<>();
        private Bindings bindings = new Bindings();

        BindingContext(Rule rule, Shape shape) {
            this.rule = rule;
            this.shape = shape;
        }
    }

    // Used when aggregating indexed lists.
    private record Pair(Object index, Object item) {}

    private static class DoubleCell {
        double value;
        DoubleCell(double value) { this.value = value; }
        double value() { return value; }
    }
}
