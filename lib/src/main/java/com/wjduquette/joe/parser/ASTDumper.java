package com.wjduquette.joe.parser;

import com.wjduquette.joe.patterns.Pattern;
import com.wjduquette.joe.scanner.Token;

import java.util.List;
import java.util.stream.Collectors;

public class ASTDumper {
    private ASTDumper() {}  // Static class

    //-------------------------------------------------------------------------
    // Public API

    public static String dump(List<Stmt> list) {
        var buff = new Buffer();

        for (var stmt : list) {
            buff.println(dump(stmt));
        }

        return buff.toString();
    }

    public static String dump(Stmt stmt) {
        var buffer = new Buffer();
        buffer.print("Stmt." + stmt.getClass().getSimpleName());

        // For each statement, add any additional content to the current
        // line, followed by any indented sub-parts.  The whole should be
        // followed by a newline.
        var content = switch (stmt) {
            case Stmt.Assert s -> buffer().nl()
                .dump("condition", s.condition())
                .dump("message", s.message());
            case Stmt.Block s -> buffer().nl()
                .dump(s.statements());
            case Stmt.Break ignored -> "";
            case Stmt.Class s -> {
                var buff = buffer();
                buff.println(" '" + s.name().lexeme() + "'");
                if (s.superclass() != null) {
                    buff.dump("extends", s.superclass());
                }
                s.staticMethods().forEach(buff::dump);
                s.methods().forEach(buff::dump);
                if (!s.staticInit().isEmpty()) {
                    buff.dump("static init", s.staticInit());
                }
                yield buff.toString();
            }
            case Stmt.Continue ignored -> "";
            case Stmt.Expression s -> buffer().nl()
                .dump(s.expr());
            case Stmt.For s -> {
                var buff = buffer().nl();
                if (s.init() != null) buff.dump("init", s.init());
                if (s.condition() != null) buff.dump("cond", s.condition());
                if (s.updater() != null) buff.dump("update", s.updater());
                buff.dump("body", s.body());
                yield buff.toString();
            }
            case Stmt.ForEach s -> buffer()
                .println(" '" + s.name().lexeme() + "'")
                .dump("items", s.items())
                .dump("body", s.body());
            case Stmt.ForEachBind s -> buffer().nl()
                .dump("pattern",s.pattern())
                .dump("items", s.items())
                .dump("body", s.body());
            case Stmt.Function s -> buffer()
                .print(" " + s.type() + " ")
                .print("'" + s.name().lexeme() + "'")
                .println("(" + tokenList(s.params()) + ")")
                .dump(s.body());
            case Stmt.If s -> {
                var buff = buffer().nl()
                    .dump("cond", s.condition())
                    .dump("then", s.thenBranch());
                if (s.elseBranch() != null) {
                    buff.dump("else", s.elseBranch());
                }
                yield buff.toString();
            }
            case Stmt.Match s -> {
                var buff = buffer().nl();
                buff.dump("value", s.expr());
                for (var c : s.cases()) {
                    buff.indent(dump(c));
                }
                buff.dump("default", s.matchDefault());
                yield buff.toString();
            }
            case Stmt.Record s -> {
                var buff = buffer();
                buff.print(" '" + s.name().lexeme() + "'")
                    .println("(" + stringList(s.fields()) + ")");
                s.staticMethods().forEach(buff::dump);
                s.methods().forEach(buff::dump);
                if (!s.staticInit().isEmpty()) {
                    buff.dump("static init", s.staticInit());
                }
                yield buff.toString();
            }
            case Stmt.Return s -> s.value() == null
                ? ""
                : buffer().nl().dump(s.value());
            case Stmt.Switch s -> {
                var buff = buffer().nl();
                buff.dump("value", s.expr());
                for (var c : s.cases()) {
                    buff.indent(dump(c));
                }
                buff.dump("default", s.switchDefault().statement());
                yield buff.toString();
            }

            case Stmt.Throw s -> buffer().nl()
                .dump(s.value());
            case Stmt.Var s -> buffer()
                .println(" '" + s.name().lexeme() + "'")
                .dump(s.value());
            case Stmt.VarPattern s -> buffer().nl()
                .dump("pattern",s.pattern())
                .dump("target", s.target());
            case Stmt.While s -> buffer.nl()
                .dump("cond", s.condition())
                .dump("body", s.body());
        };
        buffer.print(content.toString());

        return buffer.toString();
    }

    public static String dump(Stmt.MatchCase stmt) {
        var buff = new Buffer();
        buff.print("Stmt." + stmt.getClass().getSimpleName()).nl()
            .dump("pattern", stmt.pattern());
        if (stmt.guard() != null) buff.dump("guard", stmt.guard());
        buff.dump("body", stmt.statement());
        return buff.toString();
    }

    public static String dump(Stmt.SwitchCase stmt) {
        var buff = new Buffer();
        buff.print("case Stmt." + stmt.getClass().getSimpleName()).nl();

        for (var c : stmt.values()) {
            buff.dump("target", c);
        }
        buff.dump("body", stmt.statement());
        return buff.toString();
    }

