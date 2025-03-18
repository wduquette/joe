# Native Records

Joe makes it easy to define bindings for native Java record types via the
`RecordType<R>` class, a `ProxyType<T>` created for the 
purpose.  `RecordType<R>` also supports bindings that makes other Java
types *look* like record types.

Consider the following Java record type:

```java
record Thing(String id, Keyword color) {}
```

To create a binding, define `ThingType` as follows and install
`ThingType.TYPE` into an instance of `Joe` in the usual way.  The user
can now create and use instances of `Thing` in their scripts.

```java
public class ThingType extends RecordType<Thing> {
    public static final ThingType TYPE = new ThingType();

    public ThingType() {
        super("Thing");

        proxies(Thing.class);
        
        initializer(this::_init);

        recordField("id",    Thing::id);
        recordField("color", Thing::color);
    }
    
    private Object _init(Joe joe, Args args) {
        args.exactArity(2, "Thing(id, color)");
        var id = joe.toString(args.next());
        var color = joe.toKeyword(args.next());
        return new Thing(id, color);
    }
}
```

Constants, static methods, and instance methods can be added in the usual
way.