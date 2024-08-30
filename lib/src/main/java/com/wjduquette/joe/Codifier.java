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
        return statements.stream()
            .map(s -> codify(joe, s))
            .collect(Collectors.joining("\n"));
    }

    /**
     * Codifies a complete statement.
     * @param joe The engine
     * @param statement The statement
     * @return The "code" string
     */
    static String codify(Joe joe, Stmt statement) {
        return switch (statement) {
            case Stmt.Expression stmt -> codify(joe, stmt.expr()) + ";";
            case Stmt.Print stmt ->
                "print(" + codify(joe, stmt.expr()) + ");";
        };
    }

    /**
     * Converts the expression back to (something like) code.
     * @param joe The engine
     * @param expression The expression
     * @return The "code" string
     */
    static String codify(Joe joe, Expr expression) {
        return switch (expression) {
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
        };
    }
}
