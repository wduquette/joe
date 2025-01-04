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


|    | Mnemonic/argument | Stack effect         | Description               |
|----|-------------------|----------------------|---------------------------|
| 0  | ADD               | *a b* → *c*          | c = a + b                 |
| 1  | ASSERT            | *msg* → ∅            | Throws AssertError        |
| 2  | CALL *argc*       | *f args* → *c*       | c = f(args)               |
| 3  | CLASS *name*      | ∅ → *cls*            | Create class              |
| 4  | CLOSURE *func*    | ∅ → *f*              | Load closure              |
| 5  | COMMENT *name*    | ∅ → ∅                | No-op comment             |
| 6  | CONST *constant*  | ∅ → *a*              | Load constant             |
| 7  | DECR              | *a* → *b*            | b = a - 1                 |
| 8  | DIV               | *a b* → *c*          | c = a/b                   |
| 9  | DUP               | *a* → *a* *a*        | Duplicate                 |
| 10 | EQ                | *a b* → *c*          | *c* = *a* == *b*          |
| 11 | FALSE             | ∅ → false            | Load `false`              |
| 12 | GE                | *a b* → *c*          | c = a >= b                |
| 13 | GETNEXT           | *iter* → *a*         | a = iter.next()           |
| 14 | GLODEF *name*     | *a* → ∅              | Define global             |
| 15 | GLOGET *name*     | ∅ → *a*              | Get global                |
| 16 | GLOSET *name*     | *a* → *a*            | Set global                |
| 17 | GT                | *a b* → *a* > *b*    | Compare: greater          |
| 18 | HASNEXT           | *iter* → *flag*      | flag = iter.hasNext()     |
| 19 | IN                | *a coll* → bool      | a in collection           |
| 20 | INCR              | *a* → *b*            | b = a + 1                 |
| 21 | INHERIT           | *sup sub* → *sup*    | Inheritance               |
| 22 | ITER              | *coll* → *iter*      | iter = coll.iterator()    |
| 23 | JIF *offset*      | *cond* → ∅           | Jump if false             |
| 24 | JIFKEEP *offset*  | *cond* → *cond*      | Jump if false, keep value |
| 25 | JIT *offset*      | *cond* → ∅           | Jump if true              |
| 26 | JITKEEP *offset*  | *cond* → *cond*      | Jump if true, keep value  |
| 27 | JUMP *offset*     |                      | Jump forwards             |
| 28 | LE                | *a b* → *a* <= *b*   | Compare: less or equal    |
| 29 | LOCGET *slot*     | ∅ → *a*              | Get local                 |
| 30 | LOCSET *slot*     | *a* → *a*            | Set local                 |
| 31 | LOOP *offset*     |                      | Jump backwards            |
| 32 | LT                | *a b* → *a* <= *b*   | Compare: less than        |
| 33 | METHOD *name*     | *cls f* → *cls*      | Add method to class       |
| 34 | MUL               | *a b* → *a* * *b*    | Multiply                  |
| 35 | NE                | *a b* → *a* < *b*    | Compare: not equal        |
| 36 | NEGATE            | *a* → -*a*           | Negate                    |
| 37 | NI                | *a coll* → bool      | a not in collection       |
| 38 | NOT               | *a* → !*a*           | Not                       |
| 39 | NULL              | ∅ → null             | Load `null`               |
| 40 | POP               | *a* → ∅              | Pops one value            |
| 41 | POPN *n*          | *a...* → ∅           | Pops *n* values           |
| 42 | PROPGET *name*    | *obj* → *a*          | Get property value        |
| 43 | PROPSET *name*    | *obj a* → *a*        | Set property value        |
| 44 | RETURN            | *a* → ∅              | Return                    |
| 45 | SUB               | *a b* → *a* - *b*    | Subtract                  |
| 46 | SUPGET *name*     | *obj sup* → *f*      | Get superclass method     |
| 47 | TGET              | ∅ → *a*              | *a* = T                   |
| 48 | TRUE              | ∅ → true             | Load `true`               |
| 49 | TPUT              | *a* → *a*            | T = *a*                   |
| 50 | THROW             | *a* → ∅              | Throw error               |
| 51 | UPCLOSE *n*       | *v...* → ∅           | Closes *n* upvalue(s)     |
| 52 | UPGET *slot*      | ∅ → *a*              | Get upvalue               |
| 53 | UPSET *slot*      | *a* → *a*            | Set upvalue               |

**Stack Effects:** in the stack effect column, the top of the stack is on the 
right.  

For `SUB`, for example, the stack effect is (*a* *b* → *c*) where *c* is
(*a* - *b*).  When the instruction executes, *b* is on the top of the stack,
with *a* just below it.

**Variable Names:** In the above table (and in the descriptions below), 
the italicized names are used as follows:

- *a*, *b*, *c*: Arbitrary values
- *argc*: An argument count
- *args*: A callable's arguments; 0 or more, depending on *argc*
- *cls*: A class
- *constant*: Index into the chunk's constants table
- *f*: A closure, e.g., a function or method
- *func*: A scripted function definition: the `Function` itself plus upvalue
  details, suitable for building into a `Closure` in the current scope.
- *iter*: A collection iterator
- *msg*: A message string
- *n*: A count
- *name*: An index into the chunk's constants table for a name constant.
- *offset*: A jump offset
- *obj*: An object, e.g., a class instance
- *slot*: A stack slot
- *sub*: A subclass
- *sup*: A superclass
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

### INCR
---
**INCR** | *a* → *b*, where *b* = (*a* + 1)

Increments value *a*. `INCR` is used to implement the `++` operator.

`INCR` is used instead of `ADD` for two reasons:

- `INCR` provides a `++`-specific error message if *a* is not numeric. `ADD`'s 
  error message would be confusing.
- `ADD` will concatenate strings, but `++` is only for use with numbers.

### POPN
---
**POPN** *n* | *a...* → ∅ 

Pops *n* values from the stack.

### UPCLOSE
---
**UPCLOSE** *n* | *v...* → ∅

Pops *n* local variables from the stack, closing any of them that are 
open upvalues. This is used when ending a scope, and by `break` and `continue` 
when ending a scope prematurely.