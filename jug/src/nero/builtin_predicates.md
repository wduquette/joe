# Built-in Predicates

A *built-in* predicate is a pseudo-relation that can be used something like a 
library function within a rule's body.  More particularly, it is a 
pseudo-relation that instead of matching known facts, computes a set of 
facts from its terms and matches against those.  A variety of interesting
capabilities can be provided in this manner.

Normal relation names conventionally start with an uppercase letter; the names
of Nero's built-in predicates start with a lowercase letter.

Nero provides the following built-in predicates.

- [`member/item, collection`](#memberitem-collection)
- [`indexedMember/index, item, list`](#indexedmemberindex-item-list)
- [`keyedMember/key, value, map`](#keyedmemberkey-value-map)
- [`mapsTo/f, a, b`](#mapstof-a-b)

## `member/item, collection`

The `member/item, collection` predicate matches an *item* in a *collection*, 
where the *collection* is a variable bound in an earlier body atom, and 
presumably to a value of a list or a set.  It is a compilation error if 
the *collection* variable is not already bound to a value.  If it is
bound to something other than a collection, the rule will not trigger.

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
WearsHat(id) :- Owner(id, stuff), member(#hat, stuff);
```

When `id` is `#joe`, the `member` predicate takes the collection, `stuff`, and 
breaks it into the following set of temporary facts:

- `member(#hat, stuff)`
- `member(#boots, stuff)`
- `member(#truck, stuff)`

The predicate is then matched against these facts in the usual way, and 
in particular matches the fact `member(#hat, stuff)`.  The rule matches,
and so we infer `WearsHat(#joe)`.

Alternatively, we might want to disaggregate a person's belongings into
a new relation, `Owns/id,item`.  We can do that in this way:

```nero
define Owns/owner,item;
Owns(id, item) :- Owner(id, stuff), member(item, stuff);
```

Because `item` isn't bound to any previous value, the predicate will match
each of the temporary facts it generates, the rule will trigger for each,
and we will end up with these new facts:

- `Owns(#joe, #hat)`
- `Owns(#joe, #boots)`
- `Owns(#joe, #truck)`

In database terms, we have just put Joe's belongings into *normal form*.

## `indexedMember/index, item, list`

The `indexedMember/index, item, list` predicate matches an *item* and its 
*index* within a *list*, where the *list* is a Nero variable bound to
a `List` value in an earlier body atom. It is a compilation error if
the *list* variable is not already bound to a value.  If it is
bound to something other than a `List` then the rule will not trigger.

Like `member/item, collection`, the predicate can test for membership
or disaggregate the list into individual facts.  When used for the
latter purpose, the *index* allows the new facts to preserve the 
ordering of the items in the original list.

For example, this program

```nero
define Owner/id, belongings;
Owner(#joe, [#hat, #boots, #truck]);

define Owns/owner,index,item;
Owns(id, index, item) :- Owner(id, stuff), indexedMember(index, item, stuff);
```

yields these facts:

- `Owns(#joe, 0, #hat)`
- `Owns(#joe, 1, #boots)`
- `Owns(#joe, 2, #truck)`

**Note:** the `indexedList(index, item)` 
[aggregation function](aggregation_functions.md) can reaggregate the times
back into the original list.

## `keyedMember/key, value, map`

The `keyedMember/key, value, map` predicate matches a *key*, *value* 
pair within a *map*, where the *map* is a Nero variable bound to
a `Map` value in an earlier body atom. It is a compilation error if
the *map* variable is not already bound to a value.  If it is
bound to something other than a `Map` then the rule will not trigger.

Like `member/item, collection`, the predicate can test for membership
or disaggregate the map into individual facts.  

For example, this program

```nero
define Owner/id, belongings;
Owner(#joe, {#head: #hat, #feet: #boots, #body: #duster});

define Owns/owner,place,item;
Owns(id, place, item) :- Owner(id, stuff), keyedMember(place, item, stuff);
```

yields these facts:

- `Owns(#joe, #head, #hat)`
- `Owns(#joe, #feet, #boots)`
- `Owns(#joe, #body, #duster)`

**Note:** the `map(k, v)`
[aggregation function](aggregation_functions.md) can reaggregate the items
back into the original map.

## `mapsTo/f, a, b`

Logically, the `mapsTo/f, a, b` predicate verifies that the function *f*
maps *a*, a value of some type A, to *b*, a value of some type *b.
Practically, the predicate can be used to either verify that a given 
*a* maps to a given *b*, or to compute *b* given *a*.

For example, Nero provides the `#str2num` mapping function, which maps numeric 
strings to their equivalent numbers.  Clients can define additional mapping
functions using the [[joe.Nero]] and [[joe.Database]] APIs, or the equivalent
Java APIs.

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
functions using the [[joe.Nero]] and [[joe.Database]] APIs, or the 
equivalent Java APIs.

- `#str2num`
  - Maps numeric string *a* to number *b*.
