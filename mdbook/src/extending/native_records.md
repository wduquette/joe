# Native Records

Joe makes it easy to define bindings for native Java record types, and
also to define bindings that make other Java types *look* like record types.

Consider the following Java record type:

```java
record Thing(String id, Keyword color) {}
```

To create a binding, define `ThingType` as follows and install
`ThingType.TYPE` into an instance of `Joe` in the usual way.  The user
can now create and use instances of `Thing` in their scripts.

```java
public class ThingType extends ProxyType<Thing> {
    public static final ThingType TYPE = new ThingType();

    public ThingType() {
        super("Thing");

        proxies(Thing.class);
        
        initializer(this::_init);

        field("id",    Thing::id);
        field("color", Thing::color);
    }
    
    private Object _init(Joe joe, Args args) {
        args.exactArity(2, "Thing(id, color)");
        var id = joe.toString(args.next());
        var color = joe.toKeyword(args.next());
        return new Thing(id, color);
    }
}
```

The `field()` method declares that the type has a field with the given name,
and specifies a lambda to retrieve it given an instance of the native type.
As shown here the lambda is just a simple reference to the record's field,
but more complex lambdas can be used freely.

Fields declared by `field` are read-only.

Constants, static methods, and instance methods can be added in the usual
way.