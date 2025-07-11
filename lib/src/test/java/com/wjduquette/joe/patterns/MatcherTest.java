package com.wjduquette.joe.patterns;

import com.wjduquette.joe.*;
import com.wjduquette.joe.nero.Fact;
import com.wjduquette.joe.nero.ListFact;
import com.wjduquette.joe.nero.RecordFact;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.wjduquette.joe.checker.Checker.check;

public class MatcherTest extends Ted {
    private final Joe joe = new Joe();
    private List<Object> constants;

    @Before public void setup() {
        constants = new ArrayList<>();
        joe.installType(new PairType());
    }

    private Map<String,Object> bind(Pattern pattern, Object value) {
        return Matcher.bind(joe, pattern, value, constants::get);
    }

    //-------------------------------------------------------------------------

    @Test
    public void testConstant_bad() {
        test("testConstant_bad");

        var pattern = new Pattern.Constant("abc");
        var value = "def";
        check(bind(pattern, value)).eq(null);
    }

    @Test
    public void testConstant_good() {
        test("testConstant_good");

        var pattern = new Pattern.Constant("abc");
        var value = "abc";

        var bindings = bind(pattern, value);
        check(bindings).ne(null);
        check(bindings.isEmpty()).eq(true);
    }

    @Test
    public void testConstant_keywordAndEnum() {
        test("testConstant_keywordAndEnum");

        var pattern = new Pattern.Constant(new Keyword("sweet"));

        check(bind(pattern, new Keyword("sweet"))).ne(null);
        check(bind(pattern, Flavor.SWEET)).ne(null);
        check(bind(pattern, Flavor.SOUR)).eq(null);
    }

    //-------------------------------------------------------------------------
    // Expression

    @Test
    public void testExpression_bad() {
        test("testExpression_bad");

        constants = List.of("abc");
        var pattern = new Pattern.Expression(0);
        var value = "xyz";

        check(bind(pattern, value)).eq(null);
    }

    @Test
    public void testExpression_good() {
        test("testExpression_good");

        constants = List.of("abc");
        var pattern = new Pattern.Expression(0);
        var value = "abc";

        var bindings = bind(pattern, value);
        check(bindings).ne(null);
        check(bindings.isEmpty()).eq(true);
    }

    @Test
    public void testExpression_keywordAndEnum() {
        test("testExpression_keywordAndEnum");

        constants = List.of(new Keyword("sweet"));
        var pattern = new Pattern.Expression(0);

        check(bind(pattern, new Keyword("sweet"))).ne(null);
        check(bind(pattern, Flavor.SWEET)).ne(null);
        check(bind(pattern, Flavor.SOUR)).eq(null);
    }

    //-------------------------------------------------------------------------
    // ListPattern

    @Test
    public void testListPattern_notList() {
        test("testListPattern_notList");

        constants = List.of("abc");
        var pattern = new Pattern.ListPattern(List.of(
            new Pattern.Expression(0)
        ), null);
        var value = "abc";

        var bindings = bind(pattern, value);
        check(bindings).eq(null);
    }

    @Test
    public void testListPattern_tooLong() {
        test("testListPattern_tooLong");

        constants = List.of("abc", "def", "ghi");
        var pattern = new Pattern.ListPattern(List.of(
            new Pattern.Expression(0),
            new Pattern.Expression(1),
            new Pattern.Expression(2)
        ), null);
        var value = List.of("abc", "def");

        var bindings = bind(pattern, value);
        check(bindings).eq(null);
    }

    @Test
    public void testListPattern_tooShort() {
        test("testListPattern_tooShort");

        constants = List.of("abc");
        var pattern = new Pattern.ListPattern(List.of(
            new Pattern.Expression(0)
        ), null);
        var value = List.of("abc", "def");

        var bindings = bind(pattern, value);
        check(bindings).eq(null);
    }

    @Test
    public void testListPattern_badItem() {
        test("testListPattern_badItem");

        constants = List.of("abc", "def");
        var pattern = new Pattern.ListPattern(List.of(
            new Pattern.Expression(0),
            new Pattern.Expression(1)
        ), null);
        var value = List.of("abc", "xyz");

        var bindings = bind(pattern, value);
        check(bindings).eq(null);
    }

