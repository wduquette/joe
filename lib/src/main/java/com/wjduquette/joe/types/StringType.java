package com.wjduquette.joe.types;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.ProxyType;
import com.wjduquette.joe.StringFormatter;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * The type proxy for Joe's String type.
 */
public class StringType extends ProxyType<String> {
    /** The type constant, for installation. */
    public static final StringType TYPE = new StringType();

    //-------------------------------------------------------------------------
    // Constructor

    /** Creates the proxy. */
    public StringType() {
        super("String");

        //**
        // @package joe
        // @type String
        // A `String` is just a Java `String`, as accessed from a Joe script.
        //
        // **Note**: Joe provides a number of methods that Java does not.
        proxies(String.class);
        initializer(this::_init);

        staticMethod("format",        this::_format);
        staticMethod("join",          this::_join);

        // pad/padLeft

        method("charAt",              this::_charAt);
        method("contains",            this::_contains);
        method("endsWith",            this::_endsWith);
        method("equalsIgnoreCase",    this::_equalsIgnoreCase);
        method("indent",              this::_indent);
        method("indexOf",             this::_indexOf);
        method("isBlank",             this::_isBlank);
        method("isEmpty",             this::_isEmpty);
        method("lastIndexOf",         this::_lastIndexOf);
        method("length",              this::_length);
        method("lines",               this::_lines);
        method("matches",             this::_matches);
        method("repeat",              this::_repeat);
        method("replace",             this::_replace);
        method("replaceAll",          this::_replaceAll);
        method("replaceFirst",        this::_replaceFirst);
        method("split",               this::_split);
        method("splitWithDelimiters", this::_splitWithDelimiters);
        method("startsWith",          this::_startsWith);
        method("strip",               this::_strip);
        method("stripIndent",         this::_stripIndent);
        method("stripLeading",        this::_stripLeading);
        method("stripTrailing",       this::_stripTrailing);
        method("substring",           this::_substring);
        method("toLowerCase",         this::_toLowerCase);
        method("toString",            this::_toString);
        method("toUpperCase",         this::_toUpperCase);
    }

    //**
    // @typeTopic formatting
    // @title String Formatting
    // The Joe [[String#static.format]] method formats strings similarly
    // to the Java method of the same name, supporting a subset of
    // [Java's format string syntax](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Formatter.html#syntax).
    //
    // This section describes the supported subset.
    //
    // As with Java, the basic syntax is:
    //
    //     %[flags][width][.precision][conversion]
    //
    // ### Conversions
    //
    // The conversion codes are as follows:
    //
    // | Conversion | Input Type | Description                                     |
    // | ---------- | ---------- | ----------------------------------------------- |
    // | `b`, `B`   | Any        | `true` if truthy, `false` if falsey             |
    // | `h`, `H`   | Any        | The value's hash code as a hex string.          |
    // | `s`, `S`   | Any        | The value, stringified.                         |
    // | `d`        | Number     | Converted to integer and formatted.             |
    // | `x`, `X`   | Number     | Converted to integer, formatted as hex          |
    // | `e`, `E`   | Number     | Decimal, scientific notation                    |
    // | `f`        | Number     | Decimal                                         |
    // | `g`, `G`   | Number     | Decimal or scientific notation, as appropriate. |
    // | `%`        | n/a        | A literal percent sign                          |
    // | `n`        | n/a        | The platform-specific line separator            |
    //
    // - Note: the uppercase variant of the conversion is the same as the
    //   lowercase variant, but converts the output to uppercase.
    //
    // ### Precision
    //
    // - For decimal conversions, the *precision* is the number of digits after
    //   the decimal place.
    //
    // - For the conversions with input type "Any" in the above table, the
    //   natural length of the output might be longer than the desired *width*.
    //   In this case, the *precision* field specifies the maximum width of
    //   the output.
    //
    // - The *precision* field cannot be used with the `d`, `x`, and `X`
    //   conversions.
    //
    // ### Flags
    //
    // The supported flags are as follows, and apply only to the specified
    // argument type.
    //
    // | Flag        | Input Type | Description                             |
    // | ----------- | ---------- | --------------------------------------- |
    // | `-`         | Any        | Result is left-justified.               |
    // | `+`         | Number     | Result always includes a sign           |
    // | space       | Number     | Leading space for positive values       |
    // | `0`         | Number     | Padded with leading zeros               |
    // | `,`         | Number     | Use local-specific grouping separators  |
    // | `(`         | Number     | Enclose negative numbers in parentheses |
    //
    // ### What's Not Supported
    //
    // Joe's `String.format()` method does *not* support the following:
    //
    // - Argument indices, using `$` syntax.
    // - Date/time formatting.
    //
    // I don't use argument indices with Java's `String.format()`, as I find
    // them to be fragile; and I've always found it weird to combine simple
    // type formatting and complex date/time formatting in what's meant to
    // be a type-safe clone of Java's `printf()` syntax.  I'd much prefer
    // to provide a distinct API for using Java's `java.time` package.


