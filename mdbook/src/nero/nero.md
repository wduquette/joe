# Nero Datalog

Nero is Joe's own dialect of the Datalog relational query language.  Datalog
is often used as a query language for databases; Joe provides it as a means
of querying and transforming in-memory collections of data.

Joe provides Nero in two forms:

- As a standalone language, via the [`joe nero`](../joe_nero.md) tool.
- As a language embedded in Joe scripts.

The following subsections discuss these in turn.

- The [Nero Tutorial](nero_tutorial.md) describes Nero as a standalone
  language.
- [Nero in Joe Scripts](embedded_nero.md) describes how Nero is used within a
  Joe script, including the `ruleset` expression and how to use the
  [`RuleSet`](../library/type.joe.RuleSet.md) and
  [`FactBase`](../library/type.joe.FactBase.md) types.
- [Nero Reference](reference.md) is a concise statement of the
  syntax and semantics of Nero rule sets.
- [Technical Details](technical_details.md) places Nero within the larger 
  family of Datalog implementations, and describes specific differences from
  "standard" Datalog.

