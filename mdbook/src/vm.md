# Appendix: Virtual Machine Instruction Set

This appendix describes the Bert byte-engine's virtual machine.  

**NOTE:** this information is for the benefit of the language maintainer.
The VM's architecture and instruction set details are not part of the
language specification, and can change without notice.

The byte-engine is a stack machine with a few registers:

- `ip` is the instruction pointer within the current function.
- `T` is the temporary register, used to stash a value momentarily during
  an atomic operation.

## Instruction Set


|    | Mnemonic/argument | Stack effect       | Description               |
|----|-------------------|--------------------|---------------------------|
| 0  | ADD               | *a b* → *c*        | c = a + b                 |
| 1  | ASSERT            | *msg* → ∅          | Throws AssertError        |
| 2  | CALL *argc*       | *f args* → *c*     | c = f(args)               |
| 3  | CLASS *name*      | ∅ → *cls*          | Create class              |
| 4  | CLOSURE *func*    | ∅ → *f*            | Load closure              |
| 5  | COMMENT *name*    | ∅ → ∅              | No-op comment             |
| 6  | CONST *constant*  | ∅ → *a*            | Load constant             |
| 7  | DECR              | *a* → *b*          | b = a - 1                 |
| 8  | DIV               | *a b* → *c*        | c = a/b                   |
| 9  | DUP               | *a* → *a* *a*      | Duplicate top             |
| 10 | DUP2              | *a b* → *a b a b*  | Duplicate top 2           |
| 11 | EQ                | *a b* → *c*        | c = a == b                |
| 12 | FALSE             | ∅ → false          | Load `false`              |
| 13 | GE                | *a b* → *c*        | c = a >= b                |
| 14 | GETNEXT           | *iter* → *a*       | a = iter.next()           |
| 15 | GLODEF *name*     | *a* → ∅            | Define global             |
| 16 | GLOGET *name*     | ∅ → *a*            | Get global                |
| 17 | GLOLET *pattern*  | *list a* → ∅       | Bind target to pattern    |
| 18 | GLOSET *name*     | *a* → *a*          | Set global                |
| 19 | GT                | *a b* → *a* > *b*  | Compare: greater          |
| 20 | HASNEXT           | *iter* → *flag*    | flag = iter.hasNext()     |
| 21 | IN                | *a coll* → *flag*  | a in collection           |
| 22 | INCR              | *a* → *b*          | b = a + 1                 |
| 23 | INDGET            | *coll i* → *a*     | `a = coll[i]`             |
| 24 | INDSET            | *coll i a* → *a*   | `coll[i] = a`             |
| 25 | INHERIT           | *sup sub* → *sup*  | Inheritance               |
| 26 | ITER              | *coll* → *iter*    | iter = coll.iterator()    |
| 27 | JIF *offset*      | *flag* → ∅         | Jump if false             |
| 28 | JIFKEEP *offset*  | *flag* → *flag*    | Jump if false, keep value |
| 29 | JIT *offset*      | *flag* → ∅         | Jump if true              |
| 30 | JITKEEP *offset*  | *flag* → *flag*    | Jump if true, keep value  |
| 31 | JUMP *offset*     | ∅ → ∅              | Jump forwards             |
| 32 | LE                | *a b* → *a* <= *b* | Compare: less or equal    |
| 33 | LISTADD           | *list a* → *list*  | Add item to list          |
| 34 | LISTNEW           | ∅ → *list*         | Push empty list           |
| 35 | LOCGET *slot*     | ∅ → *a*            | Get local                 |
| 36 | LOCLET *pattern*  | *list a* → ∅       | Bind target to pattern    |
| 37 | LOCSET *slot*     | *a* → *a*          | Set local                 |
| 38 | LOOP *offset*     | ∅ → ∅              | Jump backwards            |
| 39 | LT                | *a b* → *a* <= *b* | Compare: less than        |
| 40 | MAPNEW            | ∅ → *map*          | Push empty ma p           |
| 41 | MAPPUT            | *map k a* → *map*  | Add entry to map          |
| 42 | METHOD *name*     | *cls f* → *cls*    | Add method to class       |
| 43 | MUL               | *a b* → *c*        | c = a*b                   |
| 44 | NE                | *a b* → *c*        | c = a != b                |
| 45 | NEGATE            | *a* → *b*          | b = -a                    |
| 46 | NI                | *a coll* → *flag*  | a not in collection       |
| 47 | NOT               | *a* → *b*          | b = !a                    |
| 48 | NULL              | ∅ → null           | Load `null`               |
| 49 | POP               | *a* → ∅            | Pops one value            |
| 50 | POPN *n*          | *a...* → ∅         | Pops *n* values           |
| 51 | PROPGET *name*    | *obj* → *a*        | Get property value        |
| 52 | PROPSET *name*    | *obj a* → *a*      | Set property value        |
| 53 | RETURN            | *a* → *a*          | Return                    |
| 54 | SUB               | *a b* → *c*        | c = a - b                 |
| 55 | SUPGET *name*     | *obj sup* → *f*    | Get superclass method     |
| 56 | TGET              | ∅ → *a*            | *a* = T                   |
| 57 | THROW             | *a* → ∅            | Throw error               |
| 58 | TPUT              | *a* → *a*          | T = *a*                   |
| 59 | TRCPOP            | ∅ → ∅              | Pops a post-trace         |
| 60 | TRCPUSH *trace*   | ∅ → ∅              | Pushes a post-trace       |
| 61 | TRUE              | ∅ → true           | Load `true`               |
| 62 | UPCLOSE *n*       | *v...* → ∅         | Closes *n* upvalue(s)     |
| 63 | UPGET *slot*      | ∅ → *a*            | Get upvalue               |
| 64 | UPSET *slot*      | *a* → *a*          | Set upvalue               |

