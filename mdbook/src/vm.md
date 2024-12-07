# Appendix: Virtual Machine Instruction Set

This is the instruction set for the experimental (and not yet
complete) Bert byte-engine.

| Opcode/argument   | Stack effect       | Description               |
| ----------------- | ------------------ | ------------------------- |
| ADD               | *a b* → *a* + *b*  | Add                       |
| CONST *constant*  | ∅ → *a*            | Load constant             |
| DIV               | *a b* → *a* / *b*  | Divide                    |
| EQ                | *a b* → *a* == *b* | Compare: equal            |
| FALSE             | ∅ → false          | Load `false`              |
| GE                | *a b* → *a* >= *b* | Compare: greater or equal |
| GLODEF *constant* | *a* → ∅            | Define gLobal             |
| GLOGET *constant* | ∅ → *a*            | Get global                |
| GLOSET *constant* | *a* → *a*          | Set global                |
| GT                | *a b* → *a* > *b*  | Compare: greater          |
| JIF *offset*      | *cond* → ∅         | Jump if false             |
| JIFKEEP *offset*  | *cond* → *cond*    | Jump if false, keep value |
| JITKEEP *offset*  | *cond* → *cond*    | Jump if true, keep value  |
| JUMP *offset*     |                    | Jump forwards             |
| LE                | *a b* → *a* <= *b* | Compare: less or equal    |
| LOCGET *slot*     | ∅ → *a*            | Get local                 |
| LOCSET *slot*     | *a* → *a*          | Set local                 |
| LOOP *offset*     |                    | Jump backwards            |
| LT                | *a b* → *a* <= *b* | Compare: less than        |
| MUL               | *a b* → *a* * *b*  | Multiply                  |
| NE                | *a b* → *a* < *b*  | Compare: not equal        |
| NEGATE            | *a* → -*a*         | Negate                    |
| NOT               | *a* → !*a*         | Not                       |
| NULL              | ∅ → null           | Load `null`               |
| POP               | *a* → ∅            | Pop                       |
| RETURN            |                    | Return                    |
| SUB               | *a b* → *a* - *b*  | Subtract                  |
| TRUE              | ∅ → true           | Load `true`               |

- *constant*: Index into the chunk's constants table
- *offset*: A jump offset
- *slot*: A stack slot
