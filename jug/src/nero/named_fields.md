# Named-Field Notation

In standard Datalog, fact terms are unnamed and get their meaning by their
position.  In `Parent(#anne, #bert)`, we know that the parent is in the first
position and the child in the second.

Nero is intended to work with data from Joe and Java, however, and specifically
with Joe record and class instances.

- Record instances have ordered fields like Datalog facts, but those fields
  have names.
- Class instances have named fields, but those fields have no intrinsic 
  ordering.

When record and class instances retain these field names when they 
are converted into facts for use with Nero.  Rules can use these names
via Nero's named-field notation.[^1]

See also: [Schema Declarations](schema.md)

For consistency, the fields of a normal Nero fact are given the names
`f0`, `f1`, `f2`, etc., in sequence from left to right.  Thus, a Nero program
can refer to the fields of any Nero fact by name.

## Named Fields in Body Atoms

Suppose `Person` facts have an `id` field and a `parent` field, possibly
among many other named fields. The following rule extracts the person's
ID and parent, and infers a `Parent` fact:

```nero
Parent(x, y) :- Person(id: x, parent: y);
``` 

To match a `Person` fact, the `Person` fact must have values for all the
named fields, even if those values are `null`.  The `Person` may also
have any number of other fields.

## Named Fields in Axioms and Rule Heads

Named-field notation can also be used in axioms and rule heads.  When
so used, the created facts are like facts created from class instances:
they have named fields with no intrinsic order, and named-field
notation must be used in body atoms in order to match them.

---

[^1]: which, not coincidentally, uses the same syntax as Joe's
named-field patterns.