**Stack Effects:** in the stack effect column, the top of the stack is on the 
right.  

For `SUB`, for example, the stack effect is (*a* *b* → *c*) where *c* is
(*a* - *b*).  When the instruction executes, *b* is on the top of the stack,
with *a* just below it.

**Variable Names:** In the above table (and in the descriptions below), 
the italicized names are used as follows:

- ∅: Indicates that the instruction expects nothing on the
  stack or leaves nothing on the stack, depending on its position.
- *a*, *b*, *c*: Arbitrary values
- *argc*: An argument count
- *args*: A callable's arguments; 0 or more, depending on *argc*
- *cls*: A class
- *coll*: An iterable collection: a Java `Collection<?>` or a value with
  a `ProxyType<T>` that supports iteration
- *constant*: An index into the chunk's constants table for an arbitrary 
  constant.
- *f*: A callable, native or scripted
- *flag*: A boolean flag, `true` or `false`.
- *func*: A scripted function definition: the `Function` itself plus its 
  upvalue data, suitable for building into a `Closure` in the current scope.
  Used only by the `CLOSURE` instruction.
- *iter*: A collection iterator, as used to compile `foreach` loops
- *k*: A value used as a map key.
- *list*: A list value
- *msg*: A message string
- *n*: A count
- *name*: An index into the chunk's constants table for a name constant
- *obj*: An object, e.g., a class instance, or any `JoeObject`
- *offset*: A jump offset
- *slot*: A stack slot
- *sub*: A subclass
- *sup*: A superclass
- *trace*: A callable trace added to the current call-frame.
- *v*: A local variable

## Instructions

### ADD
---
**ADD** | *a* *b* → *c*, where *c* = (*a* + *b*)

- If *a* and *b* are both numbers, yields the sum of the two numbers.
- If either *a* or *b* is a string, converts the other argument to a string and
  yields the concatenated string.
- Any other case is reported as an error.

### ASSERT
---
**ASSERT** | *msg* → ∅

Throws an `AssertError` with the given *msg*.  The actual assertion 
test is compiled using multiple instructions.

### CALL
---
**CALL *argc*** | *f args* → *c*

Calls callable *f* with *argc* arguments; the arguments are 
found in consecutive stack slots just above *f*.  The result *c* 
replaces the function ands its arguments on the stack.  Callable
*f* may be any valid native or Bert callable.

