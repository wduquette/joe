# Change Log

## Changes in 0.2.0 (Forthcoming)

Joe 0.2.0 extends Joe on every axis.

- Documentation
  - Added this User's Guide (still a WIP)
  
- Language
  - Added `assert` statement
  - Remove `print` statement
  - `+` will do string concatenation provided that at least one operand is
    a string.
  - Added `Keyword` values: `#abc` is an interned constant.
  - Added the ternary operator `? :`.
  - Joe `class` declarations may now include `static method` declarations
    and `static {...}` initializer blocks.  Static methods and variables are 
    always referenced by the class name, as in Java.
  - Joe functions and methods can now accept a variable length argument list 
    by defining an `args` parameter as the last parameter in the list.  `args`
    will be a list containing any excess values.
  
- Library
  - Added `catch()` function
  - Added `println()`, `print()` functions
  - Added `Number` proxy (all numbers are Java `Doubles`)
  - Added `Error` proxy (for `JoeError` and its subclasses)
  - Added `Keyword` Java type, with proxy
  - Added `Pair` Java type, with proxy
  - Added `String` proxy
    
- Embedding API
  - Cleaned up top-level error handling
  - Added `Joe::installScriptResource`
  - Added `TypeProxy<V>`, a class that defines a binding for native Java
    type `V`.  A `TypeProxy` can provide
    - Instance methods and an initializer for type V
      - E.g., a `String`'s `length()` method.
    - Static methods and constants
      - E.g., `Double.PI`, `Double.abs()`.

- Tools
  - Added `joe test`, a test runner for Joe-level test scripts, with a library
    of assertion checkers.

- Development
  - Added `joe/tests`, the script-level Joe test suite.

- Misc
  - Added LICENSE and CONTRIBUTING.md

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

