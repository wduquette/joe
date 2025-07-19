# Nero Reference

A Nero rule set is defined within a Joe script using the `ruleset`
expression, which returns a [`RuleSet`](../library/type.joe.RuleSet.md)
value.  The body of the `ruleset` is simply a Nero program.

## The `ruleset` Expression

A `ruleset` expression has this syntax:

**ruleset { *body* }**

where *body* consists of Nero declarations, axioms, and rules, just
like a standalone Nero program.

## Atoms

Nero axioms and rules are made up of atoms.  There are two kinds,
ordered atoms and named atoms.

Ordered atoms have this syntax:

***relation*(*term*, \[*term*...])**

where

- *relation* is an identifier, conventionally beginning with an uppercase
  letter.
- The *terms* are constant terms: Joe keywords, strings, numbers, `true`,
  `false`, or `null`.
 
Named atoms have this syntax:

***relation*(*name*: *term*, \[*name*: *term*...])**

where

- *relation* is an identifier, conventionally beginning with an uppercase
  letter.
- *name* is an identifier, conventionally beginning with a lowercase letter.
- The *terms* are constant terms: Joe keywords, strings, numbers, `true`,
  `false`, or `null`.

## `define` Declarations

`define` declarations define the shapes of the facts created by axioms and
rules.

**define *relation*/*arity*;**

This declaration states that facts with the given *relation* are list facts
with the given *arity*, where the *arity* is a positive integer, e.g., 
`define Person/2;`.

**define *relation*/...;**

This declaration states that facts with the given *relation* are map facts,
e.g., `define Thing/...;`.

**define *relation*/*name*\[,*name*...];**

This declaration states that facts with the given *relation* are pair facts
with the given field names; the field names are identifiers, conventionally
starting with a lowercase letter.  E.g., `define Person/name,age;`

## Axioms

An axiom has this form:

***atom*;**

where the *atom* can be an ordered atom or a named atom.

- If the *atom* is an ordered atom it will create a list fact by default,
  or a pair fact if there is an appropriate `define` declaration.
- If the *atom* is a named atom it will create a map fact.

## Rules

A rule has this form:

***head* :- *atom* [, \[not] *atom*...] \[where *constraints*] ;**

The rule *head* is an atom, ordered or named, whose terms are constant or 
variable terms. 

- All variable terms in the head must be bound by non-negated
  atoms in the rule's body.

A body atom is an atom, possibly negated, whose terms may be
constants, variables, or wildcards.

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

