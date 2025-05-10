package com.wjduquette.joe.patterns;

import com.wjduquette.joe.*;
import com.wjduquette.joe.types.RecordType;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.wjduquette.joe.checker.Checker.check;

public class MatcherTest extends Ted {
    private final Joe joe = new Joe();
    private List<Object> constants;
    private final Map<String,Object> bindings = new LinkedHashMap<>();

    @Before public void setup() {
        constants = new ArrayList<>();
        bindings.clear();
        joe.installType(new PairType());
    }

    private boolean bind(Pattern pattern, Object value) {
        return Matcher.bind(joe, pattern, value, constants::get, bindings::put);
    }

    @Test
    public void testConstant_bad() {
        test("testConstant_bad");

        constants = List.of("abc");
        var pattern = new Pattern.Constant(0);
        var value = "xyz";

        check(bind(pattern, value)).eq(false);
    }

    @Test
    public void testConstant_good() {
        test("testConstant_good");

        constants = List.of("abc");
        var pattern = new Pattern.Constant(0);
        var value = "abc";

        check(bind(pattern, value)).eq(true);
        check(bindings.isEmpty()).eq(true);
    }

    @Test
    public void testWildcard() {
        test("testWildcard");

        var pattern = new Pattern.Wildcard("_");
        var value = "abc";

        check(bind(pattern, value)).eq(true);
        check(bindings.isEmpty()).eq(true);
    }

    @Test
    public void testValueBinding() {
        test("testValueBinding");

        var pattern = new Pattern.ValueBinding("x");
        var value = "abc";

        check(bind(pattern, value)).eq(true);
        check(bindings.get("x")).eq("abc");
    }

    @Test
    public void testPatternBinding_bad() {
        test("testPatternBinding_bad");

        constants = List.of("abc");
        var pattern = new Pattern.PatternBinding("x",
            new Pattern.Constant(0));
        var value = "xyz";

        check(bind(pattern, value)).eq(false);
    }

    @Test
    public void testPatternBinding_good() {
        test("testPatternBinding_good");

        constants = List.of("abc");
        var pattern = new Pattern.PatternBinding("x",
            new Pattern.Constant(0));
        var value = "abc";

        check(bind(pattern, value)).eq(true);
        check(bindings.get("x")).eq("abc");
    }

    @Test
    public void testListPattern_notList() {
        test("testListPattern_notList");

        constants = List.of("abc");
        var pattern = new Pattern.ListPattern(List.of(
            new Pattern.Constant(0)
        ), null);
        var value = "abc";

        check(bind(pattern, value)).eq(false);
    }

