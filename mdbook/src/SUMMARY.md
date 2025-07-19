# Summary

[Introduction](introduction.md)
[Change Log](changes.md)

---

# Language Reference

- [The Joe Language](language.md)
- [Types and Values](types.md)
- [Operators](operators.md)
- [Statements](statements.md)
- [Functions](functions.md)
- [Classes](classes.md)
- [Records](records.md)
- [Introspection](introspection.md)
- [Pattern Matching](patterns.md)
- [Nero Datalog](nero/nero.md)
  - [Nero Tutorial](nero/nero_tutorial.md)
  - [Nero Reference](nero/reference.md)
  - [Nero in Joe Scripts](nero/embedded_nero.md)
  - [Technical Details](nero/technical_details.md)

---

# Embedding and Extending Joe
- [Embedding Joe](embedding/embedding.md)
- [Extending Joe](extending/extending.md)
  - [Joe and Java Data Types](extending/java_types.md)
  - [Native Functions](extending/native_functions.md)
  - [Registered Types](extending/registered_types.md)
  - [Joe Packages](extending/packages.md)
  - [Native Records](extending/native_records.md)
  - [Native Classes](extending/native_classes.md)

# Joe Tools
- [`joe version`](joe_version.md)
- [`joe run`](joe_run.md)
- [`joe repl`](joe_repl.md)
- [`joe test`](joe_test.md)
- [`joe doc`](joe_doc.md)
- [`joe nero`](joe_nero.md)
 
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
    - [TextBuilder Type](./library/type.joe.TextBuilder.md)
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
    - [Control Type](library/type.joe.win.Control.md);
    - [Button Type](library/type.joe.win.Button.md);
    - [GridPane Type](library/type.joe.win.GridPane.md);
    - [HBox Type](library/type.joe.win.HBox.md);
    - [HPos Type](library/type.joe.win.HPos.md);
    - [Insets Type](library/type.joe.win.Insets.md);
    - [Label Type](library/type.joe.win.Label.md);
    - [ListView Type](library/type.joe.win.ListView.md);
    - [Menu Type](library/type.joe.win.Menu.md);
    - [MenuBar Type](library/type.joe.win.MenuBar.md);
    - [MenuItem Type](library/type.joe.win.MenuItem.md);
    - [Node Type](library/type.joe.win.Node.md);
    - [Orientation Type](library/type.joe.win.Orientation.md);
    - [Pane Type](library/type.joe.win.Pane.md);
    - [Pos Type](library/type.joe.win.Pos.md);
    - [Priority Type](library/type.joe.win.Priority.md);
    - [Region Type](library/type.joe.win.Region.md);
    - [Separator Type](library/type.joe.win.Separator.md);
    - [Side Type](library/type.joe.win.Side.md);
    - [SplitPane Type](library/type.joe.win.SplitPane.md);
    - [StackPane Type](library/type.joe.win.StackPane.md);
    - [Tab Type](library/type.joe.win.Tab.md);
    - [TabPane Type](library/type.joe.win.TabPane.md);
    - [VBox Type](library/type.joe.win.VBox.md);
    - [VPos Type](library/type.joe.win.VPos.md);
    - [Win Type](library/type.joe.win.Win.md);
  - [Library API Index](./library/index.md)

---

[Appendix: Joe Grammar](grammar.md)
[Appendix: Clark Virtual Machine](vm.md)
