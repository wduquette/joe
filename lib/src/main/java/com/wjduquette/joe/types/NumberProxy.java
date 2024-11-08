package com.wjduquette.joe.types;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.TypeProxy;

import java.util.List;

/**
 * The type proxy for Joe's Number type.
 */
public class NumberProxy extends TypeProxy<Double> {
    /** The type constant. */
    public static final NumberProxy TYPE = new NumberProxy();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe
    // @type Number
    // The `Number` type is a static type that collects together a number of
    // useful constants and numeric methods.  Most of the methods
    // gathered here are delegated directly to Java's `java.lang.Math`
    // class; numeric details are to be found there.
    /** Creates the proxy. */
    public NumberProxy() {
        super("Number");
        staticType();
        proxies(Double.class);

        //**
        // @constant E
        // The double-precision value that is closer than any other to
        // <i>e</i>, the base of the natural logarithms.
        constant("E", Math.E);

        //**
        // @constant MAX_INT
        // The maximum value of a Java integer.
        constant("MAX_INT",             (double)Integer.MAX_VALUE);

        //**
        // @constant MAX_VALUE
        // The maximum value of a Java double.
        constant("MAX_VALUE",           Double.MAX_VALUE);

        //**
        // @constant MIN_INT
        // The minimum (most negative) value of a Java integer.
        constant("MIN_INT",             (double)Integer.MIN_VALUE);

        //**
        // @constant MIN_VALUE
        // The minimum (most negative) value of a Java double.
        constant("MIN_VALUE",           Double.MIN_VALUE);

        //**
        // @constant NEGATIVE_INFINITY
        // The double-precision value signifying negative infinity.
        constant("NEGATIVE_INFINITY",   Double.NEGATIVE_INFINITY);

        //**
        // @constant NAN
        // The double-precision value signifying "not a number".
        constant("NAN",                 Double.NaN);

        //**
        // @constant PI
        // The double-precision value that is closer than any other to
        // 𝛑, the ratio of the circumference of a circle to its diameter.
        constant("PI", Math.PI);

        //**
        // @constant POSITIVE_INFINITY
        // The double-precision value signifying positive infinity.
        constant("POSITIVE_INFINITY",   Double.POSITIVE_INFINITY);

        //**
        // @constant TAU
        // The double-precision value that is closer than any other to
        // 𝛕, the ratio of the circumference of a circle to its radius.
        constant("TAU",                 Math.TAU);

        initializer(this::_initializer);

        staticMethod("abs",       this::_abs);
        staticMethod("acos",      this::_acos);
        staticMethod("asin",      this::_asin);
        staticMethod("atan",      this::_atan);
        staticMethod("atan2",     this::_atan2);
        staticMethod("ceil",      this::_ceil);
        staticMethod("clamp",     this::_clamp);
        staticMethod("cos",       this::_cos);
        staticMethod("exp",       this::_exp);
        staticMethod("floor",     this::_floor);
        staticMethod("hypot",     this::_hypot);
        staticMethod("log",       this::_log);
        staticMethod("log10",     this::_log10);
        staticMethod("max",       this::_max);
        staticMethod("min",       this::_min);
        staticMethod("pow",       this::_pow);
        staticMethod("random",    this::_random);
        staticMethod("round",     this::_round);
        staticMethod("sin",       this::_sin);
        staticMethod("sqrt",      this::_sqrt);
        staticMethod("tan",       this::_tan);
        staticMethod("toDegrees", this::_toDegrees);
        staticMethod("toRadians", this::_toRadians);


    }

    //-------------------------------------------------------------------------
    // stringify()

    @Override
    public String stringify(Joe joe, Object value) {
        assert value instanceof Double;

        String text = ((Double)value).toString();
        if (text.endsWith(".0")) {
            text = text.substring(0, text.length() - 2);
        }
        return text;
    }

    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // @args string
    // Converts a numeric string to a number.  Supports Joe's numeric
    // literal syntax, including hexadecimals.
    private Object _initializer(Joe joe, Args args) {
        Joe.exactArity(args, 1, "Number(string)");
        var string = joe.stringify(args.next()).trim();

        try {
            if (string.startsWith("0x")) {
                string = string.substring(2);
                return (double)Integer.parseInt(string, 16);
            } else {
                return Double.parseDouble(string);
            }
        } catch (IllegalArgumentException ex) {
            throw joe.expected("numeric string", string);
        }
    }