    @Test
    public void testListPattern_good_emptyList() {
        test("testListPattern_good_emptyList");

        var pattern = new Pattern.ListPattern(List.of(
        ), null);
        var value = List.of();

        var bindings = bind(pattern, value);
        check(bindings).ne(null);
        check(bindings.isEmpty()).eq(true);
    }

    @Test
    public void testListPattern_good_noTail() {
        test("testListPattern_good_noTail");

        constants = List.of("abc", "def");
        var pattern = new Pattern.ListPattern(List.of(
            new Pattern.Expression(0),
            new Pattern.Expression(1)
        ), null);
        var value = List.of("abc", "def");

        var bindings = bind(pattern, value);
        check(bindings).ne(null);
        check(bindings.isEmpty()).eq(true);
    }

    @Test
    public void testListPattern_good_tailEmpty() {
        test("testListPattern_good_tailEmpty");

        constants = List.of("abc", "def");
        var pattern = new Pattern.ListPattern(List.of(
            new Pattern.Expression(0),
            new Pattern.Expression(1)
        ), "tail");
        var value = List.of("abc", "def");

        var bindings = bind(pattern, value);
        check(bindings).ne(null);
        check(bindings.get("tail")).eq(List.of());
    }

    @Test
    public void testListPattern_good_tailNotEmpty() {
        test("testListPattern_good_tailNotEmpty");

        constants = List.of("abc", "def");
        var pattern = new Pattern.ListPattern(List.of(
            new Pattern.Expression(0),
            new Pattern.Expression(1)
        ), "tail");
        var value = List.of("abc", "def", "ghi", "jkl");

        var bindings = bind(pattern, value);
        check(bindings).ne(null);
        check(bindings.get("tail")).eq(List.of("ghi", "jkl"));
    }

    @Test
    public void testListPattern_bad_boundTail() {
        test("testListPattern_bad_boundTail");

        var pattern = new Pattern.ListPattern(List.of(
            new Pattern.ValueBinding("x")
        ), "x");
        var value = List.of(List.of("abc"), "def");

        var bindings = bind(pattern, value);
        check(bindings).eq(null);
    }

    @Test
    public void testListPattern_good_boundTail() {
        test("testListPattern_good_boundTail");

        var pattern = new Pattern.ListPattern(List.of(
            new Pattern.ValueBinding("x")
        ), "x");
        var value = List.of(List.of("abc"), "abc");

        var bindings = bind(pattern, value);
        check(bindings).ne(null);
        check(bindings.get("x")).eq(List.of("abc"));
    }

    //-------------------------------------------------------------------------
    // MapPattern

    @Test
    public void testMapPattern_bad_noMap() {
        test("testMapPattern_bad_missingKey");

        constants = List.of("k1", "v1", "k2", "v2");
        var pattern = new Pattern.MapPattern(Map.of(
            new Pattern.Expression(0),
            new Pattern.Expression(1),
            new Pattern.Expression(2),
            new Pattern.Expression(3)
        ));
        var value = "abc";

        var bindings = bind(pattern, value);
        check(bindings).eq(null);
    }

    @Test
    public void testMapPattern_bad_missingKey() {
        test("testMapPattern_bad_missingKey");

        constants = List.of("k1", "v1", "k2", "v2");
        var pattern = new Pattern.MapPattern(Map.of(
            new Pattern.Expression(0),
            new Pattern.Expression(1),
            new Pattern.Expression(2),
            new Pattern.Expression(3)
        ));
        var value = Map.of("k1", "v1");

        var bindings = bind(pattern, value);
        check(bindings).eq(null);
    }

    @Test
    public void testMapPattern_bad_wrongValue() {
        test("testMapPattern_bad_wrongValue");

        constants = List.of("k1", "v1", "k2", "v2");
        var pattern = new Pattern.MapPattern(Map.of(
            new Pattern.Expression(0),
            new Pattern.Expression(1),
            new Pattern.Expression(2),
            new Pattern.Expression(3)
        ));
        var value = Map.of("k1", "v1", "k2", "nonesuch");

        var bindings = bind(pattern, value);
        check(bindings).eq(null);
    }

