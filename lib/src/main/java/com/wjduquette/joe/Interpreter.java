package com.wjduquette.joe;

class Interpreter {
    void interpret(Expr expression) {
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            Joe.runtimeError(error);
        }
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
                        checkNumberOperands(expr.op(), left, right);
                        yield (double)left > (double)right;
                    }
                    case GREATER_EQUAL -> {
                        checkNumberOperands(expr.op(), left, right);
                        yield (double)left >= (double)right;
                    }
                    case LESS -> {
                        checkNumberOperands(expr.op(), left, right);
                        yield (double)left < (double)right;
                    }
                    case LESS_EQUAL -> {
                        checkNumberOperands(expr.op(), left, right);
                        yield (double)left <= (double)right;
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
                        if (left instanceof Double a &&
                            right instanceof Double b)
                        {
                            yield a + b;
                        }

                        if (left instanceof String a &&
                            right instanceof String b) {
                            yield a + b;
                        }

                        // Error handling needed
                        throw new RuntimeError(expr.op(),
                            "Operands must be two numbers or two strings.");
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

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

}
