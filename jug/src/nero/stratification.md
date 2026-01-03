# Stratification

Nero uses a common Datalog technique called *stratification* to ensure that
Nero programs terminate without corrupting the set of known facts.  
Stratification involves grouping the rules in the program into sets
called *strata*.  When the program is executed, the fixed-point algorithm
is executed for each stratum in turn, just as though the program had been
broken into multiple programs to be executed sequentially.

This process is controlled by several *stratification conditions*.  If
these conditions are met, the program is said to be *stratifiable*.
Stratifiability is checked at compilation time; if Nero reports that
a program is not stratifiable, it is because one of the following conditions
is not met.

- [The Negation Condition](#the-negation-condition)
- [The Collection Literal Condition](#the-collection-literal-condition)
- [The Aggregation Function Condition](#the-aggregation-function-condition)

## The Negation Condition

The negation condition is as follows:

- If relation A depends on relation B with negation, then relation B must not
  depend on relation A either directly or indirectly.

This is to say, if there is a rule with relation A in the head atom and 
a negated atom with for relation B in the body, then there must be no
rule with B in the head that ultimately depends (possibly through a chain
of many rules) on a body atom with relation A.

Here's a simple example of a program that breaks this condition:

```nero
define A/x;
define B/x;
define C/x;

C(0);                     // Axiom (1)
A(x) :- C(x), not B(x);   // Rule (2)
B(x) :- A(x);             // Rule (3)
```

Here's why this program would be a problem:

- By axiom 1 we have fact `C(0)`.
- By rule 2, since we have `C(0)` and no `B` facts at all we get `A(0)`.
- By rule 3, since we have `A(0)` we get `B(0)`.
- But then, by rule 2 we *shouldn't* have `A(0)`.

Thus, we would end up with set of known facts that contradicts rule 2.

## The Collection Literal Condition

The collection literal condition is as follows:

- If a rule for relation A uses a 
  [collection literal](terms.md) in its head and
  relation A depends on relation B, then relation B must not depend on
  relation A directly or indirectly.

If this condition is broken then the rule set might not terminate.  Here's
an extreme example in which A depends on itself:

```nero
define A/x;
A(0);
A([x]) :- A(x);
```

If allowed, this program would produce the following infinite stream of facts:

- `A([0])`
- `A(\[[0]])`
- `A(\[[[0]]])`
- ...


## The Aggregation Function Condition

The [aggregation function](aggregation_functions.md) condition is as follows:

- If relation A aggregates over relation B then relation B must not depend on
  relation A, either directly or indirectly.

If this condition is broken then the rule set might not terminate.  Here's
an extreme example in which A depends on itself:

```nero
define A/x;
A(1);
A(2);
A(sum(x)) :- A(x);
```

If allowed, this program would produce the following infinite stream of facts:

- `A(3)`
- `A(6)`
- `A(12)`
- `A(24)`
- ...
