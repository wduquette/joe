# INSTALL.md

## Download

- Download `joe-<version>.tar`
- Untar the distribution in a temporary folder.

```shell
$ tar xf joe-0.4.0.tar
$ ls joe-0.4.0
INSTALL.md README.md bin/ docs/ lib/
```

## Documentation

- The Joe User's Guide is at `joe-<version>/docs/index.html`.
- Joe's javadoc is at `joe-<version>/docs/javadoc/index.html`.

## Add to Path

To install `joe` for use and development:

- Copy the resulting `joe-<version>` folder to any desired location.
- Add `joe-<version>/bin/` to your `PATH`.

```shell
$ joe version
Joe 0.4.0 2024-11-15T15:46:03+0000
$
```

## Use in an App

To use `joe` in an application or library:

- Copy `joe-<version>/lib/joe-<version>.jar` to any desired location.
