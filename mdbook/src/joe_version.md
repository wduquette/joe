# 'joe version'

The `joe version` tool outputs the current version number and the date
and time at which it was built:

```shell
$ joe version
Joe 0.8.0 2025-08-31T15:46:03+0000
$
```

When run in development, the release `MANIFEST.MF` is not available.  In
this case, the output is as follows:

```shell
$ joe version
Joe (dev build)
$
```
