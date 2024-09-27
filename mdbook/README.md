# Joe User's Guide sources

The Joe User's Guide is produced using 
[mdBook](https://github.com/rust-lang/mdBook); see the `mdBook` documentation
for descriptions of `book.toml` and the structure of the files in the
`src/` folder.

A portion of the User's Guide is produced by the `joe doc` tool, and
another portion is built using `javadoc`.  To build the complete User's Guide 
is therefore a multistep process

```shell
$ cd joe/mdbook
$ joe doc
...
$ mdbook build
...
$ ./javadoc.sh
...
$
```

The `build.sh` script combines the three steps.  See below for notes on 
building the Javadoc.

```shell
$ ./build.sh
...
```

The `mdBook` documentation is built in the `joe/docs` folder, with the
javadoc in `joe/docs/javadoc`; the main entry point is 
`joe/docs/index.html`.  Note: GitHub Pages is enabled for this repo; the docs 
as committed are visible at https://wduquette.github.io/joe/.

## Building the Javadoc

The javadoc is built using the script `./javadoc.sh`, which executes the
`javadoc` tool directly.  In theory Gradle will build the `javadoc` for me,
but I've been unable to figure out how to set the Javadoc `-doctitle` via
`build.gradle`.  It comes out as `lib-<version>`.  (If someone knows how to
fix that, I'd be grateful.)

**Consequently**, the `./javadoc.sh` file includes the development version
number, which is not ideal.  Don't forget to update it after each release!
