# Nero in Joe Scripts

This section explains how to use Nero within Joe scripts.

- [A Simple Rule Set](#a-simple-rule-set)
- [The `Fact` Type](#the-fact-type)
- [`Facts` and Pattern Matching](#facts-and-pattern-matching)
- [Scripted Input Facts](#scripted-input-facts)
- [Using a `FactBase`](#using-a-factbase)

## A Simple Rule Set

A Nero program is embedded in a Joe script using the `ruleset`
expression, which creates a value of type 
[`RuleSet`](../library/type.joe.RuleSet.md).  

The following code creates a rule set called `myRules`.  The content
of the `ruleset`'s body is just a Nero program as described in 
the [tutorial](nero_tutorial.md).

```joe
var myRules = ruleset {
    define Parent/parent,child;
    Parent(#anne, #bert);
    Parent(#bert, #clark);

    define Ancestor/ancestor,descendant;
    Ancestor(x, y) :- Parent(x, y);
    Ancestor(x, y) :- Parent(x, z), Ancestor(z, y);
};
```

A [`RuleSet`](../library/type.joe.RuleSet.md) can be used directly to infer facts from the
rules and axioms, or to infer facts from a scripted collection of
facts given the rules and axioms.

For example, this script executes the rule set and outputs the
inferred facts.

```joe
foreach (fact : myRules.infer()) {
    println(fact);
}
```

This will output the following (the order might differ).

```
Fact(Parent, #bert, #clark)
Fact(Ancestor, #anne, #bert)
Fact(Ancestor, #anne, #clark)
Fact(Parent, #anne, #bert)
Fact(Ancestor, #bert, #clark)
```

## The 'Fact' Type

All facts inferred by an embedded Nero rule set are of the Joe
[`Fact`](../library/type.joe.Fact.md) type; internally, a `Fact` can
be a Java `ListFact`, `MapFact`, or `PairFact`, as described in the
[tutorial](nero_tutorial.md). 

## Facts and Pattern Matching

Joe's [pattern matching](../patterns.md) features can be used to work with
`Facts`.  An ordered-field pattern can match any list or pair fact, and
a named-field pattern can match any fact at all.

When matching a fact, the fact's relation takes the place of the type name.
For example, the following code extracts the `Ancestor` facts out of the
set of inferred facts:

```joe
foreach (Ancestor(a, d) : myRules.infer()) {
    println(a + " is an ancestor of " + d);
}
```

## Scripted Input Facts

A Nero rule set can include axioms, as shown above, but usually it gets its
input facts from the script. Facts can be created using the 
[`Fact`](../library/type.joe.Fact.md) type's initializer and static methods,
but this is rarely necessary: any Joe value with named fields can be 
automatically converted into a `Fact`. This includes:

- Joe [record](../records.md) values
  - These become pair facts
- Joe [class](../classes.md) instances
  - These become map facts
- Native types whose proxies define fields.
  - These become pair facts

The type's type name is used as the input fact's relation, and its 
fields as the fact's terms.

In addition, a native type's proxy can explicitly override `isFact` and 
`toFact` to produce facts in any desired way.

This script produces a list of input facts, and passes it
to the rule set's `infer()` method.

```joe
record Parent(parent, child) {}

var inputs = [
    Parent(#anne, #bert),
    Parent(#bert, #clark)
];

var myRules = ruleset {
    Ancestor(x, y) :- Parent(x, y);
    Ancestor(x, y) :- Parent(x, z), Ancestor(z, y);
};

foreach (fact : myRules.infer(inputs)) {
    println(fact);
}
```

## Using a `FactBase`

The [`FactBase`](../library/type.joe.FactBase.md) type is an in-memory database
of [`Facts`](../library/type.joe.Fact.md), indexed by relation.  A script can:

- Create a `FactBase` for a collection of Joe values, which will be converted
  to `Fact` values as needed.
- Update the content of a `FactBase` given a `RuleSet`.
  - The `RuleSet` is given the content of the `FactBase`, and the inferred
    facts are added back into the `FactBase`.
- Query the content of a `FactBase` given a `RuleSet`; the inferred facts
  are returned to the caller, but are *not* added back into the `FactBase`.
- Add and delete values from the `FactBase`, and query and maintain it in
  a variety of other ways.
- Write the contents of the `FactBase` out as a Nero script
  - Provided that all fact terms can be expressed in Nero syntax.
- Read the contents of a Nero script in as a `FactBase`.

See the `FactBase` JoeDoc for details.

