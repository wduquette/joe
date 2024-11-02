package com.wjduquette.joe.walker;

import com.wjduquette.joe.*;

import java.util.*;

class Interpreter {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Joe joe;
    final GlobalEnvironment globals = new GlobalEnvironment();
    private Environment environment;
    private final Map<Expr, Integer> locals = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    public Interpreter(Joe joe) {
        this.joe = joe;
        this.environment = globals;
    }

    //-------------------------------------------------------------------------
    // Debugging API

    @SuppressWarnings("unused")
    void dumpEnvironment() {
        System.out.println("Local Variables:");
        for (var e : locals.entrySet()) {
            // TODO: Use source buffer to get expression e's text.
            System.out.println("  [" + e.getValue() + "]: " + e.getKey());
        }

        var env = environment;

        while (env != null) {
            env.dump();
            env = env.enclosing;
        }
    }

    //-------------------------------------------------------------------------
    // Public API

    public GlobalEnvironment globals() {
        return globals;
    }

    public Object interpret(List<Stmt> statements) throws RuntimeError {
        Object result = null;
        for (Stmt statement : statements) {
            result = execute(statement);
        }
        return result;
    }

    private Object execute(Stmt statement) {
        switch (statement) {
            case Stmt.Assert stmt -> {
                var condition = evaluate(stmt.condition());
                if (!Joe.isTruthy(condition)) {
                    var message = joe.stringify(evaluate(stmt.message()));
                    throw new AssertError(stmt.keyword().line(), message);
                }
            }
            case Stmt.Block stmt -> {
                return executeBlock(stmt.statements(), new Environment(environment));
            }
            case Stmt.Break ignored -> throw new Break();
            case Stmt.Class stmt -> {
                // Valid superclass?
                JoeClass superclass = null;
                if (stmt.superclass() != null) {
                    var object = evaluate(stmt.superclass());
                    if (object instanceof JoeClass sc) {
                        if (!sc.canBeExtended()) {
                            throw new RuntimeError(
                                stmt.superclass().name().line(),
                                "Type " + sc.name() + " cannot be subclassed.");
                        }
                        superclass = sc;
                    } else {
                        throw new RuntimeError(
                            stmt.superclass().name().line(),
                            "Superclass must be a class.");
                    }
                }

                // The class itself
                environment.setVar(stmt.name().lexeme(), null);

                // Static Methods
                Map<String, WalkerFunction> staticMethods = new HashMap<>();
                for (Stmt.Function method : stmt.staticMethods()) {
                    WalkerFunction function =
                        new WalkerFunction(this, method, environment, false);
                    staticMethods.put(method.name().lexeme(), function);
                }


                if (stmt.superclass() != null) {
                    // Push a new environment to contain "super"
                    environment = new Environment(environment);
                    environment.setVar("super", superclass);
                }

                Map<String, WalkerFunction> methods = new HashMap<>();
                for (Stmt.Function method : stmt.methods()) {
                    WalkerFunction function =
                        new WalkerFunction(this, method, environment,
                            stmt.name().lexeme().equals("init"));
                    methods.put(method.name().lexeme(), function);
                }

                JoeClass klass = new ScriptedClass(stmt.name().lexeme(),
                    superclass, staticMethods, methods);

                if (superclass != null) {
                    // Pop the "super" environment.
                    environment = environment.enclosing;
                }

                assert environment != null;
                environment.assign(stmt.name(), klass);

                // Static Initialization
                if (!stmt.staticInitializer().isEmpty()) {
                    executeBlock(stmt.staticInitializer(), environment);
                }
                return null;
            }
            case Stmt.Continue ignored -> throw new Continue();
            case Stmt.Expression stmt -> {
                return evaluate(stmt.expr());
            }
            case Stmt.For stmt -> {
                if (stmt.init() != null) {
                    execute(stmt.init());
                }

                while (Joe.isTruthy(evaluate(stmt.condition()))) {
                    try {
                        execute(stmt.body());
                    } catch (Break ex) {
                        break;
                    } catch (Continue ex) {
                        // Nothing else to do
                    }
                    evaluate(stmt.incr());
                }
            }
            case Stmt.ForEach stmt -> {
                var list = evaluate(stmt.listExpr());
                Collection<?> collection = toCollection(stmt.varName(), list);

                for (var item : collection) {
                    try {
                        environment.setVar(stmt.varName().lexeme(), item);
                        execute(stmt.body());
                    } catch (Break ex) {
                        break;
                    } catch (Continue ex) {
                        // Nothing else to do
                    }
                }
            }
            case Stmt.Function stmt -> {
                var function = new WalkerFunction(this, stmt, environment, false);
                environment.setVar(stmt.name().lexeme(), function);
            }
            case Stmt.If stmt -> {
                if (Joe.isTruthy(evaluate(stmt.condition()))) {
                    return execute(stmt.thenBranch());
                } else if (stmt.elseBranch() != null) {
                    return execute(stmt.elseBranch());
                }
            }
            case Stmt.Return stmt -> {
                Object value = null;
                if (stmt.value() != null) value = evaluate(stmt.value());

                throw new Return(value);
            }
            case Stmt.Switch stmt -> {
                var value = evaluate(stmt.expr());
                for (var c : stmt.cases()) {
                    // A normal case has values.
                    for (var caseValue : c.values()) {
                        var cv = evaluate(caseValue);
                        if (Joe.isEqual(value, cv)) {
                            return execute(c.statement());
                        }
                    }

                    // Default case; always the last
                    if (c.values().isEmpty()) {
                        return execute(c.statement());
                    }
                }

                // No case matched
                return null;
            }
            case Stmt.Throw stmt -> {
                var value = evaluate(stmt.value());
                if (value instanceof JoeError error) {
                    throw error;
                } else {
                    throw new JoeError(joe.stringify(value));
                }
            }
            case Stmt.Var stmt -> {
                Object value = null;
                if (stmt.initializer() != null) {
                    value = evaluate(stmt.initializer());
                }
                environment.setVar(stmt.name().lexeme(), value);
            }
            case Stmt.While stmt -> {
                while (Joe.isTruthy(evaluate(stmt.condition()))) {
                    try {
                        execute(stmt.body());
                    } catch (Break ex) {
                        break;
                    } catch (Continue ex) {
                        // Nothing special to do.
                    }
                }
            }
        }

        return null;
    }

