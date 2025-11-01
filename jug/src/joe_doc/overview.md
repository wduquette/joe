# 'joe doc'

The `joe doc` processor is used to produce documentation for Joe itself,
and for a client's Joe packages.  It extracts JoeDoc documentation comments
from `.java` and `.joe` source files and uses them to generate API
documentation in Markdown[^markdown] format; it can also process Markdown files,
translating [JoeDoc links](#joedoc-links) and adding content derived
from the scanned JoeDoc comments.

- [JoeDoc Comments](doc_comments.md)
- [Processing Markdown Files](markdown_files.md)
- [Using the `joe doc` tool](doc_tool.md)
 
[^markdown]: This User's Guide is
produced using [mdBook](https://github.com/rust-lang/mdBook), and so
`joe doc` produces output in a form compatible with `mdBook`.  It should
be usable with any other static site generator that works with Markdown
files.
