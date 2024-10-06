package com.wjduquette.joe.console;

import com.wjduquette.joe.*;
import com.wjduquette.joe.types.ListValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ConsolePackage extends JoePackage {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The command line arguments forwarded to the console.
    private final ListValue argList = new ListValue();

    // The current working directory
    private Path cwd = Path.of(".").toAbsolutePath().normalize();

    private BufferedReader inputReader = null;

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.console
    // This package contains an API for use by simple command-line tools.
    //
    // Using it, clients can:
    //
    // - Acquire the command line arguments
    // - Read from System.in
    // - Read from a file
    // - Write to a file
    // - Query and set the current working directory

    public ConsolePackage() {
        super("joe.console");

        type(new ConsoleProxy());
        type(new PathProxy());
    }

    //-------------------------------------------------------------------------
    // Configuration


    /**
     * The client can configure the console package with the arguments
     * after creating it.
     * @return The argument list
     */
    public ListValue getArgs() {
        return argList;
    }

    //-------------------------------------------------------------------------
    // The Console Type

    private class ConsoleProxy extends TypeProxy<Void> {
        //---------------------------------------------------------------------
        // Constructor

        //**
        // @type Console
        // This static type provides a modicum of access to the console
        // environment, sufficient to writing simple scripts.
        ConsoleProxy() {
            super("Console");
            staticType();
            staticMethod("args",      this::_args);
            staticMethod("cd",        this::_cd);
            staticMethod("exit",      this::_exit);
            staticMethod("mkdir",     this::_mkdir);
            staticMethod("pwd",       this::_pwd);
            staticMethod("read",      this::_read);
            staticMethod("readFile",  this::_readFile);
            staticMethod("readLines", this::_readLines);
            staticMethod("writeFile", this::_writeFile);
        }

        //---------------------------------------------------------------------
        // Static Methods

        //**
        // @static args
        // @result List
        // Returns a list of the arguments passed to the command line, as
        // filtered by the application.
        private Object _args(Joe joe, ArgQueue args) {
            Joe.exactArity(args, 0, "Console.args()");
            return joe.readonlyList(argList);
        }

        //**
        // @static cd
        // @args path
        // @result Path
        // Changes the current working directory to the given path.  If
        // the path is relative, it is treated as relative to the
        // current working directory.
        private Object _cd(Joe joe, ArgQueue args) {
            Joe.exactArity(args, 1, "Console.cd(path)");
            cwd = toResolvedPath(joe, args.next());
            return cwd;
        }

        //**
        // @static exit
        // @args [code]
        // Exits the application with the given exit *code*, which defaults
        // to 0.
        private Object _exit(Joe joe, ArgQueue args) {
            Joe.arityRange(args, 0, 1, "Console.exit([code])");
            int code = 0;
            if (args.size() == 1) {
                code = joe.toInteger(args.next());
            }
            System.exit(code);
            return null; // Make the compiler happy
        }

        //**
        // @static mkdir
        // @args path
        // Creates the directory given its path, including any required
        // parent directories.  If the path is relative, it is treated as
        // relative to the current working directory.
        private Object _mkdir(Joe joe, ArgQueue args) {
            Joe.exactArity(args, 1, "Console.mkdir(path)");
            var path = toResolvedPath(joe, args.next());
            try {
                Files.createDirectories(path);
            } catch (IOException ex) {
                throw new JoeError(
                    "Could not create directory: " + ex.getMessage());
            }
            return null;
        }


        //**
        // @static pwd
        // @result Path
        // Returns the current working directory.
        private Object _pwd(Joe joe, ArgQueue args) {
            Joe.exactArity(args, 0, "Console.pwd()");
            return cwd;
        }

        //**
        // @static read
        // @result String
        // Returns a string from standard input, or null at EOF.
        private Object _read(Joe joe, ArgQueue args) {
            Joe.exactArity(args, 0, "Console.read()");
            if (inputReader == null) {
                inputReader = new BufferedReader(new InputStreamReader(System.in));
            }

            try {
                return inputReader.readLine();
            } catch (IOException ex) {
                throw new JoeError(
                    "Could not read from standard input: " + ex.getMessage());
            }
        }

        //**
        // @static writeFile
        // @args filePath, text
        // Writes the *text* as a file at the given *filePath*.
        private Object _writeFile(Joe joe, ArgQueue args) {
            Joe.exactArity(args, 2, "Console.writeFile(filePath, text)");
            var filePath = toResolvedPath(joe, args.next());
            var text = joe.toString(args.next());
            try {
                Files.writeString(filePath, text);
            } catch (IOException ex) {
                throw new JoeError("Could not write file: " + ex.getMessage());
            }
            return null;
        }

        //**
        // @static readFile
        // @args filePath
        // @result String
        // Reads the contents of the file at the given *filePath* and
        // returns it as a string.
        private Object _readFile(Joe joe, ArgQueue args) {
            Joe.exactArity(args, 1, "Console.readFile(filePath)");
            var filePath = toResolvedPath(joe, args.next());
            try {
                return Files.readString(filePath);
            } catch (IOException ex) {
                throw new JoeError("Could not write file: " + ex.getMessage());
            }
        }

        //**
        // @static readLines
        // @args filePath
        // @result List
        // Reads the contents of the file at the given *filePath* and
        // returns it as a list of line strings
        private Object _readLines(Joe joe, ArgQueue args) {
            Joe.exactArity(args, 1, "Console.readLines(filePath)");
            var filePath = toResolvedPath(joe, args.next());
            var result = new ListValue();
            try {
                result.add(Files.readAllLines(filePath));
                return result;
            } catch (IOException ex) {
                throw new JoeError("Could not write file: " + ex.getMessage());
            }
        }

    }


    //---------------------------------------------------------------------
    // The PathProxy

    private class PathProxy extends TypeProxy<Path> {
        //-----------------------------------------------------------------
        // Constructor

        //**
        // @type Path
        // A Java `Path`, i.e, the path to a file or a directory in the
        // host file system.
        private PathProxy() {
            super("Path");
            proxies(Path.class);

            initializer(this::_initializer);

            // Static methods
            staticMethod("compare", this::_compare);

            // Methods
            // endsWith
            method("getFileName",   this::_getFileName);
            method("getName",       this::_getName);
            method("getNameCount",  this::_getNameCount);
            method("getParent",     this::_getParent);
            method("isAbsolute",    this::_isAbsolute);
            method("normalize",     this::_normalize);
            // relativize
            method("resolve",       this::_resolve);
            // resolveSibling
            // startsWith
            // subpath
            method("toAbsolutePath", this::_toAbsolutePath);
            method("toString",       this::_toString);
        }

        //-----------------------------------------------------------------
        // Initializer

        //**
        // @init
        // @args first,[more...]
        // Creates a path from one or more string components.
        private Object _initializer(Joe joe, ArgQueue args) {
            Joe.minArity(args, 1, "Path(first,[more...])");
            var first = joe.toString(args.next());
            var more = new String[args.size() - 1];

            for (int i = 1; i < args.size(); i++) {
                more[i-1] = joe.toString(args.get(i));
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
        // @args a, b
        // @result Number
        // Returns -1, 0, or 1 as *a* is less than, equal to, or greater
        // than *b* when compared lexicographically.
        private Object _compare(Joe joe, ArgQueue args) {
            Joe.exactArity(args, 2, "Path.compare(a,b)");
            var a = toResolvedPath(joe, args.next());
            var b = toResolvedPath(joe, args.next());

            return a.compareTo(b);
        }

        //-----------------------------------------------------------------
        // Methods

        //**
        // @method getFileName
        // @result Path
        // Returns the final path component, i.e., the name of
        // the file or directory denoted by this path.
        private Object _getFileName(Path path, Joe joe, ArgQueue args) {
            Joe.exactArity(args, 0, "getFileName");
            return path.getFileName();
        }

        //**
        // @method getName
        // @args index
        // @result Path
        // Returns the path component at the given *index*.
        private Object _getName(Path path, Joe joe, ArgQueue args) {
            Joe.exactArity(args, 1, "getName(index)");
            var index = joe.toIndex(args.next(), path.getNameCount());
            return path.getName(index);
        }

        //**
        // @method getNameCount
        // @result Number
        // Returns the number of path components.
        private Object _getNameCount(Path path, Joe joe, ArgQueue args) {
            Joe.exactArity(args, 0, "getNameCount");
            return (double)path.getNameCount();
        }

        //**
        // @method getParent
        // @result Path
        // Returns the parent of this path, or null if it doesn't have one.
        private Object _getParent(Path path, Joe joe, ArgQueue args) {
            Joe.exactArity(args, 0, "getParent()");
            return path.getParent();
        }

        //**
        // @method isAbsolute
        // @result Boolean
        // Returns `true` if the path is an absolute path, and `false`
        // otherwise.
        private Object _isAbsolute(Path path, Joe joe, ArgQueue args) {
            Joe.exactArity(args, 0, "isAbsolute()");
            return path.isAbsolute();
        }

        //**
        // @method normalize
        // @result Path
        // Returns the path with redundant name elements eliminated.
        private Object _normalize(Path path, Joe joe, ArgQueue args) {
            Joe.exactArity(args, 0, "normalize()");
            return path.normalize();
        }

        //**
        // @method resolve
        // @args other
        // @result Path
        // Resolves the *other* path against this path, if *other* is
        // relative, constructs a path starting with this path and
        // continuing with the other.  If *other* is absolute, it is
        // returned unchanged.
        //
        // The *other* path may a `Path` or a `String`.
        private Object _resolve(Path path, Joe joe, ArgQueue args) {
            Joe.exactArity(args, 1, "resolve(other)");
            return path.resolve(toPath(joe, args.next()));
        }

        //**
        // @method toAbsolutePath
        // @result Path
        // Converts the path into an absolute path by resolving it against
        // the console's current working directory.
        private Object _toAbsolutePath(Path path, Joe joe, ArgQueue args) {
            Joe.exactArity(args, 0, "toAbsolutePath()");
            return toResolvedPath(joe, path); // Normalizes and absolutizes
        }

        //**
        // @method toString
        // @result String
        // Returns the path's string representation.
        private Object _toString(Path path, Joe joe, ArgQueue args) {
            Joe.exactArity(args, 0, "toString()");
            return path.toString();
        }
    }

    //---------------------------------------------------------------------
    // Helpers

    public Path toResolvedPath(Joe joe, Object arg) {
        var path = toPath(joe, arg);
        return cwd.resolve(path).toAbsolutePath().normalize();
    }

    public Path toPath(Joe joe, Object arg) {
        try {
            return switch(arg) {
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

    private String components(List<Object> values) {
        return values.stream()
            .map(s -> "\"" + s + "\"")
            .collect(Collectors.joining(", "));
    }
}
