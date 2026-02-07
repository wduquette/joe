# Nero Basics

This section explains the basics of Datalog terminology and the Datalog
programming model.

## Simple Example

The following is a simple Datalog programming for computing 
ancestor/descendant relationships from parent/child relationships.

```nero
define Parent/p,c;                              // (1)
Parent(#anne, #bert);                           // (2)
Parent(#bert, #clark);                          // (3)

define Ancestor/a,d;                            // (4)
Ancestor(x, y) :- Parent(x, y);                 // (5)
Ancestor(x, y) :- Parent(x, z), Ancestor(z, y); // (6)
```

- Statements (1) and (4) are *schema definitions*; they define the 
  shape of the facts created by the program. 
- Statements (2) and (3) are *axioms*; they assert specific *facts*.
- Statements (5) and (6) are *rules*; they declare that specific
  facts exist under certain conditions.

When executed, this program infers the following facts from these 
axioms and rules:

- `Parent(#anne, #bert)` from (2)
- `Parent(#bert, #clark)` from (3)
- `Ancestor(#anne, #bert)` from (4)
- `Ancestor(#bert, #clark)` from (4)
- `Ancestor(#anne, #clark)` from (5)

## Axioms and Rules

In the example,

```nero
define Parent/p,c;                              // (1)
Parent(#anne, #bert);                           // (2)
Parent(#bert, #clark);                          // (3)

define Ancestor/a,d;                            // (4)
Ancestor(x, y) :- Parent(x, y);                 // (5)
Ancestor(x, y) :- Parent(x, z), Ancestor(z, y); // (6)
```

- (1) says that `Parent` is an *ordered relation* having two fields named `p`
  and `c`, meaning "parent" and "child".

- (2) is an *axiom* asserting that `#anne` is the `Parent` of `#bert`.

- (3) is an axiom asserting that `#bert` is the `Parent` of `#clark`.
 
- (4) says that `Ancestor` is an *ordered relation* having two fields named `a`
  and `d`, meaning "ancestor" and "descendant".

- (5) is a *rule* stating that `x` is an `Ancestor` of `y` if
  `x` is a `Parent` of `y`.

- (6) is a rule stating that `x` is an `Ancestor` of `y` if
  `x` is a `Parent` of `z` and `z` is an `Ancestor` of `y`.

## Axioms vs. Facts

An axiom is not a fact; it is a statement in a Nero program that unilaterally
asserts a fact, while a fact is a data record in the set of known facts.

A Nero program infers new facts from known facts via its rules, where
the set of known facts comes from the program's axioms, an external
source, or both.

## Atoms and Terms

Axioms and rules are made up of *atoms*, as shown in this diagram:

```nero
Parent(#anne, #bert);
────────────────────
 ↑
Head atom
 
Ancestor(x, y) :- Parent(x, z), Ancestor(z, y);
──────────────    ────────────  ──────────────
 ↑                 ↑             ↑
Head atom         Body atom 1   Body atom 2
```

An atom consists of a *relation* and a parenthesized list of *terms*.

The relation, e.g., `Parent` is a name for the relation between the atom's
terms, e.g., `#anne` and `#bert` are related as parent and child.  A 
relation is similar to the name of a table in an SQL database, where the
set of facts having that relation are like the rows of the table.

By convention, the names of Nero relations begin with an uppercase letter.

Nero supports several [kinds of term](terms.md); this example includes
two of them.  `#anne`, `#bert`, and `#clark` are constant terms, and `x`, 
`y`, and `z` are variable terms.

`#anne`, `#bert`, and `#clark` are, in fact, Joe keywords; Joe numbers,
strings, booleans, and `null` can also be used as constants in Nero source.

## Variables and Rule Execution

Nero variables look like Joe variables, but are quite different.  Nero
works by matching a rule's body atoms against known facts, working from 
left to right.  As it does so, it *binds* the variables from its body atoms
to the values in the matched facts.

- A variable is *bound* on first appearance in the rule's body, reading from
  left to right.
  - Left-to-right binding is unusual in Datalog implementations; see
    [Technical Details](technical_details.md) for more information.
- On all subsequent appearances, the value in the matched fact must be equal
  to the variable's bound value.
- Then, the bound values are substituted into the rule's head atom to produce
  a new fact.

Consider our example:

```nero
define Parent/p,c;                              // (1)
Parent(#anne, #bert);                           // (2)
Parent(#bert, #clark);                          // (3)

define Ancestor/a,d;                            // (4)
Ancestor(x, y) :- Parent(x, y);                 // (5)
Ancestor(x, y) :- Parent(x, z), Ancestor(z, y); // (6)
```

- Axioms (2) and (3) contain no variables; an axiom has no rule body 
  to bind variables to values.
- Rule (5) will match each `Parent` fact, binding `x` to the parent and `y`
  to the child, and will produce an `Ancestor` fact for each match.
- Rule (6) will match each`Parent` fact, binding `x` and `z`; and then will
  match any `Ancestor` fact whose first term has the value of `z`, binding
  `y` to the fact's second term and ultimately yielding a new `Ancestor` fact.

## Term Modes: INOUT, IN, and DEF

Every term in a body atom has a mode, `INOUT`, `IN`, or `DEF`, which governs 
left-to-right variable binding.

All of the terms in the above examples have mode `INOUT`. A variable in an
`INOUT` term can be bound in a body atom to the left, _or_ it can bind to a value
from a matched fact.

Certain kinds of atom (e.g., [negated atoms](negation.md)) have `IN` terms.
Variables in `IN` terms _must_ be bound in a body atom to the left.

`DEF` is a special mode that applies to 
[variables with default values](variable_defaults.md); such a variable can
appear only once in the rule's body, at the point of binding.

Terms appearing in rule heads and constraints effectively have mode `IN`, in
that all variable terms must be bound in a body atom.

## The Fixed-Point Algorithm

Nero is a declarative language; and as with any declarative language it's
essential to understand the underlying execution model.  Every imperative
language is more or less alike; every declarative language is distinct.

Datalog's execution model is straightforward.  

- First, it infers a fact for each axiom.
- Next, it repeatedly iterates over the set of rules, matching the rule
  against the known facts and inferring new facts until no previously 
  unknown facts are added to the set of known facts.

Most Datalog implementations, including Nero, are designed so that this
loop is guaranteed to terminate.  Some useful Datalog extensions break this 
guarantee, but Nero does not support any of them.



