package com.wjduquette.joe.walker;

import java.util.List;
import java.util.stream.Collectors;

class Dumper {
    private transient StringBuilder buff;
    private transient int indent;

    Dumper() {
        // Nothing to do
    }


    public String dump(List<Stmt> statements) {
        buff = new StringBuilder();
        indent = 0;

        dumpStatements(statements);
        return buff.toString();
    }

    private void dumpStatements(List<Stmt> statements) {
        for (var stmt : statements) {
            dumpStatement(stmt);
        }
    }

    private void dumpStatement(Stmt statement) {
        switch (statement) {
            case null -> {}
            case Stmt.Assert stmt ->
                buff.append(indent())
                    .append("Stmt.Assert ")
                    .append(stmt.condition())
                    .append(", ")
                    .append(stmt.message())
                    .append("\n");
            case Stmt.Block stmt -> {
                buff.append(indent())
                    .append("Stmt.Block {\n");

                ++indent;
                dumpStatements(stmt.statements());
                --indent;

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

                if (stmt.staticInitializer() !=  null &&
                    !stmt.staticInitializer().isEmpty()
                ) {
                    buff.append(indent())
                        .append("Static initializer:\n");

                    ++indent;
                    dumpStatements(stmt.staticInitializer());
                    --indent;
                }

                if (stmt.staticMethods() != null) {
                    buff.append(indent())
                        .append("Static Methods:\n");

                    ++indent;
                    for (var func : stmt.staticMethods()) {
                        dumpStatement(func);
                    }
                    --indent;
                }

                if (stmt.methods() != null) {
                    buff.append(indent())
                        .append("Instance Methods:\n");

                    ++indent;
                    for (var func : stmt.methods()) {
                        dumpStatement(func);
                    }
                    --indent;
                }

                --indent;
            }
            case Stmt.Continue ignored ->
                buff.append(indent()).append("Stmt.Continue\n");
            case Stmt.Expression stmt ->
                buff.append(indent())
                    .append("Stmt.Expression ")
                    .append(stmt.expr())
                    .append("\n");
            case Stmt.For stmt -> {
                buff.append(indent())
                    .append("Stmt.For\n");
                ++indent;
                dumpStatement(stmt.init());
                buff.append(indent())
                    .append(stmt.condition())
                    .append("\n")
                    .append(indent())
                    .append(stmt.incr())
                    .append("\n");
                ++indent;
                dumpStatement(stmt.body());
                --indent;

                --indent;
            }
            case Stmt.ForEach stmt -> {
                buff.append(indent())
                    .append("Stmt.ForEach ")
                    .append(stmt.listExpr())
                    .append("\n");
                ++indent;
                dumpStatement(stmt.body());
                --indent;
            }
            case Stmt.Function stmt -> {
                var params = stmt.params().stream()
                    .map(Token::lexeme)
                    .collect(Collectors.joining(", "));
                buff.append(indent())
                    .append("Stmt.Function ")
                    .append(stmt.kind())
                    .append(" ")
                    .append(stmt.name().lexeme())
                    .append("(")
                    .append(params)
                    .append(")\n");

                ++indent;
                dumpStatements(stmt.body());
                --indent;
            }
            case Stmt.If stmt -> {
                buff.append(indent())
                    .append("Stmt.If ")
                    .append(stmt.condition())
                    .append("\n");
                ++indent;
                buff.append(indent()).append("Then:\n");
                dumpStatement(stmt.thenBranch());
                if (stmt.elseBranch() != null) {
                    buff.append(indent()).append("Else:\n");
                    dumpStatement(stmt.elseBranch());
                }
                --indent;
            }
            case Stmt.Return stmt ->
                buff.append(indent())
                    .append("Stmt.Return ")
                    .append(stmt.value())
                    .append("\n");
            case Stmt.Switch stmt -> {
                buff.append(indent())
                    .append("Stmt.Switch ")
                    .append(stmt.expr())
                    .append("\n");
                ++indent;
                for (var c : stmt.cases()) {
                    buff.append(indent())
                        .append("Case ")
                        .append(c.values())
                        .append("\n");
                    ++indent;
                    dumpStatement(c.statement());
                    --indent;
                }
                --indent;
            }
            case Stmt.Throw stmt ->
                buff.append(indent())
                    .append("Stmt.Throw ")
                    .append(stmt.value())
                    .append("\n");
            case Stmt.Var stmt ->
                buff.append(indent())
                    .append("Stmt.Var ")
                    .append(stmt.name().lexeme())
                    .append(" = ")
                    .append(stmt.initializer())
                    .append("\n");
            case Stmt.While stmt -> {
                buff.append(indent())
                    .append("Stmt.While ")
                    .append(stmt.condition())
                    .append("\n");
                ++indent;
                dumpStatement(stmt.body());
                --indent;
            }
        }
    }

    private String indent() {
        return "    ".repeat(indent);
    }
}
