# Joe User's Guide: mdBook files

This folder, `joe/jug/mdbook`, contains the 
[mdBook](https://rust-lang.github.io/mdBook/)
configuration and inputs for the Joe User's Guide.

- `joe/jug/mdbook/book.toml` is the mdBook configuration
  - It defines the JUG title, including the Joe version number, and so most
    be updated accordingly.
- `joe/jug/mdbook/theme/` contains a few changes to the book's CSS.
- `joe/jug/mdbook/src/` contains files generated from the JoeDoc inputs, and is not committed
  to the repository.
- `joe/docs` receives the generated JUG document.

## Building the mdBook

To build the JUG once `joe doc` has done its work:

- Revise the Joe version number in `book.toml`, if needed
- Execute `mdbook` in `joe/jug/mdbook`:

```shell
$ cd joe/jug/mdbook
$ mdbook build
...
$
```

This is usually performed via `joe/jug/build.sh`.

