# Appendix: Joe Grammar

This appendix shows the grammar of the Joe language, in the syntax 
used in *Crafting Interpreters*.  Below that is the full 
[JLox grammar](#jlox-grammar).

## Grammar

- [Statements](#statements)
- [Expressions](#expressions)
- [Patterns](#patterns)
- [Nero](#nero)

## Statements

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

## Expressions

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
operator, and with a number of Joe statements.

```
pattern         → patternBinding
                | constantPattern
                | wildcardPattern
                | valueBinding
                | listPattern
                | mapPattern 
                | namedFieldPattern 
                | orderedFieldPattern ;
                
```

A `patternBinding` is a pattern that binds a subpattern's matched value
to a binding variable.  `patternBindings` are disallows as the top 
pattern in some contexts.

```
patternBinding   | IDENTIFIER "@" pattern ;
```

A `constantPattern` matches a literal or computed constant.  Variables
referenced in a `constantPattern` must be defined in the enclosing scope
and not shadowed by one of the pattern's binding variables.

```
constantPattern → "true" | "false" | "null" | STRING | NUMBER | KEYWORD
                | "$" IDENTIFIER
                | "$" "(" expression ")" ;
```

A `valueBinding` binds the matched value, whatever it is, to a variable.

```
valueBinding    → IDENTIFIER ;
```

A `listPattern` is a list of subpatterns to be matched against the items
of a list, with an optional binding variable for the tail of the list.

```
listPattern     → "[" 
                      (subpattern ( "," subpattern )* ","? )?
                      ( ":" IDENTIFIER )?
                  "]";
```

A `mapPattern` is a map of key patterns and value patterns to be matched 
against the entries of a map value.  All keys in the pattern must appear
in the map, but the pattern need not exhaust the map.  The key patterns
must be `constantPatterns`; Joe does not attempt to do full pattern
matching on the map keys.

```
mapPattern      → "{"
                      ( entryPattern ( "," entryPattern )* ","? )?
                  "}" ;
entryPattern    → constantPattern ":" subpattern ;
```

A `namedFieldPattern` matches the type and fields of a Joe value, matching
fields by name rather than by position. The first `IDENTIFIER` must match the 
value's type name.

```
namedFieldPattern → IDENTIFIER "("
                      ( fieldPattern ( "," fieldPattern )* ","? )?
                  ")" ;
fieldPattern    → IDENTIFIER ":" subpattern ;
```

An `orderedFieldPattern` matches the type and fields of a Joe object that has
ordered fields (e.g., Joe records).  The fields are matched by position
rather than by name in a list-pattern-like fashion.

```
orderedFieldPattern  → IDENTIFIER "(" 
                           ( pattern ( "," pattern )* ","? )? 
                       ")" ;
```

## Nero

This is the grammar for Nero, Joe's dialect of Datalog.  Nero rule sets can
be parsed standalone, or as part of a Joe script via the `ruleset` expression.

Differences from classic Datalog with negation:

- Comments begin with `//` rather than `%`
- Horn clauses end with `;` rather than '.'.
- Relations usually have initial caps, to match Monica type names.
- Constant terms can be Monica scalar values, or, if read
  from a scripted input fact, any Monica value.
- Variables are normal identifiers, usually lowercase.
- Wildcards are identifiers with a leading `_`.
- Constraints follow a `where` token.
- Body atoms can reference fact fields either by name or by position.

```grammar
nero        → clause* EOF ;
ruleset     → ( clause | export )* EOF ;
export      → "export" IDENTIFIER ( "as" expression )? ";" ;
clause      → axiom
            | rule ;
axiom       → head ";"
rule        → head ":-" body ( "where" constraints )? ";"
head        → indexedAtom ;
body        → "not"? bodyAtom ( "," "not"? bodyAtom )* ;
bodyAtom    → indexedAtom | namedAtom ;
indexedAtom → IDENTIFIER "(" term ( "," term )* ")" ;
orderedAtom → IDENTIFIER "(" namedTerm ( "," namedTerm )* ")" ;
namedTerm   → IDENTIFIER ":" term ;
constrants  → constraint ( "," constraint )* ;
constraint  → variable ( "==" | "!=" | ">" | ">=" | "<" | "<=" ) term ;
term        → constant | variable | wildcard ;
constant    → KEYWORD | STRING | NUMBER | TRUE | FALSE | NULL ;
variable    → IDENTIFIER ;     // No leading "_"
wildcard    → IDENTIFIER ;     // With leading "_"
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

