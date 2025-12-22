package com.wjduquette.joe.types;

import com.wjduquette.joe.*;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A ProxyType for Java Paths.
 */
public class PathType extends ProxyType<Path> {
    /** The proxy, for installation. */
    public static final PathType TYPE = new PathType();

    //-----------------------------------------------------------------
    // Constructor

    //**
    // @package joe.console
    // @type Path
    // A Java `Path`, i.e, the path to a file or a directory in the
    // host file system.
    PathType() {
        super("Path");
        proxies(Path.class);

        initializer(this::_initializer);

        // Static methods
        staticMethod("compare", this::_compare);

        // Methods
        method("endsWith",       this::_endsWith);
        method("getFileName",    this::_getFileName);
        method("getName",        this::_getName);
        method("getNameCount",   this::_getNameCount);
        method("getParent",      this::_getParent);
        method("isAbsolute",     this::_isAbsolute);
        method("normalize",      this::_normalize);
        method("relativize",     this::_relativize);
        method("resolve",        this::_resolve);
        method("startsWith",     this::_startsWith);
        method("subpath",        this::_subpath);
        method("toAbsolutePath", this::_toAbsolutePath);
        method("toString",       this::_toString);
    }

    //-----------------------------------------------------------------
    // Initializer

    //**
    // @init
    // %args first,[more...]
    // Creates a path from one or more string components.
    private Object _initializer(Joe joe, Args args) {
        args.minArity(1, "Path(first,[more...])");
        var first = joe.toString(args.next());
        var more = new String[args.size() - 1];

        for (int i = 1; i < args.size(); i++) {
            more[i - 1] = joe.toString(args.get(i));
        }

        try {
            return Path.of(first, more);
        } catch (InvalidPathException ex) {
            throw joe.expected("valid path components",
                components(args.asList()));
        }
    }

    //-----------------------------------------------------------------
    // Static Methods

    //**
    // @static compare
    // %args a, b
    // %result Number
    // Returns -1, 0, or 1 as *a* is less than, equal to, or greater
    // than *b* when compared lexicographically.
    private Object _compare(Joe joe, Args args) {
        args.exactArity(2, "Path.compare(a,b)");
        var a = toPath(joe, args.next());
        var b = toPath(joe, args.next());

        return (double)a.compareTo(b);
    }

    //-----------------------------------------------------------------
    // Methods

    //**
    // @method endsWith
    // %args path
    // %result Boolean
    // Returns `true` if this path ends with the given *path*.  The
    // *path* may be specified as a `Path` or a `String`.
    private Object _endsWith(Path path, Joe joe, Args args) {
        args.exactArity(1, "endsWith(path)");
        return path.endsWith(toPath(joe, args.next()));
    }

    //**
    // @method getFileName
    // %result Path
    // Returns the final path component, i.e., the name of
    // the file or directory denoted by this path.
    private Object _getFileName(Path path, Joe joe, Args args) {
        args.exactArity(0, "getFileName");
        return path.getFileName();
    }

    //**
    // @method getName
    // %args index
    // %result Path
    // Returns the path component at the given *index*.
    private Object _getName(Path path, Joe joe, Args args) {
        args.exactArity(1, "getName(index)");
        var index = joe.toIndex(args.next(), path.getNameCount());
        return path.getName(index);
    }

    //**
    // @method getNameCount
    // %result Number
    // Returns the number of path components.
    private Object _getNameCount(Path path, Joe joe, Args args) {
        args.exactArity(0, "getNameCount");
        return (double) path.getNameCount();
    }

    //**
    // @method getParent
    // %result Path
    // Returns the parent of this path, or null if it doesn't have one.
    private Object _getParent(Path path, Joe joe, Args args) {
        args.exactArity(0, "getParent()");
        return path.getParent();
    }

    //**
    // @method isAbsolute
    // %result Boolean
    // Returns `true` if the path is an absolute path, and `false`
    // otherwise.
    private Object _isAbsolute(Path path, Joe joe, Args args) {
        args.exactArity(0, "isAbsolute()");
        return path.isAbsolute();
    }

    //**
    // @method normalize
    // %result Path
    // Returns the path with redundant name elements eliminated.
    private Object _normalize(Path path, Joe joe, Args args) {
        args.exactArity(0, "normalize()");
        return path.normalize();
    }

    //**
    // @method relativize
    // %args other
    // %result Path
    // Returns a relative path from this path to the *other* path.
    //
    // The *other* path may a `Path` or a `String`.
    private Object _relativize(Path path, Joe joe, Args args) {
        args.exactArity(1, "relativize(other)");
        return path.relativize(toPath(joe, args.next()));
    }

    //**
    // @method resolve
    // %args other
    // %result Path
    // Resolves the *other* path against this path, if *other* is
    // relative, constructs a path starting with this path and
    // continuing with the other.  If *other* is absolute, it is
    // returned unchanged.
    //
    // The *other* path may a `Path` or a `String`.
    private Object _resolve(Path path, Joe joe, Args args) {
        args.exactArity(1, "resolve(other)");
        return path.resolve(toPath(joe, args.next()));
    }

    //**
    // @method startsWith
    // %args path
    // %result Boolean
    // Returns `true` if this path starts with the given *path*.  The
    // *path* may be specified as a `Path` or a `String`.
    private Object _startsWith(Path path, Joe joe, Args args) {
        args.exactArity(1, "startsWith(path)");
        return path.startsWith(toPath(joe, args.next()));
    }

    //**
    // @method subpath
    // %args start, [end]
    // %result Path
    // Returns the subpath of this path that starts at *start*
    // and ends before *end*, which defaults to the end of the path.
    private Object _subpath(Path path, Joe joe, Args args) {
        args.arityRange(1, 2, "subpath(start, [end])");
        var start = joe.toIndex(args.next(), path.getNameCount());

        if (!args.hasNext()) {
            return path.subpath(start, path.getNameCount());
        } else {
            var end = joe.toIndex(args.next(), path.getNameCount());
            if (end < start) {
                throw new JoeError(
                    "end '" + end + "' < start '" + start + "'.");
            }
            return path.subpath(start, end);
        }
    }

    //**
    // @method toAbsolutePath
    // %result Path
    // Converts the path into an absolute path.
    private Object _toAbsolutePath(Path path, Joe joe, Args args) {
        args.exactArity(0, "toAbsolutePath()");
        return path.toAbsolutePath();
    }

    //**
    // @method toString
    // %result String
    // Returns the path's string representation.
    private Object _toString(Path path, Joe joe, Args args) {
        args.exactArity(0, "toString()");
        return path.toString();
    }

    private String components(List<Object> values) {
        return values.stream()
            .map(s -> "\"" + s + "\"")
            .collect(Collectors.joining(", "));
    }

    /**
     * Given an argument, converts it to a Path.  The argument
     * may be a Path or a String representing a Path.
     * @param joe The interpreter
     * @param arg The argument
     * @return The path
     * @throws JoeError on failure.
     */
    public static Path toPath(Joe joe, Object arg) {
        try {
            return switch (arg) {
                case String s -> {
                    try {
                        yield Path.of(s);
                    } catch (Exception ex) {
                        throw joe.expected("path string", arg);
                    }
                }
                case Path p -> p;
                default -> throw joe.expected("path", arg);
            };
        } catch (IllegalArgumentException ex) {
            throw new JoeError(ex.getMessage());
        }
    }
}
