# Appendix: Joe Grammar

This appendix shows the grammar of the Joe language, in the syntax 
used in *Crafting Interpreters*.  Below that is the full 
[JLox grammar](#jlox-grammar).

## Grammar

Here is the grammar; see [Semantic Constraints](#semantic-constraints)
for some conditions enforced by the compiler.

```
// Statements
program         → declaration* EOF ;

declaration     → classDecl
                | funDecl
                | varDecl
                | statement ;

classDecl       | "class" IDENTIFIER ( "extends" IDENTIFIER )?
                  "{" classItem* "}" ;
classItem       → "method" function
                | "static" "method" function
                | "static" block ; 
                
funDecl         → "function" function ;
function        → IDENTIFIER "(" parameters? ")" block ;
parameters      → IDENTITIFIER ( "," IDENTIFIER )* ;

varDecl         → "var" IDENTIFIER ( "=" expression )? ";" ;
               
statement       → exprStmt
                | breakStmt
                | continueStmt
                | forStmt
                | ifStmt
                | printStmt
                | returnStmt
                | switchStmt
                | throwStmt
                | whileStmt 
                | block ;

exprStmt        → expression ";" ;
breakStmt       → "break" ";" ;
continueStmt    → "continue" ";" ;
forStmt         → "for" "(" ( varDecl | exprStmt | ";" )
                  expression? ";"
                  expression? ")" statement ;
forEachStmt     → "foreach" "(" "var" IDENTIFIER ":" expression ")" statement;
ifStmt          → "if" "(" expression ")" statement 
                  ( "else" statement )? ;
printStmt       → "print" expression ";" ;
returnStmt      → "return" expression? ";" ;
switchStmt      → "switch" "(" expression ")" "{"
                  ( "case" expression ( "," expression )* "->" statement )+
                  ( "default" "->" statement )?
                  "}" ;
throwStmt       → "throw" expression ";" ;
whileStmt       → "while" "(" expression ")" statement ;
block           → "{" declaration* "}" ;

// Expression
expression      → assignment ;
assignment      → ( ( call "." )? IDENTIFIER 
                    | call "[" primary "]"
                  )
                  ( "=" | "+=" | "-=" | "*=" | "/=" ) 
                  assignment 
                | ternary ;
ternary         → logic_or "?" logic_or ":" logic_or ;
logic_or        → logic_and ( "||" logic_and )* ;
logic_or        → equality ( "&&" equality )* ;
equality        → comparison ( ( "!=" | "==" ) comparison )* ;
comparison      → term ( ( ">" | ">=" | "<" | "<=" | "in" | "ni" ) term )* ;
term            → factor ( ( "-" | "+" ) factor )* ;
factor          → unary ( ( "/" | "*" ) unary )* ;
unary           → ( "++" | "==" | "-" | "!" ) unary 
                | postfix ;
postfix         → call ( "++" | "--" )? ;
call            → primary ( "(" arguments? ")" 
                          | "." IDENTIFIER     
                          | "[" primary "]"
                  )*  ;
arguments       → expression ( "," expression )* ;
primary         → "true" | "false" | "nil"
                | NUMBER | STRING | KEYWORD
                | "this"
                | "@" IDENTIFIER
                | IDENTIFIER 
                | lambda 
                | grouping
                | list 
                | "super" "." IDENTIFIER ;
grouping        → "(" expression ")"
lambda          → "\" parameters? "->" ( expression | block ) ; 
list            → "[" (expression ( "," expression )* ","? )? "]" ;
map             → "{" (map_entry ( "," map_entry )* ","? )? "]" ;
map_entry       → expression ":" expression ;
```

## Semantic Constraints

The Joe grammar has no clear distinction between lvalues and rvalues; as
a result, it allows a number of expressions that are forbidden by the
language engines.  At time of writing, I've not determined how to clean
up the grammar to match.  

The following expressions are valid lvalues:

- Variable reference: `IDENTIFIER`
- Property reference: `call "." IDENTIFIER`
- Collection index: `call "[" primary "]"`

Other valid `call` and `unary` expressions are not.  This primarily
affects the increment/decrement operators: `++` and `--`.  For example,
the following expressions are syntactically valid but will be rejected
by the language engines:

- `myFunc(a, b, c)++`
- `--myFunc(a, b, c)`
- `++!x`

## JLox Grammar

Here is Nystrom's JLox grammar, for comparison.

```
// Statements
program         → declaration* EOF ;

declaration     → classDecl
                | funDecl
                | varDecl
                | statement ;

classDecl       | "class" IDENTIFIER ( "extends" IDENTIFIER )?
                  "{" methodDecl* "}" ;
methodDecl      | "method" function ;
funDecl         | "function" function ;
function        → IDENTIFIER "(" parameters? ")" block ;
parameters      → IDENTITIFIER ( "," IDENTIFIER )* ;
varDecl         → "var" IDENTIFIER ( "=" expression )? ";" ;
               
statement       → exprStmt
                | forStmt
                | ifStmt
                | printStmt
                | returnStmt
                | whileStmt 
                | block ;

exprStmt        → expression ";" ;
forStmt         → "for" "(" ( varDecl | exprStmt | ";" )
                  expression? ";"
                  expression? ")" statement ;
ifStmt          → "if" "(" expression ")" statement 
                  ( "else" statement )? ;
printStmt       → "print" expression ";" ;
returnStmt      → "return" expression? ";" ;
whileStmt       → "while" "(" expression ")" statement ;
block           → "{" declaration* "}" ;

// Expression
expression      → assignment ;
assignment      → ( call "." )? IDENTIFIER "=" assignment
                | logic_or ;
logic_or        → logic_and ( "||" logic_and )* ;
logic_or        → equality ( "&&" equality )* ;
equality        → comparison ( ( "!=" | "==" ) comparison )* ;
comparison      → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term            → factor ( ( "-" | "+" ) factor )* ;
factor          → unary ( ( "/" | "*" ) unary )* ;
unary           → ( "-" | "!" ) unary 
                | call ;
call            → primary ( "(" arguments? ")" | "." IDENTIFIER )* ;
arguments       → expression ( "," expression )* ;
primary         → "true" | "false" | "nil"
                | NUMBER | STRING 
                | "this"
                | IDENTIFIER 
                | "(" expression ")" 
                | "super" "." IDENTIFIER ;
```

