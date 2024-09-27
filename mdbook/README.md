# Joe User's Guide sources

## Building Docs for Release

The Joe version number is included in `book.toml`, in the JUG's title.
Before releasing, be sure that the version number is correct; and
after releasing, be sure to bump the version number.

## Building the Docs

The Joe User's Guide is produced using 
[mdBook](https://github.com/rust-lang/mdBook); see the `mdBook` documentation
for descriptions of `book.toml` and the structure of the files in the
`src/` folder.

A portion of the User's Guide is produced by the `joe doc` tool,
To build the complete User's Guide 
is therefore a multistep process

```shell
$ cd joe/mdbook
$ joe doc
...
$ mdbook build
...
$
```

The `build.sh` script combines the three steps.

```shell
$ ./build.sh
...
```

The `mdBook` documentation is built in the `joe/docs` folder/
the main entry point is `joe/docs/index.html`.  Note: GitHub Pages is enabled 
for this repo; the docs as committed are visible 
at https://wduquette.github.io/joe/.

