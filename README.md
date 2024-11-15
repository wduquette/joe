# README.md

Joe is a dynamic scripting language, meant to be used to add scriptability
to Java applications and libraries.  Joe scripts are naturally sandboxed:
clients can provide bindings to any desired Java classes, but the 
runtime has no automatic access to the Java class hierarchy, the operating
system, or to any kind of I/O other than simple output (which can be 
captured and redirected by the client).

Because Joe is explicitly intended to be embedded in Java applications, 
Joe's Java API has been designed to making embedding as easy as possible.
Joe's values are simply Java values. Any Java value can be passed into Joe, 
saved in Joe variables, passed to Joe functions, and so forth.  

If desired the client can define a `TypeProxy<V>` for a given value type,
in order to provide a constructor, methods, and so forth.

## Documentation

See the [Joe User's Guide](https://wduquette.github.io/joe) for a 
description of the language, accompanying tools, Java bindings, etc.

## History

The Joe syntax and implementation derive from the Lox language described in
Robert Nystrom's excellent book
[*Crafting Interpreters*](https://craftinginterpreters.com).  Joe is
recognizable a Lox derivative, but the syntax is not identical and there
have been many enhancements and changes.  

## Installation

See [INSTALL.md](INSTALL.md) for details on how to install the 
Joe distribution.

