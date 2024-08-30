package com.wjduquette.joe;

public sealed interface Expr
    permits Expr.Binary, Expr.Grouping, Expr.Literal, Expr.Unary,
            Expr.Variable
{
    record Binary(Expr left, Token op, Expr right) implements Expr {}
    record Grouping(Expr expr) implements Expr {}
    record Literal(Object value) implements Expr {}
    record Unary(Token op, Expr right) implements Expr {}
    record Variable(Token name) implements Expr {}
}
