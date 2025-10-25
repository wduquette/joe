# 'joe repl'

The `joe repl` is intended primarily as a convenience to those learning 
the Joe language (and as a debugging aid for the Joe developer); it
can also be used as a calculator.

Executed with no arguments, but simply invokes a simple interactive prompt
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

## Options

`joe repl` takes the following options:

**--libpath *path***, **-l *path***

Sets the [library path](#using-local-joe-packages) to the given path.

**--clark**, **-c**

Use the "Clark" byte-engine (the default).

**--walker**, **-w**

Use the "Walker" AST-walker engine.

**--debug**, **-d**

Enable debugging output.  This is mostly of use to the Joe maintainer.


## The `joe.console` API

The Joe interpreter used by `joe repl` includes an optional Joe package,
[`joe.console`](library/pkg.joe.console.md), that allows Joe scripts to
access the command line arguments, read standard input and text files, and
write text files.

## Using Local Joe Packages

`joe repl` can find and load locally-installed Joe packages from
local [package repositories](package_repos.md) found on a library path,
a colon-delimited list of package repository folders.

`joe repl` will use the value of  the `JOE_LIB_PATH` environment variable by
default; this can be modified using the `--libpath` option.


