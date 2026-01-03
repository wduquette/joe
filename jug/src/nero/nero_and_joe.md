# Using Nero with Joe

This section explains how to use Nero within Joe scripts.

**Note:** Joe's Nero API is still experimental and may change without
notice.

- [A Simple Rule Set](#a-simple-rule-set)
- [Inferring Facts with `Nero`](#inferring-facts-with-nero)
- [The `Fact` Type](#the-fact-type)
- [`Facts` and Pattern Matching](#facts-and-pattern-matching)
- [Scripted Input Facts](#scripted-input-facts)
- [Using a `Database`](#using-a-factbase)

## A Simple Rule Set

A Nero program is embedded in a Joe script using the `ruleset`
expression, which creates a value of type
[`RuleSet`](../library/type.joe.RuleSet.md).

The following code creates a rule set called `myRules`.  The content
of the `ruleset`'s body is just a Nero program as described in
[Nero Datalog](nero.md).

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

## Inferring Facts with `Nero`

Use [[joe.Nero]] to infer facts from rule set's rules and axioms, or to infer 
facts from a scripted collection of facts given the rule set's rules and 
axioms.

For example, this script executes the rule set and outputs the
inferred facts.

```joe
var nero = Nero();
foreach (fact : nero.with(myRules).infer()) {
    println(fact);
}
```

This will output the following [[joe.Fact]] values (the order might differ).

```
Fact(Ancestor, ["ancestor", #anne, "descendant", #clark])
Fact(Ancestor, ["ancestor", #anne, "descendant", #bert])
Fact(Parent, ["parent", #bert, "child", #clark])
Fact(Parent, ["parent", #anne, "child", #bert])
Fact(Ancestor, ["ancestor", #bert, "descendant", #clark])
```

## Facts and Pattern Matching

Joe's [ordered-field](../patterns.md#ordered-field-patterns), 
[named-field](../patterns.md#named-field-patterns) 
and [type-name](../patterns.md#type-name-patterns) 
patterns can match facts.

- A type-name pattern can match a `Fact` by its  relation.

- A named-field pattern can match any `Fact` by its relation and field names.

- An ordered-field pattern can match any ordered `Fact` by its relation
  and field values.

In these examples the fact's relation takes the place of the type name.
For example, the following code extracts the `Ancestor` facts out of the
set of inferred facts:

```joe
foreach (Ancestor(a, d) : nero.with(myRules).infer()) {
    println(a + " is an ancestor of " + d);
}
```

In addition, all `Facts` can be matched as instances of the `Fact` type.

- An ordered-field pattern can match an ordered fact as having type name `Fact` 
  and two fields, the relation name and a `List` of field values.
- A named-field patterns can match any facts as having type name `Fact` and the
  following named fields:
  - `relation`: the fact's relation
  - `fieldMap`: a map of the fact's field names and values.

## Scripted Input Facts

A `ruleset` literal set can include axioms, as shown above, but `rulesets` are
usually applied to input facts provided by the script. Facts can be created 
using the [`Fact`](../library/type.joe.Fact.md) type's initializer, but this
is rarely necessary: any Joe value with named fields can be
automatically converted into a `Fact`. This includes:

- Joe [record](../records.md) values
    - These become ordered facts
- Joe [class](../classes.md) instances
    - These become unordered map facts
- Native types whose proxies define fields.
    - These become ordered facts

The type's type name is used as the input fact's relation, and its
fields as the fact's terms.

In addition, a native type's proxy can explicitly override `isFact` and
`toFact` to produce facts in any desired way.

This script produces a list of `Parent` records, and uses the list as
input to the `ruleset`.  The records are automatically
converted to `Parent` facts.

```joe
record Parent(parent, child) {}

var inputs = [
    Parent(#anne, #bert),
    Parent(#bert, #clark)
];

var myRules = ruleset {
    define Ancestor/a,d;
    Ancestor(x, y) :- Parent(x, y);
    Ancestor(x, y) :- Parent(x, z), Ancestor(z, y);
};

var nero = Nero();

foreach (fact : nero.with(myRules).infer(inputs)) {
    println(fact);
}
```

## Using a `Database`

The [[joe.Database]] type is an in-memory database of [[joe.Fact|Facts]],
indexed by relation.  A script can:

- Create a `Database` for a collection of Joe values, which will be converted
  to `Fact` values as needed.
- Update the content of a `Database` given a `ruleset`.
- Query the content of a `Database` given a `ruleset`; the inferred facts
  are returned to the caller, but are *not* added back into the `Database`.
- Add and delete values from the `Database`, and query and maintain it in
  a variety of other ways.
- Write the contents of the `Database` out as a Nero script
    - Provided that all fact terms can be expressed in Nero syntax.
- Load the contents of a Nero script file into the `Database`
- Use a [static schema](schema.md) to validate the relations included in
  a [[method:joe.Database.load]] script or [[method:joe.Database.update]]
  `ruleset`.

See the `Database` JoeDoc for details.

