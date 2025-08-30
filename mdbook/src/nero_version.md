# 'nero version'


The `nero version` tool outputs the current version number and the date
and time at which it was built:

```shell
$ nero version
Joe 0.8.0 2025-08-31T15:46:03+0000
$
```

The output reflects the `Joe` build because Nero is built as part of Joe.

When run in development, the release `MANIFEST.MF` is not available.  In
this case, the output is as follows:

```shell
$ nero version
Joe (dev build)
$
```
