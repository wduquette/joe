# Negation

Normal Nero rules fire when they match one or more known facts.  Negation
allows a rule to fire when there are no matching facts.

Suppose we have a set of `Parent` facts, and we want to identify the
individuals with no known parent and the individuals with no known children:

```nero
define Person/name;
Person(#anne);
Person(#bert);
Person(#clark);
Person(#douglas);

define Parent/parent,child;
Parent(#anne, #bert);
Parent(#bert, #clark);

define Orphan/name;
Orphan(x) :- Person(x), not Parent(_, x);       // (1)

define Childless/name;
Childless(x) :- Person(x), not Parent(x, _);    // (2)
```

- A body atom preceded by `not` is said to be *negated*.

- Rule (1) says, `x` is an `Orphan` if `x` is a `Person` and there is no
  `Parent` fact in which `x` is the child.
- Rule (2) says, `x` is `Childless` if `x` is a `Person` and there is no
  `Parent` fact in which `x` is the `Parent`.

Executing this rule set will produce the following new facts:

- `Orphan(#anne)`
- `Orphan(#douglas)`
- `Childless(#clark)`
- `Childless(#douglas)`

There are two significant conditions that must be met when negation is used.

First, a negated atom cannot bind any variables, as by definition it succeeds
when it doesn't match any facts.  Consequently, any variables that appear in
a negated atom must be bound earlier in the rule's body.

Second, a rule set that includes negation must be *stratifiable*.  This is 
not usually a problem in practice; if Nero rejects a program because it is
unstratifiable, see [Stratification](stratification.md) for an explanation
of the problem and how to fix it.
