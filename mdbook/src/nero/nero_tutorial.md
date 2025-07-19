# Nero Tutorial

This section describes the Nero language as a standalone language. The
[`joe nero`](../joe_nero.md) tool is used to execute Nero scripts
and to maintain small databases using Nero syntax as a data format.
It can also be used:

- To experiment with and learn Nero syntax.
- To prototype Nero rule sets intended for use in Joe scripts
- As a test bed for Nero's Datalog implementation.

## Topics

- [Axioms and Rules](#axioms-and-rules)
- [Basic Execution](#basic-execution)
- [Facts](#facts)
- [Named Atoms](#named-atoms)
- [`define` Declarations](#define-declarations)
- [Wildcards](#wildcards)
- [Negation](#negation)
- [Constraints](#constraints)

## Axioms and Rules

A Nero program consists of a set of _axioms_ and _rules_.  Axioms assert
facts and rules derive new facts from known facts.  These derived or 
_inferred_ facts constitute the output of the program.

For example the following program asserts the parent/child relationships
between certain individuals, and gives rules for determining ancestors and 
their descendants. Comments begin with `//` and continue to the end of the 
line, as in Joe. The [`define` declarations](#define-declarations) are 
optional, and define the *shape* of the inferred facts.

```nero
define Parent/2; // parent, child
Parent(#anne, #bert);
Parent(#bert, #clark);

define Ancestor/2; // ancestor, descendant
Ancestor(x, y) :- Parent(x, y);
Ancestor(x, y) :- Parent(x, z), Ancestor(z, y);
```

The axiom `Parent(#anne, #bert)` asserts the logical predicate that
`#anne` is the `Parent` of `#bert`.  

- `Parent` is called the _relation_.  
- `#anne` and `#bert` are _constant terms_ representing individuals.
  - A literal constant term in a Nero program must be one of the following:
    - A Joe keyword, string, number, `true`, `false`, or `null`.
- A form consisting of a relation and one or more terms is called an *atom*.

The collection of `Parent` axioms is essentially equivalent to a table
with two columns in a relational database.

The rule `Ancestor(x, y) :- Parent(x, y);` simply asserts that if some 
individual `x` is the parent of some individual `y`, then `x` is also
an ancestor if `y`.

- `Ancestor` is thus the relation of a new collection of predicates.
- The single atom to the left of the `:-` token is called the _head_ of
  the rule.
- The atom(s) to the right of the `:-` token are called the _body_ of 
  the rule
- `x` and `y` are called *variable terms*.  In Nero, variable terms are
  just normal Joe identifiers.

Similarly, the rule `Ancestor(x, y) :- Parent(x, z), Ancestor(z, y);`
simply asserts that if `x` is the parent of `z`, AND `z` is an ancestor
of `y`, then `x` must be an ancestor of `y`.

A Nero program can contain any number of axioms and rules, making use of
any number of distinct relations.


## Basic Execution

Executing a Nero program amounts to iterating over the rules, matching
the body of each rule against the known facts.  For each
successful match, Nero infers a new fact using the rule's head. 
This process continues until no new facts are produced.

For the simple program we've been looking at, 

```nero
// Parent/2 - parent, child
Parent(#anne, #bert);
Parent(#bert, #clark);

// Ancestor/2 - ancestor, descendant
Ancestor(x, y) :- Parent(x, y);
Ancestor(x, y) :- Parent(x, z), Ancestor(z, y);
```

we get the following output.

```
Ancestor(#anne, #bert)
Ancestor(#anne, #clark)
Ancestor(#bert, #clark)
Parent(#anne, #bert)
Parent(#bert, #clark)
```

The `Parent` facts are inferred trivially from the axioms; and then the
`Ancestor` facts are inferred from the `Parent` facts.

Notice how the variables are used in the second rule:

- First, some `Parent` fact is found and the variable terms `x` and `z` are 
  bound to the parent and child in that fact.
- Then, some `Ancestor` fact is found in which individual `z` is the ancestor;
  `y` is then bound to the descendant.
- The bound values of `x` and `y` are then inserted into `Ancestor(x, y)` to
  produce the new `Ancestor` fact.
 
## Facts

Axioms and rules infer *facts*: data records consisting of a relation
name and some number of constant terms.  Nero supports three kinds of
fact, also known as *shapes*, represented in Java by the
`ListFact`, `MapFact`, and `PairFact` classes.

**List facts** have a relation name and a list of one or more constant terms,
also known as *fields*.  A list fact's fields are usually referenced
by position, as in the axioms and rules shown above, but may also be
referenced using the field names `f0`, `f1`, etc.

List facts having the same relation should always have the same arity.

**Map facts** have a relation name and an arbitrary number of fields,
which are always referenced by field name.

**Pair facts** have a relation name and a list of field name/value pairs.
A pair fact's fields are usually referenced by position but can also
be referenced by field name.

Pair facts having the same relation should always have the same arity
and the same field names in the same order.

## Named Atoms

The atoms in the examples shown above are all "ordered atoms".  As
axioms and rule heads they create facts given an ordered list of values;
as body atoms, they match fact terms by position.

Nero also supports "named atoms", which reference terms by field name.  As
axiom and rule heads, they create map facts; as body atoms, they match
fact terms by field name.  Unlike ordered atoms, which must match all of
a fact's terms, a named atom can match a subset of terms.

```nero
Thing(id: #car, color: #red, make: #mini);
Thing(id: #desk, color: #brown, drawers: 1);
RedThing(id: x) :- Thing(color: #red);
```

## `define` Declarations

Nero can determine an inferred fact's *shape* from the atom used to create
it: ordered atoms produce list facts, and named atoms produce map facts.
A `define` declaration explicitly states the shape of a given relation's
facts, and also the Nero script to produce pair facts.

```nero
// A list shape is defined by its relation and arity.
define Parent/2;
Parent(#joe, #charles);

// A map shape is defined by its relation an an ellipsis.
define Thing/...;
Thing(id: #car, color: #red, make: #mini);

// A pair shape is defined by its relation and field names.
define Place/name,kind;
Place("Texas", #state);
```

It isn't necessary to use `define` declarations (unless you want to
create pair facts), but it's good practice as it helps document
the rule set.

## Wildcards

Suppose we wanted to produce a list of all the individuals mentioned in
our input database.  We could use a rule like this:

```nero
Person(x) :- Parent(x, y);
Person(x) :- Parent(y, x);
```

That is, `x` is a person if `x` is either a parent or a child in a 
parent/child relationship. 

Notice that the `x` variable does all the work in these two rules. The 
`y` variable matches something, but we never care what.  In cases like this
we usually use wildcard terms instead of variable terms:

```nero
Person(x) :- Parent(x, _);
Person(x) :- Parent(_, x);
```

A wildcard term can match anything at all; syntactically, a wildcard
is simply an identifier beginning with an underscore, usually just the
bare `_`.  A rule's body can contain any number of wildcards, each of which 
matches something different.

It's desirable to use wildcards in cases like these, especially in more 
complicated rules, as it explicitly says that "this term doesn't matter".

## Negation

The rules we've seen so far look for matches with known facts; it's also
possible to look for an absence of matches.  For example, suppose we 
want to identify the individuals in the `Parent` data who have no known 
parent.  We could add a rule like this:

```nero
Progenitor(x) :- Parent(x, y), not Parent(_, x);
```

This says that `x` is a "progenitor" is `x` is the parent of some individual
`y` but there is no individual in the input data that is the parent of
`x`.

This technique is called _negation_, and the body atom following the
`not` token is said to be _negated_.

A negated atom can only refer to variables bound in the non-negated
atoms to its left; because a negated atom matches an absence of facts, 
not a specific fact, it cannot bind new variables.  Consequently, we
have to use a wildcard for the first term in `not Parent(_, x)` rather
than a variable. (In practice, we'd probably use a wildcard for the 
`y` term in `Parent(x, y)` as well.)

Using negation requires that the rule set be _stratified_; Nero will flag
an error if it is not.  This is not usually a problem in practice; 
see the discussion of stratification in the 
[Technical Details](technical_details.md) section if the error rears its 
ugly head.

## Constraints

Barring time travel, it is impossible for a person to be his or her own 
ancestor; if our graph of parents and children contains a cycle, that 
implies an error in our data.  We can write a rule to detect such errors
by using a constraint:

```nero
CycleError(x) :- Ancestor(x, y) where x == y;
```

We flag person `x` as an error if `x` is his own ancestor.

A constraint is a simple comparison consisting of:

- A variable term, bound in a body atom.
- A comparison operator, one of `==`, `!=`, `>`, `>=`, `<`, `<=`.
- A second term, either a constant term or another variable term bound
  in a body atom.
 
```nero
CycleError(x) :- Ancestor(x, x);
```

The rule will fire for any `Ancestor` fact whose first and second terms
are the same.  This shows an important principle:  when matching facts,
Datalog binds a variable to a value on first appearance from left to 
right. Subsequent appearances must match the bound value.