    @Test
    public void testMapPattern_good_emptyMap() {
        test("testMapPattern_good_emptyMap");

        var pattern = new Pattern.MapPattern(Map.of(
        ));
        var value = Map.of();

        var bindings = bind(pattern, value);
        check(bindings).ne(null);
        check(bindings.isEmpty()).eq(true);
    }

    @Test
    public void testMapPattern_good_noExtraKey() {
        test("testMapPattern_good_noExtraKey");

        constants = List.of("k1", "v1", "k2", "v2");
        var pattern = new Pattern.MapPattern(Map.of(
            new Pattern.Expression(0),
            new Pattern.Expression(1),
            new Pattern.Expression(2),
            new Pattern.Expression(3)
        ));
        var value = Map.of("k1", "v1", "k2", "v2");

        var bindings = bind(pattern, value);
        check(bindings).ne(null);
        check(bindings.isEmpty()).eq(true);
    }

    @Test
    public void testMapPattern_good_extraKey() {
        test("testMapPattern_good_extraKey");

        constants = List.of("k1", "v1", "k2", "v2");
        var pattern = new Pattern.MapPattern(Map.of(
            new Pattern.Expression(0),
            new Pattern.Expression(1),
            new Pattern.Expression(2),
            new Pattern.Expression(3)
        ));
        var value = Map.of("k1", "v1", "k2", "v2", "k3", "v3");

        var bindings = bind(pattern, value);
        check(bindings).ne(null);
        check(bindings.isEmpty()).eq(true);
    }

    //-------------------------------------------------------------------------
    // NamedFieldPattern

    @Test
    public void testNamedFieldPattern_bad_wrongType() {
        test("testNamedFieldPattern_bad_wrongType");

        constants = List.of("123", "red");
        Map<String,Pattern> fieldMap = Map.of(
            "id", new Pattern.Expression(0),
            "red", new Pattern.Expression(1)
        );
        var pattern = new Pattern.NamedFieldPattern("Thing", fieldMap);

        var gizmo = new TestObject("Gizmo", "123", "red");

        var bindings = bind(pattern, gizmo);
        check(bindings).eq(null);
    }

    @Test
    public void testNamedFieldPattern_bad_wrongField() {
        test("testNamedFieldPattern_bad_wrongField");

        constants = List.of("123", "fancy");
        Map<String,Pattern> fieldMap = Map.of(
            "id", new Pattern.Expression(0),
            "style", new Pattern.Expression(1)
        );
        var pattern = new Pattern.NamedFieldPattern("Thing", fieldMap);

        var thing = new TestObject("Thing", "123", "red");

        var bindings = bind(pattern, thing);
        check(bindings).eq(null);
    }

    @Test
    public void testNamedFieldPattern_good() {
        test("testNamedFieldPattern_good");

        constants = List.of("123", "red");
        Map<String,Pattern> fieldMap = Map.of(
            "id", new Pattern.Expression(0),
            "color", new Pattern.Expression(1)
        );
        var pattern = new Pattern.NamedFieldPattern("Thing", fieldMap);

        var thing = new TestObject("Thing", "123", "red");

        var bindings = bind(pattern, thing);
        check(bindings).ne(null);
        check(bindings.isEmpty()).eq(true);
    }

    @Test
    public void testNamedFieldPattern_proxiedType() {
        test("testNamedFieldPattern_proxiedType");

        constants = List.of("v1", "v2");
        Map<String,Pattern> fieldMap = Map.of(
            "first", new Pattern.Expression(0),
            "second", new Pattern.Expression(1)
        );
        var pattern = new Pattern.NamedFieldPattern("Pair", fieldMap);

        var pair = new Pair("v1", "v2");

        var bindings = bind(pattern, pair);
        check(bindings).ne(null);
        check(bindings.isEmpty()).eq(true);
    }

    //-------------------------------------------------------------------------
    // OrderedFieldPattern

    @Test
    public void testOrderedFieldPattern_scalar() {
        test("testOrderedFieldPattern_scalar");

        constants = List.of("123", "red");
        var pattern = new Pattern.OrderedFieldPattern("Thing", List.of(
            new Pattern.Expression(0),
            new Pattern.Expression(1)
        ));

        var bindings = bind(pattern, "abc");
        check(bindings).eq(null);
    }

