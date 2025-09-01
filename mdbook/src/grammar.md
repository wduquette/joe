# Appendix: Grammars

This appendix shows the grammars of the Joe language and its 
sub-languages, in the syntax used in Robert Nystrom's *Crafting Interpreters*.

- [Joe Grammar](#joe-grammar)
  - [Reserved Words](#reserved-words) 
  - [Statements](#statements)
  - [Expressions](#expressions)
  - [Patterns](#patterns)
- [Nero Grammar](#nero-grammar)
- [Lexical Grammar](#lexical-grammar)
- [Lox Grammar](#lox-grammar)

## Joe Grammar

Joe's grammar is based on Nystrom's Lox grammar, with many changes and
additions.

### Reserved Words

Joe reserves the following words:

- `assert`
- `break`
- `case`, `class`, `continue`
- `default`
- `else`, `export`, `extends`
- `false`, `for`, `foreach`, `function`
- `if`, `import`, `in`
- `let`
- `match`, `method`
- `ni`, `not`, `null`
- `record`, `return`, `ruleset`
- `static`, `super`, `switch`
- `this`, `throw`, `true`
- `var`
- `where`, `while`


### Statements

```
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
parameters      → IDENTIFIER ( "," IDENTIFIER )* ;

recordDecl      → "record" IDENTIFIER "(" parameters ")" 
                  "{" recordItem* "}" ;
recordItem      → "method" function
                | "static" "method" function
                | "static" block ; 
                  
varDecl         → "var" IDENTIFIER ( "=" expression )? ";" 
                | "var" pattern "=" expression ";" ;
               
statement       → exprStmt
                | breakStmt
                | continueStmt
                | forStmt
                | foreachStmt
                | ifStmt
                | matchStmt
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
forEachStmt     → "foreach" "(" pattern ":" expression ")" statement;
ifStmt          → "if" "(" expression ")" statement 
                  ( "else" statement )? ;
matchStmt       → "match" "(" expression ")" "{" 
                  ( "case" pattern ( "if" expression )? "->" statement )+ 
                  ( "default" "->" statement )? 
                  "}" ;                  
printStmt       → "print" expression ";" ;
returnStmt      → "return" expression? ";" ;
switchStmt      → "switch" "(" expression ")" "{"
                  ( "case" expression ( "," expression )* "->" statement )+
                  ( "default" "->" statement )?
                  "}" ;
throwStmt       → "throw" expression ";" ;
whileStmt       → "while" "(" expression ")" statement ;
block           → "{" declaration* "}" ;
```

### Expressions

```
expression      → assignment ;
assignment      → ( ( call "." )? IDENTIFIER 
                    | call "[" primary "]"
                  )
                  ( "=" | "+=" | "-=" | "*=" | "/=" ) 
                  assignment 
                | ternary ;
ternary         → logic_or "?" logic_or ":" logic_or ;
logic_or        → logic_and ( "||" logic_and )* ;
logic_and       → equality ( "&&" equality )* ;
equality        → comparison ( ( "!=" | "==" ) comparison )* ;
comparison      → term ( ( ">" | ">=" | "<" | "<=" | "~" | "in" | "ni" ) term )* ;
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
                | "." IDENTIFIER
                | "ruleset" "{" ruleset "}" ;
                | IDENTIFIER 
                | lambda 
                | grouping
                | list
                | set
                | map 
                | "super" "." IDENTIFIER ;
grouping        → "(" expression ")"
lambda          → "\" parameters? "->" ( expression | block ) ; 
list            → "[" (expression ( "," expression )* ","? )? "]" ;
set             → "{" (expression ( "," expression )* ","? )? "}" ;
map             → "{:}" 
                | {" (map_entry ( "," map_entry )* ","? )? "]" ;
map_entry       → expression ":" expression ;
```

## Patterns

A `pattern` is a destructuring pattern that can be used with the `~` 
operator, and with a number of Joe statements.  Patterns are also
used as part of the [Nero Grammar](#nero-grammar).

```
pattern             → constantPattern
                    | exprPattern
                    | listPattern
                    | mapPattern 
                    | namedFieldPattern 
                    | orderedFieldPattern
                    | typeNamePattern
                    | variablePattern
                    | wildcardPattern ;
                    
neroPattern         → constantPattern
                    | listPattern
                    | mapPattern 
                    | namedFieldPattern 
                    | orderedFieldPattern
                    | typeNamePattern
                    | variablePattern
                    | wildcardPattern ;
                
constantPattern     → "true" | "false" | "null" | STRING | NUMBER | KEYWORD;
exprPattern         → "$" IDENTIFIER
                    | "$" "(" expression ")" ;
listPattern         → "[" 
                          (subpattern ( "," subpattern )* ","? )?
                          ( ":" IDENTIFIER )?
                      "]";
mapPattern          → "{"
                          ( entryPattern ( "," entryPattern )* ","? )?
                      "}" ;
entryPattern        → (constantPattern | exprPattern) ":" subpattern ;
namedFieldPattern   → IDENTIFIER "("
                        ( fieldPattern ( "," fieldPattern )* ","? )?
                    ")" ;
orderedFieldPattern → IDENTIFIER "(" 
                          ( pattern ( "," pattern )* ","? )? 
                      ")" ;
fieldPattern        → IDENTIFIER ":" subpattern ;
subpattern          → IDENTIFIER "@" pattern ;
typeNamePattern     → IDENTIFIER "(" ")" ;
variablePattern     → IDENTIFIER;
```


## Nero Grammar

This is the grammar for Nero, Joe's dialect of Datalog.  Nero programs can
be parsed standalone, or as part of a Joe script via the `ruleset` expression.

```grammar
nero          → clause* ;
clause        → defineDecl
              | transientDecl
              | axiom
              | rule ;
defineDecl    → "define" "transient"? relation "/" (
                NUMBER | "..." | ( IDENTIFIER ( "," IDENTIFIER )* )
              ) ";" ;
              
transientDecl → "transient" relation ";" ;

axiom           → head ";"
rule            → head ":-" body ( "where" constraints )? ";"
head            → headAtom ;
body            → bodyAtom ( "," "not"? bodyAtom )* ;
headAtom        → orderedHeadAtom | namedHeadAtom ;
orderedHeadAtom → relation "(" headTerm ( "," headTerm )* ")" ;
namedHeadAtom   → relation "(" IDENTIFIER ":" headTerm 
                    ( "," IDENTIFIER ":" headTerm )* 
                  ")" ;
bodyAtom        → orderedBodyAtom | namedBodyAtom ;
orderedBodyAtom → relation "(" bodyTerm ( "," bodyTerm )* ")" ;
namedBodyAtom   → relation "(" IDENTIFIER ":" bodyTerm 
                    ( "," IDENTIFIER ":" bodyTerm )* 
                  ")" ;

constraints     → constraint ( "," constraint )* ;
constraint      → variable ( "==" | "!=" | ">" | ">=" | "<" | "<=" ) 
                  constraintTerm ;

relation        → IDENTIFIER "!"? ;

headTerm        → aggregateTerm
                | constantTerm 
                | listTerm
                | mapTerm
                | setTerm
                | variableTerm ;
bodyTerm        → constantTerm 
                | patternTerm
                | variableTerm 
                | wildcardTerm ;
constraintTerm  → constantTerm 
                | variableTerm ;
                
aggregateTerm   → IDENTIFIER ( variableTerm ( "," variableTerm )* ) ;
constantTerm    → KEYWORD | STRING | NUMBER | TRUE | FALSE | NULL ;
listTerm        → "[" ( headTerm ( "," headTerm )* )? "]" ;
setTerm         → "{" ( headTerm ( "," headTerm )* )? "}" ;
mapTerm         → "{:}"
                | "{" ( headTerm ":" headTerm 
                      ( "," headTerm ":" headTerm)* )? 
                  "}" ;
patternTerm   → neroPattern ;  
variableTerm  → IDENTIFIER ;     // No leading "_"
wildcardTerm  → IDENTIFIER ;     // With leading "_"
```

## Lexical Grammar

Joe and Nero share the same lexical grammar and the same scanner.

```
NUMBER         → DIGIT+ ( "." DIGIT+ )? ( ("e" | "E") ( "." DIGIT+ )?
               | "0x" HEX_DIGIT+ ;
STRING         → '"' STRING_CHAR* '"'
               | '"""' ( STRING_CHAR | '\n' )* '"""'
               | "'" <any char but '\n'> * "'" 
               | "'''" <any char> "'''" ;
STRING_CHAR    → escapes | <any character but '\' or '"'> ;
escapes        → '\\' | '\"' | '\b' | '\t' | '\n' | '\r' | '\f' 
               | \u HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT ;
KEYWORD        → "#" IDENTIFIER ;
IDENTIFIER     → ALPHA ( ALPHA | DIGIT )* ;
ALPHA          → "a" ... "z" | "A" ... "Z" | "_" ;
DIGIT          → "0" ... "9" ;
HEX_DIGIT      → "0" ... "9" | "a" ... "f" | "A" ... "F" ;
```

## Lox Grammar

Here is Nystrom's Lox grammar, for comparison.

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

