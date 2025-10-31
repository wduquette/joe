# Nero as a Data Format

Nero `.nero` files are surprisingly useful as a data input format and
as a persistent data format.

- They are as easy to type as CSV files.
- They support multiple "tables" (i.e., multiple relations); CSV does not.
- Nero's collection literals allow structured data to be entered concisely
  and then converted back into normal form using rules.
- Nero allows an input file to define a simple schema.
- Nero has a parser/compiler that produces excellent, readable error messages.
- The [`nero` application](../nero_app.md) can read `.nero` data, do updates, 
  and output  a `.nero` file again.
- [`joe run`](../joe_run.md) scripts and Java code can do the same, provided 
  that care is taken not to create facts whose terms cannot be represented 
  in Nero syntax.

See also:

- [The `nero` Application](../nero_app.md)
- [Using Nero with Joe](nero_and_joe.md)
- [Using Nero with Java](nero_and_java.md)

