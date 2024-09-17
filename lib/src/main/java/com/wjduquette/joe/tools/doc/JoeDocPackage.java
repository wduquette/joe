package com.wjduquette.joe.tools.doc;

import com.wjduquette.joe.*;

import java.nio.file.Path;

public class JoeDocPackage extends Library {
    private final DocConfig config;

    //**
    // @package joe.doc
    // @title JoeDoc Configuration API
    // The `joe.doc` package contains the Joe API used in
    // the `joe doc` configuration file, `doc_config.joe`.
    public JoeDocPackage(DocConfig config) {
        super();
        this.config = config;
        type(new DocConfigProxy());
    }

    private class DocConfigProxy extends TypeProxy<Void> {
        //---------------------------------------------------------------------
        // Instance Variables


        //---------------------------------------------------------------------
        // Constructor

        //**
        // @type DocConfig
        // The `DocConfig` static type owns the static methods used to
        // configure the `joe doc` tool.
        public DocConfigProxy() {
            super("DocConfig");
            staticType();

            staticMethod("inputFile",    this::_inputFile);
            staticMethod("inputFolder",  this::_inputFolder);
            staticMethod("outputFolder", this::_outputFolder);
        }

        //**
        // @static inputFile
        // @args filename,...
        // @result this
        // Adds the paths of one or more files to scan for JoeDoc
        // comments.  File paths are relative to the location of the
        // `doc_config.joe` file.
        private Object _inputFile(Joe joe, ArgQueue args) {
            Joe.minArity(args, 1, "inputFile(filename, ...)");

            for (var name : args.asList()) {
                config.inputFiles().add(Path.of(joe.toString(name)));
            }
            return this;
        }

        //**
        // @static inputFolder
        // @args folder,...
        // @result this
        // Adds the paths of one or more folders to scan for files
        // containing JoeDoc comments. `joe doc` will scan all
        // `.java` and `.joe` files in the folders, recursing down
        // into subfolders.
        //
        // Folder paths are relative to the location of the
        // `doc_config.joe` file.
        private Object _inputFolder(Joe joe, ArgQueue args) {
            Joe.minArity(args, 1, "inputFolder(folder, ...)");

            for (var name : args.asList()) {
                config.inputFolders().add(Path.of(joe.toString(name)));
            }
            return this;
        }

        //**
        // @static outputFolder
        // @args folder
        // @result this
        // Sets the name of the folder to receive the generated outputs.
        // If unset, defaults to the folder containing the `doc_config.joe`
        // file.
        private Object _outputFolder(Joe joe, ArgQueue args) {
            Joe.exactArity(args, 1, "outputFolder(folder)");

            config.setOutputFolder(Path.of(joe.toString(args.next())));
            return this;
        }
    }
}
