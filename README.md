# README.md

Joe is a little language based on Lox from Robert Nystrom's excellent book,
*Crafting Interpreters*, meant to add scriptability to Java libraries and
applications.  Joe scripts are meant to be naturally sandboxed: clients can 
provide bindings to Java methods and types, but the runtime has no automatic
access to the Java class hierarchy, to the operating system, or to any kind of
I/O other than simple output (all of which can be redirected by the client).

NOTE: Joe is still in its earliest phases.
be taken as aspirational.

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