    //-------------------------------------------------------------------------
    // Initializer Implementation

    //**
    // @init
    // @args value
    // Converts the value into its string representation.
    private Object _init(Joe joe, Args args) {
        args.exactArity(1, "String(value)");
        return joe.stringify(args.next(0));
    }

    //-------------------------------------------------------------------------
    // Static Method Implementations

    //**
    // @static format
    // @args fmt, [values...]
    // @result String
    // Formats the values into a string given the format string. See
    // [[String#topic.formatting]], below, for the format string syntax.
    private Object _format(Joe joe, Args args) {
        args.minArity(1, "format(fmt, [values...])");
        var fmt = joe.toString(args.next());
        return StringFormatter.format(joe, fmt, args.remainderAsList());
    }

    //**
    // @static join
    // @args delimiter, list
    // @result String
    // Given a delimiter and a list, joins the string representations of the
    // list items into a single string, delimited by the given delimiter.
    private Object _join(Joe joe, Args args) {
        args.exactArity(2, "join(delimiter, list)");
        var delim = joe.stringify(args.next());
        var list = joe.toList(args.next());
        return list.stream()
            .map(joe::stringify)
            .collect(Collectors.joining(delim));
    }


    //-------------------------------------------------------------------------
    // Method Implementations

    //**
    // @method charAt
    // @args index
    // @result String
    // Returns the character at the *index* as a string.
    private Object _charAt(String value, Joe joe, Args args) {
        args.exactArity(1, "charAt(index)");
        var index = joe.toIndex(args.next(0), value.length());
        var c = value.charAt(index);
        return Character.toString(c);
    }

    //**
    // @method contains
    // @args target
    // @result Boolean
    // Returns `true` if this contains the *target*, and `false` otherwise.
    private Object _contains(String value, Joe joe, Args args) {
        args.exactArity(1, "contains(target)");
        var target = joe.stringify(args.next(0));
        return value.contains(target);
    }

    //**
    // @method endsWith
    // @args suffix
    // @result Boolean
    // Returns `true` if this string ends with the suffix, and `false` otherwise.
    private Object _endsWith(String value, Joe joe, Args args) {
        args.exactArity(1, "endsWith(suffix)");
        var suffix = joe.stringify(args.next(0));
        return value.endsWith(suffix);
    }

    //**
    // @method equalsIgnoreCase
    // @args other
    // @result Boolean
    // Returns `true` if this string is equal to the string representation
    // of the *other* value, ignoring case, and `false` otherwise.
    private Object _equalsIgnoreCase(String value, Joe joe, Args args) {
        args.exactArity(1, "other");
        return value.equalsIgnoreCase(joe.stringify(args.next(0)));
    }

    //**
    // @method indent
    // @args n
    // @result String
    // Indents or outdents the string by *n* characters.
    //
    // Note: Java's `String::indent` returns the result with a trailing
    // newline; this is easier to add than to remove, and is often unwanted,
    // so Joe trims it.
    private Object _indent(String value, Joe joe, Args args) {
        args.exactArity(1, "indent(n)");
        var n = joe.toInteger(args.next(0));
        return value.indent(n).stripTrailing();
    }

