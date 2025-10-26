package com.wjduquette.joe.tools.doc;

import com.wjduquette.joe.*;

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

        // Configuration functions
        function("codeFiles",       this::_codeFiles);
        function("codeFolders",     this::_codeFolders);
        function("docInputFolder",  this::docInputFolder);
        function("docOutputFolder", this::docOutputFolder);
        function("javadocRoot",     this::_javadocRoot);
        function("libOutputFolder", this::libOutputFolder);
        function("siteFolder",      this::_siteFolder);

    }

    //**
    // @function codeFiles
    // %args filename,...
    // Adds the paths of one or more Java/Joe source files to scan for JoeDoc
    // comments.  File paths are relative to the location of the
    // `doc_config.joe` file.
    private Object _codeFiles(Joe joe, Args args) {
        args.minArity(1, "codeFiles(filename, ...)");

        for (var name : args.asList()) {
            config.codeFiles().add(config.resolve(joe.toString(name)));
        }
        return null;
    }

    //**
    // @function codeFolders
    // %args folder,...
    // Adds the paths of one or more folders to scan for Java/Joe files
    // containing JoeDoc comments. `joe doc` will scan all
    // `.java` and `.joe` files in the folders, recursing down
    // into subfolders.
    //
    // Folder paths are relative to the location of the
    // `doc_config.joe` file.
    private Object _codeFolders(Joe joe, Args args) {
        args.minArity(1, "codeFolders(folder, ...)");

        for (var name : args.asList()) {
            config.codeFolders().add(config.resolve(joe.toString(name)));
        }
        return null;
    }

    //**
    // @function docInputFolder
    // %args folder
    // Sets the name of the folder in which to find input files for expansion.
    // `joe doc` will copy all files in the folder to the doc output folder,
    // recursing down into subfolders, and expanding all `.md` files.
    //
    // Output files will have the same name as their input files, and will
    // appear in an identical folder tree.
    //
    // The folder path is relative to the location of the
    // `doc_config.joe` file.
    private Object docInputFolder(Joe joe, Args args) {
        args.exactArity(1, "docInputFolder(folder)");

        config.setDocInputFolder(config.resolve(joe.toString(args.next())));
        return null;
    }

    //**
    // @function docOutputFolder
    // %args folder
    // Sets the name of the folder to receive the (possibly expanded) files
    // from the doc input folder.
    //
    // The folder path is relative to the location of the
    // `doc_config.joe` file.
    private Object docOutputFolder(Joe joe, Args args) {
        args.exactArity(1, "docOutputFolder(folder)");

        config.setDocOutputFolder(config.resolve(joe.toString(args.next())));
        return null;
    }

    //**
    // @function javadocRoot
    // %args pkg, root
    // Specifies the Javadoc URL *root* for the named package.
    // The *root* may be an HTTP URL, or a file path relative
    // to the configured `siteFolder`.
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
    // @function libOutputFolder
    // %args folder
    // Sets the name of the folder to receive the generated outputs.
    // If unset, defaults to the folder containing the `doc_config.joe`
    // file.
    private Object libOutputFolder(Joe joe, Args args) {
        args.exactArity(1, "libOutputFolder(folder)");

        config.setLibOutputFolder(config.resolve(joe.toString(args.next())));
        return null;
    }

    //**
    // @function siteFolder
    // %args folder
    // Sets the name of the folder that will contain the final output of
    // the static site generator.
    //
    // The folder path is relative to the location of the
    // `doc_config.joe` file.
    private Object _siteFolder(Joe joe, Args args) {
        args.exactArity(1, "siteFolder(folder)");

        config.setSiteFolder(config.resolve(joe.toString(args.next())));
        return null;
    }


}

