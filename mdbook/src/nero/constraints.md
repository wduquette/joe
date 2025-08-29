# Constraints

A Nero rule can place additional logical conditions on its bound variables
by defining *constraints*.  A constraint is a simple logical condition
consisting of a bound variable, a comparison operator, and a bound variable
or constant.

For example, suppose we are comparing the weight of different things. The
`NoLighterThan` rule contains two constraints, `x != y` and `wx >= wy`;
both these conditions must be met for the rule to fire.

```nero
Thing(#pen, 0.2);
Thing(#desk, 40);
Thing(#car, 3000);

Heavier(x, y) :- Thing(x, wx), Thing(y, wy) where x != y, wx >= wy;
```

This program will produce the facts

- `Heavier(#desk, #pen)`
- `Heavier(#car, #pen)`
- `Heavier(#car, #desk)`

## Constraint Operators

Constraints may use the following operators, which have
their usual meanings: `==`, `!=`, `>`, `>=`, `<`, `<=`.  The `>`, `>=`, 
`<`, and `<=` operators can be used to compare both numbers and strings,
as in Joe.
