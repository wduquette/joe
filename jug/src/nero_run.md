# 'nero run'

The `nero run` tool executes standalone Nero programs from the command
line. See `nero help run` for the complete command line syntax.

## Example

Given `simple.nero`:

```nero
Parent(#anne, #bert);
Parent(#bert, #clark);
Ancestor(x, y) :- Parent(x, y);
Ancestor(x, y) :- Parent(x, z), Ancestor(z, y);
```

```
$ nero run simple.nero
define Ancestor/2;
Ancestor(#anne, #bert);
Ancestor(#anne, #clark);
Ancestor(#bert, #clark);

define Parent/2;
Parent(#anne, #bert);
Parent(#bert, #clark);
```

## Updating Nero Data Files

`nero run` can be used to update data stored in `.nero` format given
the data file and one or more Nero programs used to update it:

```shell
$ nero run myData.nero update1.nero update2.nero ... -o myData.nero
...
$
```

## Nero Debugging

The `--debug` flag outputs a Nero execution trace (the precise output is
subject to change without notice):

```
$ nero run --debug simple.nero 
Rule Strata: \[[Ancestor]]
Iteration 0.1:
  Rule: Ancestor(x, y) :- Parent(x, z), Ancestor(z, y);
  Rule: Ancestor(x, y) :- Parent(x, y);
Iteration 0.2:
  Rule: Ancestor(x, y) :- Parent(x, z), Ancestor(z, y);
  Rule: Ancestor(x, y) :- Parent(x, y);
Iteration 0.3:
  Rule: Ancestor(x, y) :- Parent(x, z), Ancestor(z, y);
  Rule: Ancestor(x, y) :- Parent(x, y);
define Ancestor/2;
Ancestor(#anne, #bert);
Ancestor(#anne, #clark);
Ancestor(#bert, #clark);

define Parent/2;
Parent(#anne, #bert);
Parent(#bert, #clark);
```

