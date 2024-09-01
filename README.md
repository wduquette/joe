# README.md

Joe is a little language based on Lox from Robert Nystrom's excellent book,
*Crafting Interpreters*, meant to add scriptability to Java libraries and
applications.  Joe scripts are meant to be naturally sandboxed: clients can 
provide bindings to Java methods and types, but the runtime has no automatic
access to the Java class hierarchy, to the operating system, or to any kind of
I/O other than simple output (all of which can be redirected by the client).

Most of the following content will move to the Joe User's Guide once there is
such a thing.

## Joe 0.1.0 (2024/09/01)

Joe 0.1.0 is a complete implementation of JLox, with the following changes:

- Syntax
  - `null` replaces `nil`.
  - `&&` replaces `and`.
  - `||` replaces `or`.
  - Strings can include the standard string escapes, which are converted into the usual characters.
    - '\\\\', `\t`, `\b`, `\n`, `\r`, `\f`, `\"`
  - `function` replaces `fun` as the function definition keyword.
  - `method` is added as the method definition keyword.
  - The `extends` keyword replaces `<` in `class Sub extends Super {...}`.
- Semantics
  - The `<`, `<=`, `>`, and `>=` operators can compare strings.
  - A function with no `return` returns the value of the last statement in the
    block (which is usually null).
  - An expression statement yields the value of the expression.
  - Thus, running a script returns a value.
- Embedding API
  - The `Joe` engine is separated from the `App` application, so that a client
    can create `Joe` instances as needed.
    - The embedding API is still mostly non-existent.
- Tools
  - The `App` includes "tool" infrastructure, and supports two tools, accessed
    as `joe run` and `joe repl`.
    - The intent is that a client project can reuse the underlying tools in its
      own application if desired.
    - `joe repl` outputs the value of each statement; other than that, the 
      two tools are more or less as described in *Crafting Interpreters*.

## Goals

- Joe is intended for use by Java programmers, and so will be as Java-like as
  is consistent with being a dynamic scripting language.
  - All surprises are meant to be pleasant ones.
- The runtime will include a standard library with support for
  - Strings
  - Numbers
  - Lists
  - Maps
  - Sets
  - Keyword symbols
  - Errors
- The embedding API should make adding Java bindings simple, 
  straightforward, and flexible.
- Error messages should be clear and informative.
- The toolchain will include a command-line script runner, a test runner, 
  a simple REPL, and possible a documentation generator.
- There will be a thorough test suite for the language and its standard 
  library.
  - (Because if there isn't this project is dead code walking.)
- There will be a detailed user's guide, with examples.