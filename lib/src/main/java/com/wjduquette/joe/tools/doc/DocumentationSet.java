package com.wjduquette.joe.tools.doc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The documentation entries scanned from the project source.
 */
class DocumentationSet {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The packages in the documentation set.
    private final List<PackageEntry> packages = new ArrayList<>();

    // A look-up table, full mnemonic to entry.
    private final Map<String,Entry> lookupTable = new HashMap<>();

    // The mixins defined by the documentation set.
    private final Map<String,MixinEntry> mixins = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    public DocumentationSet() {
        // Nothing to do
    }

    //-------------------------------------------------------------------------
    // Accessors

    public Map<String,MixinEntry> mixins() {
        return mixins;
    }

    public List<PackageEntry> packages() {
        return packages;
    }

    /**
     * Adds the entry to the documentation set's mnemonic lookup table.
     * @param entry The entry
     */
    public void remember(Entry entry) {
        lookupTable.put(entry.fullMnemonic(), entry);
    }

    public Entry lookup(String fullMnemonic) {
        return lookupTable.get(fullMnemonic);
    }

    @SuppressWarnings("unused")
    public List<Entry> entries() {
        var result = new ArrayList<Entry>();

        for (var pkg : packages) {
            result.add(pkg);
            result.addAll(pkg.entries());
        }

        return result;
    }

    //-------------------------------------------------------------------------
    // Operations

    @SuppressWarnings("unused")
    public void dump() {
        System.out.println("Documentation Set:");

        for (var pkg : packages) {
            System.out.println("  " + pkg);
            if (pkg.title() != null) {
                System.out.println("    [" + pkg.title() + "]");
            }
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
