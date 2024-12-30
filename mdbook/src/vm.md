# Appendix: Virtual Machine Instruction Set

This is the instruction set for the experimental (and not yet
complete) Bert byte-engine.


|    | Mnemonic/argument | Stack effect         | Description               |
|----|-------------------|----------------------|---------------------------|
| 0  | ADD               | *a b* → *a* + *b*    | Add                       |
| 1  | CALL *argc*       | *f args* → *f(args)* | Call callable             |
| 2  | CLASS *name*      | ∅ → *cls*            | Create class              |
| 3  | CLOSURE *def*     | ∅ → *f*              | Load closure              |
| 4  | COMMENT *name*    | ∅ → ∅                | No-op comment             |
| 4  | CONST *constant*  | ∅ → *a*              | Load constant             |
| 5  | DIV               | *a b* → *a* / *b*    | Divide                    |
| 6  | EQ                | *a b* → *a* == *b*   | Compare: equal            |
| 7  | FALSE             | ∅ → false            | Load `false`              |
| 8  | GE                | *a b* → *a* >= *b*   | Compare: greater or equal |
| 9  | GLODEF *name*     | *a* → ∅              | Define gLobal             |
| 10 | GLOGET *name*     | ∅ → *a*              | Get global                |
| 11 | GLOSET *name*     | *a* → *a*            | Set global                |
| 12 | GT                | *a b* → *a* > *b*    | Compare: greater          |
| 13 | INHERIT           | *sup sub* → *sup*    | Inheritance               |
| 14 | JIF *offset*      | *cond* → ∅           | Jump if false             |
| 15 | JIFKEEP *offset*  | *cond* → *cond*      | Jump if false, keep value |
| 16 | JITKEEP *offset*  | *cond* → *cond*      | Jump if true, keep value  |
| 17 | JUMP *offset*     |                      | Jump forwards             |
| 18 | LE                | *a b* → *a* <= *b*   | Compare: less or equal    |
| 19 | LOCGET *slot*     | ∅ → *a*              | Get local                 |
| 20 | LOCSET *slot*     | *a* → *a*            | Set local                 |
| 21 | LOOP *offset*     |                      | Jump backwards            |
| 22 | LT                | *a b* → *a* <= *b*   | Compare: less than        |
| 23 | METHOD *name*     | *cls f* → *cls*      | Add method to class       |
| 24 | MUL               | *a b* → *a* * *b*    | Multiply                  |
| 25 | NE                | *a b* → *a* < *b*    | Compare: not equal        |
| 26 | NEGATE            | *a* → -*a*           | Negate                    |
| 27 | NOT               | *a* → !*a*           | Not                       |
| 28 | NULL              | ∅ → null             | Load `null`               |
| 29 | POP               | *a* → ∅              | Pops one value            |
| 30 | POPN *n*          | *a...* → ∅           | Pops *n* values           |
| 31 | PROPGET *name*    | *obj* → *a*          | Get property value        |
| 32 | PROPSET *name*    | *obj a* → *a*        | Set property value        |
| 33 | RETURN            | *a* → ∅              | Return                    |
| 34 | SUB               | *a b* → *a* - *b*    | Subtract                  |
| 35 | SUPGET *name*     | *obj sup* → *f*      | Get superclass method     |
| 36 | TRUE              | ∅ → true             | Load `true`               |
| 37 | UPCLOSE *n*       | *v...* → ∅           | Closes *n* upvalue(s)     |
| 38 | UPGET *slot*      | ∅ → *a*              | Get upvalue               |
| 39 | UPSET *slot*      | *a* → *a*            | Set upvalue               |

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