    @Test
    public void testOrderedFieldPattern_wrongType() {
        test("testOrderedFieldPattern_wrongType");

        constants = List.of("123", "red");
        var pattern = new Pattern.OrderedFieldPattern("Thing", List.of(
            new Pattern.Expression(0),
            new Pattern.Expression(1)
        ));

        var target = new TestObject("Gizmo", "123", "red");

        var bindings = bind(pattern, target);
        check(bindings).eq(null);
    }

    @Test
    public void testOrderedFieldPattern_wrongSize() {
        test("testOrderedFieldPattern_wrongSize");

        constants = List.of("123", "red", 456.0);
        var pattern = new Pattern.OrderedFieldPattern("Thing", List.of(
            new Pattern.Expression(0),
            new Pattern.Expression(1),
            new Pattern.Expression(2)
        ));

        var target = new TestObject("Thing", "123", "red");

        var bindings = bind(pattern, target);
        check(bindings).eq(null);
    }

    @Test
    public void testOrderedFieldPattern_wrongFieldValue() {
        test("testOrderedFieldPattern_wrongFieldValue");

        constants = List.of("123", "green");
        var pattern = new Pattern.OrderedFieldPattern("Thing", List.of(
            new Pattern.Expression(0),
            new Pattern.Expression(1)
        ));

        var target = new TestObject("Thing", "123", "red");

        var bindings = bind(pattern, target);
        check(bindings).eq(null);
    }

    @Test
    public void testOrderedFieldPattern_typeNameOnly() {
        test("testOrderedFieldPattern_typeNameOnly");

        constants = List.of();
        var pattern = new Pattern.OrderedFieldPattern("Thing", List.of());

        var target = new TestObject("Thing", "123", "red");

        var bindings = bind(pattern, target);
        check(bindings).ne(null);
        check(bindings.isEmpty()).eq(true);
    }

    @Test
    public void testOrderedFieldPattern_good() {
        test("testOrderedFieldPattern_good");

        constants = List.of("123", "red");
        var pattern = new Pattern.OrderedFieldPattern("Thing", List.of(
            new Pattern.Expression(0),
            new Pattern.Expression(1)
        ));

        var target = new TestObject("Thing", "123", "red");

        var bindings = bind(pattern, target);
        check(bindings).ne(null);
        check(bindings.isEmpty()).eq(true);
    }

    @Test
    public void testOrderedFieldPattern_proxiedType() {
        test("testOrderedFieldPattern_good");

        constants = List.of("123", "red");
        var pattern = new Pattern.OrderedFieldPattern("Pair", List.of(
            new Pattern.Expression(0),
            new Pattern.Expression(1)
        ));

        var target = new Pair("123", "red");

        var bindings = bind(pattern, target);
        check(bindings).ne(null);
        check(bindings.isEmpty()).eq(true);
    }

    //-------------------------------------------------------------------------
    // PatternBinding

    @Test
    public void testPatternBinding_bad() {
        test("testPatternBinding_bad");

        constants = List.of("abc");
        var pattern = new Pattern.PatternBinding("x",
            new Pattern.Expression(0));
        var value = "xyz";

        check(bind(pattern, value)).eq(null);
    }

    @Test
    public void testPatternBinding_good() {
        test("testPatternBinding_good");

        constants = List.of("abc");
        var pattern = new Pattern.PatternBinding("x",
            new Pattern.Expression(0));
        var value = "abc";

        var bindings = bind(pattern, value);
        check(bindings).ne(null);
        check(bindings.get("x")).eq("abc");
    }

    @Test
    public void testPatternBinding_boundVar_bad() {
        test("testPatternBinding_boundVar_bad");

        constants = List.of("abc");
        var pattern = new Pattern.ListPattern(List.of(
            new Pattern.ValueBinding("x"),
            new Pattern.PatternBinding("x",
                new Pattern.Expression(0))
        ), null);
        var value = List.of("abc", "def");

        var bindings = bind(pattern, value);
        check(bindings).eq(null);
    }

