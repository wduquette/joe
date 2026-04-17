# Built-in Predicates

A *built-in* predicate is a pseudo-relation that can be used something like a 
library function within a rule's body.  More particularly, it is a 
pseudo-relation that instead of matching known facts, computes a set of 
facts from its terms and matches against those.  A variety of interesting
capabilities can be provided in this manner.

Terms in built-in predicates may be either 
[`INOUT` or `IN`](datalog_basics.md#term-modes-inout-in-and-def), 
depending on the built-in predicate.

Normal relation names conventionally start with an uppercase letter; the names
of Nero's built-in predicates start with a lowercase letter.

Nero provides the following built-in predicates.

- [`has/collection, item`](#hascollection-item) (`IN`, `INOUT`)
- [`at/collection, key, item`](#atcollection-key-item) (`IN`, `INOUT`, `INOUT`)
- [`mapsTo/f, a, b`](#mapstof-a-b) (`IN`, `IN`, `INOUT`)
- [`size/collection, number`](#sizecollection-number) (`IN`, `INOUT`)

## `has/collection, item`

The `has/collection, item` predicate matches an *item* (`INOUT`) 
in a *collection* (`IN`), where the *collection* is presumably
a list or a set. If the *collection* is not a list or set then
the predicate will not match.

The predicate has two uses:

- Checking for membership of an *item* in the *collection*
- Disaggregating the *collection* into individual items.

Suppose we have this known fact:

```nero
define Owner/id, belongings;
Owner(#joe, {#hat, #boots, #truck});
```

That is, Joe is the owner of a set of belongings that includes his hat, his
boots, and his truck.  

We want to write a rule that flags whether a person has a hat or not:

```nero
define WearsHat/owner;
WearsHat(id) :- Owner(id, stuff), has(stuff, #hat);
```

When `id` is `#joe`, the `has` predicate takes the collection, `stuff`, and 
breaks it into the following set of temporary facts:

- `has(stuff, #hat)`
- `has(stuff, #boots)`
- `has(stuff, #truck)`

The predicate is then matched against these facts in the usual way, and 
in particular matches the fact `has(stuff, #hat)`.  The rule matches,
and so we infer `WearsHat(#joe)`.

Alternatively, we might want to disaggregate a person's belongings into
a new relation, `Owns/id,item`.  We can do that in this way:

```nero
define Owns/owner,item;
Owns(id, item) :- Owner(id, stuff), has(stuff, item);
```

Because `item` isn't bound to any previous value, the predicate will match
each of the temporary facts it generates, the rule will trigger for each,
and we will end up with these new facts:

- `Owns(#joe, #hat)`
- `Owns(#joe, #boots)`
- `Owns(#joe, #truck)`

In database terms, we have just put Joe's belongings into *normal form*.

## `at/collection, key, item`

The `at/collection, key, item` predicate matches an *item* (`INOUT`)
at its *key* (`INOUT`) within a *collection* (`IN`).  If the *collection*
is a list, the *key* will be the index of the *item* within the list; if
the *collection* is a map, the *key* will be the map key.  If the *collection*
is anything else, the predicate will not match.

Like `has/collection, item`, the predicate can test for membership
or disaggregate the collection into individual facts.  

When used to disaggregate a list, the *key* is the index of the item in
the list, which allows the individual facts to preserve the ordering of the 
items in the original list.

For example, this program

```nero
define Owner/id, belongings;
Owner(#joe, [#hat, #boots, #truck]);

define Owns/owner,index,item;
Owns(id, index, item) :- Owner(id, stuff), at(stuff, index, item);
```

yields these facts:

- `Owns(#joe, 0, #hat)`
- `Owns(#joe, 1, #boots)`
- `Owns(#joe, 2, #truck)`

**Note:** the `indexedList(index, item)` 
[aggregation function](aggregation_functions.md) can than reaggregate the items
back into the original list.

Similar, this program disaggregates a map:

```nero
define Owner/id, belongings;
Owner(#joe, {#head: #hat, #feet: #boots, #body: #duster});

define Owns/owner,place,item;
Owns(id, place, item) :- Owner(id, stuff), at(stuff, place, item);
```

yields these facts:

- `Owns(#joe, #head, #hat)`
- `Owns(#joe, #feet, #boots)`
- `Owns(#joe, #body, #duster)`

**Note:** the `map(k, v)`
[aggregation function](aggregation_functions.md) can reaggregate the items
back into the original map.

## `mapsTo/f, a, b`

Logically, the `mapsTo/f, a, b` predicate verifies that the 
function *f* (`IN`) maps *a* (`IN`), a value of some type A, 
to *b* (`INOUT`), a value of some type *b.
Practically, the predicate can be used to either verify that a given 
*a* maps to a given *b*, or to compute *b* given *a*.

Nero defines the mapping function `#str2num`, and clients can 
define additional mapping functions using Nero's Joe and Java 
API.  More standard mapping functions are likely to be added over time.

This program verifies that the two values are equivalent:

```nero
define Values/s,n;
Values("123", 123);
Values("123", 456);         // Not equivalent
Values("XYZ", 789);         // "XYZ" is not a numeric string
Values("123", #nonNumber);  // #nonNumber is not a number

define Good/s,n;
Good(s, n) :- Values(s, n), mapsTo(#str2num, s, n);
```

Of the values listed, only `"123"` and `123` are equivalent under `#str2num`,
so the program yields the single fact `Good("123", 123)`.

This program converts a string into a number, yielding `Number(123)`:

```nero
define String/s;
String("123");

define Number/n;
Number(n) :- String(s), mapsTo(#str2num, s, n);
```

### Built-in Mapping Functions

Nero defines the following standard mapping functions for use with
`mapsTo/f,a,b`.  Clients can define additional mapping
functions using Nero's Joe and Java APIs.

- `#str2num`
  - Maps numeric string *a* to number *b*.

## `size/collection, number`

The `size/collection, number` predicate generates or matches the size (`INOUT`)
of a *collection* (`IN`), where the *collection* is a list, set, or map. 
If the *collection* is none of the above, the predicate will not match.

For example, the following rule gets the number of items owned by each person.

```nero
define transient Owner/id,list;
Owner(#joe, [#hat, #boots, #truck]);
Owner(#bob, {#desk: #pc, #garage: #corvette});

define Count/id,number;
Count(id, number) :- Owner(id, list), size(list, number);
```

This yields

```
define Count/id,number;
Count(#bob, 2);
Count(#joe, 3);
```
