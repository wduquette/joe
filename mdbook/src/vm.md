# Appendix: Virtual Machine Instruction Set

This is the instruction set for the experimental (and not yet
complete) Bert byte-engine.


|    | Mnemonic/argument | Stack effect         | Description               |
|----|-------------------|----------------------|---------------------------|
| 0  | ADD               | *a b* → *a* + *b*    | Add                       |
| 1  | ASSERT            | *cond msg* → ∅       | Assert condition          |
| 2  | CALL *argc*       | *f args* → *f(args)* | Call callable             |
| 3  | CLASS *name*      | ∅ → *cls*            | Create class              |
| 4  | CLOSURE *def*     | ∅ → *f*              | Load closure              |
| 5  | COMMENT *name*    | ∅ → ∅                | No-op comment             |
| 6  | CONST *constant*  | ∅ → *a*              | Load constant             |
| 7  | DIV               | *a b* → *a* / *b*    | Divide                    |
| 8  | DUP               | *a* → *a* *a*        | Duplicate                 |
| 9  | EQ                | *a b* → *a* == *b*   | Compare: equal            |
| 10 | FALSE             | ∅ → false            | Load `false`              |
| 11 | GE                | *a b* → *a* >= *b*   | Compare: greater or equal |
| 12 | GLODEF *name*     | *a* → ∅              | Define global             |
| 13 | GLOGET *name*     | ∅ → *a*              | Get global                |
| 14 | GLOSET *name*     | *a* → *a*            | Set global                |
| 15 | GT                | *a b* → *a* > *b*    | Compare: greater          |
| 16 | INHERIT           | *sup sub* → *sup*    | Inheritance               |
| 17 | JIF *offset*      | *cond* → ∅           | Jump if false             |
| 18 | JIFKEEP *offset*  | *cond* → *cond*      | Jump if false, keep value |
| 19 | JITKEEP *offset*  | *cond* → *cond*      | Jump if true, keep value  |
| 20 | JUMP *offset*     |                      | Jump forwards             |
| 21 | LE                | *a b* → *a* <= *b*   | Compare: less or equal    |
| 22 | LOCGET *slot*     | ∅ → *a*              | Get local                 |
| 23 | LOCSET *slot*     | *a* → *a*            | Set local                 |
| 24 | LOOP *offset*     |                      | Jump backwards            |
| 25 | LT                | *a b* → *a* <= *b*   | Compare: less than        |
| 26 | METHOD *name*     | *cls f* → *cls*      | Add method to class       |
| 27 | MUL               | *a b* → *a* * *b*    | Multiply                  |
| 28 | NE                | *a b* → *a* < *b*    | Compare: not equal        |
| 29 | NEGATE            | *a* → -*a*           | Negate                    |
| 30 | NOT               | *a* → !*a*           | Not                       |
| 31 | NULL              | ∅ → null             | Load `null`               |
| 32 | POP               | *a* → ∅              | Pops one value            |
| 33 | POPN *n*          | *a...* → ∅           | Pops *n* values           |
| 34 | PROPGET *name*    | *obj* → *a*          | Get property value        |
| 35 | PROPSET *name*    | *obj a* → *a*        | Set property value        |
| 36 | RETURN            | *a* → ∅              | Return                    |
| 37 | SUB               | *a b* → *a* - *b*    | Subtract                  |
| 38 | SUPGET *name*     | *obj sup* → *f*      | Get superclass method     |
| 39 | TRUE              | ∅ → true             | Load `true`               |
| 40 | THROW             | *a* → ∅              | Throw error               |
| 41 | UPCLOSE *n*       | *v...* → ∅           | Closes *n* upvalue(s)     |
| 42 | UPGET *slot*      | ∅ → *a*              | Get upvalue               |
| 43 | UPSET *slot*      | *a* → *a*            | Set upvalue               |

## Variable Names

- *a*, *b*: Arbitrary values
- *argc*: An argument count
- *args*: A callable's arguments; 0 or more, depending on *argc*
- *cls*: A class
- *constant*: Index into the chunk's constants table
- *def*: Function info: the `Function` itself plus upvalue details
- *f*: A closure, e.g., a function or method
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