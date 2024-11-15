# BUILD.md

## Building for Development

- Let IntelliJ IDEA build it, OR
- Use the Gradle wrapper

## Building for Release

Build the Joe User's Guide if necessary

- See `joe/mdbook/README.md`
- The user's guide is usually built as it changes, so that it will appear
  in GitHub Pages.
- The version number appears in `joe/mdbook/book.toml`.

Use `ant` to build the distribution.

- Update the `version` property in `joe/build.xml` if necessary.
- Execute `ant`

```shell
$ cd joe
$ ant
```

- Distribute `joe/release/joe-<version>/joe-<version>.tar`.



