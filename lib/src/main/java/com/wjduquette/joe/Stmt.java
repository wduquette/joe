package com.wjduquette.joe;

public sealed interface Stmt
    permits Stmt.Expression, Stmt.Print, Stmt.Var
{
    record Expression(Expr expr) implements Stmt {}
    record Print(Expr expr) implements Stmt {}
    record Var(Token name, Expr initializer) implements Stmt {}
}
