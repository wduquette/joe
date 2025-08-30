# 'joe run'

The `joe run` tool is quite simple, and is intended primarily as a 
convenience to those learning the Joe language (and as a debugging aid
for the Joe developer).

It takes a single argument, a Joe script to execute.  It loads the script
into a Joe interpreter, displaying any syntax errors;
and if there are none executes the script and displays its output.

Here's a simple script.

```joe
// hello.joe
println("Hello, world!");
```

Execute it like this:

```shell
$ joe run hello.joe
Hello, world!
$
```

## The `joe.console` API

The Joe interpreter used by `joe run` includes an optional Joe package,
[`joe.console`](library/pkg.joe.console.md), that allows Joe scripts to
access the command line arguments, read standard input and text files, and
write text files.


