# Joe Package Repositories

The user can make Joe [packages](extending/packages.md) available for use
by general Joe scripts by placing them in a *package repository* on the
local file system.

- [Defining a Repository](#defining-a-repository)
- [Finding Repositories on the Disk](#finding-repositories-on-the-disk)

## Defining a Repository

A package repository is a just a folder on the disk that contains a
`repository.nero` file having this schema:

```nero
define ScriptedPackage/name, scriptFiles;
define JarPackage/name, jarFile, className;
```

For example,

```nero
ScriptedPackage("my.scripts", ["script1.joe", "script2.joe"]);
JarPackage("my.native", "myNative.jar", "my.native.MyNativePackage");
```

A `ScriptedPackage` fact declares that the named package, e.g.,
`my.scripts` is defined by the list script files, which will be found
relative to the repository folder.  In this example, they would be
in the same folder as the `repository.nero` file.

The scripts in a scripted package must `export` the types and functions
they wish to make available for 
[`import`](statements.md#import-declarations).

A `JarPackage` fact declares that the named package, e.g., `my.native`,
can be found as a `NativePackage` in the given `.jar` file with the
given class name.  The jar file will be found relative to the 
repository folder.

A package repository can contain any number of packages, possibly organized
in subfolders; it is entirely up to the author of the repository.  It is common
for a repository folder to contain the Joe packages defined for external
use by a single project.

## Finding Repositories on the Disk

`joe run`, `joe repl`, `joe win`, and `joe test` find packages on the 
local file system by searching a library path.  `joe run`, `joe repl`
and `joe win` will search the path given by the `JOE_LIB_PATH` 
environment variable by default; all four provide a `--libpath` option
to specify the library path explicitly.

A library path is a colon-delimited list of folders that contain package 
repositories. Joe will search the folder tree rooted at each folder for
`repository.nero` files, and register the packages named within.

Suppose two projects define external packages, with this set of files:

- `~/project1/libs/foo/repository.nero`
- `~/project1/libs/bar/repository.nero`
- `~/project2/stuff/repository.nero`

If `joe run` is given the library path `~/project1/libs:~/project2/stuff` it
will find all three `repository.nero` files, and register the packages
they define.

Joe client libraries and applications can also search for package repositories
using the `PackageFinder` class, and then install or register those packages
with a `Joe` interpreter.


