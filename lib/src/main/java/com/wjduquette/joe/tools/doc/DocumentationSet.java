package com.wjduquette.joe.tools.doc;

import java.util.ArrayList;
import java.util.List;

/**
 * The documentation entries scanned from the project source.
 */
class DocumentationSet {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The packages in the documentation set.
    private final List<PackageEntry> packages = new ArrayList<>();

    //-------------------------------------------------------------------------
    // Constructor

    public DocumentationSet() {
        // Nothing to do
    }

    //-------------------------------------------------------------------------
    // Accessors

    public List<PackageEntry> packages() {
        return packages;
    }

    //-------------------------------------------------------------------------
    // Operations

    public void dump() {
        System.out.println("Documentation Set:");

        for (var pkg : packages) {
            System.out.println("  " + pkg);
            dumpContent(pkg, "    ");
            for (var fn : pkg.functions()) {
                System.out.println("    " + fn);
                dumpContent(fn, "      ");
            }
            for (var type : pkg.types()) {
                System.out.println("    " + type);
                dumpContent(type, "      ");

                for (var con : type.constants()) {
                    System.out.println("      " + con);
                    dumpContent(con, "        ");
                }
                for (var fn : type.staticMethods()) {
                    System.out.println("      " + fn);
                    dumpContent(fn, "        ");
                }
                if (type.initializer() != null) {
                    System.out.println("      " + type.initializer());
                    dumpContent(type.initializer(), "        ");
                }
                for (var fn : type.methods()) {
                    System.out.println("      " + fn);
                    dumpContent(fn, "        ");
                }
            }
        }
    }

    private void dumpContent(Entry entry, String leader) {
        for (var line : entry.content()) {
            System.out.println(leader + line);
        }
    }
}
