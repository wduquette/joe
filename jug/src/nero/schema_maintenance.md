# Schema Maintenance

If Nero is used to maintain a database of facts over a period of time,
whether within a single [Joe script](nero_and_java.md) or via the
[`nero` application](../nero_app.md), a time will come when the schema
needs to be changed.  Nero provides two ways to do this using Nero scripts:

- [The `transient` Declaration](#the-transient-declaration)
- [Update Syntax](#update-syntax)

## The `transient` Declaration

The `transient` declaration (or, equivalently, the `transient`) keyword
in a [`define` declaration](schema.md) declares that a relation is
*transient*, i.e., that it will be retained until all new facts are 
inferred and will then be dropped.

The syntax is as follows:

**transient *relation*;**

For example,

```nero
transient MyRelation;
```

This is useful for relations that are used as intermediate results and
do not need to be retained; but it also allows a Nero program to compute
new facts from pre-existing facts and then drop pre-existing relations
that are no longer needed.

## Update Syntax

Sometimes one wants to change the shape of an existing relation: to add
fields or drop fields or change a field's content, just as one would do
with columns in an SQL table.  Nero provides update syntax for this 
purpose.

Suppose the `Thing` relation has the shape `Thing/id, color, size` and
one wanted to drop the `color` attribute in favor of a separate
`Color/id, color` relation.  One could write this script:

```nero
define Thing/id, color, size;
Thing(#hat, #black, #medium);
Thing(#truck, #black, #heavy);
Thing(#boots, #black, #mens12);

define Color/id, color;
Color(id, c) :- Thing(id, c, _);   // 1

define Thing!/id, size;
Thing!(id, s) :- Thing(id, _, s);  // 2
```

The trailing `!` character in Rule 2's head atom marks the `Thing!` relation 
as an *updated* relation and Rule 2 as an *updating rule*.

While the rule set is executing, a relation whose name ends in `!` is
just an ordinary relation. Such a relation can appear in

- Axioms
- Rule heads
- Body atoms
- `define` and `transient` declarations

After all new facts have been inferred the `Thing!` relation will replace
the `Thing` relation:

- The facts in the `Thing` relation will be deleted from the set of known
  facts, just as if `Thing` had been marked `transient`.
- The `Thing!` relation will be renamed `Thing`, and all facts in the 
  relation will be updated accordingly.

Thus, once this program completes the set of facts will be this:

- `Thing(#hat, #medium)`
- `Thing(#truck, #heavy)`
- `Thing(#boots, #mens12)`
- `Color(#hat, #black)`
- `Color(#truck, #black)`
- `Color(#boots, #black)`

A Nero program can update any number of relations; there is one important
condition, which is checked when the program is compiled:

- An updated relation can only appear in a rule's body atom if an 
  updated relation appears in the rule's head atom.

For this reason it would be more typical to write the above script in this 
way:

```nero
define Thing/id, color, size;
Thing(#hat, #black, #medium);
Thing(#truck, #black, #heavy);
Thing(#boots, #black, #mens12);

define Color!/id, color;
Color!(id, c) :- Thing(id, c, _);   // 1

define Thing!/id, size;
Thing!(id, s) :- Thing(id, _, s);  // 2
```

Now all the new facts are produced by updating rules, and the original
relations (just `Thing` in this case) will be dropped automatically.