    public static String dump(Expr expr) {
        var buffer = new Buffer();
        buffer.print("Expr." + expr.getClass().getSimpleName());

        var content = switch (expr) {
            case Expr.Binary e -> buffer()
                .println(" '" + e.op().lexeme() + "'")
                .dump(e.left())
                .dump(e.right());
            case Expr.Call e -> {
                var buff = buffer().nl()
                    .dump("callee", e.callee());
                for (var arg : e.arguments()) {
                    buff.dump("arg", arg);
                }
                yield buff.toString();
            }
            case Expr.False ignored -> "";
            case Expr.Grouping e -> buffer().nl()
                .dump(e.expr());
            case Expr.IndexGet e -> buffer().nl()
                .dump("collection", e.collection())
                .dump("index", e.index());
            case Expr.IndexIncrDecr e -> buffer()
                .print(e.isPre() ? " prefix " : " postfix ")
                .println("'" + e.op().lexeme() + "'")
                .dump("collection", e.collection())
                .dump("index", e.index());
            case Expr.IndexSet e -> buffer()
                .println(" '" + e.op().lexeme() + "'")
                .dump("collection", e.collection())
                .dump("index", e.index())
                .dump("value", e.value());
            case Expr.Lambda e -> buffer()
                .println(" (" + tokenList(e.declaration().params()) + ")")
                .dump(e.declaration().body());
            case Expr.ListLiteral e -> {
                var buff = buffer().nl();
                e.list().forEach(buff::dump);
                yield buff.toString();
            }
            case Expr.Literal e -> buffer()
                .println(" '" + e.value() + "'");
            case Expr.Logical e -> buffer()
                .println(" '" + e.op().lexeme() + "'")
                .dump(e.left())
                .dump(e.right());
            case Expr.MapLiteral e -> {
                var buff = buffer().nl();
                for (var i = 0; i < e.entries().size(); i += 2) {
                    buff.dump("key", e.entries().get(i));
                    buff.dump("val", e.entries().get(i + 1));
                }
                yield buff.toString();
            }
            case Expr.Match e -> buffer()
                .println(" '" + e.op().lexeme() + "'")
                .dump("target", e.target())
                .dump("pattern", e.pattern());
            case Expr.Null ignored -> "";
            case Expr.PropGet e -> buffer()
                .println(" '" + e.name().lexeme() + "'")
                .dump("object", e.object());
            case Expr.PropIncrDecr e -> buffer()
                .print(" '" + e.name().lexeme() + "'")
                .print(e.isPre() ? " prefix " : " postfix ")
                .println("'" + e.op().lexeme() + "'")
                .dump("object", e.object());
            case Expr.PropSet e -> buffer()
                .print(" '" + e.name().lexeme() + "' ")
                .println("'" + e.op().lexeme() + "'")
                .dump("object", e.object())
                .dump("value", e.value());
            case Expr.RuleSet e -> {
                var buff = buffer().nl()
                    .dump("ruleset", e.ruleSet());

                for (var export : e.exports().entrySet()) {
                    buff.dump("export " + export.getKey().lexeme(),
                        export.getValue());
                }

                yield buff.toString();
            }
            case Expr.Super e -> buffer()
                .println(" '" + e.method().lexeme() + "'");
            case Expr.Ternary e -> buffer().nl()
                .dump("condition", e.condition())
                .dump("trueExpr", e.trueExpr())
                .dump("falseExpr", e.falseExpr());
            case Expr.This ignored -> "";
            case Expr.True ignored -> "";
            case Expr.Unary e -> buffer()
                .println(" '" + e.op().lexeme() + "'")
                .dump(e.right());
            case Expr.VarGet e -> buffer()
                .println(" '" + e.name().lexeme() + "'");
            case Expr.VarIncrDecr e -> buffer()
                .print(" '" + e.name().lexeme() + "'")
                .print(e.isPre() ? " prefix " : " postfix ")
                .println("'" + e.op().lexeme() + "'");
            case Expr.VarSet e -> buffer()
                .print(" '" + e.name().lexeme() + "' ")
                .println("'" + e.op().lexeme() + "'")
                .dump("value", e.value());
        };

        buffer.print(content.toString());

        return buffer.toString();
    }

    private static String dump(ASTPattern pattern) {
        var buffer = new Buffer();
        buffer.println(pattern.getClass().getSimpleName());

        buffer.indent("bindings: " + tokenList(pattern.getBindings()));
        int i = 0;
        for (var c : pattern.getConstants()) {
            buffer.indent("constant[" + i++ + "]: " + dump(c));
        }
        buffer.dump("pattern", pattern.getPattern());
        return buffer.toString();
    }

