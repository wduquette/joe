package com.wjduquette.joe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;

class Resolver {
    private enum FunctionType { NONE, FUNCTION, INITIALIZER, METHOD }
    private enum ClassType { NONE, CLASS, SUBCLASS }

    private final Interpreter interpreter;
    private final Consumer<SyntaxError.Detail> reporter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;
    private ClassType currentClass = ClassType.NONE;

    Resolver(
        Interpreter interpreter,
        Consumer<SyntaxError.Detail> reporter)
    {
        this.interpreter = interpreter;
        this.reporter = reporter;
    }

    public void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    private void resolve(Stmt statement) {
        switch (statement) {
            case Stmt.Assert stmt -> {
                resolve(stmt.condition());
                if (stmt.message() != null) resolve(stmt.message());
            }
            case Stmt.Block stmt -> {
                beginScope();
                resolve(stmt.statements());
                endScope();
            }
            case Stmt.Class stmt -> {
                ClassType enclosingClass = currentClass;
                currentClass = ClassType.CLASS;

                declare(stmt.name());
                define(stmt.name());

                // Static Methods and Initializer
                for (Stmt.Function method : stmt.staticMethods()) {
                    FunctionType declaration = FunctionType.FUNCTION;
                    resolveFunction(method, declaration);
                }
                if (!stmt.staticInitializer().isEmpty()) {
                    resolve(stmt.staticInitializer());
                }

                if (stmt.superclass() != null) {
                    var className = stmt.name().lexeme();
                    var superName = stmt.superclass().name().lexeme();
                    if (className.equals(superName)) {
                        error(stmt.superclass().name(),
                            "A class can't inherit from itself.");
                    }
                }

                // Superclass
                if (stmt.superclass() != null) {
                    currentClass = ClassType.SUBCLASS;
                    resolve(stmt.superclass());
                }

                if (stmt.superclass() != null) {
                    // Create a scope to put "super" in, for access
                    // by all methods defined directly on this class.
                    beginScope();
                    scopes.peek().put("super", true);
                }

                // Instance methods
                beginScope();
                scopes.peek().put("this", true);
                for (Stmt.Function method : stmt.methods()) {
                    FunctionType declaration =
                        method.name().lexeme().equals(JoeClass.INIT)
                        ? FunctionType.INITIALIZER : FunctionType.METHOD;
                    resolveFunction(method, declaration);
                }
                endScope();

                if (stmt.superclass() != null) endScope();

                currentClass = enclosingClass;
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

                resolveFunction(stmt, FunctionType.FUNCTION);
            }
            case Stmt.If stmt -> {
                resolve(stmt.condition());
                resolve(stmt.thenBranch());
                if (stmt.elseBranch() != null) resolve(stmt.elseBranch());
            }
            case Stmt.Return stmt -> {
                if (currentFunction == FunctionType.NONE) {
                    error(stmt.keyword(),
                        "Attempted 'return' from top-level code.");
                }
                if (stmt.value() != null) {
                    if (currentFunction == FunctionType.INITIALIZER) {
                        error(stmt.keyword(),
                            "Attempted to return a value from an initializer.");
                    }
                    resolve(stmt.value());
                }
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
            case Expr.Get expr -> resolve(expr.object());
            case Expr.Grouping expr -> resolve(expr.expr());
            case Expr.Literal ignored -> {}
            case Expr.Logical expr -> {
                resolve(expr.left());
                resolve(expr.right());
            }
            case Expr.Set expr -> {
                resolve(expr.value());
                resolve(expr.object());
            }
            case Expr.Super expr -> {
                if (currentClass == ClassType.NONE) {
                    error(expr.keyword(),
                        "Attempted to use 'super' outside of a class.");
                } else if (currentClass != ClassType.SUBCLASS) {
                    error(expr.keyword(),
                        "Attempted to use 'super' in a class with no superclass.");
                }
                resolveLocal(expr, expr.keyword());
            }
            case Expr.This expr -> {
                if (currentClass == ClassType.NONE) {
                    error(expr.keyword(),
                        "Attempted to use 'this' outside of any class.");
                }
                resolveLocal(expr, expr.keyword());
            }
            case Expr.Unary expr -> resolve(expr.right());
            case Expr.Ternary expr -> {
                resolve(expr.condition());
                resolve(expr.trueExpr());
                resolve(expr.falseExpr());
            }
            case Expr.Variable expr -> {
                if (!scopes.isEmpty() &&
                    scopes.peek().get(expr.name().lexeme()) == Boolean.FALSE) {
                    error(expr.name(),
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

    private void resolveFunction(Stmt.Function function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;
        beginScope();
        for (Token param : function.params()) {
            declare(param);
            define(param);
        }
        resolve(function.body());
        endScope();
        currentFunction = enclosingFunction;
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        scopes.pop();
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) return;

        Map<String, Boolean> scope = scopes.peek();
        if (scope.containsKey(name.lexeme())) {
            error(name,
                "Already a variable with this name in this scope.");
        }
        scope.put(name.lexeme(), false);
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme(), true);
    }

    // Saves the error detail.
    void error(Token token, String message) {
        var line = token.line();
        var msg = "Error at '" + token.lexeme() + "': " + message;
        reporter.accept(new SyntaxError.Detail(line, msg));
    }
}
