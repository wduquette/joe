# Introduction

Joe is a scripting language, implemented in Java, and is meant to used to
add scriptability to Java applications and libraries.  This section lays
out the goals (and non-goals!) of Joe, and the rationales behind them. [^nuts]

I'm implementing Joe for use in my own personal projects, and as a hobby
language.  Others might find it useful or interesting as well.

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

Joe is growing into that language.

## Non-Goals

**High Performance**: I don't need it; and I'm following the original Tcl/Tk
dictum: write the fast code in the host language and glue it together with the
scripting language.

**Application development**: See above.  Joe is meant to be a partner with
Java, not a replacement.

**General scripting**: Use Python.  Or, better still, Tcl/Tk.

**Pure functional programming**: I'm happy to incorporate functional
techniques in my code; but I've spent over 40 years learning how to work
with mutable state without shooting myself in the foot.

**Automatic binding to Java classes**: I've written bindings by hand.  I've
used generators like Swig.  Artisanal hand-crafted bindings are much more
pleasing, at least to me.  I want to provide tools for that purpose, not 
settle for something quick and ugly.

## Why Java?

I've been using Java professionally, full-time, for the last nine years.  I'm
comfortable with it, I'm getting good results from it, and I like the way the
language is evolving. I've done lots of both non-GUI and GUI programming in 
Java (using the JavaFX toolkit), and it's my currently language of choice
for personal projects as well.

It's robust. It's stable (at least since the Java 8/Java 9 fiasco).  It's
popular.

## Why Not Javascript/LuaJ/JTcl/Groovy...

I've written lots of bindings for scripting languages, going back over a
quarter of a century, mostly for Tcl (in C/C++) and JTcl (in Java); but I've
also worked with the Nashorn version of Javascript, and the LuaJ implementation
of Lua.  I've experimented with Groovy.

I experimented with Groovy at one point, and bounced. I don't remember all 
the details, but I prefer a language in which the syntax isn't quite so...
flexible.

I like Tcl very much; but Tcl is not a great match for scripting Java APIs.
Done a lot of it; it's a great command/console language; but it can be a lot
of work.  Further, the JTcl implementation is both (A) dead, and (B) includes
a host of code I have no need of; and (C) I really don't want to take over 
maintenance of it.

I used Nashorn for a period of time, back with Java 8; I didn't care for the
API I had to use to talk to it, particular, and I had to do a lot of work
to provide useful error messages.  And then the powers that be deprecated it,
and announced that the future alternative to was to switch to the Graal VM
and run my embedded Javascript in Node.  Um, no.  The tail on that dog is
much too big.

I've used LuaJ quite a bit. It has some nice features, but the syntax and some
of the language choices are weird from a Java point of view; the error
messages are frequently unhelpful; the standard library is incomplete; and I 
kinda hate Lua tables with a passion.  (I've been acquainted with "associative 
arrays" since I learned Awk in the late 1980's.  But Lua tables are just weird,
and the standard table API has some peculiar holes.)

And then I ran into Robert Nystrom's Lox (see below), and realized that I
could build on his example and produce a language that meets my needs and 
aesthetics.
 
## Why No External Dependencies?

I know the trend these days is to pick a language with a package manager and
then construct your app from dozens, if not hundreds, of external packages.
And sure, you can do that.  Me, I'll be over here no worrying about 
supply chain issues.

## Why Sandboxed?

Because I don't want to worry about security issues; and more particularly,
I don't want to give clients an easy way to shoot the host application in 
the head.  Joe for solving domain-specific problems; it shouldn't have any
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

## Joe, Lox and JLox

Lox is a little language described in Robert Nystrom's excellent book,
[*Crafting Interpreters*](https://craftinginterpreters.com); JLox is a
tree-walker implementation of the language implemented in Java.  

The Joe language and implementation derive from Lox in general and JLox in 
particular, though it is by no means a mere copy; I've changed and added
syntax, refactored the implementation, etc., etc., as my needs (and whimsy) 
takes me.  But the only reason any of that of is possible
is because Mr. Nystrom designed and built a very nice little language and wrote
what is possibly the best programming text I've ever read to describe how the
trick is done.[^lunch]

[^nuts]: Point is, I've got reasons; here they are; I'm not inclined to argue 
about them.

[^lunch]: Should Mr. Nystrom ever find himself in my vicinity, I'll gladly
buy him lunch.
