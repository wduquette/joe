# Aggregation Functions

An *aggregation function* is a special term used in a rule head to aggregate
over a set of rule matches.  

For example, the `sum(x)` function will 
aggregate over a set of `x` values, inferring a single fact with the sum
of the values.  

```nero
Item(#a, 1);
Item(#a, 2);
Item(#a, 3);
Item(#b, 4);
Item(#b, 5);
Item(#b, 6);

Total(id, sum(x)) :- Item(id, x);
```

This program produces two new facts:

- `Total(#a, 6)`
- `Total(#b, 15)`

There are two constraints on using aggregation functions:

- A rule head can contain at most one aggregation function
- Variables appearing as arguments to the aggregation function cannot appear
  elsewhere in the rule head.

Nero provides the following aggregation functions:

- [`indexedList(index, item)`](#indexedlistindex-item)
- [`list(item)`](#listitem)
- [`map(k, v)`](#mapk-v)
- [`max(x)`](#maxx)
- [`min(x)`](#minx)
- [`set(item)`](#setitem)
- [`sum(x)`](#sumx)

## `indexedList(index, item)`

This function aggregates the *item* values into a `List` value, sorting the
items by the *index* value.  This will obviously work best if the all of the
*index* values are either numbers or strings, but the ordering will be
consistent from run to run regardless of the types of the index values.

For example,

```nero
Item(#a, 1, #hat);
Item(#a, 2, #duster);
Item(#b, 10, #boots);
Item(#b, 11, #truck);

Collect(id, indexedList(i, item)) :- Item(id, i, item);
```

will produce these facts:

- `Collect(#a, [#hat, #duster])`
- `Collect(#b, [#boots, #truck])`

## `list(item)`

This function aggregates the *item* values into a `List` value in no
particular order.

For example,

```nero
Item(#a, 1, #hat);
Item(#a, 2, #duster);
Item(#a, 3, #boots);
Item(#a, 4, #boots);

Collect(id, list(item)) :- Item(id, item);
```

will produce a fact like this, but the ordering of the items might be
different.

- `Collect(#a, [#hat, #duster, #boots, #boots])`

## `map(k, v)`

This function aggregates the *k* and *v* pairs into a `Map` value.
If the input has multiple distinct values for a single key, that
key's value will be set to the sentinel value `#duplicateKey`.

For example,

```nero
Item(#joe, #head, #hat);
Item(#joe, #body, #duster);
Item(#joe, #feet, #boots);
Item(#joe, #feet, #shoes);

Equip(id, map(place, item)) :- Item(id, place, item);
```

will produce this fact:

- `Equip(#joe, {#head: #hat, #body: #duster, #feet: #duplicateKey})`

## `max(x)`

This function computes the maximum numeric value among the aggregated 
`x` values.  If there are no numeric values among the aggregated values,
*the rule will not trigger*.

For example,

```nero
Item(#a, 1);
Item(#a, 2);
Item(#a, 3);
Item(#b, #NaN);

MaxValue(id, max(x)) :- Item(id, x);
```

yields this fact, but no fact for `id == #b`.

- `MaxValue(#a, 3)`

## `min(x)`

This function computes the minimum numeric value among the aggregated
`x` values.  If there are no numeric values among the aggregated values,
*the rule will not trigger*.

For example,

```nero
Item(#a, 1);
Item(#a, 2);
Item(#a, 3);
Item(#b, #NaN);

MaxValue(id, max(x)) :- Item(id, x);
```

yields this fact, but no fact for `id == #b`.

- `MaxValue(#a, 1)`

## `set(item)`

This function aggregates the *item* values into a `Set` value in no
particular order.

For example,

```nero
Item(#a, 1, #hat);
Item(#a, 2, #duster);
Item(#a, 3, #boots);
Item(#a, 4, #boots);

Collect(id, list(item)) :- Item(id, item);
```

will produce a fact like this:

- `Collect(#a, {#hat, #duster, #boots})`

## `sum(x)`

This function aggregates some number of numeric values into their sum,
ignoring any non-numeric values.  If the rule triggers but there are no
numeric values for `x`, the function yields `0`.

For example,

```nero
Item(#a, 1);
Item(#a, 2);
Item(#a, 3);
Item(#a, #whoops);

Total(id, sum(x)) :- Item(id, x);
```

yields this fact:

- `Total(#a, 6)`

