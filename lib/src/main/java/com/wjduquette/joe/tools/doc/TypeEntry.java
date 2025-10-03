package com.wjduquette.joe.tools.doc;

import java.util.ArrayList;
import java.util.List;

class TypeEntry extends Entry implements TypeOrMixin {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The type's name.
    private final String name;

    // The type's kind.
    private final Kind kind;

    private final List<ConstantEntry> constants = new ArrayList<>();
    private final List<StaticMethodEntry> staticMethods = new ArrayList<>();
    private String supertypeName = null;
    private InitializerEntry initializer = null;
    private final List<FieldEntry> fields = new ArrayList<>();
    private final List<MethodEntry> methods = new ArrayList<>();
    private final List<TopicEntry> topics = new ArrayList<>();
    private final List<String> mixins = new ArrayList<>();



    //-------------------------------------------------------------------------
    // Constructor

    TypeEntry(PackageEntry pkg, String name, Kind kind) {
        super(pkg);
        this.name = name;
        this.kind = kind;
    }

    //-------------------------------------------------------------------------
    // Accessors

    public List<ConstantEntry>     constants()     { return constants; }
    public List<StaticMethodEntry> staticMethods() { return staticMethods; }
    public String                  supertypeName() { return supertypeName; }
    public InitializerEntry        initializer()   { return initializer; }
    public List<FieldEntry>        fields()        { return fields; }
    public List<MethodEntry>       methods()       { return methods; }
    public List<TopicEntry>        topics()        { return topics; }
    public List<String>            mixins()        { return mixins; }

    public String  name()          { return name; }
    public Kind    kind()          { return kind; }
    public String  prefix()        { return name; }
    public String  fullMnemonic()  { return pkg().name() + "." + name; }
    public String  shortMnemonic() { return name; }
    public String  valuePrefix()   { return downCase(name); }
    public String  filename()      { return "type." + pkg().name() + "." + name + ".md"; }

    public void setSupertypeName(String supertypeName) {
        this.supertypeName = supertypeName;
    }

    public void setInitializer(InitializerEntry initializer) {
        this.initializer = initializer;
    }

    //-------------------------------------------------------------------------
    // Mixin Handling

    void includeMixin(MixinEntry mixin) {
        // Include Content
        if (!mixin.content().isEmpty()) {
            content().add("");
            var newContent = new ArrayList<String>();
            copyContent(mixin.content(), newContent);

            // Insert the new content where the @includeMixin tag was
            // found.
            var ndx = content().indexOf("<mixin " + mixin.name() + ">");
            if (ndx == -1) {
                throw new IllegalStateException(
                    "Type has mixin with no <mixin> tag in type content!");
            }

            content().remove(ndx);
            content().addAll(ndx, newContent);
        }

        // Include Constants
        for (var c : mixin.constants()) {
            var constant = new ConstantEntry(this, c.name(), c.valueType());
            copyContent(c.content(), constant.content());
            constants.add(constant);
        }

        // Include StaticMethods
        for (var m : mixin.staticMethods()) {
            var method = new StaticMethodEntry(this, m.name());
            copyContent(m.argSpecs(), method.argSpecs());
            method.setResult(subst(m.result()));
            copyContent(m.content(), method.content());
            staticMethods.add(method);
        }

        // Include Methods
        for (var m : mixin.methods()) {
            var method = new MethodEntry(this, m.name());
            copyContent(m.argSpecs(), method.argSpecs());
            method.setResult(subst(m.result()));
            copyContent(m.content(), method.content());
            methods.add(method);
        }

        // Include Topics
        for (var t : mixin.topics()) {
            var topic = new TopicEntry(this, t.name());
            topic.setTitle(t.title());
            copyContent(t.content(), topic.content());
            topics.add(topic);
        }
    }

    private void copyContent(List<String> from, List<String> to) {
        from.forEach(txt -> to.add(subst(txt)));
    }

    private String subst(String input) {
        return input.replace("<type>", name);
    }

    //-------------------------------------------------------------------------
    // Utilities

    @Override
    public String toString() {
        return "Type[" + name + "]";
    }

    private String downCase(String name) {
        if (!name.isEmpty()) {
            var ch = name.charAt(0);
            return Character.toLowerCase(ch) + name.substring(1);
        } else {
            return "";
        }
    }
}
