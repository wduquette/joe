package com.wjduquette.joe.parser;

import com.wjduquette.joe.scanner.Token;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Produces a text dump of a Joe AST.
 */
public class Dumper {
    private transient StringBuilder buff;
    private transient int indent;

    public Dumper() {
        // Nothing to do
    }


    public String dump(List<Stmt> statements) {
        buff = new StringBuilder();
        indent = -1;

        dumpStatements(statements);
        return buff.toString();
    }

    private void dumpStatements(List<Stmt> statements) {
        for (var stmt : statements) {
            dumpStatement(stmt);
        }
    }

    private void dumpStatement(Stmt statement) {
        ++indent;
        switch (statement) {
            case null -> {}
            case Stmt.Assert stmt -> {
                buff.append(indent())
                    .append("Stmt.Assert ")
                    .append(stmt.message())
                    .append("\n");
                dumpExpression(stmt.condition());
            }
            case Stmt.Block stmt -> {
                buff.append(indent())
                    .append("Stmt.Block {\n");
                dumpStatements(stmt.statements());
                buff.append(indent())
                    .append("}\n");
            }
            case Stmt.Break ignored ->
                buff.append(indent()).append("Stmt.Break\n");
            case Stmt.Class stmt -> {
                buff.append(indent())
                    .append("Stmt.Class ")
                    .append(stmt.name().lexeme());

                if (stmt.superclass() != null) {
                    buff.append(" extends ")
                        .append(stmt.superclass().name().lexeme());
                }

                buff.append("\n");

                // Class content
                ++indent;

                if (stmt.staticInit() !=  null &&
                    !stmt.staticInit().isEmpty()
                ) {
                    buff.append(indent())
                        .append("Static initializer:\n");
                    dumpStatements(stmt.staticInit());
                }

                if (stmt.staticMethods() != null) {
                    buff.append(indent())
                        .append("Static Methods:\n");
                    for (var func : stmt.staticMethods()) {
                        dumpStatement(func);
                    }
                }

                if (stmt.methods() != null) {
                    buff.append(indent())
                        .append("Instance Methods:\n");
                    for (var func : stmt.methods()) {
                        dumpStatement(func);
                    }
                }

                --indent;
            }
            case Stmt.Continue ignored ->
                buff.append(indent()).append("Stmt.Continue\n");
            case Stmt.Expression stmt -> {
                buff.append(indent())
                    .append("Stmt.Expression\n");
                dumpExpression(stmt.expr());
            }
            case Stmt.For stmt -> {
                buff.append(indent())
                    .append("Stmt.For\n");
                dumpStatement(stmt.init());
                dumpExpression(stmt.condition());
                dumpExpression(stmt.updater());
                dumpStatement(stmt.body());
            }
            case Stmt.ForEach stmt -> {
                buff.append(indent())
                    .append("Stmt.ForEach ")
                    .append(stmt.name().lexeme())
                    .append("\n");

                dumpExpression(stmt.items());
                dumpStatement(stmt.body());
            }
            case Stmt.ForEachBind stmt -> {
                buff.append(indent())
                    .append("Stmt.ForEach ")
                    .append(stmt.pattern().getPattern())
                    .append("\n");
                dumpExpression(stmt.items());
                dumpStatement(stmt.body());
            }
            case Stmt.Function stmt -> {
                var params = stmt.params().stream()
                    .map(Token::lexeme)
                    .collect(Collectors.joining(", "));
                buff.append(indent())
                    .append("Stmt.Function ")
                    .append(stmt.type())
                    .append(" ")
                    .append(stmt.name().lexeme())
                    .append("(")
                    .append(params)
                    .append(")\n");
                dumpStatements(stmt.body());
            }
            case Stmt.If stmt -> {
                buff.append(indent())
                    .append("Stmt.If\n");
                dumpExpression(stmt.condition());
                ++indent;
                buff.append(indent()).append("Then:\n");
                dumpStatement(stmt.thenBranch());
                if (stmt.elseBranch() != null) {
                    buff.append(indent()).append("Else:\n");
                    dumpStatement(stmt.elseBranch());
                }
                --indent;
            }
            case Stmt.IfLet stmt -> {
                buff.append(indent())
                    .append("Stmt.If ")
                    .append(stmt.pattern().getPattern())
                    .append("\n");

                dumpExpression(stmt.target());
                ++indent;
                buff.append(indent()).append("Then:\n");
                dumpStatement(stmt.thenBranch());
                if (stmt.elseBranch() != null) {
                    buff.append(indent()).append("Else:\n");
                    dumpStatement(stmt.elseBranch());
                }
                --indent;
            }
            case Stmt.Match stmt -> {
                buff.append(indent())
                    .append("Stmt.Match ")
                    .append(stmt.expr())
                    .append("\n");
                ++indent;
                for (var c : stmt.cases()) {
                    buff.append(indent())
                        .append("Case ")
                        .append(c.pattern().getPattern())
                        .append(" if ")
                        .append(c.guard())
                        .append("\n");
                    dumpStatement(c.statement());
                }
                if (stmt.matchDefault() != null) {
                    buff.append(indent())
                        .append("Default\n");
                    dumpStatement(stmt.matchDefault().statement());
                }
                --indent;
            }
            case Stmt.Record stmt -> {
                buff.append(indent())
                    .append("Stmt.Record ")
                    .append(stmt.name().lexeme())
                    .append("(")
                    .append(String.join(", ", stmt.fields()))
                    .append(")")
                    ;

                buff.append("\n");

                // Class content
                ++indent;

                if (stmt.staticInit() !=  null &&
                    !stmt.staticInit().isEmpty()
                ) {
                    buff.append(indent())
                        .append("Static initializer:\n");
                    dumpStatements(stmt.staticInit());
                }

                if (stmt.staticMethods() != null) {
                    buff.append(indent())
                        .append("Static Methods:\n");
                    for (var func : stmt.staticMethods()) {
                        dumpStatement(func);
                    }
                }

                if (stmt.methods() != null) {
                    buff.append(indent())
                        .append("Instance Methods:\n");
                    for (var func : stmt.methods()) {
                        dumpStatement(func);
                    }
                }

                --indent;
            }
            case Stmt.Return stmt -> {
                buff.append(indent())
                    .append("Stmt.Return\n");
                dumpExpression(stmt.value());
            }
            case Stmt.Switch stmt -> {
                buff.append(indent())
                    .append("Stmt.Switch ")
                    .append(stmt.expr())
                    .append("\n");
                ++indent;
                for (var c : stmt.cases()) {
                    buff.append(indent())
                        .append("Case\n");
                    for (var value : c.values()) {
                        dumpExpression(value);
                    }
                    dumpStatement(c.statement());
                }
                if (stmt.switchDefault() != null) {
                    buff.append(indent())
                        .append("Default\n");
                    dumpStatement(stmt.switchDefault().statement());
                }
                --indent;
            }
            case Stmt.Throw stmt -> {
                buff.append(indent())
                    .append("Stmt.Throw\n");
                dumpExpression(stmt.value());
            }
            case Stmt.Var stmt -> {
                buff.append(indent())
                    .append("Stmt.Var ")
                    .append(stmt.name().lexeme())
                    .append(" =\n");
                dumpExpression(stmt.value());
            }
            case Stmt.VarPattern stmt -> {
                // TODO: Add variables and constants
                buff.append(indent())
                    .append("Stmt.Let ")
                    .append(stmt.pattern().getPattern())
                    .append(" =\n");
                dumpExpression(stmt.target());
            }
            case Stmt.While stmt -> {
                buff.append(indent())
                    .append("Stmt.While\n");
                dumpExpression(stmt.condition());
                dumpStatement(stmt.body());
            }
        }
        --indent;
    }