    //**
    // @method indexOf
    // @args target, [beginIndex], [endIndex]
    // @result Number
    // Returns the index of the first occurrence of the *target* string in
    // this string, or -1 if the *target* is not found.  The search starts
    // at *beginIndex*, which defaults to 0, and ends at *endIndex*, which
    // defaults to the end of the string.
    private Object _indexOf(String value, Joe joe, Args args) {
        args.arityRange(1, 3, "indexOf(target, [beginIndex], [endIndex])");
        var target = joe.stringify(args.next());

        if (!args.hasNext()) {
            return (double) value.indexOf(target);
        } else if (args.remaining() == 1) {
            return (double) value.indexOf(target,
                joe.toInteger(args.next())
            );
        } else {
            return (double) value.indexOf(target,
                joe.toInteger(args.next()),
                joe.toInteger(args.next())
            );
        }
    }

    //**
    // @method isBlank
    // @result Boolean
    // Returns `true` if this string is empty or contains only
    // whitespace, and `false` otherwise.
    private Object _isBlank(String value, Joe joe, Args args) {
        args.exactArity(0, "isBlank()");
        return value.isBlank();
    }

    //**
    // @method isEmpty
    // @result Boolean
    // Returns `true` if this string is the empty string, and `false` otherwise.
    private Object _isEmpty(String value, Joe joe, Args args) {
        args.exactArity(0, "isEmpty()");
        return value.isEmpty();
    }

    //**
    // @method lastIndexOf
    // @args target, [fromIndex]
    // @result Number
    // Returns the index of the last occurrence of the *target* string in
    // this string, or -1 if the *target* is not found.  The search starts
    // at *fromIndex*, which defaults to 0, and proceeds towards the start of
    // the string.
    private Object _lastIndexOf(String value, Joe joe, Args args) {
        args.arityRange(1, 2, "lastIndexOf(target, [fromIndex]");
        var target = joe.stringify(args.next());
        if (!args.hasNext()) {
            return (double) value.lastIndexOf(target);
        } else {
            return (double) value.lastIndexOf(target,
                joe.toInteger(args.next())
            );
        }
    }

    //**
    // @method length
    // @result Double
    // Gets the string's length.
    private Object _length(String value, Joe joe, Args args) {
        args.exactArity(0, "length()");
        return (double)value.length();
    }

    //**
    // @method lines
    // @result List
    // Returns a list consisting of the lines of text in the string.
    private Object _lines(String value, Joe joe, Args args) {
        args.exactArity(0, "lines()");
        return new ListValue(value.lines().toList());
    }

    //**
    // @method matches
    // @args pattern
    // @result Boolean
    // Returns `true` if this string matches the regex pattern, and
    // `false` otherwise.
    private Object _matches(String value, Joe joe, Args args) {
        args.exactArity(1, "matches(pattern)");
        return value.matches(joe.toString(args.next()));
    }

    //**
    // @method repeat
    // @args count
    // @result String
    // Creates a string containing this string repeated *count* times.
    private Object _repeat(String value, Joe joe, Args args) {
        args.exactArity(1, "repeat(count)");
        var arg = args.next();
        var count = joe.toInteger(arg);

        if (count < 0) {
            throw joe.expected("non-negative count", arg);
        }

        return value.repeat(count);
    }

    //**
    // @method replace
    // @args target, replacement
    // @result String
    // Returns the string created by replacing every occurrence of the
    // *target* string in this string with the *replacement* string.
    private Object _replace(String value, Joe joe, Args args) {
        args.exactArity(2, "replace(target,replacement)");
        return value.replace(
            joe.stringify(args.next()),
            joe.stringify(args.next())
        );
    }

    //**
    // @method replaceAll
    // @args regex, replacement
    // @result String
    // Returns the string created by replacing each substring of this
    // string that matches the *regex* with the replacement string.
    private Object _replaceAll(String value, Joe joe, Args args) {
        args.exactArity(2, "replaceAll(regex, replacement)");
        return value.replaceAll(
            joe.toString(args.next()),
            joe.stringify(args.next())
        );
    }

