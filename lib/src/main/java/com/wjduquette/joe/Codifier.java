package com.wjduquette.joe;

import java.util.List;
import java.util.stream.Collectors;

import static com.wjduquette.joe.Expr.*;
import static com.wjduquette.joe.TokenType.*;

/**
 * Given the AST for a list of statements, a single statement, or a single
 * expression, converts it back to (badly formatted) code.
 */
class Codifier {
    private final Joe joe;

    Codifier(Joe joe) {
        this.joe = joe;
    }

    /**
     * Codifies a list of statements.
     *
     * @param statements The list of statements
     * @return The "code" string
     */
    String recodify(List<Stmt> statements) {
        return recodify(0, statements);
    }

    String recodify(int indent, List<Stmt> statements) {
        return statements.stream()
            .map(s -> recodify(indent, s))
            .collect(Collectors.joining("\n"));
    }

    /**
     * Codifies a complete statement.
     *
     * @param indent    The number of indents
     * @param statement The statement
     * @return The "code" string
     */
    String recodify(int indent, Stmt statement) {
        var code = switch (statement) {
            case Stmt.Block stmt ->
                "{\n" + recodify(indent + 1, stmt.statements()) + "\n"
                + leading(indent) +"}";
            case Stmt.If stmt -> {
                var buff = new StringBuilder();
                buff.append("if (")
                    .append(recodify(stmt.condition()))
                    .append(")");

                if (stmt.thenBranch() instanceof Stmt.Block block) {
                    buff.append(" {\n")
                        .append(recodify(indent + 1, block.statements()))
                        .append("\n")
                        .append(leading(indent))
                        .append("}\n");
                } else {
                    buff.append(" ")
                        .append(recodify(0, stmt.thenBranch()))
                        .append("\n");
                }

                if (stmt.elseBranch() != null) {
                    buff.append(leading(indent))
                        .append("else");

                    if (stmt.elseBranch() instanceof Stmt.Block block) {
                        buff.append(" {\n")
                            .append(recodify(indent + 1, block.statements()))
                            .append("\n")
                            .append(leading(indent))
                            .append("}\n");
                    } else {
                        buff.append(" ")
                            .append(recodify(0, stmt.elseBranch()));
                    }
                }
                yield buff.toString();
            }
            case Stmt.Expression stmt -> recodify(stmt.expr()) + ";";
            case Stmt.Print stmt ->
                "print " + recodify(stmt.expr()) + ";";
            case Stmt.Var stmt -> stmt.initializer() != null
                ? "var " + stmt.name().lexeme() + " = " +
                recodify(stmt.initializer()) + ";"
                : "var " + stmt.name().lexeme() + ";";
        };

        return leading(indent) + code;
    }

    private String leading(int indent) {
        return "    ".repeat(indent);
    }

    /**
     * Converts the expression back to (something like) code.
     *
     * @param expression The expression
     * @return The "code" string
     */
    String recodify(Expr expression) {
        return switch (expression) {
            case Assign expr ->
                expr.name().lexeme() + " = " + recodify(expr.value());
            case Binary expr -> {
                var type = expr.op().type();
                // Let * and / bind visually tighter.
                var space = (type == STAR || type == SLASH)
                    ? "" : " ";
                yield recodify(expr.left()) +
                    space + expr.op().lexeme() + space +
                    recodify(expr.right());
            }
            case Grouping expr -> "(" + recodify(expr.expr()) + ")";
            case Literal expr -> joe.codify(expr.value());
            case Unary expr -> expr.op().lexeme() + recodify(expr.right());
            case Variable expr -> expr.name().lexeme();
        };
    }
}
