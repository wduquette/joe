package com.wjduquette.joe.tools.doc;

import java.util.List;

interface TypeOrMixin {
    List<ConstantEntry>     constants();
    List<StaticMethodEntry> staticMethods();
    List<FieldEntry>        fields();
    List<MethodEntry>       methods();
    List<TopicEntry>        topics();
}
