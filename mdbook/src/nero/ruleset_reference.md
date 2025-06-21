# 'ruleset' Reference

A Nero rule set is defined within a Joe script using the `ruleset`
expression, which returns a [`RuleSet`](../library/type.joe.RuleSet.md)
value.

## The `ruleset` Expression

A `ruleset` expression has this form:

**ruleset { *body* }**

where *body* consists of Nero axioms and rules, just like a standalone Nero 
program, but may also contain any number of `export` declarations.

## Axioms

An axiom has this form:

***relation* (*term*, \[*term*...]);**

where

- *relation* is an identifier, conventionally beginning with an uppercase
  letter.
- The *terms* are constant terms: Joe keywords, strings, numbers, `true`,
  `false`, or `null`.

## Rules

A rule has this form:

***head* :- *bodyAtom* [, *bodyAtom*...] \[where *constraints*] ;**

The rule head is an atom whose terms are constant or variable terms:

***relation* (*term*, \[*term*...])**

- All variable terms in the head must be bound in the rule's body.

A body atom is an atom, possibly negated, whose terms may be
constants, variables, or wildcards:

**[not] *relation* (*term*, \[*term*...])**

- The first body atom must not be negated.
- Variables used in negated atoms and constraints must be bound by
  non-negated atoms to their left in the rule's body.

A rule may have zero or more optional constraints using
the `where` clause:

**where *constraint* \[, *constraint*...]**

A constraint has this form:

***variable* *op* *term***

where

- *variable* must be a variable bound in a non-negated body atom.
- *op* is a comparison operator, one of: `==`, `!=`, `>`, `>=`, `<`, `<=`
- *term* is a constant or variable term; if a variable, it must be 
  bound in a non-negated body atom.

## Export Declarations

By default, all facts inferred via axioms or rules are returned to the
script as [`Fact`](../library/type.joe.Fact.md) values.  An export
declaration directs Nero to return all facts having a given relation
as some other kind of Joe value.

An export declaration has this form:

**export *relation* [as *expression*];**

where

- *relation* is a relation used in an axiom or in the head of a rule.
- If no `as` clause is given, *relation* must also be the name of a type
  that is in scope whose initializer takes the same number of arguments
  as the relevant atoms have terms.
- If the `as` clause is given, *expression* must be a Joe expression that
  evaluates to a callable in the current scope.

However specified, the callable will be used to convert all facts having
the given relation to Joe values on output from the rule set.