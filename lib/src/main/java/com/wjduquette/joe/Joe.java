package com.wjduquette.joe;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Joe {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final GlobalEnvironment globals;
    private final Interpreter interpreter;
    private final Codifier codifier;
    private final Map<Class<?>, TypeProxy<?>> proxyTable = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    public Joe() {
        globals = new GlobalEnvironment();
        interpreter = new Interpreter(this);
        codifier = new Codifier(this);

        StandardLibrary.LIB.install(this);
    }

    //-------------------------------------------------------------------------
    // Configuration and Embedding

    public GlobalEnvironment getGlobals() {
        return globals;
    }

    /**
     * Installs a native function into Joe's global environment.
     * @param function The function
     */
    public void installGlobalFunction(NativeFunction function) {
        globals.define(function.name(), function);
    }

    public void installType(TypeProxy<?> typeProxy) {
        // FIRST, install the proxy into the proxy table.
        for (var cls : typeProxy.getProxiedTypes()) {
            proxyTable.put(cls, typeProxy);
        }

        // NEXT, install the type into the environment.
        globals.define(typeProxy.getTypeName(), typeProxy);
    }

    /**
     * Installs a resource file into the engine, executing it as a Joe
     * script.  This is the standard way to add library code written
     * in Joe from within Java.
     * @param cls The class
     * @param resource The resource name, including any relative path.
     */
    public void installScriptResource(Class<?> cls, String resource) {
        try (var stream = cls.getResourceAsStream(resource)) {
            assert stream != null;
            var source = new String(stream.readAllBytes(),
                StandardCharsets.UTF_8);
            run(source);
        } catch (SyntaxError ex) {
            System.err.println("Could not load script resource '" +
                resource + "' relative to class " +
                cls.getCanonicalName() + ":\n" + ex.getMessage());
            ex.printErrorsByLine(System.err);
            System.exit(1);
        } catch (JoeError ex) {
            System.err.println("Could not install script resource '" +
                resource + "' relative to class " +
                cls.getCanonicalName() + ":\n" + ex.getMessage());
            System.err.println(ex.getJoeStackTrace());
            System.exit(1);
        } catch (IOException ex) {
            System.err.println("Could not read script resource '" +
                resource + "' relative to class\n" +
                cls.getCanonicalName() + ": " + ex.getMessage());
            System.exit(1);
        }
    }

    //-------------------------------------------------------------------------
    // Script Execution

    /**
     * Reads the given file and executes its content as a script.
     * @param path The file's path
     * @return The script's result
     * @throws IOException if the file cannot be read.
     * @throws SyntaxError if the script could not be compiled.
     * @throws JoeError on all runtime errors.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Object runFile(String path)
        throws IOException, SyntaxError, JoeError
    {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        var script = new String(bytes, Charset.defaultCharset());

        return run(script);
    }

    /**
     * Executes the script, throwing an appropriate error on failure.
     * @param source The input
     * @return The script's result
     * @throws SyntaxError if the script could not be compiled.
     * @throws JoeError on all runtime errors.
     */
    public Object run(String source) throws SyntaxError, JoeError {
        var details = new ArrayList<SyntaxError.Detail>();

        Scanner scanner = new Scanner(source, details::add);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens, details::add);
        var statements = parser.parse();

        // Stop if there was a syntax error.
        if (!details.isEmpty()) {
            throw new SyntaxError("Syntax error in input, halting.", details);
        }

        Resolver resolver = new Resolver(interpreter, details::add);
        resolver.resolve(statements);

        // Stop if there was a resolution error.
        if (!details.isEmpty()) {
            throw new SyntaxError("Syntax error in input, halting.", details);
        }

        return interpreter.interpret(statements);
    }

    //-------------------------------------------------------------------------
    // Internal Support -- for use within this package

    /**
     * Gets the engine's actual interpreter.
     * @return The interpreter.
     */
    Interpreter interp() {
        return interpreter;
    }

    /**
     * Looks for a proxy for this object's class or its superclasses.
     *
     * <p><b>Note:</b> The construction of the proxyTable depends on
     * the proxied types returned by each TypeProxy; and the proxiedTypes
     * are constrained to be compatible with the TypeProxy's value type.
     * Thus, if this method returns a proxy, it will *always* be
     * compatible with the given object.
     * </p>
     * @param object The object for which we are looking up a proxy.
     * @return The proxy, or null
     */
    TypeProxy<?> lookupProxy(Object object) {
        var cls = object.getClass();
        do {
            var proxy = proxyTable.get(cls);

            if (proxy != null) {
                return proxy;
            }

            cls = cls.getSuperclass();
        } while (cls != null && cls != Object.class);

        return null;
    }

    //-------------------------------------------------------------------------
    // Services provided to the rest of the implementation

    /**
     * Stringify converts an object to its string representation,
     * <b>as visible at the scripting layer</b>. Normally, this is the same
     * as {@code object.toString()}, but there are special cases.
     *
     * <ul>
     * <li>The null value stringifies as "null".</li>
     * <li>Doubles with integer values stringify without the ".0".</li>
     * <li>Instances of Joe classes can provide a stringifier.</li>
     * <li>Java class bindings can provide a stringifier.</li>
     * </ul>
     *
     * <p>This method would be static except that it will need to reference
     * the engine's data for non-standard types.</p>
     * @param value The value being stringified.
     * @return The string
     */
    public String stringify(Object value) {
        if (value == null) return "null";

        if (value instanceof Double) {
            String text = value.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return value.toString();
    }

    /**
     * Converts a value to a string as it would appear in Monica code.
     * This is intended primarily for use in error messages, but
     * could also be used during code generation.
     * @param value The value
     * @return The value
     */
    public String codify(Object value) {
        if (value instanceof String string) {
            return "\"" + escape(string) + "\"";
        } else {
            return stringify(value);
        }
    }

    // Converts the expression into something that looks like code.
    String recodify(Expr expr) {
        return codifier.recodify(expr);
    }

    // Converts the statement into something that looks like code.
    String recodify(Stmt statement) {
        return recodify(List.of(statement));
    }

    // Converts the statements into something that looks like code.
    String recodify(List<Stmt> statements) {
        return codifier.recodify(statements);
    }

    /**
     * Gets the script-level type of the value, or null if null.
     * This is primarily for use in error messages.
     * @param value The value
     * @return The type string, or null.
     */
    public String typeName(Object value) {
        return switch (value) {
            case null -> null;
            case JoeFunction function -> toInitialCap(function.kind());
            default -> {
                var proxy = lookupProxy(value);
                yield proxy != null
                    ? proxy.getTypeName()
                    : value.getClass().getSimpleName();
            }
        };
    }

    /**
     * Given an arbitrary string, escapes all typical control characters as
     * they would appear in Java or Joe code.
     * @param string The input string
     * @return The string with escapes
     */
    public static String escape(String string){
        var buff = new StringBuilder();

        for (int i = 0; i < string.length(); i++) {
            var c = string.charAt(i);
            switch (c) {
                case '\\' -> buff.append("\\\\");
                case '\t' -> buff.append("\\t");
                case '\b' -> buff.append("\\b");
                case '\n' -> buff.append("\\n");
                case '\r' -> buff.append("\\r");
                case '\f' -> buff.append("\\f");
                case '"' -> buff.append("\\\"");
                default -> {
                    if (c < 256) {
                        buff.append(c);
                    } else {
                        var hex = (int)c;
                        buff.append(String.format("\\u%04X", hex));
                    }
                }
            }
        }
        return buff.toString();
    }

    /**
     * Returns true if the object is "truthy", i.e., boolean {@code true} or
     * non-null, and false otherwise.
     * @param value The value
     * @return true or false
     */
    public static boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (boolean)value;
        return true;
    }

    /**
     * Returns true if the two values are equal, and false otherwise.
     * This is essentially equivalent to {@code Objects.equals(a, b)}.
     * @param a The first value
     * @param b The second value
     * @return true or false
     */
    public static boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    private String toInitialCap(String string) {
        if (string != null && !string.isEmpty()) {
            return Character.toUpperCase(string.charAt(0)) +
                string.substring(1);
        } else {
            return string;
        }
    }

    //-------------------------------------------------------------------------
    // Argument parsing and error handling helpers

    public static JoeError arityFailure(String signature) {
        return new JoeError("Wrong number of arguments, expected: " + signature);
    }

    /**
     * Throws an arity check failure if the arguments list contains the wrong
     * number of arguments.
     * @param args The argument list
     * @param arity The expected arity
     * @param signature The signature string.
     * @throws JoeError on failure
     */
    public static void exactArity(List<Object> args, int arity, String signature) {
        if (args.size() != arity) {
            throw arityFailure(signature);
        }
    }

    /**
     * Throws an arity check failure if the arguments list contains the wrong
     * number of arguments.
     * @param args The argument list
     * @param minArity The minimum arity
     * @param maxArity The maximum arity
     * @param signature The signature string.
     * @throws JoeError on failure
     */
    public static void arityRange(
        List<Object> args,
        int minArity,
        int maxArity,
        String signature)
    {
        if (args.size() < minArity || args.size() > maxArity) {
            throw arityFailure(signature);
        }
    }

    /**
     * Factory, constructs a JoeError to be thrown by the caller.
     * @param what What kind of value the caller expected
     * @param got The value the caller got
     * @return The error
     */
    public JoeError expected(String what, Object got) {
        var message = "Expected " + what + ", got " +
            (got != null ? typeName(got) + " " : "") +
            "'"  + codify(got) + "'.";
        return new JoeError(message);
    }
}
