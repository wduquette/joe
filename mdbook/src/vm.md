# Appendix: Virtual Machine Instruction Set

This is the instruction set for the experimental (and not yet
complete) Bert byte-engine.  The byte-engine is a stack machine with a few
registers:

- `ip` is the instruction pointer within the current function.
- `T` is the temporary register, used to stash a value momentarily during
  an atomic operation.


|    | Mnemonic/argument | Stack effect         | Description               |
|----|-------------------|----------------------|---------------------------|
| 0  | ADD               | *a b* → *a* + *b*    | Add                       |
| 1  | ASSERT            | *cond msg* → ∅       | Assert condition          |
| 2  | CALL *argc*       | *f args* → *f(args)* | Call callable             |
| 3  | CLASS *name*      | ∅ → *cls*            | Create class              |
| 4  | CLOSURE *def*     | ∅ → *f*              | Load closure              |
| 5  | COMMENT *name*    | ∅ → ∅                | No-op comment             |
| 6  | CONST *constant*  | ∅ → *a*              | Load constant             |
| 7  | DECR              | *a* → *a'*           | a' = a - 1                |
| 8  | DIV               | *a b* → *a* / *b*    | Divide                    |
| 9  | DUP               | *a* → *a* *a*        | Duplicate                 |
| 10 | EQ                | *a b* → *a* == *b*   | Compare: equal            |
| 11 | FALSE             | ∅ → false            | Load `false`              |
| 12 | GE                | *a b* → *a* >= *b*   | Compare: greater or equal |
| 13 | GETNEXT           | *iter* → *a*         | a = iter.next()           |
| 14 | GLODEF *name*     | *a* → ∅              | Define global             |
| 15 | GLOGET *name*     | ∅ → *a*              | Get global                |
| 16 | GLOSET *name*     | *a* → *a*            | Set global                |
| 17 | GT                | *a b* → *a* > *b*    | Compare: greater          |
| 18 | HASNEXT           | *iter* → *flag*      | flag = iter.hasNext()     |
| 19 | IN                | *a coll* → bool      | a in collection           |
| 20 | INCR              | *a* → *a'*           | a' = a + 1                |
| 21 | INHERIT           | *sup sub* → *sup*    | Inheritance               |
| 22 | ITER              | *coll* → *iter*      | iter = coll.iterator()    |
| 23 | JIF *offset*      | *cond* → ∅           | Jump if false             |
| 24 | JIFKEEP *offset*  | *cond* → *cond*      | Jump if false, keep value |
| 25 | JITKEEP *offset*  | *cond* → *cond*      | Jump if true, keep value  |
| 26 | JUMP *offset*     |                      | Jump forwards             |
| 27 | LE                | *a b* → *a* <= *b*   | Compare: less or equal    |
| 28 | LOCGET *slot*     | ∅ → *a*              | Get local                 |
| 29 | LOCSET *slot*     | *a* → *a*            | Set local                 |
| 30 | LOOP *offset*     |                      | Jump backwards            |
| 31 | LT                | *a b* → *a* <= *b*   | Compare: less than        |
| 32 | METHOD *name*     | *cls f* → *cls*      | Add method to class       |
| 33 | MUL               | *a b* → *a* * *b*    | Multiply                  |
| 34 | NE                | *a b* → *a* < *b*    | Compare: not equal        |
| 35 | NEGATE            | *a* → -*a*           | Negate                    |
| 36 | NI                | *a coll* → bool      | a not in collection       |
| 37 | NOT               | *a* → !*a*           | Not                       |
| 38 | NULL              | ∅ → null             | Load `null`               |
| 39 | POP               | *a* → ∅              | Pops one value            |
| 40 | POPN *n*          | *a...* → ∅           | Pops *n* values           |
| 41 | PROPGET *name*    | *obj* → *a*          | Get property value        |
| 42 | PROPSET *name*    | *obj a* → *a*        | Set property value        |
| 43 | RETURN            | *a* → ∅              | Return                    |
| 44 | SUB               | *a b* → *a* - *b*    | Subtract                  |
| 45 | SUPGET *name*     | *obj sup* → *f*      | Get superclass method     |
| 46 | TGET              | ∅ → *a*              | *a* = T                   |
| 47 | TRUE              | ∅ → true             | Load `true`               |
| 48 | TPUT              | *a* → *a*            | T = *a*                   |
| 49 | THROW             | *a* → ∅              | Throw error               |
| 50 | UPCLOSE *n*       | *v...* → ∅           | Closes *n* upvalue(s)     |
| 51 | UPGET *slot*      | ∅ → *a*              | Get upvalue               |
| 52 | UPSET *slot*      | *a* → *a*            | Set upvalue               |

## Variable Names

- *a*, *b*: Arbitrary values
- *argc*: An argument count
- *args*: A callable's arguments; 0 or more, depending on *argc*
- *cls*: A class
- *constant*: Index into the chunk's constants table
- *def*: Function info: the `Function` itself plus upvalue details
- *f*: A closure, e.g., a function or method
- *iter*: A collection iterator
- *n*: A count
- *name*: An index into the chunk's constants table for a name constant.
- *offset*: A jump offset
- *obj*: An object, e.g., a class instance
- *slot*: A stack slot
- *sub*: A subclass
- *sup*: A superclass
- *v*: A local variable

## COMMENT
---
**COMMENT** *name* | ∅ → ∅

A no-op instruction used to insert comments into disassembly listings
and execution traces.  Used for VM debugging.

## POPN
---
**POPN** *n* | *a...* → ∅ 

Pops *n* values from the stack.

## UPCLOSE
---
**UPCLOSE** *n* | *v...* → ∅

Pops *n* local variables from the stack, closing any of them that are 
open upvalues. This is used when ending a scope, and by `break` and `continue` 
when ending a scope prematurely.