# 'joe repl'

The `joe repl` is intended primarily as a convenience to those learning 
the Joe language (and as a debugging aid for the Joe developer); it
can also be used as a calculator.

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

The REPL provides no means of loading or saving Joe code, nor any
"readline"-style command line editing or history.

## The `joe.console` API

The Joe interpreter used by `joe repl` includes an optional Joe package,
[`joe.console`](library/pkg.joe.console.md), that allows Joe scripts to
access the command line arguments, read standard input and text files, and
write text files.  Follow the link for details.

