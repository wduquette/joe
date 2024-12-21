# Appendix: Virtual Machine Instruction Set

This is the instruction set for the experimental (and not yet
complete) Bert byte-engine.

|    | Mnemonic/argument | Stack effect         | Description               |
|----|-------------------|----------------------|---------------------------|
| 0  | ADD               | *a b* → *a* + *b*    | Add                       |
| 1  | CALL *argc*       | *f args* → *f(args)* | Call callable             |
| 2  | CLOSURE *def*     | ∅ → *f*              | Load closure              |
| 3  | CONST *constant*  | ∅ → *a*              | Load constant             |
| 4  | DIV               | *a b* → *a* / *b*    | Divide                    |
| 5  | EQ                | *a b* → *a* == *b*   | Compare: equal            |
| 6  | FALSE             | ∅ → false            | Load `false`              |
| 7  | GE                | *a b* → *a* >= *b*   | Compare: greater or equal |
| 8  | GLODEF *constant* | *a* → ∅              | Define gLobal             |
| 9  | GLOGET *constant* | ∅ → *a*              | Get global                |
| 10 | GLOSET *constant* | *a* → *a*            | Set global                |
| 11 | GT                | *a b* → *a* > *b*    | Compare: greater          |
| 12 | JIF *offset*      | *cond* → ∅           | Jump if false             |
| 13 | JIFKEEP *offset*  | *cond* → *cond*      | Jump if false, keep value |
| 14 | JITKEEP *offset*  | *cond* → *cond*      | Jump if true, keep value  |
| 15 | JUMP *offset*     |                      | Jump forwards             |
| 16 | LE                | *a b* → *a* <= *b*   | Compare: less or equal    |
| 17 | LOCGET *slot*     | ∅ → *a*              | Get local                 |
| 18 | LOCSET *slot*     | *a* → *a*            | Set local                 |
| 19 | LOOP *offset*     |                      | Jump backwards            |
| 20 | LT                | *a b* → *a* <= *b*   | Compare: less than        |
| 21 | MUL               | *a b* → *a* * *b*    | Multiply                  |
| 22 | NE                | *a b* → *a* < *b*    | Compare: not equal        |
| 23 | NEGATE            | *a* → -*a*           | Negate                    |
| 24 | NOT               | *a* → !*a*           | Not                       |
| 25 | NULL              | ∅ → null             | Load `null`               |
| 26 | POP               | *a* → ∅              | Pop                       |
| 27 | RETURN            |                      | Return                    |
| 28 | SUB               | *a b* → *a* - *b*    | Subtract                  |
| 29 | TRUE              | ∅ → true             | Load `true`               |
| 30 | UPCLOSE           | *a* → ∅              | Close upvalue             |
| 31 | UPGET *slot*      | ∅ → *a*              | Get upvalue               |
| 32 | UPSET *slot*      | *a* → *a*            | Set upvalue               |
| 33 | PRINT             | *a* → ∅              | Print value               |

- *argc*: An argument count
- *args*: A callable's arguments; 0 or more, depending on *argc*
- *constant*: Index into the chunk's constants table
- *def*: Function info: the `Function` itself plus upvalue details
- *offset*: A jump offset
- *slot*: A stack slot