### CLASS
---
**CLASS *name*** | ∅ → *cls*

The `CLASS` instruction begins the process of creating a new class from a
`class` declaration.  Given *name*, the index of the class's name in the
constants table, it creates a new class *cls*, assigns it to variable *name*
in the current scope, and pushes it onto the stack.  Subsequent instructions
will add methods, execute static initializers, etc.

### CLOSURE
---
**CLOSURE *func*** | ∅ → *f*

Converts a *func* function definition into `Closure` *f* in the current scope.
The *func* argument is a complex set of codes, consisting of:

- The index of the `Function` definition in the constants table.
  - The `Function` knows the number of upvalues.
- For each `Upvalue` in the `Function`:
  - `isLocal`: 1 if the `Upvalue` is local to the immediately enclosing scope,
    and 0 if it is from an outer scope.
  - `index`: The `Upvalue`'s local variable index

See `Compiler::function` and `VirtualMachine::run`/`CLOSURE` for details.

### COMMENT
---
**COMMENT** *name* | ∅ → ∅

A no-op instruction used to insert comments into disassembly listings
and execution traces.  The *name* is an index of a string in the constants 
table; the string serves as the text of the comment.

### CONST
---
**CONST *constant*** | ∅ → *a*

Retrieves the constant with index *constant* from the constants table, and
pushes it onto the stack.

### DECR
---
**DECR** | *a* → *b*, where *b* = (*a* - 1)

Decrements value *a*. This is used to implement the `--` operator.

`DECR` is used instead of `SUB` because it provides a `--`-specific error 
message if *a* is not numeric. `SUB`'s error message would be confusing.

