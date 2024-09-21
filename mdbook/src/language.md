# 1. The Joe Language

Joe is a Java-like dynamic scripting language:

- Meant for adding scriptability to Java applications and libraries.
- Designed to be comfortable for Java developers.
- Designed to be easy to extend in Java.

But equally, Joe is designed to be insulated from the larger Java world.
Adding a Joe interpreter should not, on its own, add security issues to
an application.

- Joe has no access to the host operating system.
  - Unless the client implements a Java binding for Joe that 
    provides such access.
- Joe has no access to the Java standard library.
  - Except insofar as the client adds bindings to standard Java
    packages.

Joe has its origin in the JLox language from Robert Nystrom's 
outstanding book and website, 
[*Crafting Interpreters*](https://craftinginterpreters.com), but is
by no means identical to JLox.  (If you should happen to find 
Joe useful, please buy a copy of Nystrom's book.)

The following sections describe the language in more detail.