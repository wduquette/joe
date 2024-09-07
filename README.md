# README.md

Joe is a little language based on Lox from Robert Nystrom's excellent book,
*Crafting Interpreters*, meant to add scriptability to Java libraries and
applications.  Joe scripts are meant to be naturally sandboxed: clients can 
provide bindings to Java methods and types, but the runtime has no automatic
access to the Java class hierarchy, to the operating system, or to any kind of
I/O other than simple output (all of which can be redirected by the client).

TODO: Add link to Joe User's Guide.
