package com.wjduquette.joe;

import java.util.List;
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
     * Codifies a list of statements.
     *
     * @param statements The list of statements
     * @return The "code" string
     */
    String recodify(List<Stmt> statements) {
        return recodify(0, statements);
    }

    // Recodifies a list of statements with the desired indent level
    private String recodify(int indent, List<Stmt> statements) {
        return statements.stream()
            .map(s -> recodify(indent, s))
            .collect(Collectors.joining("\n"));
    }

    // Recodifies a single statement with the desired indent level.
    private String recodify(int indent, Stmt statement) {
        var code = switch (statement) {
            case Stmt.Assert stmt ->
                "assert " +
                recodify(stmt.condition()) +
                (stmt.message() != null
                    ? ", " + recodify(stmt.message())
                    : "")
                + ";";
            case Stmt.Block stmt ->
                "{\n" + recodify(indent + 1, stmt.statements()) + "\n"
                + leading(indent) +"}";
            case Stmt.Class stmt -> {
                var buff = new StringBuilder();
                buff.append("class ")
                    .append(stmt.name().lexeme());

                if (stmt.superclass() != null) {
                    buff.append(" extends ")
                        .append(recodify(stmt.superclass()));
                }

                buff.append(" {\n");

                for (var method : stmt.methods()) {
                    buff.append(recodify(indent + 1, method))
                        .append("\n");
                }

                buff.append(leading(indent)).append("}");

                yield buff.toString();
            }
            case Stmt.For stmt -> {
                var buff = new StringBuilder();
                buff.append("for (");

                if (stmt.init() != null) {
                    buff.append(recodify(0, stmt.init()));
                }

                if (stmt.condition() != null) {
                    buff.append(" ")
                        .append(recodify(stmt.condition()))
                        .append(";");
                }

                if (stmt.incr() != null) {
                    buff.append(" ")
                        .append(recodify(stmt.incr()));
                }

                buff.append(")")
                    .append(body(indent, stmt.body()));
                yield buff.toString();
            }
            case Stmt.Function stmt -> {
                var params = stmt.params().stream()
                    .map(Token::lexeme)
                    .collect(Collectors.joining(", "));
                yield stmt.name().lexeme() +
                    "(" + params + ")" +
                    body(indent, stmt.body());
            }
            case Stmt.If stmt -> {
                var buff = new StringBuilder();
                buff.append("if (")
                    .append(recodify(stmt.condition()))
                    .append(")")
                    .append(body(0, stmt.thenBranch()))
                    ;

                if (stmt.elseBranch() != null) {
                    buff.append("\n")
                        .append(leading(indent))
                        .append("else")
                        .append(body(0, stmt.elseBranch()));
                }
                yield buff.toString();
            }
            case Stmt.Expression stmt -> recodify(stmt.expr()) + ";";
            case Stmt.Return stmt ->
                "return" +
                    (stmt.value() != null ? " " + recodify(stmt.value()) : "")
                    + ";";
            case Stmt.Var stmt -> stmt.initializer() != null
                ? "var " + stmt.name().lexeme() + " = " +
                recodify(stmt.initializer()) + ";"
                : "var " + stmt.name().lexeme() + ";";
            case Stmt.While stmt ->
                    "while (" + recodify(stmt.condition()) + ")" +
                    body(indent, stmt.body());
        };

        return leading(indent) + code;
    }

    // Recodifies a block with the given indent level.  The opening
    // brace is on the current line, the statements are indented an
    // additional level, and the close brace is on a line by itself
    // at the given indent level.
    private String body(int indent, Stmt stmt) {
        if (stmt instanceof Stmt.Block block) {
            return body(indent, block.statements());
        } else {
            return "\n" + recodify(indent + 1, stmt);
        }
    }

    // Recodifies a block's statements with the given indent level.  The opening
    // brace is on the current line, the statements are indented an
    // additional level, and the close brace is on a line by itself
    // at the given indent level.
    private String body(int indent, List<Stmt> statements) {
        return
            " {\n" +
            recodify(indent + 1, statements) +
            "\n" +
            leading(indent) +
            "}";
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
            case Call expr -> {
                var args = expr.arguments().stream()
                    .map(this::recodify)
                    .collect(Collectors.joining(", "));
                yield recodify(expr.callee()) + "(" + args + ")";
            }
            case Get expr -> recodify(expr.object()) + "." + expr.name().lexeme();
            case Grouping expr -> "(" + recodify(expr.expr()) + ")";
            case Literal expr -> joe.codify(expr.value());
            case Logical expr ->
                recodify(expr.left()) +
                " " + expr.op().lexeme() + " " +
                recodify(expr.right());
            case Set expr ->
                recodify(expr.object()) + "." + expr.name().lexeme() + " = " +
                recodify(expr.value());
            case Super expr -> "super." + expr.method().lexeme();
            case This ignored -> "this";
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
