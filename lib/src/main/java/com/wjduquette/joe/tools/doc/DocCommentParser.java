package com.wjduquette.joe.tools.doc;


import com.wjduquette.joe.Joe;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class DocCommentParser {
    //-------------------------------------------------------------------------
    // Static Constants

    private static final boolean TRACE = false;

    //-------------------------------------------------------------------------
    // Instance Variables

    private final DocumentationSet docSet;
    private transient List<Line> lines;
    private transient Line previous = null;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * This parser will parse file content into the documentation set.
     * @param docSet The documentation set
     */
    DocCommentParser(DocumentationSet docSet) {
        this.docSet = docSet;
    }

    //-------------------------------------------------------------------------
    // API

    void parse(Path docFile) {
        // FIRST, extract the lines from the file.
        lines = Extractor.process(docFile);
        if (lines.isEmpty()) {
            return;
        }

        // NEXT, begin parsing.  There might be lines before the first tag;
        // if any are not blank, that's an error.
        System.out.println("Reading: " + docFile);

        _parse();
    }

    //-------------------------------------------------------------------------
    // Parser

    private static final String PACKAGE = "@package";
    private static final String TITLE = "@title";
    private static final String FUNCTION = "@function";
    private static final String ARGS = "@args";
    private static final String RESULT = "@result";
    private static final String TYPE = "@type";
    private static final String EXTENDS = "@extends";
    private static final String CONSTANT = "@constant";
    private static final String STATIC = "@static";
    private static final String INIT = "@init";
    private static final String METHOD = "@method";

    private static final Set<String> PACKAGE_ENDERS = Set.of(
        PACKAGE
    );

    private static final Set<String> PACKAGE_CHILD_ENDERS = Set.of(
        PACKAGE, FUNCTION, TYPE
    );

    private static final Set<String> TYPE_CHILD_ENDERS = Set.of(
        PACKAGE, FUNCTION, TYPE, CONSTANT, STATIC, INIT, METHOD
    );

    private void _parse() {
        while(!atEnd()) {
            if (!advanceToTag(null)) return;

            var tag = advance().getTag();

            if (tag.name().equals(PACKAGE)) {
                _package(tag);
            } else {
                throw error(previous(), "Unexpected tag: " + tag);
            }
        }
    }

    private void trace(Object... args) {
        if (TRACE) {
            var text = Arrays.stream(args)
                .map(Object::toString)
                .collect(Collectors.joining(" "));

            if (atEnd()) {
                System.out.println(text + "\n    peek: [At end]");
            } else {
                System.out.println(text + "\n    peek: " + peek());
            }
        }
    }

    private void _package(Tag pkgTag) {
        trace("_package", pkgTag);
        if (!Joe.isPackageName(pkgTag.value())) {
            throw error(previous(), expected(pkgTag));
        }
        var pkg = findPackage(pkgTag.value());

        if (pkg == null) {
            pkg = new PackageEntry(pkgTag.value());
            docSet.packages().add(pkg);
            docSet.remember(pkg);
        }

        while (!atEnd()) {
            if (!advanceToTag(pkg)) break;

            var tag = peek().getTag();

            if (PACKAGE_ENDERS.contains(tag.name())) {
                break;
            }

            advance();
            switch (tag.name()) {
                case TITLE -> pkg.setTitle(tag.value());
                case FUNCTION -> _function(pkg, tag);
                case TYPE -> _type(pkg, tag);
                default -> throw error(previous(), "Unexpected tag: " + tag);
            }
        }
    }

    private PackageEntry findPackage(String name) {
        return docSet.packages().stream()
            .filter(p -> p.name().equals(name))
            .findFirst().orElse(null);
    }

    private void _function(PackageEntry pkg, Tag funcTag) {
        FunctionEntry func = new FunctionEntry(pkg, funcTag.value());
        remember(func);

        if (!Joe.isIdentifier(funcTag.value())) {
            throw error(previous(), expected(funcTag));
        }
        pkg.functions().add(func);

        while (!atEnd()) {
            if (!advanceToTag(func)) break;

            var tag = peek().getTag();

            if (PACKAGE_CHILD_ENDERS.contains(tag.name())) {
                break;
            }

            advance();
            switch (tag.name()) {
                case ARGS -> func.argSpecs().add(_argSpec(tag));
                case RESULT -> func.setResult(_result(tag));
                default -> throw error(previous(), "Unexpected tag: " + tag);
            }
        }
    }

    private void _type(PackageEntry pkg, Tag typeTag) {
        TypeEntry type = new TypeEntry(pkg, typeTag.value());
        remember(type);

        if (!Joe.isIdentifier(typeTag.value())) {
            throw error(previous(), expected(typeTag));
        }
        pkg.types().add(type);

        while (!atEnd()) {
            if (!advanceToTag(type)) break;

            var tag = peek().getTag();

            if (PACKAGE_CHILD_ENDERS.contains(tag.name())) {
                break;
            }

            advance();
            switch (tag.name()) {
                case EXTENDS -> type.setSupertypeName(_extends(tag));
                case CONSTANT -> _constant(type, tag);
                case STATIC -> _static(type, tag);
                case INIT -> _init(type, tag);
                case METHOD -> _method(type, tag);
                default -> throw error(previous(), "Unexpected tag: " + tag);
            }
        }
    }

    private String _extends(Tag tag) {
        var result = tag.value().trim();
        if (result.split("\\s").length > 1) {
            throw error(previous(), "Expected supertype name");
        }
        return result;
    }

    private void _constant(TypeEntry type, Tag constantTag) {
        ConstantEntry constant = new ConstantEntry(type, constantTag.value());
        remember(constant);

        if (!Joe.isIdentifier(constantTag.value())) {
            throw error(previous(), expected(constantTag));
        }
        type.constants().add(constant);

        // Constants have no tags, only content.
        if (!advanceToTag(constant)) return;

        var tag = peek().getTag();

        if (!TYPE_CHILD_ENDERS.contains(tag.name())) {
            advance();
            throw error(previous(), "Unexpected tag: " + tag);
        }
    }

    private void _static(TypeEntry type, Tag methodTag) {
        StaticMethodEntry method = new StaticMethodEntry(type, methodTag.value());
        remember(method);

        if (!Joe.isIdentifier(methodTag.value())) {
            throw error(previous(), expected(methodTag));
        }
        type.staticMethods().add(method);

        while (!atEnd()) {
            if (!advanceToTag(method)) break;

            var tag = peek().getTag();

            if (TYPE_CHILD_ENDERS.contains(tag.name())) {
                break;
            }

            advance();
            switch (tag.name()) {
                case ARGS -> method.argSpecs().add(_argSpec(tag));
                case RESULT -> method.setResult(_result(tag));
                default -> throw error(previous(), "Unexpected tag: " + tag);
            }
        }
    }

    private void _init(TypeEntry type, Tag initTag) {
        InitializerEntry init = new InitializerEntry(type);
        remember(init);

        if (!initTag.value().isBlank()) {
            throw error(previous(),
                initTag.name() + " has unexpected value: '" +
                initTag.value() + "'.");
        }
        type.setInitializer(init);

        while (!atEnd()) {
            if (!advanceToTag(init)) break;

            var tag = peek().getTag();

            if (TYPE_CHILD_ENDERS.contains(tag.name())) {
                break;
            }

            advance();
            if (tag.name().equals(ARGS)) {
                init.argSpecs().add(_argSpec(tag));
            } else {
                throw error(previous(), "Unexpected tag: " + tag);
            }
        }
    }

    private void _method(TypeEntry type, Tag methodTag) {
        MethodEntry method = new MethodEntry(type, methodTag.value());
        remember(method);

        if (!Joe.isIdentifier(methodTag.value())) {
            throw error(previous(), expected(methodTag));
        }
        type.methods().add(method);

        while (!atEnd()) {
            if (!advanceToTag(method)) break;

            var tag = peek().getTag();

            if (TYPE_CHILD_ENDERS.contains(tag.name())) {
                break;
            }

            advance();
            switch (tag.name()) {
                case ARGS -> method.argSpecs().add(_argSpec(tag));
                case RESULT -> method.setResult(_result(tag));
                default -> throw error(previous(), "Unexpected tag: " + tag);
            }
        }
    }

    private String _argSpec(Tag tag) {
        if (ArgSpec.isValid(tag.value())) {
            return tag.value().trim();
        } else {
            throw error(previous, expected(tag));
        }
    }

    private String _result(Tag tag) {
        var result = tag.value().trim();
        if (result.split("\\s").length > 1) {
            throw error(previous(), "Expected result type");
        }
        return result;
    }

    //-------------------------------------------------------------------------
    // Primitives

    private void remember(Entry entry) {
        if (docSet.lookup(entry.fullMnemonic()) != null) {
            throw error(previous(), "Duplicate entry.");
        } else {
            docSet.remember(entry);
        }
    }

    // ParseError is just a convenient way to break out of the parser.
    // We halt on the first error for now.
    static class ParseError extends RuntimeException { }

    // Is there any input left?
    private boolean atEnd() {
        return lines.isEmpty();
    }

    // Peeks at the current line.
    private Line peek() {
        return lines.getFirst();
    }

    // Advances to the next line, saving the current line as
    // previous().
    private Line advance() {
        previous = peek();
        return lines.removeFirst();
    }

    // Advances to the next tag, or the end of the input, adding
    // content lines to the entry.  Returns true if a tag is found,
    // and false otherwise.
    private boolean advanceToTag(Entry entry) {
        while (!atEnd() && !peek().isTagged()) {
            var line = advance();
            if (entry == null) {
                if (!line.isBlank()) {
                    throw error(line, "Unexpected comment text before first entry tag.");
                }
            } else {
                entry.content().add(line.text());
                trace("advanceToTag", previous());
            }
        }

        return !atEnd();
    }

    // Returns the previous line.
    private Line previous() {
        return previous;
    }

    private String expected(Tag tag) {
        return "Expected " + tag.name() + " value, got: '" +
            tag.value() + "'.";
    }

    private ParseError error(Line line, String message) {
        System.err.println("[line " + line.number() + "] " + message);
        System.err.println("  --> // " + line.text());
        return new ParseError();
    }

}
