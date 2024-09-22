package com.wjduquette.joe;

import com.wjduquette.joe.types.ListValue;
import com.wjduquette.joe.types.ListWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

public class Joe {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final GlobalEnvironment globals;
    private final Interpreter interpreter;
    private final Codifier codifier;
    private final Map<Class<?>, TypeProxy<?>> proxyTable = new HashMap<>();

    // The handler for all script-generated output
    private Consumer<String> outputHandler = this::systemOutHandler;

    //-------------------------------------------------------------------------
    // Constructor

    public Joe() {
        globals = new GlobalEnvironment();
        interpreter = new Interpreter(this);
        codifier = new Codifier(this);

        StandardLibrary.LIB.install(this);
        installGlobalFunction(new NativeFunction("dumpEnv", this::_dumpEnv));
    }

    private Object _dumpEnv(Joe joe, ArgQueue args) {
        interpreter.dumpEnvironment();
        return null;
    }

    //-------------------------------------------------------------------------
    // Configuration and Embedding

    public GlobalEnvironment getGlobals() {
        return globals;
    }

    /**
     * Installs a package into this interpreter.
     * @param pkg The package
     */
    public void installPackage(Package pkg) {
        pkg.install(this);
    }

    /**
     * Installs a native function into Joe's global environment.
     * @param function The function
     */
    public void installGlobalFunction(NativeFunction function) {
        globals.define(function.name(), function);
    }

    /**
     * Installs a type proxy into Joe's global environment.
     * @param typeProxy The type proxy.
     */
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
    // Output Handling


    /**
     * Gets the client's output handler.
     * @return the handler
     */
    @SuppressWarnings("unused")
    public Consumer<String> getOutputHandler() {
        return outputHandler;
    }

    /**
     * Sets the client's output handler.  By default, all script output
     * goes to System.out.
     * @param outputHandler The output handler.
     */
    @SuppressWarnings("unused")
    public void setOutputHandler(Consumer<String> outputHandler) {
        this.outputHandler = Objects.requireNonNull(outputHandler);
    }

    /**
     * Prints the text, followed by a newline, using the client's output handler.
     * @param text The text
     */
    public void println(String text) {
        print(text);
        print(System.lineSeparator());
    }

    /**
     * Prints a newline using the client's output handler.
     */
    public void println() {
        print(System.lineSeparator());
    }

    /**
     * Prints the text using the client's output handler.
     * @param text The text
     */
    public void print(String text) {
        outputHandler.accept(text);
    }

    /**
     * The default output handler; writes to System.out.
     * @param text The text
     */
    private void systemOutHandler(String text) {
        System.out.print(text);
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
        return lookupProxyByClass(object.getClass());
    }

    TypeProxy<?> lookupProxyByClass(Class<?> cls) {
        do {
            var proxy = proxyTable.get(cls);

            if (proxy != null) {
                return proxy;
            }

            cls = cls.getSuperclass();
        } while (cls != null && cls != Object.class);

        return null;
    }

    JoeObject getJoeObject(Object value) {
        if (value instanceof JoeObject obj) {
            return obj;
        } else {
            return new ProxiedValue(this, lookupProxy(value), value);
        }
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

        var proxy = lookupProxy(value);

        if (proxy != null) {
            return proxy.stringify(this, value);
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
        if (value == null) {
            return "null";
        }

        // TODO: Move to StringProxy
        if (value instanceof String string) {
            return "\"" + escape(string) + "\"";
        }

        var proxy = lookupProxy(value);

        if (proxy != null) {
            return proxy.codify(this, value);
        }

        return stringify(value);
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
            case JoeFunction function -> "<" + function.kind() + " " + function.name() + ">";
            case NativeFunction function -> "<native " + function.name() + ">";
            case TypeProxy<?> proxy -> "<proxy " + proxy.getTypeName() + ">";
            case JoeClass cls -> "<class " + cls.name() + ">";
            case JoeInstance obj -> obj.joeClass().name();
            default -> {
                var proxy = lookupProxy(value);
                yield proxy != null
                    ? proxy.getTypeName()
                    : "<java " + value.getClass().getCanonicalName() + ">";
            }
        };
    }

