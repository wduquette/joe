package com.wjduquette.joe.types;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.ProxyType;
import com.wjduquette.joe.nero.Equivalence;
import com.wjduquette.joe.nero.Nero;
import com.wjduquette.joe.nero.NeroRuleSet;

import java.util.ArrayList;

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
        // The `Nero` object allows Joe code to make use of
        // [Nero Datalog](../nero/nero.md) [[RuleSet|RuleSets]],
        // as created by the `ruleset` expression.
        proxies(Nero.class);

        initializer(this::_init);

        method("equivalence",     this::_equivalence);
        method("equivalences",    this::_equivalences);
        method("getEquivalences", this::_getEquivalences);
        method("toNeroScript",    this::_toNeroScript);
        method("toNeroAxiom",     this::_toNeroAxiom);
        method("toString",        this::_toString);
        method("with",            this::_with);
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
    // @method equivalence
    // %args equivalence
    // %result this
    // Adds an equivalence relation for use with the
    // `equivalent/equivalence,a,b` built-in predicate.
    private Object _equivalence(Nero nero, Joe joe, Args args) {
        args.exactArity(1, "equivalence(equivalence)");
        nero.addEquivalence(joe.toType(Equivalence.class, args.next()));
        return nero;
    }

    //**
    // @method equivalences
    // %args equivalence, ...
    // %args list
    // %result this
    // Adds equivalences relation for use with the
    // `equivalent/equivalence,a,b` built-in predicate.  The equivalences
    // can be passed as individual arguments or as a single list.
    private Object _equivalences(Nero nero, Joe joe, Args args) {
        args = args.expandOrRemaining();
        var list = new ArrayList<Equivalence>();
        while (args.hasNext()) {
            list.add(joe.toType(Equivalence.class, args.next()));
        }
        nero.addEquivalences(list);
        return nero;
    }

    //**
    // @method getEquivalences
    // %result Set
    // Returns a set of all client-defined equivalences.
    private Object _getEquivalences(Nero nero, Joe joe, Args args) {
        args.exactArity(0, "getEquivalences()");
        return new SetValue(nero.getEquivalences().values());
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
    // @method with
    // %args rules
    // %result NeroPipeline
    // Returns an object allowing the *rules* to be executed in a variety
    // of ways. The *rules* may be passed as a [[RuleSet]] or as a
    // string to be compiled into a [[RuleSet]].
    private Object _with(Nero nero, Joe joe, Args args) {
        args.exactArity(1, "with(rules)");
        var arg = args.next();
        if (arg instanceof NeroRuleSet rules) {
            return nero.with(rules);
        } else {
            return nero.with(joe.toString(arg));
        }
    }
}