    //**
    // @method replaceFirst
    // @args regex, replacement
    // @result String
    // Returns the string created by replacing the first substring of this
    // string that matches the *regex* with the replacement string.
    private Object _replaceFirst(String value, Joe joe, Args args) {
        args.exactArity(2, "replaceFirst(regex, replacement)");
        return value.replaceFirst(
            joe.toString(args.next()),
            joe.stringify(args.next())
        );
    }

    //**
    // @method split
    // @args delimiter
    // @result List
    // Returns a list of the tokens formed by splitting the string on
    // each of the substrings that match the *delimiter* regular
    // expression pattern.  The delimiter substrings are not included
    // in the list.
    private Object _split(String value, Joe joe, Args args) {
        args.exactArity(1, "split(delimiter)");
        var delim = joe.toString(args.next());
        var tokens = value.split(delim);
        return new ListValue(Arrays.asList(tokens));
    }

    //**
    // @method splitWithDelimiters
    // @args delimiter
    // @result List
    // Returns a list of the tokens formed by splitting the string on
    // each of the substrings that match the *delimiter* regular
    // expression pattern.  The delimiter substrings are included
    // as tokens in the list.
    private Object _splitWithDelimiters(String value, Joe joe, Args args) {
        args.exactArity(1, "splitWithDelimiters(delimiter)");
        var delim = joe.toString(args.next());
        var tokens = value.splitWithDelimiters(delim, 0);
        return new ListValue(Arrays.asList(tokens));
    }

    //**
    // @method startsWith
    // @args prefix
    // @result Boolean
    // Returns `true` if this string starts with the prefix, and `false` otherwise.
    private Object _startsWith(String value, Joe joe, Args args) {
        args.exactArity(1, "startsWith(prefix)");
        var prefix = joe.stringify(args.next(0));
        return value.startsWith(prefix);
    }

    //**
    // @method strip
    // @result String
    // Returns this string, stripped of all leading and trailing whitespace.
    private Object _strip(String value, Joe joe, Args args) {
        args.exactArity(0, "strip()");
        return value.strip();
    }

    //**
    // @method stripIndent
    // @result String
    // Strips the indent from each line of the string, preserving relative
    // indentation.
    private Object _stripIndent(String value, Joe joe, Args args) {
        args.exactArity(0, "stripIndent()");
        return value.stripIndent();
    }

    //**
    // @method stripLeading
    // @result String
    // Returns this string, stripped of all leading whitespace.
    private Object _stripLeading(String value, Joe joe, Args args) {
        args.exactArity(0, "stripLeading()");
        return value.stripLeading();
    }

    //**
    // @method stripTrailing
    // @result String
    // Returns this string, stripped of all trailing whitespace.
    private Object _stripTrailing(String value, Joe joe, Args args) {
        args.exactArity(0, "stripTrailing()");
        return value.stripTrailing();
    }

    //**
    // @method substring
    // @args beginIndex, [endIndex]
    // @result String
    // Returns the substring of this string that starts at the
    // *beginIndex* and ends at the *endIndex*; *endIndex* defaults
    // to the end of the string.
    private Object _substring(String value, Joe joe, Args args) {
        args.arityRange(1, 2, "substring(beginIndex, [endIndex]");
        if (args.remaining() == 1) {
            return value.substring(
                joe.toIndex(args.next(), value.length())
            );
        } else {
            return value.substring(
                joe.toIndex(args.next(), value.length()),
                joe.toIndex(args.next(), value.length())
            );
        }
    }

    //**
    // @method toLowerCase
    // @result String
    // Returns this string converted to lowercase.
    private Object _toLowerCase(String value, Joe joe, Args args) {
        args.exactArity(0, "toLowerCase()");
        return value.toLowerCase();
    }

    //**
    // @method toString
    // @result String
    // Returns this string.
    private Object _toString(String value, Joe joe, Args args) {
        args.exactArity(0, "toString()");
        return value;
    }

    //**
    // @method toUpperCase
    // @result String
    // Returns this string converted to uppercase.
    private Object _toUpperCase(String value, Joe joe, Args args) {
        args.exactArity(0, "toUpperCase()");
        return value.toUpperCase();
    }
}
