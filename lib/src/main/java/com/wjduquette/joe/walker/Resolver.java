package com.wjduquette.joe.walker;

import com.wjduquette.joe.JoeClass;
import com.wjduquette.joe.Trace;
import com.wjduquette.joe.parser.Expr;
import com.wjduquette.joe.parser.FunctionType;
import com.wjduquette.joe.parser.Stmt;
import com.wjduquette.joe.scanner.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;

/**
 * The resolver is responsible for doing resolving variable names
 * to scopes, and for doing other scope-related checks.
 */
class Resolver {
    private enum ClassType {
        NONE,                // Not in a class
        CLASS,               // In a root class
        SUBCLASS,            // In a subclass
        RECORD               // In a record type
    }

    private final Interpreter interpreter;
    private final Consumer<Trace> reporter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.SCRIPT;
    private ClassType currentClass = ClassType.NONE;

    // Tracks whether we are in a loop or not.
    private int loopCounter = 0;

    Resolver(
        Interpreter interpreter,
        Consumer<Trace> reporter)
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
            case Stmt.Break stmt -> {
                if (loopCounter == 0) {
                    error(stmt.keyword(),
                        "'break' used outside of loop.");
                }
            }
            case Stmt.Class stmt -> {
                ClassType enclosingClass = currentClass;
                currentClass = ClassType.CLASS;

                declare(stmt.name());
                define(stmt.name());

                // Superclass
                if (stmt.superclass() != null) {
                    currentClass = ClassType.SUBCLASS;
                    resolve(stmt.superclass());
                }

                // Static Methods and Initializer
                for (Stmt.Function method : stmt.staticMethods()) {
                    FunctionType declaration = FunctionType.STATIC_METHOD;
                    resolveFunction(method, declaration);
                }

                if (!stmt.staticInit().isEmpty()) {
                    var oldFunction = currentFunction;
                    currentFunction = FunctionType.STATIC_INITIALIZER;
                    resolve(stmt.staticInit());
                    currentFunction = oldFunction;
                }

                if (stmt.superclass() != null) {
                    var className = stmt.name().lexeme();
                    var superName = stmt.superclass().name().lexeme();
                    if (className.equals(superName)) {
                        error(stmt.superclass().name(),
                            "A class can't inherit from itself.");
                    }
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
            case Stmt.Continue stmt -> {
                if (loopCounter == 0) {
                    error(stmt.keyword(),
                        "'continue' used outside of loop.");
                }
            }
            case Stmt.Expression stmt -> resolve(stmt.expr());
            case Stmt.For stmt -> {
                ++loopCounter;
                if (stmt.init() != null)      resolve(stmt.init());
                if (stmt.condition() != null) resolve(stmt.condition());
                if (stmt.updater() != null)      resolve(stmt.updater());
                resolve(stmt.body());
                --loopCounter;
            }
            case Stmt.ForEach stmt -> {
                ++loopCounter;
                resolve(stmt.items());
                resolve(stmt.body());
                --loopCounter;
            }
            case Stmt.ForEachBind stmt -> {
                ++loopCounter;
                stmt.pattern().getConstants().forEach(this::resolve);
                stmt.pattern().getBindings().forEach(this::declare);
                stmt.pattern().getBindings().forEach(this::define);
                resolve(stmt.items());
                resolve(stmt.body());
                --loopCounter;
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
            case Stmt.IfLet stmt -> {
                // Resolve any constants and the target.
                stmt.pattern().getConstants().forEach(this::resolve);
                resolve(stmt.target());

                // Resolve the variable bindings and "then" branch
                // in the pattern scope
                beginScope();
                stmt.pattern().getBindings().forEach(this::declare);
                stmt.pattern().getBindings().forEach(this::define);
                resolve(stmt.thenBranch());
                endScope();

                if (stmt.elseBranch() != null) {
                    resolve(stmt.elseBranch());
                }
            }
            case Stmt.Match stmt -> {
                resolve(stmt.expr());
                for (var c : stmt.cases()) {
                    c.pattern().getConstants().forEach(this::resolve);
                    beginScope();
                    c.pattern().getBindings().forEach(this::declare);
                    c.pattern().getBindings().forEach(this::define);
                    if (c.guard() != null) resolve(c.guard());
                    resolve(c.statement());
                    endScope();
                }
                if (stmt.matchDefault() != null) {
                    resolve(stmt.matchDefault());
                }
            }
            case Stmt.Record stmt -> {
                ClassType enclosingClass = currentClass;
                currentClass = ClassType.RECORD;

                declare(stmt.name());
                define(stmt.name());

                // Static Methods and Initializer
                for (Stmt.Function method : stmt.staticMethods()) {
                    FunctionType declaration = FunctionType.STATIC_METHOD;
                    resolveFunction(method, declaration);
                }

                if (!stmt.staticInit().isEmpty()) {
                    var oldFunction = currentFunction;
                    currentFunction = FunctionType.STATIC_INITIALIZER;
                    resolve(stmt.staticInit());
                    currentFunction = oldFunction;
                }

                // Instance methods
                beginScope();
                scopes.peek().put("this", true);
                for (Stmt.Function method : stmt.methods()) {
                    resolveFunction(method, FunctionType.METHOD);
                }
                endScope();

                currentClass = enclosingClass;
            }
            case Stmt.Return stmt -> {
                if (currentFunction == FunctionType.STATIC_INITIALIZER) {
                    error(stmt.keyword(),
                        "Attempted 'return' from static initializer.");
                }

                if (stmt.value() != null) {
                    if (currentFunction == FunctionType.INITIALIZER) {
                        error(stmt.keyword(),
                            "Attempted to return a value from an instance initializer.");
                    }
                    resolve(stmt.value());
                }
            }
            case Stmt.Switch stmt -> {
                resolve(stmt.expr());
                for (var c : stmt.cases()) {
                    c.values().forEach(this::resolve);
                    resolve(c.statement());
                }
                if (stmt.switchDefault() != null) {
                    resolve(stmt.switchDefault().statement());
                }
            }
            case Stmt.Throw stmt -> resolve(stmt.value());
            case Stmt.Var stmt -> {
                declare(stmt.name());
                resolve(stmt.value());
                define(stmt.name());
            }
            case Stmt.VarPattern stmt -> {
                stmt.pattern().getBindings().forEach(this::declare);
                stmt.pattern().getConstants().forEach(this::resolve);
                resolve(stmt.target());
                stmt.pattern().getBindings().forEach(this::define);
            }
            case Stmt.While stmt -> {
                ++loopCounter;
                resolve(stmt.condition());
                resolve(stmt.body());
                --loopCounter;
            }
        }
    }

    private void resolve(Expr expression) {
        switch (expression) {
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
            case Expr.False ignored -> {}
            case Expr.Grouping expr -> resolve(expr.expr());
            case Expr.IndexGet expr -> {
                resolve(expr.collection());
                resolve(expr.index());
            }
            case Expr.IndexIncrDecr expr -> {
                resolve(expr.collection());
                resolve(expr.index());
            }
            case Expr.IndexSet expr -> {
                resolve(expr.collection());
                resolve(expr.index());
                resolve(expr.value());
            }
            case Expr.Lambda expr ->
                resolveFunction(expr.declaration(), FunctionType.LAMBDA);
            case Expr.ListLiteral expr -> expr.list().forEach(this::resolve);
            case Expr.Literal ignored -> {}
            case Expr.Logical expr -> {
                resolve(expr.left());
                resolve(expr.right());
            }
            case Expr.MapLiteral expr -> expr.entries().forEach(this::resolve);
            case Expr.Null ignored -> {}
            case Expr.PropGet expr -> resolve(expr.object());
            case Expr.PropIncrDecr expr -> resolve(expr.object());
            case Expr.PropSet expr -> {
                resolve(expr.value());
                resolve(expr.object());
            }
            case Expr.RuleSet expr ->
                expr.exports().values().forEach(this::resolve);
            case Expr.Super expr -> {
                if (currentClass == ClassType.NONE) {
                    error(expr.keyword(),
                        "Attempted to use 'super' outside of a class.");
                } else if (currentClass == ClassType.RECORD) {
                    error(expr.keyword(),
                        "Attempted to use 'super' in a record type.");
                } else if (currentFunction == FunctionType.STATIC_INITIALIZER) {
                    error(expr.keyword(),
                        "Attempted to use 'super' in a static initializer.");
                } else if (currentFunction == FunctionType.STATIC_METHOD) {
                    error(expr.keyword(),
                        "Attempted to use 'super' in a static method.");
                } else if (currentClass != ClassType.SUBCLASS) {
                    error(expr.keyword(),
                        "Attempted to use 'super' in a class with no superclass.");
                }
                resolveLocal(expr, expr.keyword());
            }
            case Expr.This expr -> {
                if (currentClass == ClassType.NONE) {
                    error(expr.keyword(),
                        "Attempted to use '" + expr.keyword().lexeme() +
                        "' outside of any class.");
                } else if (currentFunction == FunctionType.STATIC_INITIALIZER) {
                    error(expr.keyword(),
                        "Attempted to use '" + expr.keyword().lexeme() +
                        "' in a static initializer.");
                } else if (currentFunction == FunctionType.STATIC_METHOD) {
                    error(expr.keyword(),
                        "Attempted to use '" + expr.keyword().lexeme() +
                        "' in a static method.");
                }
                // The expr.keyword might be '@'
                resolveLocal(expr, Token.synthetic("this"));
            }
            case Expr.Unary expr -> resolve(expr.right());
            case Expr.Ternary expr -> {
                resolve(expr.condition());
                resolve(expr.trueExpr());
                resolve(expr.falseExpr());
            }
            case Expr.True ignored -> {}
            case Expr.VarGet expr -> {
                if (!scopes.isEmpty() &&
                    scopes.peek().get(expr.name().lexeme()) == Boolean.FALSE) {
                    error(expr.name(),
                        "Can't read local variable in its own initializer.");
                }

                resolveLocal(expr, expr.name());
            }
            case Expr.VarIncrDecr expr -> resolveLocal(expr, expr.name());
            case Expr.VarSet expr -> {
                resolve(expr.value());
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
        var msg = "Error at '" + token.lexeme() + "': " + message;
        reporter.accept(new Trace(token.span(), msg));
    }
}
