package com.wjduquette.joe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class Resolver {
    private final Joe joe;
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();

    Resolver(Joe joe, Interpreter interpreter) {
        this.joe = joe;
        this.interpreter = interpreter;
    }

    public void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    private void resolve(Stmt statement) {
        switch (statement) {
            case Stmt.Block stmt -> {
                beginScope();
                resolve(stmt.statements());
                endScope();
            }
            case Stmt.Expression stmt -> resolve(stmt.expr());
            case Stmt.For stmt -> {
                if (stmt.init() != null)      resolve(stmt.init());
                if (stmt.condition() != null) resolve(stmt.condition());
                if (stmt.incr() != null)      resolve(stmt.incr());
                resolve(stmt.body());
            }
            case Stmt.Function stmt -> {
                declare(stmt.name());
                define(stmt.name());

                resolveFunction(stmt);
            }
            case Stmt.If stmt -> {
                resolve(stmt.condition());
                resolve(stmt.thenBranch());
                if (stmt.elseBranch() != null) resolve(stmt.elseBranch());
            }
            case Stmt.Print stmt -> resolve(stmt.expr());
            case Stmt.Return stmt -> {
                if (stmt.value() != null) resolve(stmt.value());
            }
            case Stmt.While stmt -> {
                resolve(stmt.condition());
                resolve(stmt.body());
            }
            case Stmt.Var stmt -> {
                declare(stmt.name());
                if (stmt.initializer() != null) {
                    resolve(stmt.initializer());
                }
                define(stmt.name());
            }
        }
    }

    private void resolve(Expr expression) {
        switch (expression) {
            case Expr.Assign expr -> {
                resolve(expr.value());
                resolveLocal(expr, expr.name());
            }
            case Expr.Binary expr -> {
                resolve(expr.left());
                resolve(expr.right());
            }
            case Expr.Call expr -> {
                resolve(expr.callee());

                for (var arg : expr.arguments()) {
                    resolve(arg);
                }
            }
            case Expr.Grouping expr -> resolve(expr.expr());
            case Expr.Literal expr -> {}
            case Expr.Logical expr -> {
                resolve(expr.left());
                resolve(expr.right());
            }
            case Expr.Unary expr -> resolve(expr.right());
            case Expr.Variable expr -> {
                if (!scopes.isEmpty() &&
                    scopes.peek().get(expr.name().lexeme()) == Boolean.FALSE) {
                    joe.error(expr.name(),
                        "Can't read local variable in its own initializer.");
                }

                resolveLocal(expr, expr.name());
            }
        }
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme())) {
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
    }

    private void resolveFunction(Stmt.Function function) {
        beginScope();
        for (Token param : function.params()) {
            declare(param);
            define(param);
        }
        resolve(function.body());
        endScope();
    }

    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    private void endScope() {
        scopes.pop();
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) return;

        Map<String, Boolean> scope = scopes.peek();
        scope.put(name.lexeme(), false);
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme(), true);
    }
}
