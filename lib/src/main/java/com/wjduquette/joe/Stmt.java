package com.wjduquette.joe;

import java.util.List;

public sealed interface Stmt
    permits Stmt.Block, Stmt.Expression, Stmt.If, Stmt.Print, Stmt.Var
{
    record Block(List<Stmt> statements) implements Stmt {}
    record Expression(Expr expr) implements Stmt {}
    record If(Expr condition, Stmt thenBranch, Stmt elseBranch) implements Stmt {}
    record Print(Expr expr) implements Stmt {}
    record Var(Token name, Expr initializer) implements Stmt {}
}
