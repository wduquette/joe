package com.wjduquette.joe;

class Interpreter {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Joe joe;

    //-------------------------------------------------------------------------
    // Constructor

    public Interpreter(Joe joe) {
        this.joe = joe;
    }

    //-------------------------------------------------------------------------
    // Public API

    public Object interpret(Expr expression) throws RuntimeError {
        Object value = evaluate(expression);
        System.out.println(stringify(value));
        return value;
    }

    Object evaluate(Expr expression) {
        return switch (expression) {
            case Expr.Binary expr -> {
                Object left = evaluate(expr.left());
                Object right = evaluate(expr.right());

                yield switch (expr.op().type()) {
                    case BANG_EQUAL -> !isEqual(left, right);
                    case EQUAL_EQUAL -> isEqual(left, right);
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
                        }

                        if (left instanceof String a && right instanceof String b) {
                            yield a + b;
                        }

                        throw notSimilar(expr.op());
                    }
                    default -> throw new IllegalStateException(
                        "Unexpected operator: " + expr.op());
                };
            }
            case Expr.Grouping expr -> evaluate(expr.expr());
            case Expr.Literal expr -> expr.value();
            case Expr.Unary expr -> {
                Object right = evaluate(expr.right());

                yield switch (expr.op().type()) {
                    case BANG -> !isTruthy(right);
                    case MINUS -> {
                        checkNumberOperand(expr.op(), right);
                        yield -(double)right;
                    }
                    default -> throw new IllegalStateException(
                        "Unexpected operator: " + expr.op());
                };
            }
        };
    }

    //-------------------------------------------------------------------------
    // Predicates

    private String stringify(Object object) {
        if (object == null) return "null";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

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
            "Operands must both be numbers or both be strings.");
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }
}
