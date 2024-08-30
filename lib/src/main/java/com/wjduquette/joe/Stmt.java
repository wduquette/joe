package com.wjduquette.joe;

public sealed interface Stmt
    permits Stmt.Expression, Stmt.Print
{
    record Expression(Expr expr) implements Stmt {}
    record Print(Expr expr) implements Stmt {}
}
