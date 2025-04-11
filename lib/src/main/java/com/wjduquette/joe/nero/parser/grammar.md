# Nero Grammar

The Nero grammar: Datalog as modified for consistency with Joe
conventions and enhancements. Eventually this should go in an appendix of 
the JUG.

Comments begin with `//`, as in Joe.

## Grammar v3:  Datalog with negation and constraints

The `body` now includes a `where` clause.

```grammar
nero        → clause* EOF ;
clause      → fact | rule ;
fact        → atom "." ;
rule        → head ":-" body ( "where" constraints )? "." ; 
head        → atom ;
body        → bodyItem ( "," bodyItem )* ;
bodyItem    → "not"? atom ;
constraints → "where" constraint ( "," constraint )*
constraint  → variable ( "==" | "!=" | ">" | ">=" | "<" | "<=" ) term ;

atom        → IDENTIFIER "(" term+ ")"
term        → constant | variable ;
constant    → KEYWORD | STRING | NUMBER ;
variable    → IDENTIFIER ;
```

## Grammar v2:  Datalog with negation

`bodyItem` now includes `"not"?`

```grammar
nero      → clause* EOF ;
clause    → fact | rule ;
fact      → atom "." ;
rule      → head ":-" body ; 
head      → atom ;
body      → bodyItem ( "," bodyItem )* ;
bodyItem  → "not"? atom ;

atom      → IDENTIFIER "(" term+ ")"
term      → constant | variable ;
constant  → KEYWORD | STRING | NUMBER ;
variable  → IDENTIFIER ;
```

## Grammar v1: Datalog

As modified for consistency with Joe conventions.

```grammar
nero      → clause* EOF ;
clause    → fact | rule ;
fact      → atom "." ;
rule      → head ":-" body "." ;
head      → atom ;
body      → bodyItem ( "," bodyItem )* ;
bodyItem  → atom ;

atom      → IDENTIFIER "(" term+ ")"
term      → constant | variable ;
constant  → KEYWORD | STRING | NUMBER ;
variable  → IDENTIFIER ;
```