# joe nero

The `joe nero` tool executes standalone Nero programs from the command
line.

Given `simple.nero`:

```nero
Parent(#anne, #bert);
Parent(#bert, #clark);
Ancestor(x, y) :- Parent(x, y);
Ancestor(x, y) :- Parent(x, z), Ancestor(z, y);
```

```
$ joe nero simple.nero
New Facts:
Ancestor(#anne, #bert)
Ancestor(#anne, #clark)
Ancestor(#bert, #clark)
```

To see the axioms as well as the facts inferred by the rules, pass
`--all`:


```
$ joe nero --all simple.nero
All Facts:
Ancestor(#anne, #bert)
Ancestor(#anne, #clark)
Ancestor(#bert, #clark)
Parent(#anne, #bert)
Parent(#bert, #clark)
```

The `--debug` flag outputs a Nero execution trace (the precise output is
subject to change without notice):

```
$ joe nero --debug simple.nero 
Rule Strata: [[Ancestor]]
Iteration 0.1:
  Rule: Ancestor(x, y) :- Parent(x, z), Ancestor(z, y);
  Rule: Ancestor(x, y) :- Parent(x, y);
    Fact: Ancestor(#bert, #clark)
    Fact: Ancestor(#anne, #bert)
Iteration 0.2:
  Rule: Ancestor(x, y) :- Parent(x, z), Ancestor(z, y);
    Fact: Ancestor(#anne, #clark)
  Rule: Ancestor(x, y) :- Parent(x, y);
Iteration 0.3:
  Rule: Ancestor(x, y) :- Parent(x, z), Ancestor(z, y);
  Rule: Ancestor(x, y) :- Parent(x, y);
New Facts:
Ancestor(#anne, #bert)
Ancestor(#anne, #clark)
Ancestor(#bert, #clark)
```

See `joe help nero` for the complete command line syntax.