    @Test
    public void testPatternBinding_boundVar_good() {
        test("testPatternBinding_boundVar_good");

        constants = List.of("abc");
        var pattern = new Pattern.ListPattern(List.of(
            new Pattern.ValueBinding("x"),
            new Pattern.PatternBinding("x",
                new Pattern.Expression(0))
        ), null);
        var value = List.of("abc", "abc");

        var bindings = bind(pattern, value);
        check(bindings).ne(null);
        check(bindings.get("x")).eq("abc");
    }

    //-------------------------------------------------------------------------
    // TypeName

    @Test
    public void testTypeName_fact() {
        test("testTypeName_fact");

        var p1 = new Pattern.TypeName("Thing");
        var p2 = new Pattern.TypeName("Fact");
        var value = new ListFact("Thing", List.of("hat"));

        check(bind(p1, value)).ne(null);
        check(bind(p2, value)).ne(null);
    }

    @Test
    public void testTypeName_canBeFact() {
        test("testTypeName_canBeFact");

        var pattern = new Pattern.TypeName("Thing");
        var value = new TestObject("Thing", "hat", "black");

        var bindings = bind(pattern, value);
        check(bindings).ne(null);
        check(bindings.isEmpty()).eq(true);
    }

    @Test
    public void testTypeName_notFact() {
        test("testTypeName_notFact");

        var pattern = new Pattern.TypeName("String");
        var value = "abc";

        var bindings = bind(pattern, value);
        check(bindings).ne(null);
        check(bindings.isEmpty()).eq(true);
    }

    @Test
    public void testTypeName_noMatch() {
        test("testTypeName_noMatch");

        var pattern = new Pattern.TypeName("String");
        var value = 5;

        var bindings = bind(pattern, value);
        check(bindings).eq(null);
    }

    //-------------------------------------------------------------------------
    // ValueBinding

    @Test
    public void testValueBinding_newVar() {
        test("testValueBinding_newVar");

        var pattern = new Pattern.ValueBinding("x");
        var value = "abc";

        var bindings = bind(pattern, value);
        check(bindings).ne(null);
        check(bindings.get("x")).eq("abc");
    }

    @Test
    public void testValueBinding_boundVar_bad() {
        test("testValueBinding_boundVar_bad");

        var pattern = new Pattern.ListPattern(List.of(
            new Pattern.ValueBinding("x"),
            new Pattern.ValueBinding("x")
        ), null);
        var value = List.of("abc", "def");

        var bindings = bind(pattern, value);
        check(bindings).eq(null);
    }

    @Test
    public void testValueBinding_boundVar_good() {
        test("testValueBinding_boundVar_good");

        var pattern = new Pattern.ListPattern(List.of(
            new Pattern.ValueBinding("x"),
            new Pattern.ValueBinding("x")
        ), null);
        var value = List.of("abc", "abc");

        var bindings = bind(pattern, value);
        check(bindings).ne(null);
        check(bindings.get("x")).eq("abc");
    }

    //-------------------------------------------------------------------------
    // Wildcard

    @Test
    public void testWildcard() {
        test("testWildcard");

        var pattern = new Pattern.Wildcard("_");
        var value = "abc";

        var bindings = bind(pattern, value);
        check(bindings).ne(null);
        check(bindings.isEmpty()).eq(true);
    }


    //-------------------------------------------------------------------------
    // Helper

    private enum Flavor { SWEET, SOUR }

    public record TestType(String name) implements JoeType {
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
        @Override public List<String> getFieldNames() { return List.of("id", "color"); }
        @Override public Object get(String name) { return fields.get(name); }
        @Override public void set(String name, Object value) { }
        @Override public boolean isFact() { return true; }
        @Override public Fact toFact() {
            return new RecordFact(typeName, List.of("id", "color"), fields);
        }
    }

    private record Pair(Object first, Object second) {}

    private static class PairType extends ProxyType<Pair> {
        PairType() {
            super("Pair");
            proxies(Pair.class);
            field("first", this::_first);
            field("second", this::_second);

            initializer(this::_init);
        }

        private Object _init(Joe joe, Args args) {
            args.exactArity(2, "Pair(first,second)");
            return new Pair(args.next(), args.next());
        }

        private Object _first(Joe joe, Pair value) {
            return value.first();
        }

        private Object _second(Joe joe, Pair value) {
            return value.second();
        }
    }
}
