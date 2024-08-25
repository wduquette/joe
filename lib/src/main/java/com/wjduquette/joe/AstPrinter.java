package com.wjduquette.joe;

import static com.wjduquette.joe.Expr.*;

public class AstPrinter {
    String print(Expr expression) {
        return switch (expression) {
            case Binary expr ->
                parenthesize(expr.op().lexeme(), expr.left(), expr.right());
            case Grouping expr ->
                parenthesize("group", expr.expr());
            case Literal expr ->
                expr.value() == null ? "null" : expr.value().toString();
            case Unary expr ->
                    parenthesize(expr.op().lexeme(), expr.right());
        };
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(print(expr));
        }
        builder.append(")");

        return builder.toString();
    }
}