    @Test
    public void testListPattern_tooLong() {
        test("testListPattern_tooLong");

        constants = List.of("abc", "def", "ghi");
        var pattern = new Pattern.ListPattern(List.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1),
            new Pattern.Constant(2)
        ), null);
        var value = List.of("abc", "def");

        check(bind(pattern, value)).eq(false);
    }

    @Test
    public void testListPattern_tooShort() {
        test("testListPattern_tooShort");

        constants = List.of("abc");
        var pattern = new Pattern.ListPattern(List.of(
            new Pattern.Constant(0)
        ), null);
        var value = List.of("abc", "def");

        check(bind(pattern, value)).eq(false);
    }

    @Test
    public void testListPattern_badItem() {
        test("testListPattern_badItem");

        constants = List.of("abc", "def");
        var pattern = new Pattern.ListPattern(List.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1)
        ), null);
        var value = List.of("abc", "xyz");

        check(bind(pattern, value)).eq(false);
    }

    @Test
    public void testListPattern_good_emptyList() {
        test("testListPattern_good_emptyList");

        var pattern = new Pattern.ListPattern(List.of(
        ), null);
        var value = List.of();

        check(bind(pattern, value)).eq(true);
    }

    @Test
    public void testListPattern_good_noTail() {
        test("testListPattern_good_noTail");

        constants = List.of("abc", "def");
        var pattern = new Pattern.ListPattern(List.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1)
        ), null);
        var value = List.of("abc", "def");

        check(bind(pattern, value)).eq(true);
    }

    @Test
    public void testListPattern_good_tailEmpty() {
        test("testListPattern_good_tailEmpty");

        constants = List.of("abc", "def");
        var pattern = new Pattern.ListPattern(List.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1)
        ), "tail");
        var value = List.of("abc", "def");

        check(bind(pattern, value)).eq(true);
        check(bindings.get("tail")).eq(List.of());
    }

    @Test
    public void testListPattern_good_tailNotEmpty() {
        test("testListPattern_good_tailNotEmpty");

        constants = List.of("abc", "def");
        var pattern = new Pattern.ListPattern(List.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1)
        ), "tail");
        var value = List.of("abc", "def", "ghi", "jkl");

        check(bind(pattern, value)).eq(true);
        check(bindings.get("tail")).eq(List.of("ghi", "jkl"));
    }

    @Test
    public void testMapPattern_bad_noMap() {
        test("testMapPattern_bad_missingKey");

        constants = List.of("k1", "v1", "k2", "v2");
        var pattern = new Pattern.MapPattern(Map.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1),
            new Pattern.Constant(2),
            new Pattern.Constant(3)
        ));
        var value = "abc";

        check(bind(pattern, value)).eq(false);
    }

    @Test
    public void testMapPattern_bad_missingKey() {
        test("testMapPattern_bad_missingKey");

        constants = List.of("k1", "v1", "k2", "v2");
        var pattern = new Pattern.MapPattern(Map.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1),
            new Pattern.Constant(2),
            new Pattern.Constant(3)
        ));
        var value = Map.of("k1", "v1");

        check(bind(pattern, value)).eq(false);
    }

    @Test
    public void testMapPattern_bad_wrongValue() {
        test("testMapPattern_bad_wrongValue");

        constants = List.of("k1", "v1", "k2", "v2");
        var pattern = new Pattern.MapPattern(Map.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1),
            new Pattern.Constant(2),
            new Pattern.Constant(3)
        ));
        var value = Map.of("k1", "v1", "k2", "nonesuch");

        check(bind(pattern, value)).eq(false);
    }

    @Test
    public void testMapPattern_good_emptyMap() {
        test("testMapPattern_good_emptyMap");

        var pattern = new Pattern.MapPattern(Map.of(
        ));
        var value = Map.of();

        check(bind(pattern, value)).eq(true);
    }

    @Test
    public void testMapPattern_good_noExtraKey() {
        test("testMapPattern_good_noExtraKey");

        constants = List.of("k1", "v1", "k2", "v2");
        var pattern = new Pattern.MapPattern(Map.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1),
            new Pattern.Constant(2),
            new Pattern.Constant(3)
        ));
        var value = Map.of("k1", "v1", "k2", "v2");

        check(bind(pattern, value)).eq(true);
    }

    @Test
    public void testMapPattern_good_extraKey() {
        test("testMapPattern_good_extraKey");

        constants = List.of("k1", "v1", "k2", "v2");
        var pattern = new Pattern.MapPattern(Map.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1),
            new Pattern.Constant(2),
            new Pattern.Constant(3)
        ));
        var value = Map.of("k1", "v1", "k2", "v2", "k3", "v3");

        check(bind(pattern, value)).eq(true);
    }

    @Test
    public void testMapPattern_proxiedType() {
        test("testMapPattern_proxiedType");

        constants = List.of("first", "v1", "second", "v2");
        var pattern = new Pattern.MapPattern(Map.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1),
            new Pattern.Constant(2),
            new Pattern.Constant(3)
        ));
        var value = new Pair("v1", "v2");

        check(bind(pattern, value)).eq(true);
    }


    @Test
    public void testInstancePattern_bad_wrongType() {
        test("testInstancePattern_bad_wrongType");

        constants = List.of("id", "123", "color", "red");
        var fieldMap = new Pattern.MapPattern(Map.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1),
            new Pattern.Constant(2),
            new Pattern.Constant(3)
        ));
        var pattern = new Pattern.InstancePattern("Thing", fieldMap);

        var gizmo = new TestObject("Gizmo", "123", "red");

        check(bind(pattern, gizmo)).eq(false);
    }

    @Test
    public void testInstancePattern_bad_wrongField() {
        test("testInstancePattern_wrongField");

        constants = List.of("id", "123", "style", "fancy");
        var fieldMap = new Pattern.MapPattern(Map.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1),
            new Pattern.Constant(2),
            new Pattern.Constant(3)
        ));
        var pattern = new Pattern.InstancePattern("Thing", fieldMap);

        var thing = new TestObject("Thing", "123", "red");

        check(bind(pattern, thing)).eq(false);
    }

    @Test
    public void testInstancePattern_good() {
        test("testInstancePattern_good");

        constants = List.of("id", "123", "color", "red");
        var fieldMap = new Pattern.MapPattern(Map.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1),
            new Pattern.Constant(2),
            new Pattern.Constant(3)
        ));
        var pattern = new Pattern.InstancePattern("Thing", fieldMap);

        var thing = new TestObject("Thing", "123", "red");

        check(bind(pattern, thing)).eq(true);
    }

    @Test
    public void testInstancePattern_proxiedType() {
        test("testInstancePattern_proxiedType");

        constants = List.of("first", "v1", "second", "v2");
        var fieldMap = new Pattern.MapPattern(Map.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1),
            new Pattern.Constant(2),
            new Pattern.Constant(3)
        ));
        var pattern = new Pattern.InstancePattern("Pair", fieldMap);

        var value = new Pair("v1", "v2");

        check(bind(pattern, value)).eq(true);
    }

    @Test
    public void testRecordPattern_notRecord() {
        test("testRecordPattern_notRecord");

        constants = List.of("123", "red");
        var pattern = new Pattern.RecordPattern("Thing", List.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1)
        ));

        check(bind(pattern, "abc")).eq(false);
    }

    @Test
    public void testRecordPattern_wrongType() {
        test("testRecordPattern_wrongType");

        constants = List.of("123", "red");
        var pattern = new Pattern.RecordPattern("Thing", List.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1)
        ));

        var target = new TestObject("Gizmo", "123", "red");

        check(bind(pattern, target)).eq(false);
    }

    @Test
    public void testRecordPattern_wrongSize() {
        test("testRecordPattern_wrongSize");

        constants = List.of("123", "red", 456.0);
        var pattern = new Pattern.RecordPattern("Thing", List.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1),
            new Pattern.Constant(2)
        ));

        var target = new TestObject("Thing", "123", "red");

        check(bind(pattern, target)).eq(false);
    }

    @Test
    public void testRecordPattern_wrongFieldValue() {
        test("testRecordPattern_wrongFieldValue");

        constants = List.of("123", "green");
        var pattern = new Pattern.RecordPattern("Thing", List.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1)
        ));

        var target = new TestObject("Thing", "123", "red");

        check(bind(pattern, target)).eq(false);
    }

    @Test
    public void testRecordPattern_good() {
        test("testRecordPattern_good");

        constants = List.of("123", "red");
        var pattern = new Pattern.RecordPattern("Thing", List.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1)
        ));

        var target = new TestObject("Thing", "123", "red");

        check(bind(pattern, target)).eq(true);
    }

    @Test
    public void testRecordPattern_proxiedType() {
        test("testRecordPattern_good");

        constants = List.of("123", "red");
        var pattern = new Pattern.RecordPattern("Pair", List.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1)
        ));

        var target = new Pair("123", "red");

        check(bind(pattern, target)).eq(true);
    }

    //-------------------------------------------------------------------------
    // Helper

    public record TestType(String name) implements JoeType {
        public boolean isRecordType() { return true; }
    }

    private static class TestObject implements JoeValue {
        final String typeName;
        final Map<String, Object> fields = new HashMap<>();

        TestObject(String typeName, String id, String color) {
            this.typeName = typeName;
            fields.put("id", id);
            fields.put("color", color);
        }

        @Override public JoeType type() { return new TestType(typeName); }
        @Override public boolean hasField(String name) { return fields.containsKey(name); }
        @Override public List<String> getFieldNames() { return List.of("id", "color"); }
        @Override public Object get(String name) { return fields.get(name); }
        @Override public void set(String name, Object value) { }
    }

    private record Pair(Object first, Object second) {}

    private static class PairType extends RecordType<Pair> {
        PairType() {
            super("Pair");
            proxies(Pair.class);
            recordField("first", Pair::first);
            recordField("second", Pair::second);

            initializer(this::_init);
        }

        private Object _init(Joe joe, Args args) {
            args.exactArity(2, "Pair(first,second)");
            return new Pair(args.next(), args.next());
        }
    }
}
