# Named-Field Notation

In the examples we've shown so far, the terms in an atom get their meaning 
from their position.  In the axiom `Parent(#anne, #bert);`, for example, we 
know that the parent is in the first position and the child in the second.

Nero is intended to work with data from Joe and Java, and specifically
with Joe record and class instances. Joe record instances have ordered and 
named fields like Nero's [[joe.Fact|Facts]]. Class instances
have named fields, but those fields have no intrinsic ordering.

When record and class instances retain these field names when they 
are converted into facts for use with Nero.  Rules can use these names
via Nero's named-field notation.[^1]

See also: [Schema Declarations](schema.md)

## Named Fields in Body Atoms

Suppose `Person` facts have an `id` field and a `parent` field, possibly
among many other named fields. The following rule extracts the person's
ID and parent, and infers a `Parent` fact:

```nero
define Person/id,parent;
define Parent/x,y;
Parent(x, y) :- Person(id: x, parent: y);
``` 

A `Person` body atom with named fields will match any `Person` fact
which matching values in the named fields. The `Person` fact may also
have any number of other fields.

## Named Fields in Axioms and Rule Heads

Named-field notation can also be used in axioms and rule heads. If the
relation is `define`'d to be unordered, the created fact will have 
exactly the fields given in the head atom.  If the relation is defined
to be ordered, any fields not given in the head atom will have null
values.

---

[^1]: which, not coincidentally, uses the same syntax as Joe's
named-field patterns.
