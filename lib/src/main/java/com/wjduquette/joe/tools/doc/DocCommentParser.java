package com.wjduquette.joe.tools.doc;


import com.wjduquette.joe.Joe;

import java.nio.file.Path;
import java.util.ArrayList;
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
    private Path docFile = null;
    private final boolean verbose;
    private transient List<Line> lines;
    private transient Line previous = null;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * This parser will parse file content into the documentation set.
     * @param docSet The documentation set
     */
    DocCommentParser(DocumentationSet docSet, boolean verbose) {
        this.docSet = docSet;
        this.verbose = verbose;
    }

    //-------------------------------------------------------------------------
    // API

    void parse(Path docFile) {
        this.docFile = docFile;

        // FIRST, extract the lines from the file.
        lines = Extractor.process(docFile);
        if (lines.isEmpty()) {
            return;
        }

        // NEXT, begin parsing.  There might be lines before the first tag;
        // if any are not blank, that's an error.
        if (verbose) {
            System.out.println("Reading: " + docFile);
        }

        _parse();
    }

    //-------------------------------------------------------------------------
    // Parser

    private static final String MIXIN = "@mixin";
    private static final String PACKAGE = "@package";
    private static final String TITLE = "@title";
    private static final String PACKAGE_TOPIC = "@packageTopic";
    private static final String FUNCTION = "@function";
    private static final String ARGS = "@args";
    private static final String RESULT = "@result";
    private static final String TYPE = "@type";
    private static final String JAVA_TYPE = "%javaType";
    private static final String PROXY_TYPE = "%proxyType";
    private static final String CLASS = "@class";
    private static final String RECORD = "@record";
    private static final String ENUM = "@enum";
    private static final String ENUM_CONSTANTS = "%enumConstants";
    private static final String WIDGET = "@widget";
    private static final String SINGLETON = "@singleton";
    private static final String TYPE_TOPIC = "@typeTopic";
    private static final String INCLUDE_MIXIN = "@includeMixin";
    private static final String EXTENDS = "@extends";
    private static final String CONSTANT = "@constant";
    private static final String STATIC = "@static";
    private static final String INIT = "@init";
    private static final String FIELD = "@field";
    private static final String PROPERTY = "@property";
    private static final String METHOD = "@method";

    private static final Set<String> MIXIN_ENDERS = Set.of(
        PACKAGE, MIXIN
    );

    private static final Set<String> MIXIN_CHILD_ENDERS = Set.of(
        PACKAGE, MIXIN, CONSTANT, STATIC, METHOD, TYPE_TOPIC
    );

    private static final Set<String> PACKAGE_ENDERS = Set.of(
        PACKAGE, MIXIN
    );

    private static final Set<String> PACKAGE_CHILD_ENDERS = Set.of(
        PACKAGE, MIXIN, FUNCTION, TYPE, CLASS, RECORD, ENUM, WIDGET, SINGLETON,
        PACKAGE_TOPIC
    );

    private static final Set<String> TYPE_CHILD_ENDERS = Set.of(
        PACKAGE, MIXIN, FUNCTION, TYPE, CLASS, RECORD, ENUM, WIDGET, SINGLETON,
        PACKAGE_TOPIC, CONSTANT, STATIC, INIT, FIELD, PROPERTY, METHOD,
        TYPE_TOPIC
    );

    private void _parse() {
        while(!atEnd()) {
            if (!advanceToTag(null)) return;

            var tag = advance().getTag();

            if (tag.name().equals(MIXIN)) {
                _mixin(tag);
            } else if (tag.name().equals(PACKAGE)) {
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

    private void _mixin(Tag mixinTag) {
        // FIRST, create the mixin, validating its name and making sure
        // it's unique.
        trace("_mixin", mixinTag);
        MixinEntry mixin = new MixinEntry(mixinTag.value());

        if (!Joe.isIdentifier(mixinTag.value())) {
            throw error(previous(), expected(mixinTag));
        }

        if (docSet.mixins().containsKey(mixinTag.value())) {
            throw error(previous(), "Duplicate mixin.");
        }

        // NEXT, remember the mixin.
        docSet.mixins().put(mixin.name(), mixin);

        // NEXT, parse its content.
        while (!atEnd()) {
            if (!advanceToTag(mixin)) break;

            var tag = peek().getTag();

            if (MIXIN_ENDERS.contains(tag.name())) {
                break;
            }

            advance();
            switch (tag.name()) {
                case CONSTANT -> _constant(mixin, tag);
                case STATIC -> _static(mixin, tag);
                case METHOD -> _method(mixin, tag);
                case TYPE_TOPIC -> _typeTopic(mixin, tag);
                default -> throw error(previous(), "Unexpected tag: " + tag);
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
                case TYPE, CLASS, RECORD, ENUM, WIDGET, SINGLETON -> _type(pkg, tag);
                case PACKAGE_TOPIC -> _packageTopic(pkg, tag);
                default -> throw error(previous(), "Unexpected tag: " + tag);
            }
        }
    }


    private void _packageTopic(PackageEntry pkg, Tag topicTag) {
        trace("_packageTopic", pkg, topicTag);

        TopicEntry topic = new TopicEntry(pkg, topicTag.value());

        if (!Joe.isIdentifier(topicTag.value())) {
            throw error(previous(), expected(topicTag));
        }

        pkg.topics().add(topic);

        while (!atEnd()) {
            if (!advanceToTag(topic)) break;

            var tag = peek().getTag();

            if (PACKAGE_CHILD_ENDERS.contains(tag.name())) {
                break;
            }

            advance();
            if (tag.name().equals(TITLE)) {
                topic.setTitle(tag.value());
            } else {
                throw error(previous(), "Unexpected tag: " + tag);
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
        // FIRST, create the type, validating its name and making sure
        // it's unique.
        trace("_type", typeTag);
        var kind = switch (typeTag.name()) {
            case CLASS -> Kind.CLASS;
            case RECORD -> Kind.RECORD;
            case ENUM -> Kind.ENUM;
            case WIDGET -> Kind.WIDGET;
            case SINGLETON -> Kind.SINGLETON;
            default -> Kind.TYPE;
        };
        TypeEntry type = new TypeEntry(pkg, typeTag.value(), kind);
        remember(type);

        if (!Joe.isIdentifier(typeTag.value())) {
            throw error(previous(), expected(typeTag));
        }
        pkg.types().add(type);

        // NEXT, if it's an enum add the standard enum content.
        if (kind == Kind.ENUM) {
            addEnumContent(type);
        }

        // NEXT, parse its content.
        while (!atEnd()) {
            if (!advanceToTag(type)) break;

            var tag = peek().getTag();

            trace("_type ending?");

            if (PACKAGE_CHILD_ENDERS.contains(tag.name())) {
                break;
            }
            trace("_type parsing");

            advance();
            switch (tag.name()) {
                case JAVA_TYPE -> type.setJavaType(tag.value());
                case PROXY_TYPE -> type.setProxyType(tag.value());
                case ENUM_CONSTANTS -> {
                    if (kind != Kind.ENUM) {
                        throw error(previous(), "Unexpected tag: " + tag);
                    }
                    if (type.javaType() == null) {
                        throw error(previous(),
                            "%enumConstants requires %javaType, but %javaType is not set.");
                    }
                    addEnumConstants(type);
                }
                case EXTENDS -> type.setSupertypeName(_extends(tag));
                case INCLUDE_MIXIN -> {
                    type.mixins().add(_includeMixin(tag));
                    type.content().add("<mixin " + tag.value() + ">");
                }
                case CONSTANT -> _constant(type, tag);
                case STATIC -> _static(type, tag);
                case INIT -> _init(type, tag);
                case FIELD -> _field(type, tag);
                case PROPERTY -> _property(type, tag);
                case METHOD -> _method(type, tag);
                case TYPE_TOPIC -> _typeTopic(type, tag);
                default -> throw error(previous(), "Unexpected tag: " + tag);
            }
        }
    }

    // This is kind of ugly, but enums are a special case in almost
    // every way.
    private void addEnumContent(TypeEntry type) {
        // Add initializer`
        var init = new InitializerEntry(type);
        init.argSpecs().add("value");
        init.content().add("""
            Attempts to convert the *value* to an enum constant for this
            enum.  The value may be:
            
            - One of the enum's constants.
            - A string that matches the name of a constant,
              disregarding case.
            - A keyword whose name matches the name of a constant,
              disregarding case.
            """);
        type.setInitializer(init);

        // Add static method `values()`
        var values = new StaticMethodEntry(type, "values");
        values.setResult("List");
        values.content().add("""
            Returns a list of the enumerated type's values.
            """);
        type.staticMethods().add(values);


        // Add method `name()`

        var name = new MethodEntry(type, "name");
        name.setResult("String");
        name.content().add("""
            Returns the name of the enumerated constant.
            """);
        type.methods().add(name);

        var ordinal = new MethodEntry(type, "ordinal");
        ordinal.setResult("Number");
        ordinal.content().add("""
            Returns the index of the enumerated constant
            in the `values()` list.
            """);
        type.methods().add(ordinal);

        var toString = new MethodEntry(type, "toString");
        toString.setResult("String");
        toString.content().add("""
            Returns the name of the enumerated constant.
            """);
        type.methods().add(toString);
    }

    private void addEnumConstants(TypeEntry type) {
        var loader = ClassLoader.getSystemClassLoader();
        Class<?> cls;
        try {
            cls = loader.loadClass(type.javaType());
        } catch (Exception ex) {
            throw error(previous(), "Enum type could not be loaded: '" +
                type.javaType() + "'.");
        }

        if (cls.isEnum()) {
            var constants = new ArrayList<>(List.of(cls.getEnumConstants()));
            var first = constants.removeFirst();
            addConstant(type, first.toString(), "enum", "See Javadoc for details.");
            for (var c : constants) {
                addConstant(type, c.toString(), "enum", "-");
            }
        } else {
            throw error(previous(), "Not an enum: '" + type.javaType() + "'.");
        }
    }

    private void addConstant(
        TypeEntry type,
        String name,
        String valueType,
        String description
    ) {
        var constant = new ConstantEntry(type, name, valueType);
        constant.content().add(description);
        type.constants().add(constant);
        remember(constant);
    }

    private String _extends(Tag tag) {
        var result = tag.value().trim();
        if (result.split("\\s").length > 1) {
            throw error(previous(), "Expected supertype name");
        }
        return result;
    }

    private String _includeMixin(Tag tag) {
        var result = tag.value().trim();
        if (result.split("\\s").length > 1) {
            throw error(previous(), "Expected mixin name");
        }
        return result;
    }

    private void _constant(TypeOrMixin parent, Tag constantTag) {
        var isType = parent instanceof TypeEntry;
        var type = isType ? (TypeEntry)parent : null;
        var name = before(" ", constantTag.value());
        var valueType = after(" ", constantTag.value());

        if (!Joe.isIdentifier(name)) {
            throw error(previous(), expected(constantTag));
        }

        ConstantEntry constant = new ConstantEntry(type, name, valueType);
        if (isType) remember(constant);

        parent.constants().add(constant);

        // Constants have no tags, only content.
        if (!advanceToTag(constant)) return;

        var tag = peek().getTag();

        var enders = isType ? TYPE_CHILD_ENDERS : MIXIN_CHILD_ENDERS;

        if (!enders.contains(tag.name())) {
            advance();
            throw error(previous(), "Unexpected tag: " + tag);
        }
    }

    private void _static(TypeOrMixin parent, Tag methodTag) {
        var isType = parent instanceof TypeEntry;
        var type = isType ? (TypeEntry)parent : null;
        StaticMethodEntry method = new StaticMethodEntry(type, methodTag.value());

        if (!Joe.isIdentifier(methodTag.value())) {
            throw error(previous(), expected(methodTag));
        }

        if (isType) remember(method);
        parent.staticMethods().add(method);

        while (!atEnd()) {
            if (!advanceToTag(method)) break;

            var tag = peek().getTag();

            var enders = isType ? TYPE_CHILD_ENDERS : MIXIN_CHILD_ENDERS;
            if (enders.contains(tag.name())) {
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

    private void _field(TypeOrMixin parent, Tag fieldTag) {
        var isType = parent instanceof TypeEntry;
        var type = isType ? (TypeEntry)parent : null;
        var name = before(" ", fieldTag.value());
        var valueType = after(" ", fieldTag.value());

        if (!Joe.isIdentifier(name)) {
            throw error(previous(), expected(fieldTag));
        }

        FieldEntry field = new FieldEntry(type, name, valueType);

        if (isType) remember(field);

        parent.fields().add(field);

        // Fields have no tags, only content.
        if (!advanceToTag(field)) return;

        var tag = peek().getTag();

        var enders = isType ? TYPE_CHILD_ENDERS : MIXIN_CHILD_ENDERS;

        if (!enders.contains(tag.name())) {
            advance();
            throw error(previous(), "Unexpected tag: " + tag);
        }
    }

    private void _property(TypeEntry type, Tag propertyTag) {
        var name = before(" ", propertyTag.value());
        var valueType = after(" ", propertyTag.value());

        if (!Joe.isIdentifier(name)) {
            throw error(previous(), expected(propertyTag));
        }

        PropertyEntry property = new PropertyEntry(type, name, valueType);

        remember(property);

        type.properties().add(property);

        // Fields have no tags, only content.
        if (!advanceToTag(property)) return;

        var tag = peek().getTag();

        if (!TYPE_CHILD_ENDERS.contains(tag.name())) {
            advance();
            throw error(previous(), "Unexpected tag: " + tag);
        }
    }

    private void _method(TypeOrMixin parent, Tag methodTag) {
        var isType = parent instanceof TypeEntry;
        var type = isType ? (TypeEntry)parent : null;
        MethodEntry method = new MethodEntry(type, methodTag.value());

        if (!Joe.isIdentifier(methodTag.value())) {
            throw error(previous(), expected(methodTag));
        }
        if (isType) remember(method);

        parent.methods().add(method);

        while (!atEnd()) {
            if (!advanceToTag(method)) break;

            var tag = peek().getTag();

            var enders = isType ? TYPE_CHILD_ENDERS : MIXIN_CHILD_ENDERS;
            if (enders.contains(tag.name())) {
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

    private void _typeTopic(TypeOrMixin parent, Tag topicTag) {
        var isType = parent instanceof TypeEntry;
        var type = isType ? (TypeEntry)parent : null;
        trace("_typeTopic", type, topicTag);

        if (!Joe.isIdentifier(topicTag.value())) {
            throw error(previous(), expected(topicTag));
        }

        TopicEntry topic = new TopicEntry(type, topicTag.value());

        parent.topics().add(topic);

        while (!atEnd()) {
            if (!advanceToTag(topic)) break;

            var tag = peek().getTag();

            var enders = isType ? TYPE_CHILD_ENDERS : MIXIN_CHILD_ENDERS;
            if (enders.contains(tag.name())) {
                break;
            }

            advance();
            if (tag.name().equals(TITLE)) {
                topic.setTitle(tag.value());
            } else {
                throw error(previous(), "Unexpected tag: " + tag);
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

    // Gets the text preceding the separator, or the whole string if
    // none.
    @SuppressWarnings("SameParameterValue")
    private String before(String separator, String string) {
        var ndx = string.indexOf(separator);
        return ndx == -1
            ? string
            : string.substring(0, ndx);
    }

    // Gets the text following the separator, or null if none.
    @SuppressWarnings("SameParameterValue")
    private String after(String separator, String string) {
        var ndx = string.indexOf(separator);
        return ndx == -1
            ? null
            : string.substring(ndx + separator.length());
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
        if (!verbose) {
            System.err.println("*** Error in " + docFile);
        }
        System.err.println("[line " + line.number() + "] " + message);
        System.err.println("  --> // " + line.text());
        return new ParseError();
    }

}