    //-------------------------------------------------------------------------
    // Static Methods

    //**
    // @static abs
    // @args num
    // @result Number
    // Returns the absolute value of the given *number*.
    private Object _abs(Joe joe, Args args) {
        Joe.exactArity(args, 1, "Number.abs(num)");
        return Math.abs(joe.toDouble(args.next(0)));
    }

    //**
    // @static acos
    // @args num
    // @result Number
    // Returns the arc cosine of the number
    private Object _acos(Joe joe, Args args) {
        Joe.exactArity(args, 1, "Number.acos(num)");
        return Math.acos(joe.toDouble(args.next(0)));
    }

    //**
    // @static asin
    // @args num
    // @result Number
    // Returns the arc sine of the number.
    private Object _asin(Joe joe, Args args) {
        Joe.exactArity(args, 1, "Number.asin(num)");
        return Math.asin(joe.toDouble(args.next(0)));
    }

    //**
    // @static atan
    // @args num
    // @result Number
    // Returns the arc tangent of the number.
    private Object _atan(Joe joe, Args args) {
        Joe.exactArity(args, 1, "Number.atan(num)");
        return Math.atan(joe.toDouble(args.next(0)));
    }

    //**
    // @static atan2
    // @args x, y
    // @result Number
    // Returns the angle *theta* from the conversion of rectangular coordinates
    // (*x*, *y*) to polar coordinates (*r*, *theta*).
    private Object _atan2(Joe joe, Args args) {
        Joe.exactArity(args, 2, "Number.atan2(x, y)");
        return Math.atan2(
            joe.toDouble(args.next(0)),
            joe.toDouble(args.next(0))
        );
    }

    //**
    // @static ceil
    // @args num
    // @result Number
    // Returns the smallest integer number that is greater than or
    // equal to *num*.
    private Object _ceil(Joe joe, Args args) {
        Joe.exactArity(args, 1, "Number.ceil(num)");
        return Math.ceil(joe.toDouble(args.next(0)));
    }

    //**
    // @static clamp
    // @args num, min, max
    // @result Number
    // Clamps *num* to fit between *min* and *max*.
    private Object _clamp(Joe joe, Args args) {
        Joe.exactArity(args, 3, "Number.clamp(num, min, max)");
        return Math.clamp(
            joe.toDouble(args.next(0)),
            joe.toDouble(args.next(0)),
            joe.toDouble(args.next(0))
        );
    }

    //**
    // @static cos
    // @args a
    // @result Number
    // Returns the cosine of angle *a*.
    private Object _cos(Joe joe, Args args) {
        Joe.exactArity(args, 1, "Number.cos(a)");
        return Math.cos(joe.toDouble(args.next(0)));
    }

    //**
    // @static exp
    // @args num
    // @result Number
    // Returns [[Number#constant.E]] raised the *num* power.
    private Object _exp(Joe joe, Args args) {
        Joe.exactArity(args, 1, "Number.exp(num)");
        return Math.exp(joe.toDouble(args.next(0)));
    }

    //**
    // @static floor
    // @args num
    // @result Number
    // Returns the largest integer number that is less than or
    // equal to *num*.
    private Object _floor(Joe joe, Args args) {
        Joe.exactArity(args, 1, "Number.floor(num)");
        return Math.floor(joe.toDouble(args.next(0)));
    }

    //**
    // @static hypot
    // @args x, y
    // @result Number
    // Returns the length of the hypotenuse of a right triangle with
    // legs of length *x* and *y*.
    private Object _hypot(Joe joe, Args args) {
        Joe.exactArity(args, 2, "Number.hypot(x, y)");
        return Math.hypot(
            joe.toDouble(args.next(0)),
            joe.toDouble(args.next(0))
        );
    }

