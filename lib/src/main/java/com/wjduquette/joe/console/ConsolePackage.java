package com.wjduquette.joe.console;

import com.wjduquette.joe.*;
import com.wjduquette.joe.types.ListValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConsolePackage extends JoePackage {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The command line arguments forwarded to the console.
    private final ListValue argList = new ListValue();

    // The current working directory
    private Path cwd = Path.of(".").toAbsolutePath().normalize();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.console
    // This package contains an API for use by simple command-line tools.
    // It is provided primarily as an example, and in an effort to make
    // `joe run` and `joe repl` somewhat useful.
    public ConsolePackage() {
        super("joe.console");

        type(new ConsoleProxy());
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
            staticMethod("pwd",       this::_pwd);
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
            cwd = toPath(joe, args.next());
            return cwd;
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
        // @static writeFile
        // @args filePath, text
        // Writes the *text* to the given *filePath*.
        private Object _writeFile(Joe joe, ArgQueue args) {
            Joe.exactArity(args, 2, "Console.writeFile(filePath, text)");
            var filePath = toPath(joe, args.next());
            var text = joe.toString(args.next());
            try {
                Files.writeString(filePath, text);
            } catch (IOException ex) {
                throw new JoeError("Could not write file: " + ex.getMessage());
            }
            return null;
        }

        //---------------------------------------------------------------------
        // Helpers

        private Path toPath(Joe joe, Object arg) {
            try {
                Path path = switch(arg) {
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
                return cwd.resolve(path).toAbsolutePath().normalize();
            } catch (IllegalArgumentException ex) {
                throw new JoeError(ex.getMessage());
            }
        }
    }
}
