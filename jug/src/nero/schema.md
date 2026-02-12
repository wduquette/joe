# Schema Declarations

Nero supports two kinds of relation:

- Ordered relations, having a relation name and a fixed list of ordered fields 
  with specific names.
- Unordered relations, having a relation name and a map of any number of 
  unordered fields with arbitrary names.

Taken together, the relation's name, kind, and (for ordered relations) the field 
names constitute the fact's *shape*.  Nero requires that a relation has the 
same shape in all axioms, rules, and facts having that relation.

- [`define` Declarations](#define-declarations)
  - [Ordered Relations](#ordered-relations) 
  - [Unordered Relations](#unordered-relations)
- [Shape Inference](#shape-inference)
- [Static Schemas](#static-schemas)

## `define` Declarations

A Nero program defines the shape of relations using the `define` 
declaration. The collection of relations and their expected shapes is called 
the program's *schema*.

The `define` declaration has two forms, one for each kind of relation.
Each form has an optional `transient` keyword, which is used to
mark intermediate values; see 
[Schema Maintenance](schema_maintenance.md).

### Ordered Relations

**define \[transient] *relation*/*name* \[, *name*...];**

This form states that the *relation* has a fixed number of ordered
fields with the given names. For example,

`define Person/parent, child;`

**define \[transient] *relation*/*n*;**

This form states that the *relation* has *n* ordered fields, where
*n* is an integer from 1 to 9.  The fields are assigned the first
*n* names from the list `a`, `b`, `c`, `d`, `e`, `f`, `g`, `h`, `i`.

Facts having this shape can be matched by body atoms using either
ordered-field or named-field notation.

### Unordered Relations

**define \[transient] *relation*/...;**

This form states that the *relation* is unordered: it can have any 
number of named but unordered fields. For example,

`define Person/...;`

Facts having this shape can only be matched by body atoms using 
named-field notation, and can only be created by axioms and
rule heads using named-field notation.

### Shape Inference

It is considered a best practice for a Nero script to explicitly define the 
shape of all relations appearing in axioms and rule heads.  As this is a
nuisance when entering Nero query scripts interactively, Nero can also infer
the shape of a relation from its first appearance in the script.

When the parser finds an undefined relation, it infers a shape as follows:

- For an atom using named-field notation, infer an unordered relation.
- For an atom using positional notation, infer an ordered relation with
  field names `a`, `b`, `c`, ..., up to 9 fields.

Ordered relations with more than 9 fields _must_ be explicitly defined.

For example,

```nero
// Infers: define Triple/a,b,c;
Triple(1, 2, 3);

// Error, too many terms.
Triple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

// Infers: define Result/...;
Result(a: 1, b: 2);
```

## Static Schemas

As described above, a rule set's schema is collection of shapes of the relations 
`define`'d by the rule set.  A collection of [[joe.Fact|Facts]] also has
a schema: the shapes of all relations appearing in the collection.

As described in [Schema Maintenance](schema_maintenance.md), a rule set's
schema can include `transient` keywords and the shapes of relations defined 
using *update syntax*; these reflect actions taken by Nero's rule engine
while executing the rule set.  

A [[joe.Fact]] collection's schema, on the other hand, reflects the "static"
state of the collection, and so can't contain either of these things.

Certain [[joe.Database]] methods take an optional *schema* parameter, which
is used to validate an input rule set; this schema must be static.

A rule set used to define a static schema is called a *static ruleset*; it
must contain only `define` directives, and must not use `transient` or 
update syntax.





