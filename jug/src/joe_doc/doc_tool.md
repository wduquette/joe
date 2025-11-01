# Using the 'joe doc' Tool

`joe doc` generates Markdown input to be fed to a static site generator
like [mdBook](https://rust-lang.github.io/mdBook/).  This section explains
how to structure the documentation on the disk, how to configure `joe doc`,
and how to run it. 

- [Running `joe doc`](#running-joe-doc)
- [Generated Library API Files](#generated-library-api-files)
- [Configuring Javadoc Roots](#configuring-javadoc-roots)
- [Example: The Joe User's Guide](#example-the-joe-users-guide)
 
## Running `joe doc`

At startup `joe doc` reads and executes the `doc_config.joe` file, which
it expects to find in the current working directory.  This is a Joe script,
using the commands defined by the [[joe.doc]] package.  It configures the
following settings; all paths are relative to the location of the 
`doc_config.joe` file.

| 'joe doc' Configuration | Disk Folder                                       |
|-------------------------|---------------------------------------------------|
| `siteFolder`            | Destination of the final HTML site.               |
| `codeFolders`           | Java/Joe source trees to scan for JoeDoc comments |
| `codeFiles`             | Java/Joe files to scan for JoeDoc comments        |
| `docInputFolder`        | Source tree of Markdown files for preprocessing.  |
| `docOutputFolder`       | Destination tree for processed Markdown files.    |
| `libOutputFolder`       | Destination folder for generated API docs.        |

Given this, `joe doc`:

- Scans the `codeFolders` trees and the `codeFiles` for Java and Joe source 
  files containing JoeDoc comments.
- Copies all files from the `docInputFolder` tree to the `docOutputFolder` tree,
  retaining the tree structure and file types, and processing `*.md` files
  as described in [Processing Markdown Files](markdown_files.md)
- Generates library API Markdown files in the `libOutputFolder`.
 
## Generated Library API Files

All generated API documentation files are written to the configured
`libOutputFolder`, which will then contain:

- `index.md`: A detailed index of the entire documentation set, e.g.,
  the Joe [Library API Index](../library/index.md).
- `pkg.<pkg>.md`: The package file for the given package., e.g.,
  [[joe|`pkg.joe.md`]].
- `type.<pkg>.<type>.md`: The type file for the given type, e.g.,
  [[joe.String|`type.joe.String.md`]].

## Configuring Javadoc Roots

As described under [JoeDoc Links](doc_comments.md#joedoc-links), `joe doc` can
insert links to Javadoc pages given JoeDoc links of the form

`\[[java:<className>]]`

E.g.,

`\[[java:java.lang.String]]`

In order to do this, `joe doc` needs to the know the location of the Javadoc
on the web or on local disk.  This is configured using the 
[[function:joe.doc.javadocRoot]] function, which takes two arguments: a 
Java package name and Javadoc root URL:

```joe
// Javadoc roots
javadocRoot("com.wjduquette.joe",            "javadoc/");
javadocRoot("com.wjduquette.joe.console",    "javadoc/");
javadocRoot("com.wjduquette.joe.tools.test", "javadoc/");
javadocRoot("com.wjduquette.joe.types",      "javadoc/");
javadocRoot("com.wjduquette.joe.win",        "javadoc/");

var jdk = "https://docs.oracle.com/en/java/javase/21/docs/api/";
javadocRoot("java.lang", jdk + "java.base/");
javadocRoot("java.util", jdk + "java.base/");
```

The root URL can be an `http(s)` URL or a local disk path relative to the
configured `siteFolder`, as shown.   


## Example: The Joe User's Guide

The Joe User's Guide is converted from Markdown to HTML using the 
[mdBook](https://rust-lang.github.io/mdBook/) static site generator; 
consequently, its `docOutputFolder` and `libOutputFolder` are configured
to feed into mdBook.  It's all laid out on disk as follows:

- `joe/`
  - `lib/`: Library source tree 
  - `jug/`: The JUG source folder
    - `doc_config.joe`: The `joe doc` configuration file
    - `src/`: The `docInputFolder`
    - `mdbook/`: The mdBook tree
      - `book.toml`: The mdBook configuration file 
      - `src/`: The `docOutputFolder`
        - `library/`: The `libOutputFolder`  
  - `docs/`: The `siteFolder`
      - `javadoc/`: The Joe Javadoc folder

Thus, the process works like this:

- `joe doc`:
  - Reads `doc_config.joe`
  - Reads all JoeDoc comments from the `joe/lib/` tree.
  - Processes all files from `jug/src/`, placing the processed files in
    `jug/mdbook/src/`
    - This includes `jug/mdbook/src/SUMMARY.md`, which defines the structure 
      of the final document.
  - Generates the API docs in `jug/mdbook/src/library`.
- `mdbook`:
  - Generates the static site in `joe/docs/` based on `book.toml` and the
    files in `joe/jug/mdbook/src/...`
- Finally, the Joe javadoc is generated as a separate step (not described here)
  and placed in `joe/docs/javadoc/`.
