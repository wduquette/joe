# 'joe run'

The `joe run` tool is used to execute Joe scripts.  The command line
syntax is as follows:

**joe run \[*options*...] *filename.joe***

When executed, it compiles the script, displaying any compilation errors;
and if there are none executes the script and displays its output.

`joe run` supports [locally installed Joe packages](#using-local-joe-packages).

## Example

For example, to execute

```joe
// hello.joe
println("Hello, world!");
```

Do this:

```shell
$ joe run hello.joe
Hello, world!
$
```

## Options

`joe run` takes the following options:

**--libpath *path***, **-l *path***

Sets the [library path](#using-local-joe-packages) to the given path. 

**--clark**, **-c**

Use the "Clark" byte-engine (the default).

**--walker**, **-w**

Use the "Walker" AST-walker engine.

**--debug**, **-d**

Enable debugging output.  This is mostly of use to the Joe maintainer.

## The `joe.console` API

The Joe interpreter used by `joe run` includes an optional Joe package,
[`joe.console`](library/pkg.joe.console.md), that allows Joe scripts to
access the command line arguments, read standard input and text files, and
write text files.

## Using Local Joe Packages

`joe run` can find and load locally-installed Joe packages from 
local [package repositories](package_repos.md) found on a library path,
a colon-delimited list of package repository folders.  

`joe run` will use the value of  the `JOE_LIB_PATH` environment variable by 
default; this can be modified using the `--libpath` option.




