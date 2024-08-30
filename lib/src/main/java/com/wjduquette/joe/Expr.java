package com.wjduquette.joe;

public sealed interface Expr
    permits Expr.Assign, Expr.Binary, Expr.Grouping, Expr.Literal, Expr.Unary,
            Expr.Variable
{
    record Assign(Token name, Expr value) implements Expr {}
    record Binary(Expr left, Token op, Expr right) implements Expr {}
    record Grouping(Expr expr) implements Expr {}
    record Literal(Object value) implements Expr {}
    record Unary(Token op, Expr right) implements Expr {}
    record Variable(Token name) implements Expr {}
}
