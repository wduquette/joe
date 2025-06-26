package com.wjduquette.joe.walker;

import com.wjduquette.joe.*;
import com.wjduquette.joe.nero.*;
import com.wjduquette.joe.parser.Expr;
import com.wjduquette.joe.parser.Stmt;
import com.wjduquette.joe.patterns.Matcher;
import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.scanner.Token;
import com.wjduquette.joe.scanner.TokenType;
import com.wjduquette.joe.types.ListValue;
import com.wjduquette.joe.types.MapValue;
import com.wjduquette.joe.types.RuleSetValue;

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
                    throw new AssertError(stmt.keyword().span(), message);
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
                                stmt.superclass().name().span(),
                                "Type " + sc.name() + " cannot be subclassed.");
                        }
                        superclass = sc;
                    } else {
                        throw new RuntimeError(
                            stmt.superclass().name().span(),
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

                JoeClass klass = new WalkerClass(stmt.name().lexeme(),
                    stmt.classSpan(), superclass, staticMethods, methods);

                if (superclass != null) {
                    // Pop the "super" environment.
                    environment = environment.enclosing;
                }

                assert environment != null;
                environment.assign(stmt.name(), klass);

                // Static Initialization
                if (!stmt.staticInit().isEmpty()) {
                    try {
                        executeBlock(stmt.staticInit(), environment);
                    } catch (JoeError ex) {
                        var buff = stmt.classSpan().buffer();
                        var context = buff.lineSpan(stmt.classSpan().endLine());
                        throw ex
                            .addPendingFrame(context, "In static initializer")
                            .addPendingFrame(context,
                                "In class " + stmt.name().lexeme());
                    }
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

                while (stmt.condition() == null
                    || Joe.isTruthy(evaluate(stmt.condition()))
                ) {
                    try {
                        execute(stmt.body());
                    } catch (Break ex) {
                        break;
                    } catch (Continue ex) {
                        // Nothing else to do
                    }
                    if (stmt.updater() != null) {
                        evaluate(stmt.updater());
                    }
                }
            }
            case Stmt.ForEach stmt -> {
                var list = evaluate(stmt.items());
                Collection<?> collection = toCollection(stmt.name(), list);

                for (var item : collection) {
                    try {
                        environment.setVar(stmt.name().lexeme(), item);
                        execute(stmt.body());
                    } catch (Break ex) {
                        break;
                    } catch (Continue ex) {
                        // Nothing else to do
                    }
                }
            }
            case Stmt.ForEachBind stmt -> {
                // Evaluate the pattern's constants
                var constants = new ArrayList<>();
                stmt.pattern().getConstants().forEach(e ->
                    constants.add(evaluate(e)));

                // Evaluate the collection
                var list = evaluate(stmt.items());
                Collection<?> collection = toCollection(stmt.keyword(), list);

                for (var item : collection) {
                    try {
                        var bound = Matcher.bind(
                            joe,
                            stmt.pattern().getPattern(),
                            item,
                            constants::get);
                        if (bound != null) {
                            bind(bound);
                            execute(stmt.body());
                        }
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
            case Stmt.Match stmt -> {
                var target = evaluate(stmt.expr());
                for (var c : stmt.cases()) {
                    // case pattern ->
                    var constants = new ArrayList<>();
                    c.pattern().getConstants().forEach(e ->
                        constants.add(evaluate(e)));

                    var previous = this.environment;
                    try {
                        this.environment = new Environment(previous);
                        var bound = Matcher.bind(
                            joe,
                            c.pattern().getPattern(),
                            target,
                            constants::get);
                        if (bound != null) {
                            bind(bound);
                            var guard = c.guard() != null
                                ? evaluate(c.guard()) : true;
                            if (Joe.isTruthy(guard)) {
                                return execute(c.statement());
                            }
                        }
                    } finally {
                        this.environment = previous;
                    }
                }

                // Default case; always the last
                if (stmt.matchDefault() != null) {
                    return execute(stmt.matchDefault());
                }

                // No case matched
                return null;
            }
            case Stmt.Record stmt -> {
                // The type itself
                environment.setVar(stmt.name().lexeme(), null);

                // Static Methods
                Map<String, WalkerFunction> staticMethods = new HashMap<>();
                for (Stmt.Function method : stmt.staticMethods()) {
                    WalkerFunction function =
                        new WalkerFunction(this, method, environment, false);
                    staticMethods.put(method.name().lexeme(), function);
                }

                Map<String, WalkerFunction> methods = new HashMap<>();
                for (Stmt.Function method : stmt.methods()) {
                    WalkerFunction function =
                        new WalkerFunction(this, method, environment, false);
                    methods.put(method.name().lexeme(), function);
                }

                WalkerRecord type = new WalkerRecord(
                    stmt.name().lexeme(),
                    stmt.typeSpan(),
                    stmt.fields(),
                    staticMethods,
                    methods);

                assert environment != null;
                environment.assign(stmt.name(), type);

                // Static Initialization
                if (!stmt.staticInit().isEmpty()) {
                    try {
                        executeBlock(stmt.staticInit(), environment);
                    } catch (JoeError ex) {
                        var buff = stmt.typeSpan().buffer();
                        var context = buff.lineSpan(stmt.typeSpan().endLine());
                        throw ex
                            .addPendingFrame(context, "In static initializer")
                            .addPendingFrame(context,
                                "In type " + stmt.name().lexeme());
                    }
                }
                return null;
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
                }

                // Default case; always the last
                if (stmt.switchDefault() != null) {
                    return execute(stmt.switchDefault().statement());
                }

                // No case matched
                return null;
            }
            case Stmt.Throw stmt -> {
                var value = evaluate(stmt.value());
                if (value instanceof JoeError error) {
                    throw error.addPendingFrame(stmt.keyword().span(),
                        "Rethrowing existing error.");
                } else {
                    throw new RuntimeError(stmt.keyword().span(),
                        joe.stringify(value));
                }
            }
            case Stmt.Var stmt -> {
                Object value = evaluate(stmt.value());
                environment.setVar(stmt.name().lexeme(), value);
            }
            case Stmt.VarPattern stmt -> {
                var constants = new ArrayList<>();
                stmt.pattern().getConstants().forEach(e ->
                    constants.add(evaluate(e)));
                var target = evaluate(stmt.target());
                var bound = Matcher.bind(
                    joe,
                    stmt.pattern().getPattern(),
                    target,
                    constants::get
                );

                if (bound != null) {
                    bind(bound);
                } else {
                    throw new RuntimeError(stmt.keyword().span(),
                        "'var' pattern failed to match target value.");
                }
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

    // Bind the pattern's binding variables to the matching values from
    // the target.
    private void bind(Map<String,Object> bound) {
        for (var e : bound.entrySet()) {
            environment.setVar(e.getKey(), e.getValue());
        }
    }

    void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    //------------------------------------------------------------------------
    // Expressions

    Object evaluate(Expr expression) {
        return switch (expression) {
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
                            throw new RuntimeError(expr.op().span(),
                                "The '+' operator expects two Numbers or at least one String.");
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

                if (callee instanceof NativeCallable callable) {
                    try {
                        yield callable.call(joe, new Args(args));
                    } catch (JoeError ex) {
                        var msg = "In " + callable.callableType() + " " +
                            callable.signature();
                        if (callable.isScripted()) {
                            throw ex.addPendingFrame(expr.paren().span(), msg);
                        } else {
                            throw ex.addInfo(expr.paren().span(), msg);
                        }
                    } catch (Exception ex) {
                        throw new UnexpectedError(expr.paren().span(),
                            "Unexpected Java error: " + ex, ex);
                    }
                } else {
                    throw expected(expr.paren().span(), "callable", callee);
                }
            }
            // false
            case Expr.False ignored -> false;
            // (expr...)
            case Expr.Grouping expr -> evaluate(expr.expr());
            // expr[index]
            case Expr.IndexGet expr -> {
                var target = evaluate(expr.collection());
                var index = evaluate(expr.index());
                if (target instanceof List<?> list) {
                    int i = checkListIndex(expr.bracket(), list, index);
                    yield list.get(i);
                } else if (target instanceof Map<?,?> map) {
                    yield map.get(index);
                } else {
                    throw new RuntimeError(expr.bracket().span(),
                        "Expected indexed collection, got: " +
                        joe.typedValue(target) + ".");
                }
            }
            // ++ and -- with an indexed collection
            case Expr.IndexIncrDecr expr -> {
                var target = evaluate(expr.collection());
                var index = evaluate(expr.index());
                Object prior;

                if (target instanceof JoeList list) {
                    int i = checkListIndex(expr.bracket(), list, index);
                    prior = list.get(i);
                } else if (target instanceof JoeMap map) {
                    prior = map.get(index);
                } else {
                    throw new RuntimeError(expr.bracket().span(),
                        "Expected indexed collection, got: " +
                            joe.typedValue(target));
                }

                checkNumericTarget(expr.op(), prior);

                double assigned = expr.op().type() == TokenType.PLUS_PLUS
                    ? (double)prior + 1
                    : (double)prior - 1;
                var result = expr.isPre() ? assigned : prior;

                if (target instanceof JoeList list) {
                    int i = checkListIndex(expr.bracket(), list, index);
                    list.set(i, assigned);
                } else {
                    ((JoeMap)target).put(index, assigned);
                }

                yield result;
            }
            // expr[index] = value, etc.
            case Expr.IndexSet expr -> {
                var target = evaluate(expr.collection());
                var index = evaluate(expr.index());
                var value = evaluate(expr.value());

                if (target instanceof JoeList list) {
                    int i = checkListIndex(expr.bracket(), list, index);

                    var right = value;

                    if (expr.op().type() != TokenType.EQUAL) {
                        var left = list.get(i);
                        right = computeExtendedAssignment(left, expr.op(), right);
                    }

                    list.set(i, right);
                    yield right;
                } else if (target instanceof JoeMap map) {
                    var right = value;

                    if (expr.op().type() != TokenType.EQUAL) {
                        var left = map.get(index);
                        right = computeExtendedAssignment(left, expr.op(), right);
                    }

                    map.put(index, right);
                    yield right;
                } else {
                    throw new RuntimeError(expr.bracket().span(),
                        "Expected indexed collection, got: " +
                            joe.typedValue(target));
                }
            }
            // Return a callable for the given lambda
            case Expr.Lambda expr ->
                new WalkerFunction(this, expr.declaration(), environment, false);
            // A list literal
            case Expr.ListLiteral expr -> {
                var list = new ListValue();
                for (var e : expr.list()) {
                    list.add(evaluate(e));
                }
                yield list;
            }
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
            // A map literal
            case Expr.MapLiteral expr -> {
                var map = new MapValue();
                for (var i = 0; i < expr.entries().size(); i += 2) {
                    var key = evaluate(expr.entries().get(i));
                    var value = evaluate(expr.entries().get(i + 1));
                    map.put(key, value);
                }
                yield map;
            }
            // A pattern match
            case Expr.Match expr -> {
                // FIRST, Evaluate the target and any pattern constants
                var target = evaluate(expr.target());
                var constants = new ArrayList<>();
                expr.pattern().getConstants().forEach(e ->
                    constants.add(evaluate(e)));

                // NEXT, do the pattern match
                var bound = Matcher.bind(
                    joe,
                    expr.pattern().getPattern(),
                    target,
                    constants::get
                );

                if (bound != null) {
                    bind(bound);
                    yield true;
                } else {
                    bound = new LinkedHashMap<>();
                    for (var name : expr.pattern().getBindings()) {
                        bound.put(name.lexeme(), null);
                    }
                    bind(bound);
                    yield false;
                }
            }
            // null
            case Expr.Null ignored -> null;
            // Get an object property.  The expression must evaluate to
            // a JoeValue, i.e., a JoeInstance or a ProxiedValue.
            case Expr.PropGet expr -> {
                Object object = evaluate(expr.object());
                if (object == null) {
                    throw new RuntimeError(expr.name().span(),
                        "Tried to retrieve '" + expr.name().lexeme() +
                            "' property from null value.");
                }
                JoeValue instance = joe.getJoeValue(object);
                yield instance.get(expr.name().lexeme());
            }
            // ++ and -- with an object property
            case Expr.PropIncrDecr expr -> {
                Object object = evaluate(expr.object());
                JoeValue instance = joe.getJoeValue(object);
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
            case Expr.PropSet expr -> {
                Object object = evaluate(expr.object());
                JoeValue instance = joe.getJoeValue(object);

                Object right = evaluate(expr.value());
                var name = expr.name().lexeme();

                if (expr.op().type() != TokenType.EQUAL) {
                    var left = instance.get(name);
                    right = computeExtendedAssignment(left, expr.op(), right);
                }

                instance.set(name, right);
                yield right;
            }
            // Evaluate the rule set.
            case Expr.RuleSet expr -> {
                var rsc = new RuleSetCompiler(expr.ruleSet());
                rsc.setFactFactory(ListFact::new);
                var ruleset = rsc.compile();

                if (!ruleset.isStratified()) {
                    throw new RuntimeError(expr.keyword().span(),
                        "Rule set is not stratified.");
                }

                var exports = new HashMap<String, Object>();

                for (var export : expr.exports().entrySet()) {
                    var name = export.getKey();
                    var callable = evaluate(export.getValue());
                    exports.put(name.lexeme(), checkCallable(name, callable));
                }

                yield new RuleSetValue(ruleset, exports);
            }
            // Handle `super.<methodName>` in methods
            case Expr.Super expr -> {
                int distance = locals.get(expr);
                JoeClass superclass = (JoeClass)environment.getAt(
                    distance, "super");
                JoeValue instance = (JoeValue)environment.getAt(
                    distance - 1, "this");
                JoeCallable method =
                    superclass.bind(instance, expr.method().lexeme());

                if (method == null) {
                    throw new RuntimeError(expr.method().span(),
                        "Undefined property '" +
                            expr.method().lexeme() + "'.");
                }

                yield method;
            }
            // Handle `this.<property>` in methods.  Note: the expression's
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
            // true
            case Expr.True ignored -> true;
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
            case Expr.VarGet expr -> lookupVariable(expr.name(), expr);
            // ++ and -- with a variable name
            case Expr.VarIncrDecr expr -> {
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
            // Assign a value to a variable or an object property, using
            // =, +=, -=, *=, /=
            case Expr.VarSet expr -> {
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
        };
    }

    //-------------------------------------------------------------------------
    // Evaluation Helpers

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
        return switch(op.type()) {
            case PLUS_EQUAL -> {
                if (left instanceof String s) {
                    yield s + joe.stringify(right);
                } else if (right instanceof String s) {
                    yield joe.stringify(left) + s;
                } else if (left instanceof Double a && right instanceof Double b) {
                    yield a + b;
                } else {
                    throw new RuntimeError(op.span(),
                        "The '+' operator expects two Numbers or at least one String.");
                }
            }
            case MINUS_EQUAL -> {
                checkNumberOperands(op, left, right);
                yield (double)left - (double)right;
            }
            case STAR_EQUAL -> {
                checkNumberOperands(op, left, right);
                yield (double)left * (double)right;
            }
            case SLASH_EQUAL -> {
                checkNumberOperands(op, left, right);
                yield (double)left / (double)right;
            }
            default -> throw new IllegalStateException(
                "Unexpected operator: " + op.type());
        };
    }

    // Gets the argument as a collection, if possible
    private Collection<?> toCollection(Token token, Object arg) {
        if (arg instanceof Collection<?> c) {
            return c;
        } else {
            var instance = joe.getJoeValue(arg);
            if (instance.canIterate()) {
                return instance.getItems();
            } else {
                throw new RuntimeError(token.span(),
                    "Expected iterable, got: " +
                        joe.typedValue(arg) + ".");
            }
        }
    }

    //-------------------------------------------------------------------------
    // Error Checking

    private JoeError expected(SourceBuffer.Span context, String what, Object got) {
        var message = "Expected " + what + ", got: " +
            joe.typedValue(got) + ".";
        return new RuntimeError(context, message);
    }


    private void checkNumberOperands(
        Token operator,
        Object left,
        Object right)
    {
        if (left instanceof Double && right instanceof Double) return;

        var op = operator.lexeme().substring(0, 1);

        throw new RuntimeError(operator.span(),
            "The '" + op + "' operator expects two numeric operands.");
    }

    private RuntimeError notSimilar(Token operator) {
        return new RuntimeError(operator.span(),
            "The '" + operator.lexeme() + "' operator expects two Numbers or two Strings.");
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator.span(), "Operand must be a number.");
    }

    private void checkNumericTarget(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator.span(), "Target of '" +
            operator.lexeme() +
            "' must contain a number.");
    }

    private int checkListIndex(Token bracket, List<?> list, Object index) {
        if (index instanceof Double d) {
            int i = d.intValue();
            if (i >= 0 && i < list.size()) {
                return i;
            } else {
                throw new RuntimeError(bracket.span(),
                    "List index out of range [0, " + (list.size() - 1) + "]: " + i + ".");
            }
        } else {
            throw expected(bracket.span(), "list index", index);
        }
    }

    private JoeCallable checkCallable(Token token, Object value) {
        if (value instanceof JoeCallable jc) {
            return jc;
        } else {
            throw new RuntimeError(token.span(),
                "Expected callable, got: " + joe.typedValue(value) + ".");
        }
    }
}
