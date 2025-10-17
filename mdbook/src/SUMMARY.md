# Summary

[Introduction](introduction.md)
[Change Log](changes.md)

---

# The Joe Language

- [The Joe Language](language.md)
- [Types and Values](types.md)
- [Operators](operators.md)
- [Statements](statements.md)
- [Functions](functions.md)
- [Classes](classes.md)
- [Records](records.md)
- [Pattern Matching](patterns.md)
- [Introspection](introspection.md)

---

# Embedding and Extending Joe
- [Embedding Joe in Java](embedding/embedding.md)
- [Extending Joe in Java](extending/extending.md)
  - [Joe and Java Data Types](extending/java_types.md)
  - [Native Functions](extending/native_functions.md)
  - [Registered Types](extending/registered_types.md)
  - [Joe Packages](extending/packages.md)
  - [Native Records](extending/native_records.md)
  - [Extending Native Types in Joe](extending/native_classes.md)
- [Joe Package Repositories](package_repos.md)
 
---

# Nero Datalog

- [Nero Datalog](nero/nero.md)
  - [Datalog Basics](nero/datalog_basics.md) 
  - [Nero Terms](nero/terms.md)
  - [Negation](nero/negation.md)
  - [Constraints](nero/constraints.md)
  - [Named-Field Notation](nero/named_fields.md)
  - [Schema Declarations](nero/schema.md)
  - [Schema Maintenance](nero/schema_maintenance.md)
  - [Built-in Predicates](nero/builtin_predicates.md)
  - [Aggregation Functions](nero/aggregation_functions.md)
  - [Stratification](nero/stratification.md) 
  - [Technical Details](nero/technical_details.md)
- [Nero as a Data Format](nero/nero_data.md)
- [Using Nero with Joe](nero/nero_and_joe.md)
- [Using Nero with Java](nero/nero_and_java.md)
---

# Tools
- [The 'joe' Application](joe_app.md)
  - ['joe version'](joe_version.md)
  - ['joe run'](joe_run.md)
  - ['joe repl'](joe_repl.md)
  - ['joe test'](joe_test.md)
  - ['joe doc'](joe_doc.md)
  - ['joe dump'](joe_dump.md)
  - ['joe win'](joe_win.md)
- [The 'nero' Application](nero_app.md)
  - ['nero version'](nero_version.md)
  - ['nero run'](nero_run.md)
 
---
 
# Library Reference

- [Library API](library/index.md)
  - [Joe Standard Library (joe)](./library/pkg.joe.md)
    - [AssertError Type](./library/type.joe.AssertError.md)
    - [Boolean Type](./library/type.joe.Boolean.md)
    - [CatchResult Type](./library/type.joe.CatchResult.md)
    - [Error Type](./library/type.joe.Error.md)
    - [Fact Type](./library/type.joe.Fact.md)
    - [FactBase Type](./library/type.joe.FactBase.md)
    - [Function Type](./library/type.joe.Function.md)
    - [Joe Singleton](./library/type.joe.Joe.md)
    - [Keyword Type](./library/type.joe.Keyword.md)
    - [List Type](./library/type.joe.List.md)
    - [Map Type](./library/type.joe.Map.md)
    - [Number Type](./library/type.joe.Number.md)
    - [Opaque Type](./library/type.joe.Opaque.md)
    - [RuleSet Type](./library/type.joe.RuleSet.md)
    - [Set Type](./library/type.joe.Set.md)
    - [String Type](./library/type.joe.String.md)
    - [TextBuilder Class](./library/type.joe.TextBuilder.md)
    - [Type Singleton](./library/type.joe.Type.md)
  - [Joe Test Tool API (joe.test)](./library/pkg.joe.test.md)
    - [CatchChecker Type](./library/type.joe.test.CatchChecker.md)
    - [JoeTest Type](./library/type.joe.test.JoeTest.md)
    - [ValueChecker Type](./library/type.joe.test.ValueChecker.md)
  - [JoeDoc Configuration API (joe.doc)](./library/pkg.joe.doc.md)
    - [DocConfig Type](./library/type.joe.doc.DocConfig.md)
  - [Console API (joe.console)](./library/pkg.joe.console.md)
    - [Console Type](library/type.joe.console.Console.md);
    - [Path Type](library/type.joe.console.Path.md);
  - [GUI API (joe.win)](./library/pkg.joe.win.md)
    - [Control Widget](library/type.joe.win.Control.md);
    - [Button Widget](library/type.joe.win.Button.md);
    - [GridPane Widget](library/type.joe.win.GridPane.md);
    - [HBox Widget](library/type.joe.win.HBox.md);
    - [HPos Enum](library/type.joe.win.HPos.md);
    - [Insets Type](library/type.joe.win.Insets.md);
    - [Label Widget](library/type.joe.win.Label.md);
    - [Labeled Widget](library/type.joe.win.Labeled.md);
    - [Listener Type](library/type.joe.win.Listener.md);
    - [ListView Widget](library/type.joe.win.ListView.md);
    - [Menu Widget](library/type.joe.win.Menu.md);
    - [MenuBar Widget](library/type.joe.win.MenuBar.md);
    - [MenuItem Widget](library/type.joe.win.MenuItem.md);
    - [Node Widget](library/type.joe.win.Node.md);
    - [Orientation Enum](library/type.joe.win.Orientation.md);
    - [Pane Widget](library/type.joe.win.Pane.md);
    - [Pos Enum](library/type.joe.win.Pos.md);
    - [Priority Enum](library/type.joe.win.Priority.md);
    - [Region Widget](library/type.joe.win.Region.md);
    - [Separator Widget](library/type.joe.win.Separator.md);
    - [Side Enum](library/type.joe.win.Side.md);
    - [SplitPane Widget](library/type.joe.win.SplitPane.md);
    - [StackPane Widget](library/type.joe.win.StackPane.md);
    - [Tab Widget](library/type.joe.win.Tab.md);
    - [TabPane Widget](library/type.joe.win.TabPane.md);
    - [VBox Widget](library/type.joe.win.VBox.md);
    - [VPos Widget](library/type.joe.win.VPos.md);
    - [Widget Widget](library/type.joe.win.Widget.md);
    - [Win Singleton](library/type.joe.win.Win.md);
  - [Library API Index](./library/index.md)

---

[Appendix: Grammars](grammar.md)
[Appendix: Virtual Machine](vm.md)
