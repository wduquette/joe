# Extending Joe

Joe can easily be extended with new functions, data types, and bindings
to existing Java types.  This section explains how the trick is done.

- [Joe and Java Data Types](java_types.md)
- [Native Functions](native_functions.md)
- [Registered Types](registered_types.md)
- [Joe Packages](packages.md)
- [Native Records](native_records.md)
- [Native Classes](native_classes.md)

These sections will refer constantly to Joe's Java API.  See the 
Joe Javadoc for details.

But first, a little philosophy.

## Handwritten Bindings vs. Generated Bindings

It is possible to automatically generate script-level bindings for Java classes
by using Java reflection or by scanning the Java source code, as
[Swig](https://swig.org) is often used to create dynamic language bindings for C APIs.

In my experience, an automatically generated binding falls far short of what 
can be done with a good handwritten binding.  A handwritten binding:

- Can take full advantage of the features of the scripting language to 
  provide a simpler, easier, more powerful API to the programmer, rather than
  simply mimicking the Java API.
 
- Can trivially ignore those aspects of the Java API
  that need not or should not be exposed at the scripting level.

- Can do a much better job of providing useful, detailed error messages
  to the user than a generated binding.
 
- Need not expose dangerous Java APIs (e.g., network APIs) to clients of
  the binding.

It is therefore a non-goal of Joe to automatically provide bindings to 
existing classes via Java reflection or code generation.  And it is very much 
a goal of Joe to make writing handwritten bindings as easily and 
pain-free as possible.

