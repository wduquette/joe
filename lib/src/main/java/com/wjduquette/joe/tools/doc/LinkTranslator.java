package com.wjduquette.joe.tools.doc;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Translates JoeDoc links into Markdown links.
 */
class LinkTranslator {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The configuration
    private final DocConfig config;

    // The current DocumentationSet
    private final DocumentationSet docSet;

    // The warning handler
    private final Consumer<String> onWarning;

    // The table of short mnemonics for the current package.
    private PackageEntry currentPackage = null;
    private final Map<String,Entry> pkgTable = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a link translator.  If the pkg is non-null, it will translate
     * short links in the context of that package.
     * @param config The configuration
     * @param docSet The documentation set
     * @param onWarning The warning handler.
     */
    public LinkTranslator(
        DocConfig config,
        DocumentationSet docSet,
        Consumer<String> onWarning
    ) {
        this.config = config;
        this.docSet = docSet;
        this.onWarning = onWarning;
    }

    //-------------------------------------------------------------------------
    // Configuration

    @SuppressWarnings("unused")
    public PackageEntry getCurrentPackage() {
        return currentPackage;
    }

    public void setCurrentPackage(PackageEntry pkg) {
        this.currentPackage = pkg;
        pkgTable.clear();
        if (pkg != null) {
            pkg.entries().forEach(e -> pkgTable.put(e.shortMnemonic(), e));
        }
    }

    //-------------------------------------------------------------------------
    // Queries

    /**
     * Looks up a mnemonic and returns the entry, taking the current package
     * into account.  Returns null if no entry is found.
     * @param mnemonic The mnemonic
     * @return The entry or null
     */
    public Entry lookup(String mnemonic) {
        var entry = docSet.lookup(mnemonic);
        if (entry == null) entry = pkgTable.get(mnemonic);
        return entry;
    }

    /**
     * Given a type name, either fully qualified or short in the current
     * package, returns the type entry.
     * @param name The name
     * @return The type entry or null
     */
    public TypeEntry lookupType(String name) {
        if (name == null) {
            return null;
        }

        var entry = docSet.lookup(name);

        if (entry == null) {
            entry = pkgTable.get(name);
        }

        if (entry instanceof TypeEntry type) {
            return type;
        } else {
            return null;
        }
    }

    /**
     * Given a type name, returns a Markdown link to the type's docs
     * or just the name if not found.
     * @param name The name
     * @return the link or name
     */
    public String typeLinkOrName(String name) {
        var type = lookupType(name);

        if (type != null) {
            return "[" + type.name() + "](" + type.filename() + ")";
        } else {
            return name;
        }
    }

    //-------------------------------------------------------------------------
    // Link Expansion

    /**
     * Expands JoeDoc links in the given line, taking the current package
     * into account.  Links preceded with "\" are left intact
     * @param line The input line
     * @return The translated line
     */
    public String translateLinks(String line) {
        var buff = new StringBuilder();
        var head = line;
        int ndx;

        while ((ndx = head.indexOf("[[")) != -1) {
            buff.append(head, 0, ndx);
            head = head.substring(ndx);

            // NEXT, for incomplete link
            var tail = head.indexOf("]]");
            if (tail == -1) {
                buff.append(head);
                return buff.toString();
            }

            // NEXT, extract the entire mnemonic.
            var linkSpec = head.substring(2, tail);
            head = head.substring(tail + 2);

            // NEXT, look for link text.
            var tokens = linkSpec.split("\\|");
            var mnemonic = tokens[0];

            // NEXT, is this a javadoc link?
            if (mnemonic.startsWith("java:")) {
                var className = mnemonic.substring(5);
                var url = javadocUrl(className);

                var linkText = tokens.length > 1
                    ? tokens[1] : className;

                if (url == null) {
                    warn("Unknown Java package in '[[" + linkSpec + "]]");
                    buff.append(linkText);
                } else {
                    buff.append(link(linkText, url));
                }
            } else {
                // FIRST, get the entity.  If not found, issue a warning and
                // leave a placeholder.
                var entry = lookup(mnemonic);
                if (entry == null) {
                    // Leave it in place; it's incorrect.
                    warn("Unknown mnemonic in link: [[" + mnemonic + "]]");
                    buff.append("[[").append(linkSpec).append("]]");
                } else {
                    var linkText = tokens.length > 1
                        ? tokens[1]
                        : inlineLinkText(entry);
                    buff.append(link(linkText, entry.url()));
                }
            }
        }

        buff.append(head);
        return buff.toString();
    }


    /**
     * Given the fully-qualified name of a Java class, return the
     * Markdown link to its Javadoc page, or the monospace class name
     * if its Javadoc root is unknown.
     * @param className The Java class name.
     * @return The URL or null.
     */
    public String javadocLink(String className) {
        var linkText = "`" + className + "`";
        var url = javadocUrl(className);
        return url == null
            ? linkText
            : link(linkText, url);
    }

    /**
     * Given the fully-qualified name of a Java class, return the URL to its
     * Javadoc page, or null if its Javadoc root is unknown.
     * @param className The Java class name.
     * @return The URL or null.
     */
    public String javadocUrl(String className) {
        var pkg = getPackageName(className);
        var root = config.javadocRoots().get(pkg);
        if (root == null) return null;

        var separator = root.endsWith("/") ? "" : "/";
        return root + separator + className.replace(".", "/") + ".html";
    }

    // Given a qualified class name, get the package name.
    private String getPackageName(String className) {
        var ndx = className.lastIndexOf(".");
        return className.substring(0, ndx);
    }

    // Given an entry, get the link text used for in-line links.
    private String inlineLinkText(Entry entry) {
        return switch (entry) {
            case PackageEntry pkg -> mono(pkg.name());
            case FunctionEntry fn -> mono(fn.name() + "()");
            case TypeEntry t -> mono(t.name());
            case ConstantEntry c
                -> mono(c.type().name() + "." + c.name());
            case StaticMethodEntry m
                -> mono(m.type().name() + "." + m.name() + "()");
            case InitializerEntry fn
                -> mono(fn.name() + "()");
            case FieldEntry f
                -> mono(f.prefix() + "." + f.name());
            case PropertyEntry p
                -> mono("#" + p.name());
            case MethodEntry m
                -> mono(m.name() + "()");
            case TopicEntry t
                -> t.title();
            default -> throw new IllegalArgumentException("Unknown entry type!");
        };
    }

    private String mono(String text) {
        return "`" + text + "`";
    }

    private String link(String text, String url) {
        return "[" + text + "](" + url + ")";
    }

    private void warn(String message) {
        onWarning.accept(message);
    }
}
