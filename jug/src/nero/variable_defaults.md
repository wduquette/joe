# Variable Defaults

Variables in body atoms can be given default values, subject to certain
restrictions, using the syntax ***variable** | **value***.

For example, consider the following rule set:

```nero
define Key/k;
define Value/k, v;
define Result/k, v;

Key(#a);
Key(#b);
Value(#a, #xyz);

Result(k, v) :- Key(k), Value(k, v | null);   // 1
```

- Key `#a` has a known `Value`
- Key `#b` does not.
- In rule 1, the `Value` atom will match a known `Value` fact if possible; and
  if not it will default the value of `v` to `null`.

Thus, this rule set produces the following `Result` facts: 

```nero
Result(#a, #xyz);
Result(#b, null);
```

One can produce the same output without using a default value; see rules 2 and
3:

```nero
Result(k, v)    :- Key(k), Value(k, v);      // 2
Result(k, null) :- Key(k), not Value(k, v);  // 3
```

However, default value syntax is much more flexible.

## Restrictions

In a default value term ***variable** | **value***:

- The *variable* has
  [mode `DEF`](datalog_basics.md#term-modes-inout-in-and-def);
  it can appear in no other body atom.
- The *value* has mode `IN`; it must be either a constant or a variable
  bound in an atom to the left.
