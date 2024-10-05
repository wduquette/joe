package com.wjduquette.joe.console;

import com.wjduquette.joe.*;
import com.wjduquette.joe.types.ListValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

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
            cwd = toPath(joe, args.next());
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
            var path = toPath(joe, args.next());
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
            var filePath = toPath(joe, args.next());
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
            var filePath = toPath(joe, args.next());
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
            var filePath = toPath(joe, args.next());
            var result = new ListValue();
            try {
                result.add(Files.readAllLines(filePath));
                return result;
            } catch (IOException ex) {
                throw new JoeError("Could not write file: " + ex.getMessage());
            }
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
