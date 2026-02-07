# Technical Details

This section contains details about Nero and its implementation for
those familiar with other implementations of Datalog, and for those
interested in the implementation and maintenance of Nero itself.

## Nero vs. "Standard Datalog"

At base, Nero is an implementation of standard Datalog implemented
using fixpoint semantics and the "naive" solution algorithm, augmented
with:

- Unordered relations
- The ability to reference fact fields by name as well as by position.
- Schema declarations
- Left-to-right binding
- Stratified negation
- Variable defaults
- Constraints
- Built-in predicates
- Aggregation functions
- Collection literals
- Destructuring patterns
- Update syntax

Nero's syntax and conventions have been modified from standard Datalog to 
better support integration into Joe scripts.

- Horn clauses are terminated by `;` rather than `.`.
- Relation names are uppercase by convention, to follow Joe's convention
  for type names.
- Nero variable terms are simple identifiers, lowercase by convention.
- Nero constant terms can be any of the following Joe literal values:
  - In rules and axioms: keywords, strings, numbers, booleans, and `null`.
  - In scripted input facts: any Joe value
- Nero body atoms can include wildcard terms, which are written as 
  identifiers with a leading underscore.  A wildcard matches any value and
  creates no binding.
- Rule constraints appear at the end of the rule, prefixed with `where`.
- Named atoms can match named fields in the manner of Joe's
  named-field patterns.

## Left-to-Right Binding

In standard Datalog, a rule's body atoms can appear in any order without 
changing the meaning of the rule.  The execution engine must match them 
against facts in _some_ order, but the order genuinely doesn't matter.

Advanced features, e.g., [Negation](negation.md), put constraints on 
the execution order.  A negated atom cannot match known facts by definition,
and therefore cannot bind values to variables; any variables in a negated 
atom _must_ be bound before the atom is executed.

[Built-in predicates](builtin_predicates.md) complicate the picture
further, as a built-in predicate usually has both 
[IN terms and INOUT terms](datalog_basics.md#term-modes-inout-in-and-def).  This 
enables pathological recursive variable binds:

```nero
A(x,y) :- mapsTo(#f, x, y), mapsTo(#g, y, x);
```

Here, the first atom requires `x`, which is defined by the second atom, and
computes `y`, which is required by the second atom.  There is no way to
order these atoms that allows the rule to be executed.

This is an error, and ideally should be detected by the Nero parser.

Nero handles this by enforcing left-to-right binding:  any IN variable must
be bound in some atom to the left.  Rather than providing a complex algorithm
to put the atoms in execution order, detecting pathological cycles, Nero
requires the programmer to _prove_ that the rule is executable by writing
it in an executable order.

The left-to-right rule is simple to remember, easy to follow, and efficient
to check, and results in no loss of generality.


## Stratified Negation

A Nero program uses rules to produce new facts.  Those rules are allowed
to be recursive: relation A can depend on facts of type B, while
simultaneously relation B can depend on facts of type A, either directly
or indirectly.  For example, the following program says that `A` is true
of `x` IFF `B` is true of `x`.

```nero
// OK
define A/x;
define B/x;
A(x) :- B(x);
B(x) :- A(x);
```

But when negation is added, this changes.  Consider this program:

```nero
// BAD
define A/x;
define B/x;
B(#anne);
A(x) :- C(x), not B(x);
B(x) : A(x);
```

This will infer:

- `A(#anne)` because we have `C(#anne)` and no `B(#anne)`
- `B(#anne)` because we have `A(#anne)`
- But now we have both `A(#anne)` and `B(#anne)`, which contradicts the first
  rule.  This is the Datalog equivalent of a memory corruption error in other
  languages.

To prevent this kind of problem, Nero (like many Datalog engines) requires
that every ruleset meets the following condition: 

- If relation `A` depends on relation `B` with negation, directly or indirectly,
  relation `B` cannot depend on relation `A`, directly or indirectly.

To prevent this, Nero uses a technique called _stratified negation_, in which
the head relations in the rule set are divided into *strata* such that if
relation `A` depends on relation `B` with negation, the rules that
infer B are all in a lower strata than the rules that infer `A`.  If the
rules cannot be so stratified, then the program is rejected.

In this case, look for the kind of circular dependency with negation shown
above.

## References

- [Datalog](https://en.wikipedia.org/wiki/Datalog) (wikipedia)
- [Datalog and Logic Databases](https://www.amazon.com/Datalog-Databases-Synthesis-Lectures-Management/dp/3031007263/ref=sr_1_1) by Greco & Molinaro
- [Modern Datalog Engines](https://www.amazon.com/Modern-Datalog-Engines-Foundations-Databases/dp/1638280428/ref=sr_1_2) by Ketsman & Koutris
 