    //**
    // @static log
    // @args num
    // @result Number
    // Returns the natural logarithm of the number.
    private Object _log(Joe joe, Args args) {
        Joe.exactArity(args, 1, "Number.log(num)");
        return Math.log(joe.toDouble(args.next(0)));
    }

    //**
    // @static log10
    // @args num
    // @result Number
    // Returns the base-10 logarithm of the number.
    private Object _log10(Joe joe, Args args) {
        Joe.exactArity(args, 1, "Number.log10(num)");
        return Math.log10(joe.toDouble(args.next(0)));
    }

    //**
    // @static max
    // @args num...
    // @result Number
    // Given one or more numbers, returns the maximum value.
    // The numbers may be passed as individual arguments or as a
    // single [[List]].
    private Object _max(Joe joe, Args args) {
        Joe.minArity(args, 1, "Number.max(num...)");
        List<?> list = (args.remaining() == 1 && args.next(0) instanceof List<?>)
            ? joe.toList(args.next(0))
            : args.asList();
        return list.stream()
            .map(joe::toDouble)
            .max(Double::compare);
    }

    //**
    // @static min
    // @args num...
    // @result Number
    // Given one or more numbers, returns the minimum value.
    // The numbers may be passed as individual arguments or as a
    // single [[List]].
    private Object _min(Joe joe, Args args) {
        Joe.minArity(args, 1, "Number.min(num...)");

        List<?> list = (args.remaining() == 1 && args.next(0) instanceof List<?>)
            ? joe.toList(args.next(0))
            : args.asList();
        return list.stream()
            .map(joe::toDouble)
            .min(Double::compare);
    }

    //**
    // @static pow
    // @args a, b
    // @result Number
    // Returns *a* raised to the *b* power.
    private Object _pow(Joe joe, Args args) {
        Joe.exactArity(args, 2, "Number.pow(a, b)");
        return Math.pow(
            joe.toDouble(args.next(0)),
            joe.toDouble(args.next(0))
        );
    }

    //**
    // @static random
    // @result Number
    // Returns a random number in the range `0.0 <= x < 1.0`.
    private Object _random(Joe joe, Args args) {
        Joe.exactArity(args, 0, "Number.random()");
        return Math.random();
    }

    //**
    // @static round
    // @args num
    // @result Number
    // Returns the closest integer number to *num*, rounding ties
    // toward positive infinity.
    private Object _round(Joe joe, Args args) {
        Joe.exactArity(args, 1, "Number.round(num)");
        return (double)Math.round(joe.toDouble(args.next(0)));
    }

    //**
    // @static sin
    // @args a
    // @result Number
    // Returns the sine of the angle.
    private Object _sin(Joe joe, Args args) {
        Joe.exactArity(args, 1, "Number.sin(a)");
        return Math.sin(joe.toDouble(args.next(0)));
    }

    //**
    // @static sqrt
    // @args num
    // @result Number
    // Returns the square root of the number.
    private Object _sqrt(Joe joe, Args args) {
        Joe.exactArity(args, 1, "Number.sqrt(num)");
        return Math.sqrt(joe.toDouble(args.next(0)));
    }

    //**
    // @static tan
    // @args a
    // @result Number
    // Returns the tangent of the angle.
    private Object _tan(Joe joe, Args args) {
        Joe.exactArity(args, 1, "Number.tan(a)");
        return Math.tan(joe.toDouble(args.next(0)));
    }

    //**
    // @static toDegrees
    // @args radians
    // @result Number
    // Converts an angle in radians to an angle in degrees.
    private Object _toDegrees(Joe joe, Args args) {
        Joe.exactArity(args, 1, "Number.toDegrees(radians)");
        return Math.toDegrees(joe.toDouble(args.next(0)));
    }

    //**
    // @static toRadians
    // @args degrees
    // @result Number
    // Converts an angle in degrees to an angle in radians.
    private Object _toRadians(Joe joe, Args args) {
        Joe.exactArity(args, 1, "Number.toRadians(degrees)");
        return Math.toRadians(joe.toDouble(args.next(0)));
    }
}
