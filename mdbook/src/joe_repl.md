# joe repl

The `joe repl` tool is every bit as simple as `joe run`, and like it
is intended primarily as a convenience to those learning the Joe language 
(and as a debugging aid for the Joe developer).

It takes no arguments, but simply invokes a simple interactive prompt
for entering and execution Joe statements.

```shell
$ joe repl
> 1 + 1;
-> 2
> println("Hello, world!");
Hello, world!
>
$
```

To display the result of an expression, type in the expression followed
by a semicolon, as shown.  To execute the statement, do likewise.

To exit the REPL, press `^D` or `^C`.

It is expected that projects using Joe will want to define an
equivalent tool that includes the project-specific bindings, along with
various other niceties.  See
[Embedding Joe](embedding/embedding.md) and
[Extending Joe](extending/extending.md) for details.

