# Technical Details

This section contains details about Nero and its implementation for
those familiar with other implementations of Datalog, and for those
interested in the implementation and maintenance of Nero itself.

## Nero vs. "Standard Datalog"

Semantically, Nero is a standard implementation of Datalog implemented
using fixpoint semantics and the "naive" solution algorithm, augmented
with:

- Stratified negation
- Constraints
- The ability to use arbitrary Joe values as input facts.
- The ability to export inferred facts as values of particular Joe types.

Nero's syntax and conventions have been modified from standard Datalog to 
better support integration into Joe scripts.

- Horn clauses are terminated by `;` rather than `.`.
- Relation names are uppercase by convention, to follow Joe's convention
  for type names.
- Nero variable terms are simple identifiers, lowercase by convention.
- Nero constant terms can be any of the following Joe literal values:
  - In rules and axioms: keywords, strings, numbers, `true`, `false`, and `null`.
  - In scripted input facts: any Joe value
- Nero body atoms can include wildcard terms, which are written as 
  identifiers with a leading underscore.  A wildcard matches any value and
  creates no binding.
- Rule constraints appear at the end of the rule, prefixed with `where`.
- Rule body atoms can match named fields in the manner of Joe's
  named-field patterns.

## Stratified Negation

A Nero program uses rules to produce new facts.  Those rules are allowed
to be recursive: relation A can depend on facts of type B, while
simultaneously relation B can depend on facts of type A, either directly
or indirectly.  For example, the following program says that `A` is true
of `x` IFF `B` is true of `x`.

```nero
// OK
A(x) :- B(x);
B(x) :- A(x);
```

But when negation is added, this changes.  Consider this program:

```nero
// BAD
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
 


