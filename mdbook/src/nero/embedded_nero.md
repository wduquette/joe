# Embedded Nero

A Nero program is embedded in a Joe script using the `ruleset` 
declaration.  The following declaration defines a
[`RuleSet`](../library/type.joe.RuleSet.md)
called `myRules`. The content of the declaration's body is just a
Nero program, as described in the [tutorial](nero_tutorial.md),
possibly augmented with 
[`export` declarations](#exported-outputs).

- [A Simple Rule Set](#a-simple-rule-set)
- [The `Fact` Type](#the-fact-type)
- [Scripted Input Facts](#scripted-input-facts)
- [Exporting Domain Values](#exporting-domain-values)

## A Simple Rule Set

```joe
ruleset myRules {
    // Parent/2 - parent, child
    Parent(#anne, #bert);
    Parent(#bert, #clark);

    // Ancestor/2 - ancestor, descendant
    Ancestor(x, y) :- Parent(x, y);
    Ancestor(x, y) :- Parent(x, z), Ancestor(z, y);
}
```

A [`RuleSet`](../library/type.joe.RuleSet.md) provides a number of
ways to infer the facts resulting from the rules; the simplest is
`infer()`, which simply executes the rule set as is, given any
defined axioms, and returns the resulting facts.

```joe
foreach (fact : myRules.infer()) {
    println(fact);
}
```

This will output the following:

```
Fact(Ancestor, #anne, #bert)
Fact(Ancestor, #bert, #clark)
Fact(Parent, #anne, #bert)
Fact(Parent, #bert, #clark)
Fact(Ancestor, #anne, #clark)
```

## The 'Fact' Type

By default, all new facts inferred by an embedded Nero rule set are of 
type [`Fact`](../library/type.joe.Fact.md).  A `Fact` consists of a 
relation, e.g., `"Ancestor"`, and a list of terms, e.g., `#anne` and 
`#bert`. 

In most ways a `Fact` value can be treated like an 
_ad hoc_ Joe record value, i.e., the same pattern that will match a record of
type `Ancestor` will also match a `Fact` whose relation is `"Ancestor"`:

```joe
foreach (Ancestor(a, d) : myRules.infer()) {
    println(a + " is an ancestor of " + d);
}
```

When inferred facts are going to be used immediately, e.g., to produce
output, the `Fact` type is often sufficient.  However, it is also possible
to make the rule set produce outputs using the script's own defined
types; see [Exported Domain Values](#exported-domain-values), below.

## Scripted Input Facts

A Nero rule set can include axioms, as shown above, but usually it gets its
input facts from the script.  Any Joe value with named fields can be used
as an input fact.  This includes instances of:

- The [`Fact`](../library/type.joe.Fact.md) type.
- Any Joe [record](../records.md) type.
- Any joe [class](../classes.md).
- Native types whose proxies define fields.

The type's type name is used as the input fact's relation, and its 
fields as the fact's terms.

The scripts a list or set of all of the input facts, and passes it
to the rule set's `infer()` method.

```joe
record Parent(parent, child) {}

var inputs = [
    Parent(#anne, #bert),
    Parent(#bert, #clark)
];

ruleset myRules {
    // Ancestor/2 - ancestor, descendant
    Ancestor(x, y) :- Parent(x, y);
    Ancestor(x, y) :- Parent(x, z), Ancestor(z, y);
}

foreach (fact : myRules.infer(inputs)) {
    println(fact);
}
```

### Input Facts without Ordered Fields

Class instances do not have ordered fields the way `Fact` values and record
values do. Therefore, the usual rule syntax that depends on
ordered fields will not work:

```joe
class Parent {
    method init(parent, child) {
        @parent = parent;
        @child = child;
    }
}

ruleset myRules {
    // Parent(x, y) cannot match an instance of class parent!
    Ancestor(x, y) :- Parent(x, y);
    Ancestor(x, y) :- Parent(x, z), Ancestor(z, y);
}
```

Consequently, we must use named-field atoms for class instances
and other types with named fields but not ordered fields:


```joe
ruleset myRules {
    Ancestor(x, y) :- Parent(parent: x, child: y);
    Ancestor(x, y) :- Parent(parent: x, child: z), Ancestor(z, y);
}
```

Named-field atoms also allow a rule to match on a subset of a fact's
fields, whether ordered or not.

## Exported Domain Values

As noted [above](#the-fact-type), a rule set outputs inferred facts
using the [`Fact`](../library/type.joe.Fact.md) type.  This is sufficient
in many cases; but it is also common to want the outputs to be of types
defined by the script itself.  Accomplish this by adding `export` 
declarations to the rule set.

The simplest `export` declaration simply states that a given relation
has a matching domain type in the current scope. When rule set execution
is complete, facts with the exported relation will be converted to 
values of the named type using the type's initializer.

For example, the following script defines a record type called
`Ancestor` and tells the rule set to export all `Ancestor` facts using
the `Ancestor` type.

```joe
record Ancestor(ancestor, dependent) {}

ruleset myRules {
    export Ancestor;
    Ancestor(x, y) :- Parent(x, y);
    Ancestor(x, y) :- Parent(x, z), Ancestor(z, y);
}
```

- The `Ancestor` type must be in scope when the rule set is defined.
- The `Ancestor` type's initializer must take the same number of arguments
  as the `Ancestor` relation in the rule set.

However, it is also possible to export an inferred fact as any kind of
value the script's author desires by providing an explicit creation 
function as a callable reference.  For example, suppose the script
author wants `Ancestor` facts returned as two-item lists:

```joe
function ancestor2list(x, y) {
    return [x, y];
}

ruleset myRules {
    export Ancestor as ancestor2list;
    Ancestor(x, y) :- Parent(x, y);
    Ancestor(x, y) :- Parent(x, z), Ancestor(z, y);
}
```

A lambda function could also be used:

```joe
ruleset myRules {
    export Ancestor as \x,y -> [x, y];
    Ancestor(x, y) :- Parent(x, y);
    Ancestor(x, y) :- Parent(x, z), Ancestor(z, y);
}
```

The only restriction is that the callable referenced by the `as` clause
is defined in the scope in which the rule set is declared.


