package com.wjduquette.joe;

public sealed interface Expr
    permits Expr.Binary, Expr.Grouping, Expr.Literal, Expr.Unary
{
    record Binary(Expr left, Token operator, Expr right) implements Expr {}
    record Grouping(Expr expr) implements Expr {}
    record Literal(Object value) implements Expr {}
    record Unary(Token operator, Expr right) implements Expr {}
}
