package com.wjduquette.joe;

import static com.wjduquette.joe.Expr.*;
import static com.wjduquette.joe.TokenType.*;

/**
 * Given the AST for an expression, converts it back to code.
 */
public class ASTPrinter {
    private ASTPrinter() {} // Not instantiable

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
