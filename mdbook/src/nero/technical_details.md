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

TODO

## References

- [Datalog](https://en.wikipedia.org/wiki/Datalog) (wikipedia)
- [Datalog and Logic Databases](https://www.amazon.com/Datalog-Databases-Synthesis-Lectures-Management/dp/3031007263/ref=sr_1_1) by Greco & Molinaro
- [Modern Datalog Engines](https://www.amazon.com/Modern-Datalog-Engines-Foundations-Databases/dp/1638280428/ref=sr_1_2) by Ketsman & Koutris
 


