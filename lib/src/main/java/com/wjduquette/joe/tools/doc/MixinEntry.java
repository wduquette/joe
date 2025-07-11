package com.wjduquette.joe.tools.doc;

import java.util.ArrayList;
import java.util.List;

/**
 * A `@mixin` is a type-like template for content to be mixed into a type's
 * documentation.  It may include constants, static methods, instance methods,
 * and topics, along with its own content.  It may not extend another type
 * or define an initializer.
 */
class MixinEntry extends Entry implements TypeOrMixin {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The mixin's name.
    private final String name;

    private final List<ConstantEntry> constants = new ArrayList<>();
    private final List<StaticMethodEntry> staticMethods = new ArrayList<>();
    private final List<FieldEntry> fields = new ArrayList<>();
    private final List<MethodEntry> methods = new ArrayList<>();
    private final List<TopicEntry> topics = new ArrayList<>();


    //-------------------------------------------------------------------------
    // Constructor

    MixinEntry(String name) {
        super();
        this.name = name;
    }

    //-------------------------------------------------------------------------
    // Accessors

    public String                  name()          { return name; }
    public List<ConstantEntry>     constants()     { return constants; }
    public List<StaticMethodEntry> staticMethods() { return staticMethods; }
    public List<FieldEntry>        fields()        { return fields; }
    public List<MethodEntry>       methods()       { return methods; }
    public List<TopicEntry>        topics()        { return topics; }

    @Override
    public String fullMnemonic() {
        throw new UnsupportedOperationException("Mixins do not have mnemonics.");
    }

    @Override
    public String shortMnemonic() {
        throw new UnsupportedOperationException("Mixins do not have mnemonics.");
    }

    @Override
    public String filename() {
        throw new UnsupportedOperationException("Mixins do not have output files.");
    }

    @Override
    public String toString() {
        return "Mixin[" + name + "]";
    }
}
