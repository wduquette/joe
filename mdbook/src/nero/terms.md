# Nero Terms

Nero atoms can contain the following kinds of term:

| Kind                  | Where Usable                            |
|-----------------------|-----------------------------------------|
| Aggregation functions | Rule heads only                         |
| Constants             | Anywhere                                |
| List literals         | Axioms and rule heads                   |
| Map literals          | Axioms and rule heads                   |
| Patterns              | Body atoms only                         |
| Set literals          | Axioms and rule heads                   |
| Variables             | Rule heads, body atoms, and constraints |
| Wildcards             | Body atoms only                         |

See the [Aggregation Functions](aggregation_functions.md) section for 
a discussion of aggregation functions and their use.

## Constants

Nero accepts the same scalar constants as Joe, with the same syntax: keywords,
numbers, strings, booleans, and `null`.

## Variables

Rule heads, body atoms, and [constraints](constraints.md) can contain
variables, as described in the 
[Datalog Basics](datalog_basics.md#variables-and-rule-execution)
section.

## Collection Literals

List, map, and collection literals can appear as terms in axioms and rule heads
using the same syntax as in Joe scripts.  Collection literals in rule heads
can contain variables; collection literals in axioms cannot.

## Patterns

Body atoms can contain any of Joe's 
[destructuring patterns](../patterns.md)
as terms, except
that `$` syntax cannot be used to interpolate variables and expressions from
the host language at the present time.

## Wildcards

As in Joe's destructuring patterns, a wildcard matches and ignores any value.
Nero wildcards have the same syntax as pattern wildcards, an identifier 
beginning with an underscore.
