# joe doc

The `joe doc` processor is used to extract JoeDoc documentation comments
from `.java` and `.joe` source files, and from them generate Markdown 
API documentation.[^markdown] 

The [API sections](library.md) of this user's guide were generated by
`joe doc`.

- [Documentation Set](#joedoc-structure)
  - [Documentation Comments](#documentation-comments)
  - [Entity Tags](#entity-tags)
  - [Example](#example)
- [Entity Types](#entity-types)
  - [The `@package` Entity](#the-package-entity)
  - [The `@packageTopic` Entity](#the-packagetopic-entity)
  - [The `@function` Entity](#the-function-entity)
  - [The `@type` Entity](#the-type-entity)
  - [The `@enum` Entity](#the-enum-entity)
  - [The `@typeTopic` Entity](#the-typetopic-entity)
  - [The `@constant` Entity](#the-constant-entity)
  - [The `@static` Entity](#the-static-entity)
  - [The `@init` Entity](#the-init-entity)
  - [The `@field` Entity](#the-field-entity)
  - [The `@method` Entity](#the-method-entity)
  - [The `@mixin` Entity](#the-mixin-entity)
- [JoeDoc Links](#joedoc-links)
  - [Qualified Type Names](#qualified-type-names)
  - [Entity Mnemonics](#entity-mnemonics)
- [Running `joe doc`](#running-joe-doc)
  - [Configuring `joe doc`](#configuring-joe-doc)
  - [Generated Files](#generated-files)

## Documentation Set

A *documentation set* is the generated JoeDoc documentation for the
packages in the current project.  It will include documentation for the
following types of entity:

- Packages
  - Functions
  - Types (including Enums)
    - Constants
    - Static Methods
    - Initializers
    - Fields
    - Methods
  - Additional topics concerning a specific package or type
- Mixins
  - A pseudo-type used for documentation content to be included in
    multiple types.

Unsurprisingly, this is the same structure you'll find in the 
[Extending Joe](extending/extending.md) section.  And unsurprisingly,
the generated documentation has this same structure.

### Documentation Comments

A documentation comment is a block of comments beginning with the following
marker comment: `//**` and continuing through contiguous comment lines until
the next non-comment line:

```java
// Not in a doc comment

//**
// @package my.package
//
// Information about `my.package` in Markdown format.
// ...

// Not in a doc comment
```

The comment prefix `//` must be the first non-whitespace text on each line.

`joe doc` extracts all doc comment lines from a source file, removes the 
comment prefixes, and then parses them to extract the documentation 
according to its structure.

### Entity Tags

In the example just shown, the tag `@package` indicates that the following 
comment text concerns the named package.  `joe doc` understands the following
entity tags.

- [`@package <name>`](#the-package-entity)
  - [`@function <name>`](#the-function-entity)
  - [`@type <name>`](#the-type-entity)
    - [`@constant <name>`](#the-constant-entity)
    - [`@static <name>`](#the-static-entity)
    - [`@init`](#the-init-entity)
    - [`@field <name>`](#the-field-entity)
    - [`@method <name>`](#the-method-entity)
    - [`@typeTopic <name>`](#the-typetopic-entity)
  - [`@packageTopic <name>`](#the-packagetopic-entity)
- [`@mixin <name>`](#the-mixin-entity)
  - [`@constant <name>`](#the-constant-entity)
  - [`@static <name>`](#the-static-entity)
  - [`@field <name>`](#the-field-entity)
  - [`@method <name>`](#the-method-entity)
  - [`@typeTopic <name>`](#the-typetopic-entity)

This is the same outline [shown above](#joedoc-structure), and purposely so.  Each entity
has a parent, and the parentage is indicated by the outline.

- `@package` and `@mixin` entities belong to the documentation set.
- Every `@function` and `@type` entity belongs to the `@package` that
  most nearly precedes it in the same file.
- Every `@constant`, `@static`, `@init`, `@field` and `@method` entity belongs to
  the `@type` or `@mixin` that most nearly precedes it in the same file. 
- It's an error if the expected parent entity is missing.
- `joe doc` checks carefully for duplicate names.
- A `@type` can have at most a single `@init` (initializer) entity; and it 
  has no name because it always has the same name as its type.

Packages are special; a single Joe package's code is often spread across 
a number of files. Thus, the same `@package` name can appear in multiple
files, and the related entities will all be ascribed to the same package.

A single source file may (in principle) contain documentation for multiple
mixins, packages and types.  If `joe doc` sees a second `@package`, 
it immediately switches to that `@package`, and so for the others.

### Example

Here is the beginning of the documentation in Joe's `StandardLibrary.java`
file:

```java
//**
// @package joe
// @title Joe Standard Library
// The `joe` package contains Joe's standard library.

... java code ...

//**
// @function catch
// @args callable
// @result CatchResult
// Executes the callable, which must not require any arguments.
// Returns a [[CatchResult]] indicating success or failure and providing
// the returned result or the error message respectively.
        
... java code ...
```

This file defines the package itself, and the functions in the package.
Consequently, it begins with a comment describing the `@package`, and
the goes on to document each `@function` in turn.  Some things to note:

- Each entity's documentation begins with its `@entity` tag.

- This is followed by metadata tags: `@title` for the `@package` and
  `@args` and `@result` for the `@function`.
 
- And these are followed by any number of lines of Markdown text.  All such
  lines will be accumulated and ascribed to this entity until the next entity 
  tag is seen.

## Entity Types

This section describes each entity type and its metadata.

### The `@package` Entity

The `@package` entity begins with `@package <name>`, and has the
following optional metadata:

| Metadata Tag    | Meaning                    |
|-----------------|----------------------------|
| `@title string` | Gives the package a title. |

The title string is used in the generated documentation.

The `@package` entity may have 
[`@function`](#the-function-entity), 
[`@type`](#the-type-entity), and 
[`@packageTopic`](#the-packagetopic-entity) children.

For example,

```java
//**
// @package joe
// @title Joe Standard Library
// The `joe` package contains Joe's standard library.
```

### The `@packageTopic` Entity

A package's normal Markdown content goes near the top of the generated
package page, below the index but above the function entries.  The
`@packageTopic` entity is way to add titled Markdown content at the
*bottom* of the package file, where it is out of the way.

The `@packageTopic` entity begins with `@packageTopic <name>`, and has
the following metadata:

| Metadata Tag    | Meaning                  |
|-----------------|--------------------------|
| `@title string` | Gives the topic a title. |

The title string is used as a header in the generated content, and in
links to that content.  In theory the `@title` tag is optional, but 
in practice one always wants to use it.


### The `@function` Entity

The `@function` entity documents a global function, and 
begins with `@function <name>`.  The *name* should be the name of 
a function defined in the current package.

The entity has the following optional metadata:

| Metadata Tag       | Meaning                         |
|--------------------|---------------------------------|
| `@args <spec>`     | Names the function's arguments. |
| `@result <result>` | What the function returns.      |

The `@args` *spec* is typically a comma-delimited list of argument
names, with `...` used to indicate a variable length argument list
and square brackets used to indicate optional arguments.  Here are some
examples:

- `x`: The single argument `x`.
- `x, y`: The two arguments `x` and `y`.
- `start, [end]`: The two arguments `start` and `end`; `end` is optional.
- `x, ...`: The argument `x` plus any number of additional arguments.
- `name, value, [name, value]...`: One or more `name`,`value` pairs.

The `@args` tag can be omitted if the function takes no arguments, and
can be repeated if the function has two or more distinct signatures.[^sigs]

The `@result`'s *result* is a single token, usually either the name of 
a Joe type or a variable name like `value` or `this`.  It can be
omitted if the function returns nothing (i.e., always returns 
`null`). If the *result* names a type, it will appear as a link to that type 
in the generated documentation.

### The `@type` Entity

The `@type` entity documents a type, and so begins with `@type <name>`. The 
*name* should be the name of a type defined in the current package.

The entity has the following optional metadata:

| Metadata Tag            | Meaning                                             |
|-------------------------|-----------------------------------------------------|
| `@extends <type>`       | This type extends the named type.                   |
| `@includeMixin <mixin>` | This type's documentation includes the named mixin. |

- `<mixin>` should be the name of a 

The `@extends <type>` tag is used when this type extends (i.e., subclasses) 
another script-visible type.  The `<type>` should be the name or qualified name 
of a referenced `@type`.

The `@includesMixin <mixin>` tag indicates that this type's documentation should 
include the documentation from a [`@mixin`](#the-mixin-entity) defined in this 
documentation set. The included documentation will be represented as part of 
the including type.

The `@type` entity may have 
[`@constant`](#the-constant-entity),
[`@static`](#the-static-entity),
[`@init`](#the-init-entity),
[`@field`](#the-field-entity),
[`@method`](#the-method-entity),
and
[`@typeTopic`](#the-method-entity)
children.

For example, in the standard library the [`AssertError`](library/type.joe.AssertError.md) type extends the
[`Error`](library/type.joe.Error.md) type, both of which are visible at the script-level.


```java
//**
// @type AssertError
// @extends Error
// ...
```

### The `@enum` Entity

The `@enum` entity documents a native Java enumerated type whose
binding is created using Joe's `EnumProxy<E>` type proxy class. An 
`@enum` entity is just like a [`@type` entity](#the-type-entity),
except that it automatically includes documentation for the enum's static 
methods, `values()` and `valueOf`, and for the enum's instance methods,
`name()`, `ordinal()`, and `toString()`.

A simple `@enum` doc comment will usually need to include only a description
of the enum and the enum's `@constant` entities. If the specific enum
defines other static or instance methods, they can be included as well.

### The `@typeTopic` Entity

A type's normal Markdown content goes near the top of the generated
type page, below the index but above the various constant and method 
entries.  The `@typeTopic` entity is way to add titled Markdown content 
at the *bottom* of the type's file, where it is out of the way.

The `@typeTopic` entity begins with `@typeTopic <name>`, and has
the following metadata:

| Metadata Tag    | Meaning                  |
|-----------------|--------------------------|
| `@title string` | Gives the topic a title. |

The title string is used as a header in the generated content, and in
links to that content.  In theory the `@title` tag is optional, but
in practice one always wants to use it.

As an example, the [`String` type](library/type.joe.String.md) uses a
`@typeTopic` to document its 
[format string syntax](library/type.joe.String.md#topic.formatting).

### The `@constant` Entity

The `@constant` entity documents a constant defined by a type, and so 
begins with `@constant <name>`. The *name* should be the name of a 
constant actually defined by the current type.

At present, the `@constant` entity has no metadata tags.

### The `@static` Entity

The `@static` entity documents a static method defined by the
current type, and begins with `@static <name>`.  The *name* should be 
the name of a static method defined by the current type.

The `@static` entity has the same metadata as the `@function` entity.

### The `@init` Entity

The `@init` entity documents the current type's initializer function.
The `@init` tag doesn't include a name, as the initializer always has
the same name as the type.

The `@init` entity accepts the `@args` metadata tag, just as any
function or method does, but not the `@result` tag, as its result is
always an instance of the current type.

### The `@field` Entity

The `@field` entity documents an instance field defined by the current type,
and begins with `@field <name>` where the *name* is the name of the field.
The entity has Markdown content but no metadata tags.

### The `@method` Entity

The `@method` entity documents a method defined by the
current type, and begins with `@method <name>`.  The *name* should be
the name of a method defined by the current type.

The `@method` entity has the same metadata as the `@function` entity.

### The `@mixin` Entity

The `@mixin` entity is like a pseudo-`@type`; it contains type content that
would otherwise be duplicated in multiple `@type` entries.  Mixin entities
belong to the documentation set, not to any specific package, and may be
included by any `@type` entity.

Every mixin begins with `@mixin <name>`; mixin names must be unique within
the documentation set.

Mixins have no metadata tags, and may have Markdown content and
[`@constant`](#the-constant-entity),
[`@static`](#the-static-entity),
[`@field`](#the-field-entity),
[`@method`](#the-method-entity),
and
[`@typeTopic`](#the-method-entity)
children.

When a type wants to include a mixin, it uses the `@includesMixin <name>`
metadata tag.  

- The mixin's Markdown content is inserted into the type's
  Markdown content at the location of the `@includesMixin` tag
- The mixin's children are then added to the type.

A mixin is like a template; the string `<type>` may be included anywhere
in the content of the mixin itself and its children, and will be replaced
on inclusion by the including type's name.

For example, the mixin might define:

```java
//**
// @mixin MyMixin
// 
// The `<type>` type is able to...
//
// @method myMethod
// @result <type>
// This method...
```

Then, another type can include it like this:

```java
//**
// @type MyType
// The `MyType` type represents...
//
// @includeMixin MyMixin
//
// The MyMixin Markdown content is included above, replacing the include tag.
```

## JoeDoc Links

The Markdown content for each entity can of course contain normal
Markdown links.  In addition, though, JoeDoc supports abbreviated 
links to any entity in the documentation set.  For example,
if `[[String]]` is found in Markdown content for the `joe` package,
it will be replaced by a link to the file documenting the `String` type.

### Qualified Type Names

Every `@type` entity is defined within a package, and so has two names:
its bare or *unqualified* name, e.g., `String`, and its qualified name,
`joe.String`.

### Entity Mnemonics

Every entity can be identified in a JoeDoc link by its *mnemonic*, which
may be qualified or unqualified.  

- An entity can be linked to by its unqualified mnemonic from any Markdown
  content *in the same package*.
- An entity can be linked to by its qualified mnemonic from any Markdown
  content in the entire documentation set.

| Entity          | Qualified                      | Unqualified              |
|-----------------|--------------------------------|--------------------------|
| `@package`      | `<pkg>`                        | `<pkg>`                  |
| `@packageTopic` | `<pkg>#topic.<name>`           | `<pkg>#topic.<name>`     |
| `@function`     | `<pkg>#function.<name>`        | `function.<name>`        |
| `@type`         | `<pkg>.<type>`                 | `<type>`                 | 
| `@typeTopic`    | `<pkg>.<type>#topic.<name>`    | `<type>#topic.<name>`    |
| `@constant`     | `<pkg>.<type>#constant.<name>` | `<type>#constant.<name>` |                    
| `@static`       | `<pkg>.<type>#static.<name>`   | `<type>#static.<name>`   |                    
| `@init`         | `<pkg>.<type>#init`            | `<type>#init`            |                    
| `@method`       | `<pkg>.<type>#method.<name>`   | `<type>#method.<name>`   |                    

Thus,

- `[[joe.String#method.length]]` links to the `String` type's `length` method,
  from anywhere in the documentation set.
- `[[String#method.length]]` links to the `String` type's `length` method,
  from anywhere in the `joe` package's own documentation.

## Running `joe doc`

To run `joe doc`, `cd` to the folder containing the `joe doc` configuration
file, `doc_config.joe`, and then run `joe doc`.  In Joe's own case, it
looks like this:

```shell
$ cd joe/mdbook
$ ls doc_config.joe
doc_config.joe
$ joe doc
...
$
```

`joe doc` will report any parsing errors, duplicate entities, or unrecognized
mnemonics in JoeDoc links.

### Configuring `joe doc`

The `joe doc` configuration file, `doc_config.joe`, is itself a Joe script, 
using the API defined in the [`joe.doc`](library/pkg.joe.doc.md) package.

Joe's own `doc_config.file` looks like this (at time of writing):

```joe
DocConfig
    .inputFolder("../lib/src/main/java/com/wjduquette/joe")
    .inputFolder("../lib/src/main/resources/com/wjduquette/joe")
    .outputFolder("src/library");
```

All file paths shown are relative to the location of the `doc_config.joe`
file.

Each `inputFolder()` method names a folder that contains `.java` and/or `.joe` 
files to scan for doc comments.  `joe doc` will scan for such files in the
named folder, recursing down into subfolders.  

There is also an `inputFile()` method that names a specific file to scan.

The `outputFolder()` method names the folder to receive the generated
documentation files.

### Generated Files

When run, `joe doc` produces the following files, all within the configured
output directory.

- `index.md`: A detailed index of the entire documentation set, e.g.,
  the Joe [Library API Index](library/index.md).
- `pkg.<pkg>.md`: The package file for the given package., e.g., 
  [pkg.joe.md](library/pkg.joe.md)
- `type.<pkg>.<type>.md`: The type file for the given type, e.g.,
  [`type.joe.String.md`](library/type.joe.String.md)

The link IDs for the functions in a package file, and for the methods
and other entities in a type file, are exactly the same as in the
[entity mnemonics](#entity-mnemonics).  Thus, to link to the `length` 
method of the `String` type from outside the documentation set, you'd
use the partial URL `<folder>/type.joe.String.md#method.length`.

[^markdown]: This User's Guide is
produced using [mdBook](https://github.com/rust-lang/mdBook), and so
`joe doc` produces output in a form compatible with `mdBook`.  It should
be usable with any other static site generator that works with Markdown
files.

[^sigs]: Joe does not allow for overloaded function and method names, but
a single function or method can simulate overloading through the use of
a variable length argument list.  This technique is common in
[native functions](extending/native_functions.md) and methods.