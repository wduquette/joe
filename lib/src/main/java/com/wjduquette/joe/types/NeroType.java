package com.wjduquette.joe.types;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.Keyword;
import com.wjduquette.joe.ProxyType;
import com.wjduquette.joe.nero.Mapper;
import com.wjduquette.joe.nero.Nero;

import java.util.HashMap;

/**
 * A ProxyType for the Nero type.
 */
public class NeroType extends ProxyType<Nero> {
    /** The type, ready for installation. */
    public static final NeroType TYPE = new NeroType();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates the proxy.
     */
    NeroType() {
        super("Nero");

        //**
        // @package joe
        // @type Nero
        // The `Nero` type allows Joe code to execute of
        // [Nero Datalog](../nero/nero.md) [[RuleSet|RuleSets]],
        // as created by the `ruleset` expression.  The process is as follows:
        //
        // - Create and configure an instance of `Nero`.
        // - Create a [[NeroPipeline]] using [[method:Nero.withFile]] or
        //   [[method:Nero.withRules]].
        // - Use the pipeline's methods to configure optional execution
        //   settings.
        // - Invoke one of the pipeline's execution methods.
        //
        // See [[NeroPipeline]] for more.
        proxies(Nero.class);

        initializer(this::_init);

        method("addMapper",       this::_addMapper);
        method("addMappers",      this::_addMappers);
        method("toNeroScript",    this::_toNeroScript);
        method("toNeroAxiom",     this::_toNeroAxiom);
        method("toString",        this::_toString);
        method("withFile",        this::_withFile);
        method("withRules",       this::_withRules);
    }

    //-------------------------------------------------------------------------
    // Stringify

    public String stringify(Joe joe, Object value) {
        return name() + "@" + value.hashCode();
    }

    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // Creates a new instance of `Nero`.
    private Object _init(Joe joe, Args args) {
        args.exactArity(0, "Nero()");
        return new Nero(joe);
    }

    //-------------------------------------------------------------------------
    // Instance Method Implementations

    //**
    // @method addMapper
    // %args keyword, mapper
    // %result this
    // Adds a `mapsTo/f,a,b` mapper function named by the *keyword*.  The
    // *mapper* should a `callable/1` taking a value of some type A and
    // returning a value of some type B.  If the conversion fails, the
    // *mapper* should return null or throw an error.
    private Object _addMapper(Nero nero, Joe joe, Args args) {
        args.exactArity(2, "addMapper(keyword, mapper)");
        var k = joe.toKeyword(args.next());
        var f = joe.toCallable(args.next());
        nero.addMapper(k.name(), a -> joe.call(f, a));
        return nero;
    }

    //**
    // @method addMappers
    // %args map
    // %result this
    // Adds a collection of `mapsTo/f,a,b` functions, where *map* is a map from
    // keyword to `callable/1`.  See [[method:Nero.addMapper]].
    private Object _addMappers(Nero nero, Joe joe, Args args) {
        args.exactArity(1, "addMappers(map)");
        var input = joe.toMap(args.next());
        var map = new HashMap<Keyword, Mapper>();
        for (var e : input.entrySet()) {
            var k = joe.toKeyword(e.getKey());
            var f = joe.toCallable(e.getValue());
            map.put(k, a -> joe.call(f, a));
        }
        map.forEach((k, f) -> nero.addMapper(k.name(), f));
        return nero;
    }

    //**
    // @method toNeroScript
    // %args facts
    // %result String
    // Returns a collection of values as a script of
    // Nero axioms.  Every value in the collection must either be a
    // [[Fact]] or a value that can be converted into a [[Fact]].
    private Object _toNeroScript(Nero nero, Joe joe, Args args) {
        args.exactArity(1, "toNeroScript(facts)");
        return nero.toNeroScript(joe.toFacts(args.next()));
    }

    //**
    // @method toNeroAxiom
    // %args fact
    // %result String
    // Returns the *fact* as a Nero axiom string.  The *fact* must either
    // be a [[Fact]] or an object that can be converted into a [[Fact]].
    private Object _toNeroAxiom(Nero nero, Joe joe, Args args) {
        args.exactArity(1, "toNeroAxiom(fact)");
        return nero.toNeroAxiom(joe.toFact(args.next()));
    }

    //**
    // @method toString
    // %result String
    // Returns the value's string representation.
    private Object _toString(Nero nero, Joe joe, Args args) {
        args.exactArity(0, "toString()");
        return stringify(joe, nero);
    }

    //**
    // @method withFile
    // %args scriptFile
    // %result NeroPipeline
    // Returns an object allowing the Nero *scriptFile* to be executed in a
    // variety of ways. The *scriptFile* must be the file's [[Path]].
    private Object _withFile(Nero nero, Joe joe, Args args) {
        args.exactArity(1, "withFile(scriptFile)");
        var path = joe.toPath(args.next());
        return nero.withFile(path);
    }

    //**
    // @method withRules
    // %args rules
    // %result NeroPipeline
    // Returns an object allowing the *rules* to be executed in a variety
    // of ways.  The *rules* can be passed as a [[RuleSet]] or as a
    // string for compilation.
    private Object _withRules(Nero nero, Joe joe, Args args) {
        args.exactArity(1, "withRules(rules)");
        var rules = joe.toRules(args.next());
        return nero.withRules(rules);
    }
}
