# Processing Markdown Files

In addition to producing Joe API documentation in Markdown format, 
`joe doc` can pre-process Markdown files before they are given to a
static site generator to produce HTML.  At present the preprocessor can
translate JoeDoc links into Markdown links, as it does in 
[JoeDoc comments](doc_comments.md), and insert content derived from the 
JoeDoc comments into the Markdown output.

- [JoeDoc Links](#joedoc-links)
- [Inserted Content](#inserted-content)
  - [`@:indent`](#indent)
  - [`@:packageIndex`](#packageindex) 
- [Generating `SUMMARY.md`](#generating-summarymd)

## JoeDoc Links

JoeDoc [links](doc_comments.md#joedoc-links) have the same syntax in the
input Markdown as they do in JoeDoc comments. Entry mnemonics must be
qualified by their package names, e.g., `\[[joe.String]]` will link to the
API page for the Joe [[joe.String]] type, but `\[[String]]` will yield an error.

## Inserted Content

The pre-processor scans for `@:` tags in the Markdown input and replaces them
with generated content.  Tags must be flush-left, with no leading whitespace.

### `@:indent`

The `@:indent` tag sets the indent level, in character columns, for 
any subsequently inserted content.  This is useful when inserting content
into an indented bulleted list.

For example, the following adds two spaces to inserted content.

```
  @:indent 2
```

### `@:packageIndex`

The `@:packageIndex` tag inserts a brief index of the named package, including
a link to the package itself and indented links for all types defined in the
package.

For example

```
  @:packageIndex my.package
```

will expand to something like

```markdown
- [My Package Title](./library/pkg.my.package.md)
  - [MyData type](./library/type.my.package.MyData.md)
  - ...
```

## Generating `SUMMARY.md`

[mdBook](https://rust-lang.github.io/mdBook/) generates a static site given
a `SUMMARY.md` file that defines the site's table of contents, and the
mapping from the table-of-contents entries to the matching Markdown files.  The
`@:packageIndex` tag is often used to add library API docs to the relevant
portion of this table of contents.

For example, when processing the following file `joe doc` will insert the
API documentation links for `my.first.package` and `my.second.package`,
indented under the `Library API` heading.

```markdown
 - [Overview](overview.md)
 - [Library API](library.md)
 @:indent 2
 @:packageIndex my.first.package
 @:packageIndex my.second.package
 - [Tools](tools.md)
```


