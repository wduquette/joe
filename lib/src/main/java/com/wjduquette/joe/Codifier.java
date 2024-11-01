package com.wjduquette.joe;

import java.util.stream.Collectors;

import static com.wjduquette.joe.Expr.*;
import static com.wjduquette.joe.TokenType.*;

/**
 * Given the AST for a list of statements, a single statement, or a single
 * expression, converts it back to (badly formatted) code.
 *
 * <p><b>NOTE:</b> Recodifying expressions is useful in producing high-quality
 * error messages.  Recodifying statements accurately means that statements
 * can't easily be implemented using desugaring techniques.  It remains to
 * be seen whether recodifying statements provides enough value to be retained.
 * </p>
 */
class Codifier {
    private final Joe joe;

    Codifier(Joe joe) {
        this.joe = joe;
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
                expr.name().lexeme() + " " + expr.op().lexeme() + " " +
                    recodify(expr.value());
            case Binary expr -> {
                var type = expr.op().type();
                // Let * and / bind visually tighter.
                var space = (type == STAR || type == SLASH)
                    ? "" : " ";
                yield recodify(expr.left()) +
                    space + expr.op().lexeme() + space +
                    recodify(expr.right());
            }
            case Call expr -> {
                var args = expr.arguments().stream()
                    .map(this::recodify)
                    .collect(Collectors.joining(", "));
                yield recodify(expr.callee()) + "(" + args + ")";
            }
            case Get expr -> {
                String target;
                if (expr.object() instanceof Expr.This t &&
                    t.keyword().lexeme().equals("@")
                ) {
                    target = "@";
                } else {
                    target = recodify(expr.object()) + ".";
                }
                yield target + expr.name().lexeme();
            }
            case Grouping expr -> "(" + recodify(expr.expr()) + ")";
            case Lambda expr -> {
                var params = expr.declaration().params().stream()
                    .map(Token::lexeme).collect(Collectors.joining(", "));
                yield "\\" + params + " -> ...";
            }
            case Literal expr -> joe.codify(expr.value());
            case Logical expr ->
                recodify(expr.left()) +
                " " + expr.op().lexeme() + " " +
                recodify(expr.right());
            case PrePostAssign expr -> expr.isPre()
                ? expr.op().lexeme() + expr.name().lexeme()
                : expr.name().lexeme() + expr.op().lexeme();
            case PrePostSet expr -> {
                var prop = recodify(expr.object()) + "." + expr.name().lexeme();
                yield expr.isPre()
                    ? expr.op().lexeme() + prop
                    : prop + expr.op().lexeme();
            }
            case Set expr -> {
                String target;
                if (expr.object() instanceof Expr.This t &&
                    t.keyword().lexeme().equals("@")
                ) {
                    target = "@";
                } else {
                    target = recodify(expr.object()) + ".";
                }
                yield target + expr.name().lexeme() + " " +
                    expr.op().lexeme() + " " +
                    recodify(expr.value());
            }
            case Super expr -> "super." + expr.method().lexeme();
            case This expr -> expr.keyword().lexeme();
            case Ternary expr ->
                recodify(expr.condition())
                + " ? "
                + recodify(expr.trueExpr())
                + " : "
                + recodify(expr.falseExpr());
            case Unary expr -> expr.op().lexeme() + recodify(expr.right());
            case Variable expr -> expr.name().lexeme();
        };
    }
}