    Object executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        Object result = null;

        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                result = execute(statement);
            }
        } finally {
            this.environment = previous;
        }

        return result;
    }

    void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    //------------------------------------------------------------------------
    // Expressions

    Object evaluate(Expr expression) {
        return switch (expression) {
            // Assign a value to a variable or an object property, using
            // =, +=, -=, *=, /=
            case Expr.Assign expr -> {
                Object right = evaluate(expr.value());
                var distance = locals.get(expr);

                if (expr.op().type() != TokenType.EQUAL) {
                    Object left = lookupVariable(expr.name(), expr);
                    right = computeExtendedAssignment(left, expr.op(), right);
                }

                if (distance != null) {
                    environment.assignAt(distance, expr.name(), right);
                } else {
                    globals.assign(expr.name(), right);
                }
                yield right;
            }
            // Compute any binary operation except for && and ||
            case Expr.Binary expr -> {
                Object left = evaluate(expr.left());
                Object right = evaluate(expr.right());

                yield switch (expr.op().type()) {
                    case BANG_EQUAL -> !Joe.isEqual(left, right);
                    case EQUAL_EQUAL -> Joe.isEqual(left, right);
                    case GREATER -> {
                        if (left instanceof Double a && right instanceof Double b) {
                            yield a > b;
                        }

                        if (left instanceof String a && right instanceof String b) {
                            yield a.compareTo(b) > 0;
                        }

                        throw notSimilar(expr.op());
                    }
                    case GREATER_EQUAL -> {
                        if (left instanceof Double a && right instanceof Double b) {
                            yield a >= b;
                        }

                        if (left instanceof String a && right instanceof String b) {
                            yield a.compareTo(b) >= 0;
                        }

                        throw notSimilar(expr.op());
                    }
                    case IN -> toCollection(expr.op(), right).contains(left);
                    case LESS -> {
                        if (left instanceof Double a && right instanceof Double b) {
                            yield a < b;
                        }

                        if (left instanceof String a && right instanceof String b) {
                            yield a.compareTo(b) < 0;
                        }

                        throw notSimilar(expr.op());
                    }
                    case LESS_EQUAL -> {
                        if (left instanceof Double a && right instanceof Double b) {
                            yield a <= b;
                        }

                        if (left instanceof String a && right instanceof String b) {
                            yield a.compareTo(b) <= 0;
                        }

                        throw notSimilar(expr.op());
                    }
                    case MINUS -> {
                        checkNumberOperands(expr.op(), left, right);
                        yield (double)left - (double)right;
                    }
                    case NI -> !toCollection(expr.op(), right).contains(left);
                    case SLASH -> {
                        checkNumberOperands(expr.op(), left, right);
                        yield (double)left / (double)right;
                    }
                    case STAR -> {
                        checkNumberOperands(expr.op(), left, right);
                        yield (double)left * (double)right;
                    }
                    case PLUS -> {
                        if (left instanceof Double a && right instanceof Double b) {
                           yield a + b;
                        } else if (left instanceof String || right instanceof String) {
                            yield joe.stringify(left) + joe.stringify(right);
                        } else {
                            throw new RuntimeError(expr.op().line(),
                                "'+' cannot combine the given operands.");
                        }

                    }
                    default -> throw new IllegalStateException(
                        "Unexpected operator: " + expr.op());
                };
            }
            // Call a function or method
            case Expr.Call expr -> {
                Object callee = evaluate(expr.callee());

                var args = new Object[expr.arguments().size()];
                for (var i = 0; i < expr.arguments().size(); i++) {
                    args[i] = evaluate(expr.arguments().get(i));
                }

                if (callee instanceof JoeCallable callable) {
                    yield callable.call(joe, new Args(args));
                } else {
                    // TODO add recodify(expr.callee()) as a stack frame!
                    throw joe.expected("a callable", callee);
                }
            }
            // Get an object property.  The expression must evaluate to
            // a JoeObject, i.e., a JoeInstance or a ProxiedValue.
            case Expr.Get expr -> {
                Object object = evaluate(expr.object());
                JoeObject instance = joe.getJoeObject(object);
                yield instance.get(expr.name().lexeme());
            }
            // (expr...)
            case Expr.Grouping expr -> evaluate(expr.expr());
            // Return a callable for the given lambda
            case Expr.Lambda expr ->
                new WalkerFunction(this, expr.declaration(), environment, false);
            // Any literal
            case Expr.Literal expr -> expr.value();
            // && and ||
            case Expr.Logical expr -> {
                Object left = evaluate(expr.left());

                if (expr.op().type() == TokenType.OR) {
                    if (Joe.isTruthy(left)) yield left;
                } else {
                    if (!Joe.isTruthy(left)) yield left;
                }

                yield evaluate(expr.right());
            }
            // ++ and -- with a variable name
            case Expr.PrePostAssign expr -> {
                var distance = locals.get(expr);
                Object prior = lookupVariable(expr.name(), expr);
                checkNumericTarget(expr.op(), prior);

                double assigned = expr.op().type() == TokenType.PLUS_PLUS
                    ? (double)prior + 1
                    : (double)prior - 1;
                var result = expr.isPre() ? assigned : prior;

                if (distance != null) {
                    environment.assignAt(distance, expr.name(), assigned);
                } else {
                    globals.assign(expr.name(), assigned);
                }
                yield result;
            }
            // ++ and -- with an object property
            case Expr.PrePostSet expr -> {
                Object object = evaluate(expr.object());
                JoeObject instance = joe.getJoeObject(object);
                var name = expr.name().lexeme();
                var prior = instance.get(name);
                checkNumericTarget(expr.op(), prior);

                double assigned = expr.op().type() == TokenType.PLUS_PLUS
                    ? (double)prior + 1
                    : (double)prior - 1;
                var result = expr.isPre() ? assigned : prior;

                instance.set(name, assigned);
                yield result;
            }
            // Assign a value to an object property using =, +=, -=, *=, /=
            case Expr.Set expr -> {
                Object object = evaluate(expr.object());
                JoeObject instance = joe.getJoeObject(object);

                Object right = evaluate(expr.value());
                var name = expr.name().lexeme();

                if (expr.op().type() != TokenType.EQUAL) {
                    var left = instance.get(name);
                    right = computeExtendedAssignment(left, expr.op(), right);
                }

                instance.set(name, right);
                yield right;
            }
            // Handle `super.<methodName>` in methods
            case Expr.Super expr -> {
                int distance = locals.get(expr);
                JoeClass superclass = (JoeClass)environment.getAt(
                    distance, "super");
                JoeObject instance = (JoeObject)environment.getAt(
                    distance - 1, "this");
                JoeCallable method =
                    superclass.bind(instance, expr.method().lexeme());

                if (method == null) {
                    throw new RuntimeError(expr.method().line(),
                        "Undefined property '" +
                            expr.method().lexeme() + "'.");
                }

                yield method;
            }
            // Handle `this.<property>` in methods.  Note: the expr's
            // keyword might be "@".
            case Expr.This expr -> lookupVariable(Token.synthetic("this"), expr);
            // The ternary `? :` operator
            case Expr.Ternary expr -> {
                var condition = Joe.isTruthy(evaluate(expr.condition()));

                if (Joe.isTruthy(condition)) {
                    yield evaluate(expr.trueExpr());
                } else {
                    yield evaluate(expr.falseExpr());
                }
            }
            // The unary operators
            case Expr.Unary expr -> {
                Object right = evaluate(expr.right());

                yield switch (expr.op().type()) {
                    case BANG -> !Joe.isTruthy(right);
                    case MINUS -> {
                        checkNumberOperand(expr.op(), right);
                        yield -(double)right;
                    }
                    default -> throw new IllegalStateException(
                        "Unexpected operator: " + expr.op());
                };
            }
            // Get a variable's value
            case Expr.Variable expr -> lookupVariable(expr.name(), expr);
        };
    }

    private Object lookupVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, name.lexeme());
        } else {
            return globals.get(name);
        }
    }

    // Given the value of a variable or property, and one of the extended
    // assignment operators, computes the new value of the variable or
    // property.
    private Object computeExtendedAssignment(
        Object left, Token op, Object right
    ) {
        // FIRST, check for concatenation
        if (left instanceof String s && op.type() == TokenType.PLUS_EQUAL) {
            return s + joe.stringify(right);
        }

        // NEXT, both must be numbers.
        checkNumberOperands(op, left, right);

        return switch(op.type()) {
            case PLUS_EQUAL -> (double)left + (double)right;
            case MINUS_EQUAL -> (double)left - (double)right;
            case STAR_EQUAL -> (double)left * (double)right;
            case SLASH_EQUAL -> (double)left / (double)right;
            default -> throw new IllegalStateException(
                "Unexpected operator: " + op.type());
        };
    }

    // Gets the argument as a collection, if possible
    private Collection<?> toCollection(Token token, Object arg) {
        return switch (arg) {
            case Collection<?> c -> c;
            case JoeIterable i -> i.getItems();
            default -> {
                var instance = joe.getJoeObject(arg);
                if (instance.canIterate()) {
                    yield instance.getItems();
                } else {
                    throw new RuntimeError(token.line(),
                        "Expected iterable, got: " +
                            joe.typeName(arg) + " '" +
                            joe.codify(arg) + "'.");
                }
            }
        };
    }

    //-------------------------------------------------------------------------
    // Error Checking

    private void checkNumberOperands(
        Token operator,
        Object left,
        Object right)
    {
        if (left instanceof Double && right instanceof Double) return;

        throw new RuntimeError(operator.line(), "Operands must be numbers.");
    }

    private RuntimeError notSimilar(Token operator) {
        return new RuntimeError(operator.line(),
            "Expected two doubles or two strings.");
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator.line(), "Operand must be a number.");
    }

    private void checkNumericTarget(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator.line(), "Target of operand must contain a number.");
    }
}
