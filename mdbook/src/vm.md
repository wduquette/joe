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
| 5  | CONST *constant*  | ∅ → *a*              | Load constant             |
| 6  | DIV               | *a b* → *a* / *b*    | Divide                    |
| 7  | EQ                | *a b* → *a* == *b*   | Compare: equal            |
| 8  | FALSE             | ∅ → false            | Load `false`              |
| 9  | GE                | *a b* → *a* >= *b*   | Compare: greater or equal |
| 10 | GLODEF *name*     | *a* → ∅              | Define global             |
| 11 | GLOGET *name*     | ∅ → *a*              | Get global                |
| 12 | GLOSET *name*     | *a* → *a*            | Set global                |
| 13 | GT                | *a b* → *a* > *b*    | Compare: greater          |
| 14 | INHERIT           | *sup sub* → *sup*    | Inheritance               |
| 15 | JIF *offset*      | *cond* → ∅           | Jump if false             |
| 16 | JIFKEEP *offset*  | *cond* → *cond*      | Jump if false, keep value |
| 17 | JITKEEP *offset*  | *cond* → *cond*      | Jump if true, keep value  |
| 18 | JUMP *offset*     |                      | Jump forwards             |
| 19 | LE                | *a b* → *a* <= *b*   | Compare: less or equal    |
| 20 | LOCGET *slot*     | ∅ → *a*              | Get local                 |
| 21 | LOCSET *slot*     | *a* → *a*            | Set local                 |
| 22 | LOOP *offset*     |                      | Jump backwards            |
| 23 | LT                | *a b* → *a* <= *b*   | Compare: less than        |
| 24 | METHOD *name*     | *cls f* → *cls*      | Add method to class       |
| 25 | MUL               | *a b* → *a* * *b*    | Multiply                  |
| 26 | NE                | *a b* → *a* < *b*    | Compare: not equal        |
| 27 | NEGATE            | *a* → -*a*           | Negate                    |
| 28 | NOT               | *a* → !*a*           | Not                       |
| 29 | NULL              | ∅ → null             | Load `null`               |
| 30 | POP               | *a* → ∅              | Pops one value            |
| 31 | POPN *n*          | *a...* → ∅           | Pops *n* values           |
| 32 | PROPGET *name*    | *obj* → *a*          | Get property value        |
| 33 | PROPSET *name*    | *obj a* → *a*        | Set property value        |
| 34 | RETURN            | *a* → ∅              | Return                    |
| 35 | SUB               | *a b* → *a* - *b*    | Subtract                  |
| 36 | SUPGET *name*     | *obj sup* → *f*      | Get superclass method     |
| 37 | TRUE              | ∅ → true             | Load `true`               |
| 38 | THROW             | *a* → ∅              | Throw error               |
| 39 | UPCLOSE *n*       | *v...* → ∅           | Closes *n* upvalue(s)     |
| 40 | UPGET *slot*      | ∅ → *a*              | Get upvalue               |
| 41 | UPSET *slot*      | *a* → *a*            | Set upvalue               |

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