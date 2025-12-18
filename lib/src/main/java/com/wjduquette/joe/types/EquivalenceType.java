package com.wjduquette.joe.types;

import com.wjduquette.joe.*;
import com.wjduquette.joe.nero.Equivalence;
import com.wjduquette.joe.nero.LambdaEquivalence;

/**
 * The type proxy for {@link Equivalence} values.
 */
public class EquivalenceType extends ProxyType<Equivalence> {
    /** The proxy's TYPE constant. */
    public static final EquivalenceType TYPE = new EquivalenceType();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an instance of the proxy.
     */
    public EquivalenceType() {
        super("Equivalence");

        //**
        // @package joe
        // @type Equivalence
        // The `Equivalence` type represents an equivalence relation between
        // two data types.  It allows instances of either type to be converted
        // to the other, and it can check for the equivalence of a pair of
        // values of the two types.
        //
        // The Nero `equivalent/equivalence,a,b` built-in predicate does
        // these kinds of conversions and checks given a registered
        // `Equivalence`.
        proxies(Equivalence.class);

        initializer(this::_initializer);

        method("a2b",           this::_a2b);
        method("b2a",           this::_b2a);
        method("isEquivalent",  this::_isEquivalent);
        method("keyword",       this::_keyword);
    }

    //-------------------------------------------------------------------------
    // Initializer implementation

    //**
    // @init
    // %args keyword, a2b, b2a
    // Creates an `Equivalence` given two conversion lambdas and an identifying
    // *keyword*.
    //
    // - The *keyword* identifies the equivalence in Nero scripts.
    // - *a2b* takes a value of type A and returns the equivalent value of
    //   type B, or null on any failure.
    // - *b2a* takes a value of type B and returns the equivalent value of
    //   type A, or null on any failure.
    // - Failures include:
    //   - Passing a value of the wrong type.
    //   - Passing a value with no equivalent of the other type.
    //
    // For example, the following defines an equivalence between numeric
    // strings and numbers:
    //
    // ```joe
    // var str2num = Equivalence(#str2num,
    //     \s -> s ~ String() ? Number(s) : null,
    //     \n -> n ~ Number() ? Joe.stringify(n) : null);
    // ```
    private Object _initializer(Joe joe, Args args) {
        args.exactArity(3, "Equivalence(keyword, a2b, b2a)");
        var keyword = joe.toKeyword(args.next());
        var a2b = joe.toCallable(args.next());
        var b2a = joe.toCallable(args.next());

        return new LambdaEquivalence(keyword,
            a -> joe.call(a2b, a),
            b -> joe.call(b2a, b));
    }

    //-------------------------------------------------------------------------
    // Method Implementations

    //**
    // @method a2b
    // %args a
    // %result b
    // Converts a value of type A to a value of type B, or null on
    // conversion failure.
    private Object _a2b(Equivalence equiv, Joe joe, Args args) {
        args.exactArity(1, "a2b(a)");
        return equiv.a2b(args.next());
    }

    //**
    // @method b2a
    // %args b
    // %result a
    // Converts a value of type B to a value of type A, or null on
    // conversion failure.
    private Object _b2a(Equivalence equiv, Joe joe, Args args) {
        args.exactArity(1, "b2a(b)");
        return equiv.b2a(args.next());
    }

    //**
    // @method isEquivalent
    // %args a, b
    // %result Boolean
    // Returns true if the two values are equivalent according to this
    // equivalence relation, and false if they are not.
    private Object _isEquivalent(Equivalence equiv, Joe joe, Args args) {
        args.exactArity(2, "isEquivalent(a, b)");
        return equiv.isEquivalent(args.next(), args.next());
    }

    //**
    // @method keyword
    // %result Keyword
    // Returns the equivalence relation's keyword.
    private Object _keyword(Equivalence equiv, Joe joe, Args args) {
        args.exactArity(0, "keyword()");
        return equiv.keyword();
    }
}
