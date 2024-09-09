package com.wjduquette.joe;

import java.util.*;

public class Interpreter {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Joe joe;
    final Environment globals;
    private Environment environment;
    private final Map<Expr, Integer> locals = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    public Interpreter(Joe joe) {
        this.joe = joe;
        this.globals = joe.getGlobals();
        this.environment = globals;
    }

    //-------------------------------------------------------------------------
    // Debugging API

    @SuppressWarnings("unused")
    void dumpEnvironment() {
        var env = environment;

        while (env != null) {
            env.dump();
            env = env.enclosing;
        }
    }

    //-------------------------------------------------------------------------
    // Public API

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
                    var message = (stmt.message() != null
                        ? joe.stringify(evaluate(stmt.message()))
                        : "Assertion unmet: " + joe.recodify(stmt.condition()));
                    throw new AssertError(stmt.keyword().line(), message);
                }
            }
            case Stmt.Block stmt -> {
                return executeBlock(stmt.statements(), new Environment(environment));
            }
            case Stmt.Class stmt -> {
                // Valid superclass?
                JoeClass superclass = null;
                if (stmt.superclass() != null) {
                    var object = evaluate(stmt.superclass());
                    if (object instanceof JoeClass sc) {
                        superclass = sc;
                    } else {
                        throw new RuntimeError(stmt.superclass().name(),
                            "Superclass must be a class.");
                    }
                }

                // The class itself
                environment.define(stmt.name().lexeme(), null);

                // Static Methods
                Map<String, JoeFunction> staticMethods = new HashMap<>();
                for (Stmt.Function method : stmt.staticMethods()) {
                    JoeFunction function = new JoeFunction(method, environment,
                        false);
                    staticMethods.put(method.name().lexeme(), function);
                }


                if (stmt.superclass() != null) {
                    // Push a new environment to contain "super"
                    environment = new Environment(environment);
                    environment.define("super", superclass);
                }

                Map<String, JoeFunction> methods = new HashMap<>();
                for (Stmt.Function method : stmt.methods()) {
                    JoeFunction function = new JoeFunction(method, environment,
                        stmt.name().lexeme().equals("init"));
                    methods.put(method.name().lexeme(), function);
                }

                JoeClass klass =
                    new JoeClass(stmt.name().lexeme(),
                        superclass, staticMethods, methods);

                if (superclass != null) {
                    // Pop the "super" environment.
                    environment = environment.enclosing;
                }

                environment.assign(stmt.name(), klass);

                // Static Initialization
                if (!stmt.staticInitializer().isEmpty()) {
                    executeBlock(stmt.staticInitializer(), environment);
                }
                return null;
            }
            case Stmt.Expression stmt -> {
                return evaluate(stmt.expr());
            }
            case Stmt.For stmt -> {
                if (stmt.init() != null) {
                    execute(stmt.init());
                }

                while (Joe.isTruthy(evaluate(stmt.condition()))) {
                    execute(stmt.body());
                    evaluate(stmt.incr());
                }
            }
            case Stmt.Function stmt -> {
                var function = new JoeFunction(stmt, environment, false);
                environment.define(stmt.name().lexeme(), function);
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
            case Stmt.Var stmt -> {
                Object value = null;
                if (stmt.initializer() != null) {
                    value = evaluate(stmt.initializer());
                }
                environment.define(stmt.name().lexeme(), value);
            }
            case Stmt.While stmt -> {
                while (Joe.isTruthy(evaluate(stmt.condition()))) {
                    execute(stmt.body());
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
            case Expr.Assign expr -> {
                Object value = evaluate(expr.value());
                var distance = locals.get(expr);

                if (distance != null) {
                    environment.assignAt(distance, expr.name(), value);
                } else {
                    globals.assign(expr.name(), value);
                }
                yield value;
            }
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
                            throw new RuntimeError(expr.op(),
                                "'+' cannot combine the given operands.");
                        }

                    }
                    default -> throw new IllegalStateException(
                        "Unexpected operator: " + expr.op());
                };
            }
            case Expr.Call expr -> {
                Object callee = evaluate(expr.callee());

                var args = new ArrayList<>();
                for (var arg : expr.arguments()) {
                    args.add(evaluate(arg));
                }

                if (callee instanceof JoeCallable callable) {
                    yield callable.call(joe, new ArgQueue(args));
                } else {
                    // TODO add recodify(expr.callee()) as a stack frame!
                    throw joe.expected("a callable", callee);
                }
            }
            case Expr.Get expr -> {
                Object object = evaluate(expr.object());
                JoeObject instance = joe.getJoeObject(object);
                yield instance.get(expr.name().lexeme());
            }
            case Expr.Grouping expr -> evaluate(expr.expr());
            case Expr.Lambda expr ->
                throw new RuntimeError(expr.token(), "Lambdas not yet implemented.");
            case Expr.Literal expr -> expr.value();
            case Expr.Logical expr -> {
                Object left = evaluate(expr.left());

                if (expr.op().type() == TokenType.OR) {
                    if (Joe.isTruthy(left)) yield left;
                } else {
                    if (!Joe.isTruthy(left)) yield left;
                }

                yield evaluate(expr.right());
            }
            case Expr.Set expr -> {
                Object object = evaluate(expr.object());
                JoeObject instance = joe.getJoeObject(object);

                Object value = evaluate(expr.value());
                var name = expr.name().lexeme();
                instance.set(name, value);
                yield value;
            }
            case Expr.Super expr -> {
                int distance = locals.get(expr);
                JoeClass superclass = (JoeClass)environment.getAt(
                    distance, "super");
                JoeInstance instance = (JoeInstance)environment.getAt(
                    distance - 1, "this");
                JoeFunction method =
                    superclass.findMethod(expr.method().lexeme());

                if (method == null) {
                    throw new RuntimeError(expr.method(),
                        "Undefined property '" +
                            expr.method().lexeme() + "'.");
                }

                yield method.bind(instance);
            }
            case Expr.This expr -> lookupVariable(expr.keyword(), expr);
            case Expr.Ternary expr -> {
                var condition = Joe.isTruthy(evaluate(expr.condition()));

                if (Joe.isTruthy(condition)) {
                    yield evaluate(expr.trueExpr());
                } else {
                    yield evaluate(expr.falseExpr());
                }
            }
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

    //-------------------------------------------------------------------------
    // Predicates

    //-------------------------------------------------------------------------
    // Error Checking

    private void checkNumberOperands(
        Token operator,
        Object left,
        Object right)
    {
        if (left instanceof Double && right instanceof Double) return;

        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private RuntimeError notSimilar(Token operator) {
        return new RuntimeError(operator,
            "Expected two doubles or two strings.");
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }
}
