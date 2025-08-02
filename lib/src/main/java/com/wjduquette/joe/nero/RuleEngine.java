package com.wjduquette.joe.nero;

import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.Keyword;

import java.util.*;

/**
 * The Nero inference engine.  Given a {@link NeroRuleSet} and a set of
 * input {@link Fact Facts}, the engine will infer the facts implied by
 * the two.
 */
public class RuleEngine {
    public static final String INFER_ERROR =
        "Call `infer()` before querying results.";

    //-------------------------------------------------------------------------
    // Static Built-In Predicate Schema

    private static final Schema BUILT_INS = new Schema();
    public static final String MEMBER = "member";
    public static final String INDEXED_MEMBER = "indexedMember";
    public static final String KEYED_MEMBER = "keyedMember";

    static {
        BUILT_INS.checkAndAdd(new Shape.PairShape(MEMBER,
            List.of("item", "collection")));
        BUILT_INS.checkAndAdd(new Shape.PairShape(INDEXED_MEMBER,
            List.of("index", "item", "list")));
        BUILT_INS.checkAndAdd(new Shape.PairShape(KEYED_MEMBER,
            List.of("key", "value", "map")));
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

    // The Nero rule set, i.e., the compiled Nero program.
    private final NeroRuleSet ruleset;

    // Map from head relation to rules with that head.
    private final Map<String,List<Rule>> ruleMap = new HashMap<>();

    //
    // Configuration Data
    //

    // Debug Flag
    private boolean debug = false;

    //
    // Working Data
    //

    // The set of known facts, initialized by the constructor
    private final FactSet knownFacts = new FactSet();

    // Whether the infer() method has been called or not.  We only do
    // inference once per instance of the engine.
    private boolean inferenceComplete = false;

    // Facts inferred from the rule set's axioms and rules (aka,
    // the "intensional database" in Datalog jargon).
    private final Set<Fact> inferredFacts = new HashSet<>();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a new RuleEngine with the given ruleset and no external database
     * of facts.
     * @param ruleset The ruleset
     */
    public RuleEngine(NeroRuleSet ruleset) {
        this.ruleset = ruleset;

        // NEXT, Categorize the rules by head relation
        for (var rule : ruleset.rules()) {
            var head = rule.head().relation();
            var list = ruleMap.computeIfAbsent(head, k -> new ArrayList<>());
            list.add(rule);
        }
    }

    /**
     * Creates a new RuleEngine with the given ruleset and the given set of
     * input facts.
     * @param ruleset The ruleset
     * @param facts The fact set
     */
    public RuleEngine(NeroRuleSet ruleset, FactSet facts) {
        this(ruleset);
        knownFacts.addAll(facts);
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
    // Queries

    /**
     * Gets all facts known after inference is complete.
     * @return The known facts.
     */
    public FactSet getKnownFacts() {
        if (!inferenceComplete) {
            throw new IllegalStateException(INFER_ERROR);
        }
        return knownFacts;
    }

    /**
     * Gets the facts inferred from the inputs given the axioms and rules.
     * @return The inferred facts.
     */
    public Set<Fact> getInferredFacts() {
        if (!inferenceComplete) {
            throw new IllegalStateException(INFER_ERROR);
        }
        return inferredFacts;
    }

    //-------------------------------------------------------------------------
    // Inference

    /**
     * Executes the inference algorithm, computing all facts knowable
     * from the axioms and rules given the input facts
     * (the "extensional database").
     * @throws JoeError if the rule set is not stratified.
     */
    public void infer() {
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
        if (inferenceComplete) return;
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

        var bc = new BindingContext(rule);


        matchNextBodyAtom(bc, 0);

        if (!bc.inferredFacts.isEmpty()) {
            knownFacts.addAll(bc.inferredFacts);
            inferredFacts.addAll(bc.inferredFacts);
            return true;
        } else {
            return false;
        }
    }

    // Matches the rule's index-th body atom against the relevant facts.
    private void matchNextBodyAtom(BindingContext bc, int index) {
        var atom = bc.rule.body().get(index);
        Set<Fact> facts;

        if (isBuiltIn(atom.relation())) {
            // The NeroParser ensures that atom conforms to the built-in's shape.
            // TODO: Make BindingContext static, and add a registry.
            facts = switch (atom.relation()) {
                case "member" -> _member(bc, atom);
                default -> throw new UnsupportedOperationException("TODO");
            };
        } else {
            facts = knownFacts.getRelation(atom.relation());
        }

        // FIRST, Save the current bindings, as we will begin with them for each
        // fact.
        var givenBindings = bc.bindings;

        for (var fact : facts) {
            // FIRST, Copy the bindings for this fact.
            bc.bindings = new Bindings(givenBindings);

            // NEXT, if it doesn't match, go on to the next fact.
            if (!matchAtom(atom, fact, bc)) continue;

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
            var newFact = createFact(bc);
            if (!knownFacts.getAll().contains(newFact)) {
                bc.inferredFacts.add(newFact);
                if (debug) System.out.println("      Fact: " + newFact);
            }
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
            case Variable v -> {
                var bound = bc.bindings.get(v);

                if (bound == null) {
                    bc.bindings.put(v, value);
                    yield true;
                } else {
                    yield Objects.equals(bound, value);
                }
            }
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
            case Wildcard ignored -> true;
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
        for (var atom : bc. rule.negations()) {
            for (var fact : knownFacts.getRelation(atom.relation())) {
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
                    termMap.put(e.getKey(), ((Constant)e.getValue()).value());
                }

                yield new MapFact(atom.relation(), termMap);
            }
            case OrderedAtom atom -> {
                var shape = ruleset.schema().get(atom.relation());
                var terms = new ArrayList<>();

                for (var term : atom.terms()) {
                    terms.add(((Constant)term).value());
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
        return switch (term) {
            case Constant c -> c.value();
            case Variable v -> bc.bindings.get(v);
            case Wildcard ignored -> throw new IllegalStateException(
                "Rule head contains a Wildcard term.");
        };
    }

    private boolean constraintMet(
        Constraint constraint,
        Map<Variable,Object> bindings
    ) {
        var a = bindings.get(constraint.a());

        var b = switch (constraint.b()) {
            case Variable v -> bindings.get(v);
            case Constant c -> c.value();
            case Wildcard ignored -> throw new IllegalStateException(
                "Constraint contains a Wildcard term.");
        };

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
    //
    // The collection
    private Set<Fact> _member(BindingContext bc, Atom atom) {
        var coll = extractVar(bc, atom, 1);

        var facts = new HashSet<Fact>();
        if (coll instanceof Collection<?> c) {
            for (var item : c) {
                facts.add(new ListFact("member", List.of(item, c)));
            }
        }

        return facts;
    }

    private Object extractVar(BindingContext bc, Atom atom, int index) {
        assert atom instanceof OrderedAtom;
        var a = (OrderedAtom)atom;
        var term = a.terms().get(index);
        assert term instanceof Variable;
        var theVar = (Variable)term;
        return bc.bindings.get(theVar);
    }

    //-------------------------------------------------------------------------
    // Helpers

    // The context for the recursive matchBodyAtom method.
    private class BindingContext {
        private final Rule rule;
        private final Shape shape;
        private final List<Fact> inferredFacts = new ArrayList<>();
        private Bindings bindings = new Bindings();

        BindingContext(Rule rule) {
            this.rule = rule;
            this.shape = ruleset.schema().get(rule.head().relation());
        }
    }

}
