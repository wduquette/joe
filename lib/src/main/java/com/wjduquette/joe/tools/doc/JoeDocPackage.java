package com.wjduquette.joe.tools.doc;

import com.wjduquette.joe.*;

import java.nio.file.Path;

class JoeDocPackage extends NativePackage {
    private final DocConfig config;

    //**
    // @package joe.doc
    // %title JoeDoc Configuration API
    // The `joe.doc` package contains the Joe API used in
    // the `joe doc` configuration file, `doc_config.joe`.
    public JoeDocPackage(DocConfig config) {
        super("joe.doc");
        this.config = config;

        function("inputFile",     this::_inputFile);
        function("inputFolder",   this::_inputFolder);
        function("javadocRoot",   this::_javadocRoot);
        function("outputFolder",  this::_outputFolder);
    }

    //**
    // @function inputFile
    // %args filename,...
    // Adds the paths of one or more files to scan for JoeDoc
    // comments.  File paths are relative to the location of the
    // `doc_config.joe` file.
    private Object _inputFile(Joe joe, Args args) {
        args.minArity(1, "inputFile(filename, ...)");

        for (var name : args.asList()) {
            config.inputFiles().add(Path.of(joe.toString(name)));
        }
        return null;
    }

    //**
    // @function inputFolder
    // %args folder,...
    // Adds the paths of one or more folders to scan for files
    // containing JoeDoc comments. `joe doc` will scan all
    // `.java` and `.joe` files in the folders, recursing down
    // into subfolders.
    //
    // Folder paths are relative to the location of the
    // `doc_config.joe` file.
    private Object _inputFolder(Joe joe, Args args) {
        args.minArity(1, "inputFolder(folder, ...)");

        for (var name : args.asList()) {
            config.inputFolders().add(Path.of(joe.toString(name)));
        }
        return null;
    }

    //**
    // @function javadocRoot
    // %args pkg, root
    // Specifies the Javadoc URL *root* for the named package.
    // The *root* may be an HTTP URL, or a file path relative
    // to the mdBook `docs/` folder.
    //
    // ```joe
    // var jdk = "https://docs.oracle.com/en/java/javase/21/docs/api/";
    // javadocPrefix("java.lang", jdk + "java.base/");
    // javadocPrefix("java.util", jdk + "java.base/");
    //
    // // Project javadoc is copied to docs/javadoc/
    // javadocPrefix("my.package", "javadoc/");
    // ```
    //
    // Given this prefix, `joe doc` can convert class names
    // in the *pkg* to JavaDoc links.
    private Object _javadocRoot(Joe joe, Args args) {
        args.minArity(1, "javadocRoot(pkg, prefix)");
        config.javadocRoots().put(
            joe.toString(args.next()),
            joe.toString(args.next())
        );
        return null;
    }

    //**
    // @function outputFolder
    // %args folder
    // Sets the name of the folder to receive the generated outputs.
    // If unset, defaults to the folder containing the `doc_config.joe`
    // file.
    private Object _outputFolder(Joe joe, Args args) {
        args.exactArity(1, "outputFolder(folder)");

        config.setOutputFolder(Path.of(joe.toString(args.next())));
        return null;
    }
}

