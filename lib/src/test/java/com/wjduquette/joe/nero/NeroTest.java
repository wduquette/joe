package com.wjduquette.joe.nero;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.Keyword;
import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.Ted;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.wjduquette.joe.checker.Checker.check;
import static com.wjduquette.joe.checker.Checker.checkThrow;

// Tests for the Nero entry point.
public class NeroTest extends Ted {
    private final Nero nero = new Nero(new Joe());

    //-------------------------------------------------------------------------
    // execute

    @Test
    public void testExecute() {
        test("testExecute");

        var script = """
            define Person/name,age;
            Person("Joe", 90);
            
            define Place/...;
            Place(attire: "Stetson", name: "Texas");
            
            define Thing/2;
            Thing("hat", "black");
            """;


        var facts = Set.of(
            new PairFact("Person", List.of("name", "age"), List.of("Joe", 90.0)),
            new MapFact("Place", Map.of("name", "Texas", "attire", "Stetson")),
            new ListFact("Thing", List.of("hat", "black"))
        );
        var db = new FactSet(facts);
        check(nero.asNeroScript(db)).eq(script);
    }

    //-------------------------------------------------------------------------
    // asNeroScript

    @Test
    public void testAsNeroScript() {
        test("testAsNeroScript");
        var script = """
            define Person/name,age;
            Person("Joe", 90);
            
            define Place/...;
            Place(attire: "Stetson", name: "Texas");
            
            define Thing/2;
            Thing("hat", "black");
            """;
        var result = infer(script);
        check(result).eq("""
            ListFact[relation=Thing, fields=[hat, black]]
            MapFact[relation=Place, fieldMap={attire=Stetson, name=Texas}]
            PairFact[relation=Person, fieldNames=[name, age], fields=[Joe, 90.0]]
            """);
    }

    //-------------------------------------------------------------------------
    // asNeroAxiom

    @Test
    public void testAsNeroAxiom_listFact() {
        test("testAsNeroAxiom_listFact");
        var fact = new ListFact("Thing", List.of("car", "red"));
        check(nero.asNeroAxiom(fact))
            .eq("Thing(\"car\", \"red\");");
    }

    @Test
    public void testAsNeroAxiom_mapFact() {
        test("testAsNeroAxiom_mapFact");
        var fact = new MapFact("Thing", Map.of("id", "car", "color", "red"));
        check(nero.asNeroAxiom(fact))
            .eq("Thing(color: \"red\", id: \"car\");");
    }

    @Test
    public void testAsNeroAxiom_pairFact() {
        test("testAsNeroAxiom_pairFact");
        var fact = new PairFact("Thing",
            List.of("id", "color"), List.of("car", "red"));
        check(nero.asNeroAxiom(fact))
            .eq("Thing(\"car\", \"red\");");
    }

    //-------------------------------------------------------------------------
    // asNeroTerm

    @Test
    public void testAsNeroTerm() {
        test("testAsNeroTerm");
        check(nero.asNeroTerm(null)).eq("null");
        check(nero.asNeroTerm(true)).eq("true");
        check(nero.asNeroTerm(false)).eq("false");
        check(nero.asNeroTerm(5.0)).eq("5");
        check(nero.asNeroTerm(5.1)).eq("5.1");
        check(nero.asNeroTerm(new Keyword("id"))).eq("#id");
        check(nero.asNeroTerm("abc")).eq("\"abc\"");

        checkThrow(() -> nero.asNeroTerm(nero))
            .containsString("Non-Nero term:");
    }


    //-------------------------------------------------------------------------
    // Helpers

    private String infer(String source) {
        var engine = nero.execute(new SourceBuffer("-", source));
        return engine.getKnownFacts().getAll().stream()
            .map(Fact::toString)
            .sorted()
            .collect(Collectors.joining("\n")) + "\n";
    }
}
