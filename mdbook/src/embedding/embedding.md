# Embedding Joe

This section explains how to embed a Joe interpreter into an application
or library.[^embed]

There are several basic tasks.

- [Creating the Interpreter](#creating-the-interpreter)
- [Installing Bindings](#installing-bindings)
- [Executing Scripts](#executing-scripts)
- [Executing a REPL](#executing-a-repl)
- [Redirecting Output](#redirective-output)
- [Practical Usage](#practical-usage)

## Creating the Interpreter

To create an interpreter, just create an instance of `Joe`.

```java
var joe = new Joe();
```

That's all there is to it.

## Installing Bindings

Most use cases will involve defining domain-specific bindings, as described
in the [Extending Joe](../extending/extending.md) section.  Use the following
`Joe` methods to install bindings into your instance of `Joe`:

- `setVar()` to set a variable in the global environment.
- `installGlobalFunction()` to install a native function.
- `installType()` to register a type proxy for a native type
- `installScriptResource()` to load a Joe script from the project jar file.
- `installScriptResource()` to load a Joe script from the project jar file.

See the above link and `Joe`'s javadoc for details on how to create these 
various kinds of bindings.

## Executing Scripts

`Joe` provides two ways to execute scripts:

- `joe.runFile()` reads a script from a named file and executes it.
- `joe.run()` executes a script passed in as a string.

Each can throw two Joe-specific exceptions.

Joe throws `SyntaxError` if there's any error found while parsing the
script.  The exception's message is fairly vanilla; but the exception
includes a list of "details" describing all syntax errors found, by 
source line.  `SyntaxError::getErrorsByLine()` returns the list;
`SyntaxError::printErrorsByLine()` will print the errors to either
`System.out` (as `joe run` does) or to the `PrintStream` of your choice.

Joe throws `JoeError` for any runtime error.  There are three subclasses.

- `RuntimeError` is an error thrown by Joe's interpreter itself.  
  `RuntimeError` exceptions will usually include the source line number
  at which the error occurred; see the `line()` method.
- `AssertError` is an error thrown by Joe's `assert` statement.  The message
  details the condition that failed.
- A vanilla `JoeError` is an error thrown by a Joe binding or by a Joe
  script using Joe's `throw` statement.

All kinds of `JoeError` can accumulate a script-level stack trace.  Use
the exception's `getFrames()` method to get the stack frames (a list of 
strings); or its `getJoeStackTrace()` method to get the stack trace in a 
form suitable for printing.

## Executing a REPL

Joe provides [`joe repl`](../joe_repl.md), a tool that invokes an interactive
read/eval/print loop on a vanilla Joe interpreter.  A project can easily
implement its own REPL with its own bindings; see 
`com.wjduquette.joe.tools.ReplTool` for an example.  It's essentially
some I/O code wrapped around the `joe.run()` call.

Joe doesn't provide a `Joe` method for invoking the REPL simply because
clients users will want to customize it heavily for their needs.

## Redirecting Output

The Joe tools, `joe run` *et al*, send their output to `System.out`.  This
is appropriate for a simple command-line tool; a more complex application
might wish to redirect all script output to a log, or perhaps to swallow
it unseen.  Joe handles this via its `outputHandler`, a simple 
`Consumer<String>`.

```java
var joe = new Joe();
joe.setOutputHandler(text -> log(text));
```

The script-level `print()` and `println()` functions all send their output
through this handler.  Further, `Joe` provides Java-level `print()` and
`println()` functions that do the same; this allows the body of any 
native function to easily output whatever it likes.

```java
private Object _square(Joe joe, ArgQueue args) {
    ...
    joe.println("In square()!");
}
```

## Practical Usage

There are many ways to use Joe in an application or library.  One can define
a single instance of `Joe` and use it for running all scripts; or one can
allow each scriptable component to have its own instance of `Joe`.

---

[^embed]: Not how to run Joe in an embedded hardware environment.  Just
to be clear.