    public String classTypeName(Class<?> cls) {
        var proxy = lookupProxyByClass(cls);
        return proxy != null
            ? proxy.getTypeName() : cls.getSimpleName();
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

    /**
     * Compares two objects, returning -1, 0, or 1, provided
     * they are both Strings or both Numbers
     * @param a the first object
     * @param b the second object
     * @return -1, 0, or 1
     */
    public static int compare(Object a, Object b) {
        if (a instanceof String s && b instanceof String t) {
            return s.compareTo(t);
        } else if (a instanceof Double m && b instanceof Double n) {
            return m.compareTo(n);
        } else {
            throw new JoeError("Expected two strings or two numbers.");
        }
    }

    /**
     * Determines whether the name is a valid Joe identifier or not.
     * @param name The name
     * @return true or false
     */
    public static boolean isIdentifier(String name) {
        if (Scanner.RESERVED_WORDS.contains(name)) {
            return false;
        }
        return name.matches("[_A-Za-z][_A-Za-z0-9]*");
    }

    /**
     * Determines whether or not this is a valid Joe package name.
     * @param name The putative name
     * @return true or false
     */
    public static boolean isPackageName(String name) {
        var tokens = name.split("\\.");
        if (tokens.length == 0) {
            return false;
        }
        for (var token : tokens) {
            if (!isIdentifier(token)) {
                return false;
            }
        }
        return true;
    }

    public Object call(Object callee, Object... args) {
        if (callee instanceof JoeCallable callable) {
            return callable.call(this, new ArgQueue(List.of(args)));
        } else {
            throw expected("callable", callee);
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
    public static void exactArity(ArgQueue args, int arity, String signature) {
        if (args.size() != arity) {
            throw arityFailure(signature);
        }
    }

    /**
     * Throws an arity check failure if the arguments list contains fewer
     * than the minimum number of arguments.
     * @param args The argument list
     * @param minArity The minimum arity
     * @param signature The signature string.
     * @throws JoeError on failure
     */
    public static void minArity(ArgQueue args, int minArity, String signature) {
        if (args.size() < minArity) {
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
        ArgQueue args,
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
        var message = "Expected " + what + ", got: " +
            (got != null ? typeName(got) + " " : "") +
            "'"  + codify(got) + "'.";
        return new JoeError(message);
    }

    /**
     * Converts a Monica comparator callable to a Java comparator.
     * @param arg The callable
     * @return The comparator
     */
    public Comparator<Object> toComparator(Object arg) {
        if (arg instanceof JoeCallable) {
            return (Object a, Object b) -> toInteger(call(arg, a, b));
        } else {
            throw expected("comparator", arg);
        }
    }

    public double toDouble(Object arg) {
        if (arg instanceof Double num) {
            return num;
        }

        throw expected("double", arg);
    }

    public int toInteger(Object arg) {
        if (arg instanceof Double num) {
            return num.intValue();
        }

        throw expected("number", arg);
    }

    public int toIndex(Object arg, int limit) {
        var value = toInteger(arg);

        if (0 <= value && value < limit) {
            return value;
        }

        throw expected("0 <= index < " + limit, arg);
    }

    @SuppressWarnings("unused")
    public Keyword toKeyword(Object arg) {
        if (arg instanceof Keyword keyword) {
            return keyword;
        }

        throw expected("keyword", arg);
    }

    public JoeList toList(Object arg) {
        if (arg instanceof JoeList list) {
            return list;
        } else if (arg instanceof List<?> list) {
            return new ListValue(list);
        } else {
            throw expected("List", arg);
        }
    }

    public String toString(Object arg) {
        if (arg instanceof String string) {
            return string;
        }

        throw expected("string", arg);
    }

    @SuppressWarnings("unchecked")
    public <T> T toType(Class<T> cls, Object arg) {
        if (arg != null && cls.isAssignableFrom(arg.getClass())) {
            return (T) arg;
        } else {
            throw expected(classTypeName(cls), arg);
        }
    }

    //-------------------------------------------------------------------------
    // Wrapped Collections

    /**
     * Given an arbitrary Java list, wraps it as a JoeList and ensures that
     * any added values are can be assigned to the given itemType.  Updates
     * made by the script will update the underlying Java list.  If this
     * is not desired, either use `Joe::readonlyList()` or return it as a
     * new `ListValue`.
     * @param list The list to wrap
     * @param itemType The type
     * @return The wrapped list.
     */
    @SuppressWarnings("unused")
    public JoeList wrapList(List<?> list, Class<?> itemType) {
        return ListWrapper.wrap(this, list, itemType);
    }

    /**
     * Given an arbitrary Java list, wraps it to be read-only.  It can
     * be accessed freely at the script level, but not modified.
     * @param list The list to wrap
     * @return The wrapped list
     */
    @SuppressWarnings("unused")
    public JoeList readonlyList(List<?> list) {
        return ListWrapper.readOnly(this, list);
    }
}