### DIV
---
**DIV** | *a b* → *c*, where *c* = (*a*/*b*)

Computes the quotient of *a* and *b*.

### DUP
---
**DUP** | *a* → *a a*

Duplicates the value on the top of the stack.

### DUP2
---
**DUP2** | *a b* → *a b a b*

Duplicates the top two values on the stack.

### EQ
---
**EQ** | *a* *b* → *c*, where *c* = (*a* == *b*).

Given two values *a* and *b*, yields `true` if the two are equal, and `false`
otherwise.

### FALSE
---
**FALSE** | ∅ → `false`

Pushes `false` onto the stack.

### GE
---
**GE** | *a* *b* → *c*, where *c* = (*a* >= *b*)

Yields `true` if *a* is greater than or equal to *b*, and `false` otherwise.

### GETNEXT
---
**GETNEXT** | *iter* → *a*

Gets the next value from *iter*, which must be an iterator created by the 
`ITER` instruction.  This instruction should always be paired with `HASNEXT`.

### GLODEF
---
**GLODEF *name*** | *a* → ∅

Defines a global variable called *name*, assigning it the value *a*.

### GLOGET
---
**GLOGET *name*** | ∅ → *a*

Retrieves the value of global variable *name* and pushes it on the stack.

### GLOLET
---
**GLOLET *pattern*** | *list* *a* → ∅

The `GLOLET` instruction implements the `let` statement where the binding
variables are bound in the global scope.  It receives the
`let` pattern as a constant index.  The *list* is a list of the constants,
computed or literal, used in the pattern.  The value *a* is the target value
to be matched.  If the match succeeds then the values of the binding 
variables will be added to the global environment. If the match fails then the
instruction throws a `RuntimeError`.

### GLOSET
---
**GLOSET *name*** | *a* → *a*

Assigns value *a* to global variable *name*, leaving *a* on the stack.

### GT
---
**GT** | *a* *b* → *c*, where *c* = (*a* > *b*)

Yields `true` if *a* is greater than or equal to *b*, and `false` otherwise.

### HASNEXT
---
**HASNEXT** | *iter* → *flag*

Gets whether iterator *iter* has another value.  The *iter* must be an
iterator created by the `ITER` instruction.

### IN
---
**IN** | *a* *coll* → *flag*

Pushes `true` if value *a* is in collection *coll*, and `false` otherwise.  The
*coll* can be any Java `Collection<?>` or a value whose `ProxyType<T>` makes it
iterable.

### INCR
---
**INCR** | *a* → *b*, where *b* = (*a* + 1)

Increments value *a*. `INCR` is used to implement the `++` operator.

`INCR` is used instead of `ADD` for two reasons:

- `INCR` provides a `++`-specific error message if *a* is not numeric. `ADD`'s 
  error message would be confusing.
- `ADD` will concatenate strings, but `++` is only for use with numbers.
 
### INDGET
---
**INDGET** | *coll i* → *a*

Retrieves the value at index *i* in the indexed collection *coll*, and pushes
it on the stack.  

- If *coll* is a Joe `List`, *i* must be an index to an existing item.
- If *coll* is a Joe `Map`, *i* can be any Monica value; returns null if the
  value doesn't exist.
- It is a runtime error if *coll* is neither a `List` nor a `Map`.

### INDSET
---
**INDSET** | *coll i a* → *a*

Assigns the value *a* to index *i* in the indexed collection *coll*,
leaving *a* on the stack.

- If *coll* is a Joe `List`, *i* must be an index to an existing item.
- If *coll* is a Joe `Map`, *i* can be any Monica value.
- It is a runtime error if *coll* is neither a `List` nor a `Map`.

### INHERIT
---
**INHERIT** | *sup* *sub* → *sup*

This instruction is used when compiling a class definition.  Given superclass
*sup* and the class being defined, *sub*, copies all of *sup*'s methods to 
*sub*'s methods table, thereby inheriting *sup*'s behavior.  **NOTE**: If Joe 
were modified to allow methods to be added to superclasses, this implementation 
would need to change.

### ITER
---
**ITER** | *coll* → *iter*

Give collection *coll*, creates a Java `Iterator<?>` for the collection.  The
*coll* can be any Java `Collection<?>` or value whose `ProxyType<?>` 
provides iterability.  This is used to implement the `foreach` statement.

### JIF
---
**JIF *offset*** | *flag* → ∅

Jumps forward by *offset* if *flag* is false.

### JIFKEEP
---
**JIFKEEP *offset*** | *flag* → *flag*

Jumps forward by *offset* if *flag* is false, retaining the flag on the
stack.

### JIT
---
**JIT *offset*** | *flag* → ∅

Jumps forward by *offset* if *flag* is true.

### JITKEEP
---
**JITKEEP *offset*** | *flag* → *flag*

Jumps forward by *offset* if *flag* is true, retaining the flag on the
stack.

### JUMP
---
**JUMP *offset*** | ∅ → ∅

Jumps forward by *offset*.

### LE
---
**LE** | *a* *b* → *c*, where *c* = (*a* <= *b*)

### LISTADD
---
**LISTADD** | *list* *a* → *list*

Pops *a* and adds it to the *list* value.

### LISTNEW
---
**LISTNEW** | ∅ → *list*

Pushes an empty *list* value onto the stack.

### LOCGET
---
**LOCGET *slot*** | ∅ → *a*

Gets the value of the local variable in the given stack *slot*, 
relative to the current call frame.

### LOCLET
---
**LOCLET *pattern*** | *list* *a* → ∅

The `LOCLET` instruction implements the `let` statement where the binding
variables are bound in a local scope.  It receives the
`let` pattern as a constant index.  The *list* is a list of the constants,
computed or literal, used in the pattern.  The value *a* is the target value
to be matched.

If the match succeeds then the values of the binding variables will be pushed
onto the stack, initializing them as locals. If the match fails then the 
instruction throws a `RuntimeError`.

### LOCSET
---
**LOCSET *slot*** | *a* → *a*

Sets the value of the local variable in the given stack *slot*, 
relative to the current call frame, to *a*, leaving the value on the
stack.

### LOOP
---
**LOOP *offset*** | ∅ → ∅

Jumps backward by *offset*.

### LT
---
**LT** | *a* *b* → *c*, where *c* = (*a* < *b*)

### MAPNEW
---
**MAPNEW** | ∅ → *map*

Pushes an empty *map* value onto the stack.

### MAPPUT
---
**MAPPUT** | *map* *k* *a* → *map*

Pops *k* and *a* and puts {*k*: *a*} into the *map* value.

### METHOD
---
**METHOD *name*** | *cls* *f* → *cls*

This instruction is used when compiling a class definition. Adds closure *f* 
to class *cls* as a method.  It is illegal to add a method to a class after
its definition.

### MUL
---
**MUL** | *a* *b* → *c, where *c* = (*a* * *b*)

Computes the product of *a* and *b*. 

### NE
---
**NE** | *a* *b* → *c*, where *c* = (*a* != *b*)

Pushes `true` if *a* and *b* are not equal, and *false* otherwise.

### NEGATE
---
**NEGATE** | *a* → *b*, where *b* = (-*a*)

Negates *a*.

### NI
---
**NI** | *a* *coll* → *flag*

Pushes `false` if value *a* is in collection *coll*, and `true` otherwise.  The
*coll* can be any Java `Collection<?>` or a value whose `ProxyType<T>` makes it
iterable.

### NOT
---
**NOT** | *a* → *b*, where *b* = (!*a*)

### NULL
---
**NULL** | ∅ → `null`

Pushes `null` on the stack.

### POP
---
**POP** | *a* -> ∅

Pops one value from the stack.

### POPN
---
**POPN** *n* | *a...* → ∅ 

Pops *n* values from the stack.

### PROPGET
---
**PROPGET *name*** | *obj* → *a*

Gets the value of property *name* of object *obj*.

### PROPSET
---
**PROPSET *name*** | *obj* *a* → *a*

Sets property *name* of object *obj* to *a*, leaving *a* on the stack.

### RETURN
---
**RETURN** | *a* → *a* 

Returns *a* from the current function/call frame.  If the function was invoked
from Java via `VirtualMachine::execute` or `VirtualMachine::callFromJava`, the
result is popped and returned to the Java caller.

### SUB
---
**SUB** | *a* *b* → *c, where *c* = (*a* - *b*)

Computes the difference of *a* and *b*.

### SUPGET
---
**SUPGET *name*** | *obj* *sup* → *f*

Retrieves method *name* for superclass *sup* of object *obj*.
This is used to compile the `super.<method>` syntax.  

### TGET
---
**TGET** | ∅ → *a*

Pushes the value of the T register onto the stack.

### THROW
---
**THROW** | *a* -> ∅

Throws a `MonicaError` given *a*, which may be a `MonicaError` or a
string, creating a new `MonicaError` in the latter case.

### TPUT
---
**TPUT** | *a* → *a*

Loads *a* into the T register, leaving it on the stack.

### TRCPOP
---
**TRCPOP** | ∅ → ∅

Pops a post-trace from the current `CallFrame`.  See `TRCPUSH`.

### TRCPUSH
---
**TRCPUSH** *trace* | ∅ → ∅

Pushes a post-trace into the current `CallFrame`.  The *trace* is a 
`Trace` constant identifying a particular scope in a loaded script.
If an error is thrown while the post-trace is present in the `CallFrame`,
the trace will be included in the error stack trace.

This is used to add the `class` as a stack level when errors are found
in a class static initializer block.

### TRUE
---
**TRUE** | ∅ → `true`

Pushes `true` onto the stack.

### UPCLOSE
---
**UPCLOSE** *n* | *v...* → ∅

Pops *n* local variables from the stack, closing any of them that are 
open upvalues. This is used when ending a scope, and by `break` and `continue` 
when ending a scope prematurely.

### UPGET
---
**UPGET *slot*** | ∅ → *a*

Gets the value of the local variable in the given stack *slot*,
relative to the current call frame.  This is used when the local
variable has been captured by a closure.

### UPSET
---
**UPSET *slot*** | *a* → *a*

Sets the value of the local variable in the given stack *slot*,
relative to the current call frame, to *a*, leaving the value on the
stack.  This is used when the local
variable has been captured by a closure.


