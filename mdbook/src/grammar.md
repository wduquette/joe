# Joe Grammar

This appendix shows the grammar of the Joe language, in the syntax 
used in *Crafting Interpreters*.  Below that is the full 
[JLox grammar](#jlox-grammar).

## Grammar

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
throwStmt       → "throw" expression ";" ;
whileStmt       → "while" "(" expression ")" statement ;
block           → "{" declaration* "}" ;

// Expression
expression      → assignment ;
assignment      → ( call "." )? IDENTIFIER 
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
call            → primary ( "(" arguments? ")" | "." IDENTIFIER )* ;
arguments       → expression ( "," expression )* ;
primary         → "true" | "false" | "nil"
                | NUMBER | STRING | KEYWORD
                | "this"
                | IDENTIFIER 
                | "\" parameters? "->" ( expression | block ) ; 
                | "(" expression ")" 
                | "super" "." IDENTIFIER ;
```

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

