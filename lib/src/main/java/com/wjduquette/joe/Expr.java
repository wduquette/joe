package com.wjduquette.joe;

import java.util.List;

public sealed interface Expr
    permits Expr.Assign, Expr.Binary, Expr.Call, Expr.Get, Expr.Grouping,
            Expr.Literal, Expr.Logical, Expr.Set, Expr.This,
            Expr.Unary, Expr.Variable
{
    record Assign(Token name, Expr value) implements Expr {}
    record Binary(Expr left, Token op, Expr right) implements Expr {}
    record Call(Expr callee, Token paren, List<Expr> arguments) implements Expr {}
    record Get(Expr object, Token name) implements Expr {}
    record Grouping(Expr expr) implements Expr {}
    record Literal(Object value) implements Expr {}
    record Logical(Expr left, Token op, Expr right) implements Expr {}
    record Set(Expr object, Token name, Expr value) implements Expr {}
    record This(Token keyword) implements Expr {}
    record Unary(Token op, Expr right) implements Expr {}
    record Variable(Token name) implements Expr {}
}
