# Schema Declarations

Nero supports three kinds of fact:

- "List" facts, having a relation and a list of unnamed fields.
- "Map" facts, having a relation and a map of field names and values.
- "Pair" facts, having a relation and a list of field name/value pairs.

Taken together, the kind of the fact and its arity are called the fact's
*shape*.  Nero expects that all facts with a given relation and all
uses of that relation in axioms and rules will have the same shapes.

In particular, the Nero parser will reject a Nero program that has more
than one shape for a single relation.

The collection of relations and their expected shapes is called the 
programs or database's *schema*.

Schemas can be inferred from a set of facts or from a Nero program's
axioms and rules; alternatively, a Nero program can declare the schema
explicitly using the `define` declaration.

The `define` declaration has three forms, one for each kind of fact.
Each form has an optional `transient` keyword, which is used to
mark intermediate values; see 
[Schema Maintenance](schema_maintenance.md) for more.

While `define` declarations are never required, it's a good practice to
include them.

## List Shape

**define \[transient] *relation*/*arity*;**

This form states that the *relation* has a list shape with the given
*arity*, i.e., the given number of terms or fields.  The *arity* must be
an integer greater than or equal to 1.

For example,

`define Parent/2;`

Relations having this shape can be matched by body atoms using either 
ordered-field or named-field notation, and can be created by axioms and
rule heads using ordered-field notation.  When named-field notation
is used, the field names are `f0`, `f1`, `f2`, and so on.

## Map Shape

**define \[transient] *relation*/...;**

This form states that the *relation* has a map shape; it can have any 
number of named but unordered fields.

For example,

`define Person/...;`

Relations having this shape can only be matched by body atoms using 
named-field notation, and can only be created by axioms and
rule heads using named-field notation.

## Pair Shape

**define \[transient] *relation*/*name* \[, *name*]...;**

This form states that the *relation* has a pair shape: it has ordered
fields with the given names.

For example,

`define Person/parent, child;`

Relations having this shape can be matched by body atoms using either
ordered-field or named-field notation, and can be created by axioms and
rule heads using ordered-field notation. 

## Default Shapes

If no shape is known for a relation used in an axiom or rule head, Nero
will assume list shape for ordered-field notation and map shape for
named-field notation.





