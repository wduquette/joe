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
            for (var fn : pkg.functions()) {
                System.out.println("    " + fn);
            }
            for (var type : pkg.types()) {
                System.out.println("    " + type);

                for (var con : type.constants()) {
                    System.out.println("      " + con);
                }
                for (var fn : type.staticMethods()) {
                    System.out.println("      " + fn);
                }
                if (type.initializer() != null) {
                    System.out.println("      " + type.initializer());
                }
                for (var fn : type.methods()) {
                    System.out.println("      " + fn);
                }
            }
        }
    }
}
