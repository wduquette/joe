# Classes

Joe supports a simple notion of classes, including single inheritance.

```joe
class Person {
    method init(first, last) {
        this.first = first;
        this.last = last;
    }
    
    method fullName() {
        return this.first + " " + this.last;
    }
}

var joe = Person("Joe", "Pro");
println(joe.first);         // Prints "Joe"
println(joe.fullName());    // Prints "Joe Pro"
```

## Class Properties

A class has two kinds of property, method properties and variable properties,
aka instance variables.

The two kinds of property share a namespace.  Take care not to give instance 
variables the same name as methods; the variable will shadow the method,
yielding surprising results.

## Variable Properties

Instance variables are accessed via the `this` variable within the class's
methods, and as properties of the instance in other scopes:

```joe
class Person {
    ...
    method fullName() {
        return this.first + " " + this.last;
    }
}

var joe = Person("Joe", "Pro");
println(joe.first);         // Prints "Joe"
```

## Method Properties

Methods are defined in the class body using the `method` statement.  
Methods are identical to Joe functions, except that each method is bound
to its instance and has access to its instance via the `this` variable.

Methods are accessed via the `this` variable within the class's
methods, and as properties of the instance in other scopes:

```joe
class Person {
    method fullName() { ... }
    method greet() { println("Howdy," + this.fullName()); }
}

var joe = Person("Joe", "Pro");
joe.greet();    // Prints "Howdy, Joe Pro!"
```

## The `toString` Method

If the class defines a `toString()` method, then `Joe::stringify` will use
it to convert instances of the class to their string representations.  

## The Class Initializer

The class's name, which by convention begins with an uppercase letter,
serves as the class's constructor.  It delegates its work to the class's
optional `init` method, and takes the same arguments.

```joe
class Person {
    method init(first, last) {
        this.first = first;
        this.last = last;
    }
    ...    
}

var joe = Person("Joe", "Pro");
```

The `init` method can be called directly on an instance to reinitialize
the object, but this is rarely done.

## The `@` Operator

Joe requires that methods refer to all instance variables and methods
using `this.`, which yields cluttered, noisy looking code.  As a convenience,
the `@` operator is equivalent to `this.`, enabling code like this:

```joe
class Person {
    method init(first, last) {
        @first = first;
        @last = last;
    }
    
    method fullname() {
        return @first + " " + @last; 
    }
    
    method greet(greeting) {
        return greeting + ", " + @fullname() + "!";
    }
}
```

## Method References

An instance method is a first class object, just like a function, and
can be referenced by its name. For example,

```joe
class Person {
    ...
    method fullName() {
        return this.first + " " + this.last;
    }
}

var joe = Person("Joe", "Pro");
var myFunc = joe.fullName;
println(myFunc());          // Prints "Joe Pro"
```

## Superclasses and Inheritance

A class can inherit from a superclass using the `extends` keyword:

```joe
class Superclass {
    method name() { return "Superclass"; }
}

class Subclass extends Superclass {
    method name() {
        return "Subclass of " + super.name();
    }
}

var sub = Subclass();
println(sub.name());    // Prints "Subclass of Superclass".
```

As shown in the example, a subclass can call superclass methods by
means of the `super` variable.  `super` is only defined in methods
of subclasses.
