package com.wjduquette.joe;

import java.util.List;
import java.util.stream.Collectors;

import static com.wjduquette.joe.Expr.*;
import static com.wjduquette.joe.TokenType.*;

/**
 * Given the AST for a list of statements, a single statement, or a single
 * expression, converts it back to (badly formatted) code.
 */
public class Codifier {
    private Codifier() {} // Not instantiable

    /**
     * Codifies a list of statements.
     * @param joe The engine
     * @param statements The list of statements
     * @return The "code" string
     */
    static String codify(Joe joe, List<Stmt> statements) {
        return codify(joe, 0, statements);
    }

    static String codify(Joe joe, int indent, List<Stmt> statements) {
        return statements.stream()
            .map(s -> codify(joe, indent, s))
            .collect(Collectors.joining("\n"));
    }

    /**
     * Codifies a complete statement.
     * @param joe The engine
     * @param indent The number of indents
     * @param statement The statement
     * @return The "code" string
     */
    static String codify(Joe joe, int indent, Stmt statement) {
        var code = switch (statement) {
            case Stmt.Block stmt ->
                "{\n" + codify(joe, indent + 1, stmt.statements()) + "\n"
                + leading(indent) +"}";
            case Stmt.Expression stmt -> codify(joe, stmt.expr()) + ";";
            case Stmt.Print stmt ->
                "print " + codify(joe, stmt.expr()) + ";";
            case Stmt.Var stmt -> stmt.initializer() != null
                ? "var " + stmt.name().lexeme() + " = " +
                codify(joe, stmt.initializer()) + ";"
                : "var " + stmt.name().lexeme() + ";";
        };

        return leading(indent) + code;
    }

    private static String leading(int indent) {
        return "    ".repeat(indent);
    }

    /**
     * Converts the expression back to (something like) code.
     * @param joe The engine
     * @param expression The expression
     * @return The "code" string
     */
    static String codify(Joe joe, Expr expression) {
        return switch (expression) {
            case Assign expr ->
                expr.name().lexeme() + " = " + codify(joe, expr.value());
            case Binary expr -> {
                var type = expr.op().type();
                // Let * and / bind visually tighter.
                var space = (type == STAR || type == SLASH)
                    ? "" : " ";
                yield codify(joe, expr.left()) +
                    space + expr.op().lexeme() + space +
                    codify(joe, expr.right());
            }
            case Grouping expr -> "(" + codify(joe, expr.expr()) + ")";
            case Literal expr -> joe.codify(expr.value());
            case Unary expr -> expr.op().lexeme() + codify(joe, expr.right());
            case Variable expr -> expr.name().lexeme();
        };
    }
}
