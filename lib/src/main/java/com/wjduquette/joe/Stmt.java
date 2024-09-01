package com.wjduquette.joe;

import java.util.List;

public sealed interface Stmt
    permits Stmt.Block, Stmt.Class, Stmt.Expression, Stmt.Function, Stmt.For,
            Stmt.If, Stmt.Print, Stmt.Return, Stmt.Var, Stmt.While
{
    record Block(List<Stmt> statements) implements Stmt {}
    record Class(Token name, List<Stmt.Function> methods) implements Stmt {}
    record Expression(Expr expr) implements Stmt {}
    record For(Stmt init, Expr condition, Expr incr, Stmt body) implements Stmt {}
    record Function(String kind, Token name, List<Token> params, List<Stmt> body) implements Stmt {}
    record If(Expr condition, Stmt thenBranch, Stmt elseBranch) implements Stmt {}
    record Print(Expr expr) implements Stmt {}
    record Return(Token keyword, Expr value) implements Stmt {}
    record Var(Token name, Expr initializer) implements Stmt {}
    record While(Expr condition, Stmt body) implements Stmt {}
}