    private static String dump(Pattern pattern) {
        var buffer = new Buffer();
        buffer.print("Pattern." + pattern.getClass().getSimpleName());

        var content = switch (pattern) {
            case Pattern.Constant p -> buffer()
                .println(" '" + p.id() + "'");
            case Pattern.ListPattern p -> {
                var buff = buffer().nl();
                p.patterns().forEach(buff::dump);
                yield buff.toString();
            }
            case Pattern.MapPattern p -> {
                var buff = buffer().nl();
                for (var e : p.patterns().entrySet()) {
                    buff.dump("key", e.getKey());
                    buff.dump("val", e.getValue());
                }
                yield buff.toString();
            }
            case Pattern.NamedFieldPattern p -> {
                var buff = buffer()
                    .println(" '" + p.typeName() + "'");
                for (var key : p.fieldMap().keySet()) {
                    buff.dump("'" + key + "'", p.fieldMap().get(key));
                }
                yield buff.toString();
            }
            case Pattern.PatternBinding p -> buffer()
                .println(" '" + p.name() + "'")
                .dump(p.subpattern());
            case Pattern.RecordPattern p -> {
                var buff = buffer()
                    .println(" '" + p.typeName() + "'");
                for (var sub : p.patterns()) {
                    buff.dump(sub);
                }
                yield buff.toString();
            }
            case Pattern.ValueBinding p -> buffer()
                .println(" '" + p.name() + "'");
            case Pattern.Wildcard p -> buffer()
                .println(" '" + p.name() + "'");
        };


        buffer.print(content.toString());

        return buffer.toString();
    }

    private static String dump(ASTRuleSet ruleset) {
        var buff = new Buffer();
        buff.println(ruleset.getClass().getSimpleName());

        for (var fact : ruleset.facts()) {
            buff.indent("fact: " + dump(fact));
        }

        for (var rule : ruleset.rules()) {
            buff.indent("rule: " + dump(rule));
        }

        return buff.toString();
    }

    private static String dump(ASTRuleSet.ASTOrderedAtom atom) {
        return atom.toString();
    }

    private static String dump(ASTRuleSet.ASTRule rule) {
        var buff = new Buffer();
        buff.print(rule.getClass().getSimpleName())
            .print(" ")
            .println(rule.head().toString());

        for (var atom : rule.body()) {
            buff.indent("body: " + atom.toString());
        }

        for (var atom : rule.negations()) {
            buff.indent("not: " + atom.toString());
        }

        for (var constraint : rule.constraints()) {
            buff.indent("where: " + constraint.toString());
        }

        return buff.toString();
    }

    private static String tokenList(List<Token> list) {
        return list.stream()
            .map(t -> "'" + t.lexeme() + "'")
            .collect(Collectors.joining(", "));
    }

    private static String stringList(List<String> list) {
        return list.stream()
            .map(s -> "'" + s  + "'")
            .collect(Collectors.joining(", "));
    }

    //-------------------------------------------------------------------------
    // Helpers

    private static Buffer buffer() {
        return new Buffer();
    }

    @SuppressWarnings("UnusedReturnValue")
    private static class Buffer {
        private static final int SPACES = 2;
        private final StringBuilder buff = new StringBuilder();

        /**
         * Prints the text.
         * @param text the text
         * @return The buffer
         */
        Buffer print(String text) {
            buff.append(text);
            return this;
        }

        /**
         * Prints the text followed by a newline.
         * @param text the text
         * @return The buffer
         */
        Buffer println(String text) {
            buff.append(text).append("\n");
            return this;
        }

        /**
         * Prints a single newline.
         * @return The buffer.
         */
        Buffer nl() {
            buff.append("\n");
            return this;
        }

        /**
         * Prints the text as an indented block followed by a newline.
         * @param text The text
         * @return The buffer
         */
        Buffer indent(String text) {
            // Note: String::indent adds a newline on the end.
            return print(text.indent(SPACES));
        }

        Buffer dump(List<Stmt> statements) {
            indent(ASTDumper.dump(statements));
            return this;
        }

        Buffer dump(String name, List<Stmt> statements) {
            indent(name + ": " + ASTDumper.dump(statements));
            return this;
        }

        Buffer dump(Stmt statement) {
            indent(ASTDumper.dump(statement));
            return this;
        }

        Buffer dump(String name, Stmt statement) {
            indent(name + ": " + ASTDumper.dump(statement));
            return this;
        }

        Buffer dump(String name, Expr value) {
            return indent(name + ": " + ASTDumper.dump(value));
        }

        Buffer dump(Expr value) {
            return indent(ASTDumper.dump(value));
        }

        Buffer dump(String name, ASTPattern pattern) {
            return indent(name + ": " + ASTDumper.dump(pattern));
        }

        Buffer dump(String name, Pattern pattern) {
            return indent(name + ": " + ASTDumper.dump(pattern));
        }

        Buffer dump(Pattern pattern) {
            return indent(ASTDumper.dump(pattern));
        }

        Buffer dump(String name, ASTRuleSet ruleset) {
            return indent(name + ": " + ASTDumper.dump(ruleset));
        }

        /**
         * Returns the buffer's content, stripping any trailing whitespace.
         * @return The content
         */
        @Override
        public String toString() {
            return buff.toString().stripTrailing();
        }
    }
}
