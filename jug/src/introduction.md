# Introduction

Joe is a scripting language, implemented in Java, for adding scriptability 
to Java applications and libraries.  I have developed Joe for use in my
own personal projects, and as a hobby language; others might find it useful 
as well.

The syntax is meant to be both familiar and a pleasant surprise for 
Java programmers: Joe loses much of the boilerplate and adds many new
features.

```joe
record Greeter(name) {
    method greet() {
        println("Hello, " + .name + "!");
    }
}

var greeter = Greeter("World");
greeter.greet();
```

Joe includes:

- Numbers, strings, keywords, booleans, lists, sets, and maps as basic 
  data types.
- First-class functions with proper closures.
- Lambdas
- Classes with single inheritance
- Immutable record types
- Rust-like pattern matching
- Nero, a tightly integrated Datalog subsystem.
- A test runner for scripted testing.
- An API documentation generator based on "JoeDoc" comments.
- A rich Java API for embedding Joe in Java apps and libraries, and
  for extending Joe's library to support new problem domains.
- Support for the JavaFX GUI library

See [The Joe Language](language.md) and the sections that follow for 
a description of the language.  

The remainder of this introduction lays out my goals and non-goals for Joe,
and the rationale for its creation, and can safely be skipped by those
who want to get right to it.

## Goals

I wanted a language 

- For adding scriptability to Java applications and libraries,
- With few, if any, external dependencies
- That is Java-friendly in its syntax and semantics
- That can work with native Java types
- With a simple API for embedding it in an application or library
- That provides excellent compilation and runtime error messages
- That is naturally sandboxed at the script level
  - I.e., scripts cannot access arbitrary Java packages, the operating system,
    or I/O, except insofar as the client adds bindings for that purpose.
- That provides a simple, easy to use API for writing bindings to native
  code
- That adds features Java hasn't gotten around to yet.

Joe has become that language.

## Non-Goals

The following were not goals in this development

**High Performance**: Joe is byte-compiled, so it's faster than it could
be.  However, I'm following the original Tcl/Tk dictum: write the fast code 
in the host language and glue it together with the scripting 
language. (And if I were seeking super-high-performance I wouldn't be working 
in Java anyway.)

**Application development**: See above.  Joe is meant to be a partner with
Java, not a replacement.  However, it can be used to write small apps.

**General scripting**: Joe's ecosystem is growing to support general
scripting, but that isn't the primary use case.

**Pure functional programming**: I'm happy to incorporate functional
and declarative techniques in my code; but I've spent over 40 years learning 
how to work with mutable state without shooting myself in the foot.

**Automatic binding to Java classes**: I've used generators like Swig, and
I understand Java reflection.  I _could_ write the code to let Joe call
Java code without any help, effectively generating the glue code on the
fly.  But in my experience, generated APIs of this kind:

- Produce horrible, cryptic, unhelpful error messages
- Fail to make good use of the scripting language's strengths
 
Artisanal hand-crafted bindings can do a much better job, and so Joe is 
designed to make it easy to write custom bindings that are a pleasure to use.

## Why Java?

I've been using Java professionally, full-time, for over a decade.  I'm
comfortable with it, I'm getting good results from it, and I like the way the
language is evolving. I've done lots of both non-GUI and GUI programming in 
Java (using the JavaFX toolkit), and it's my currently language of choice
for personal projects as well.

It's robust. It's stable (at least since the Java 8/Java 9 fiasco).  It's
popular. The JavaFX GUI is portable to multiple platforms.

## Why Not Javascript/LuaJ/JTcl/Groovy...

I've written lots of bindings for scripting languages, going back over a
quarter of a century, mostly for Tcl (in C/C++) and JTcl (in Java); but I've
also worked with the Nashorn version of Javascript, and the LuaJ implementation
of Lua.

I like Tcl very much; but Java is innately object-oriented, and Tcl is not a 
great match for scripting object-oriented APIs. In addition, the JTcl
implementation is dead, and as it includes a bunch of code I have no need of
or interest in I don't really want to fork it and take over maintenance.

I used Nashorn for a period of time, back with Java 8; it was easy to get
started with, but I didn't care for the API I had to use to talk to it, and I 
had to do a lot of work to provide useful error messages.  And then
the powers that be deprecated it, and announced that the future alternative
was to switch to the Graal VM and run my embedded Javascript in Node.  Um, 
no.  The tail on that dog is much too big.

I've used LuaJ quite a bit. It has some nice features, but it's incomplete,
unsupported, and has a number of bugs.  The error messages are generally
unhelpful.  In addition, while Lua is quite popular these days, its syntax
and some of the other choices are weird from a Java point of view.

And then I ran into Robert Nystrom's Lox (see below), and realized that I
could build on his example and produce a language that meets my needs and 
aesthetics.
 
## Why No External Dependencies?

I know the trend these days is to pick a language with a package manager and
then construct your app from dozens, if not hundreds, of external packages.
And sure, you can do that. Go wild. Me, I'll be over here not worrying about 
supply chain issues.

## Why Sandboxed?

Because I don't want to worry about security issues; and more particularly,
I don't want to give clients an easy way to shoot the host application in 
the head.  Joe is for solving domain-specific problems; it shouldn't have any
more access than the domain requires.

## Why not automatic access to Java classes and methods?

Even given that, why not use Java reflection so that a scripting API can
automatically bind to specific, required, Java classes and methods?

LuaJ and Nashorn do this; and in each case I found that I didn't like the 
error messages I got if I passed an incorrect value to a native 
method. I ended up having to write custom Java classes that wrapped the native 
classes in order to provide the API I wanted with the error handling I wanted.

I want an embedding API that helps me to do a good job, not one I have to
work around.


## Acknowledgements

Joe was made possible by Robert Nystrom's excellent book,
[*Crafting Interpreters*](https://craftinginterpreters.com). In origin, Joe
is a dialect of Nystrom's Lox language.  I started with Nystrom's JLox
AST-walker, ultimately adding a byte-compiler patterned after the CLox 
implementation.  Joe retains Lox semantics at its core, but I've subsequently 
changed and added syntax, refactored the implementation, added features,
etc., etc., as my needs (and whimsy) take me.

But the only reason any of that has been possible is that Mr. Nystrom designed
and built a very nice and capable little language and wrote what is possibly 
the best programming text I've ever read to describe how the trick 
is done.[^lunch]

In addition, I want to thank my colleague Jonathan Castello for advice,
encouragement, and a long string of lunches discussing programming language
theory and design.  I haven't always taken his advice, but Joe and Nero are 
all the better for it for my having received it.

[^lunch]: Should Mr. Nystrom ever find himself in my vicinity, I'll gladly
buy him lunch.