    private void dumpExpression(Expr expr) {
        if (expr == null) {
            return;
        }

        ++indent;

        buff.append(indent())
            .append("Expr.")
            .append(expr.getClass().getSimpleName())
            .append(" ");
        switch (expr) {
            case Expr.Binary e -> {
                buff.append(e.op().lexeme())
                    .append("\n");
                dumpExpression(e.left());
                dumpExpression(e.right());
            }
            case Expr.Call e -> {
                buff.append("\n");
                dumpExpression(e.callee());
                for (var arg : e.arguments()) {
                    dumpExpression(arg);
                }
            }
            case Expr.False ignored -> buff.append("\n");
            case Expr.Grouping e -> {
                buff.append(" (\n");
                dumpExpression(e.expr());
                buff.append(indent())
                    .append(")\n");
            }
            case Expr.IndexGet e -> {
                dumpExpression(e.collection());
                dumpExpression(e.index());
            }
            case Expr.IndexIncrDecr e -> {
                dumpExpression(e.collection());
                dumpExpression(e.index());
            }
            case Expr.IndexSet e -> {
                dumpExpression(e.collection());
                dumpExpression(e.index());
                dumpExpression(e.value());
            }
            case Expr.Lambda e -> {
                buff.append("\n");
                ++indent;
                dumpStatement(e.declaration());
                --indent;
            }
            case Expr.ListLiteral e -> {
                if (e.list().isEmpty()) {
                    buff.append("[]\n");
                } else {
                    buff.append("\n");
                    e.list().forEach(this::dumpExpression);
                }
            }
            case Expr.Literal e -> {
                if (e.value() == null) {
                    buff.append("null\n");
                } else {
                    buff.append(e.value().getClass().getSimpleName())
                        .append(" '")
                        .append(e.value())
                        .append("'\n");
                }
            }
            case Expr.Logical e -> {
                buff.append(e.op().lexeme())
                    .append("\n");
                dumpExpression(e.left());
                dumpExpression(e.right());
            }
            case Expr.MapLiteral e -> e.entries().forEach(this::dumpExpression);
            case Expr.Null ignored -> buff.append("\n");
            case Expr.PropGet e -> {
                buff.append(e.name().lexeme())
                    .append(" of:\n");
                dumpExpression(e.object());
            }
            case Expr.PropIncrDecr e -> {
                if (e.isPre()) {
                    buff.append(e.op().lexeme())
                        .append(e.name().lexeme())
                        .append(" of\n");
                } else {
                    buff.append(e.name().lexeme())
                        .append(e.op().lexeme())
                        .append(" of\n");
                }
                dumpExpression(e.object());
            }
            case Expr.PropSet e -> {
                buff.append(e.name().lexeme())
                    .append(" of\n");
                dumpExpression(e.object());
            }
            case Expr.Super e ->
                buff.append(e.method().lexeme()).append("\n");
            case Expr.Ternary e -> {
                buff.append("\n");
                dumpExpression(e.condition());
                dumpExpression(e.trueExpr());
                dumpExpression(e.falseExpr());
            }
            case Expr.This e ->
                buff.append("'")
                    .append(e.keyword().lexeme())
                    .append("'\n");
            case Expr.True ignored -> buff.append("\n");
            case Expr.Unary e -> {
                buff.append(e.op().lexeme())
                    .append("\n");
                dumpExpression(e.right());
            }
            case Expr.VarGet e ->
                buff.append(e.name().lexeme())
                    .append("\n");
            case Expr.VarIncrDecr e -> {
                if (e.isPre()) {
                    buff.append(e.op().lexeme())
                        .append(e.name().lexeme())
                        .append("\n");
                } else {
                    buff.append(e.name().lexeme())
                        .append(e.op().lexeme())
                        .append("\n");
                }
            }
            case Expr.VarSet e -> {
                buff.append(e.name().lexeme())
                    .append(" ")
                    .append(e.op().lexeme())
                    .append("\n");
                dumpExpression(e.value());
            }
        }

        --indent;
    }

    private String indent() {
        return "    ".repeat(indent);
    }
}
