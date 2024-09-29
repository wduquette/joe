package com.wjduquette.joe;

import com.wjduquette.joe.types.ListValue;
import com.wjduquette.joe.types.ListWrapper;
import com.wjduquette.joe.types.MapWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The Joe language interpreter.  Clients create an instance of Joe,
 * install any needed bindings, and then use it to execute Joe scripts.
 */
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

    /**
     * Creates a clean instance of Joe.  It will include the complete
     * standard library, but nothing else.
     */
    public Joe() {
        globals = new GlobalEnvironment();
        interpreter = new Interpreter(this);
        codifier = new Codifier(this);

        StandardLibrary.PACKAGE.install(this);
        installGlobalFunction(new NativeFunction("dumpEnv", "function", this::_dumpEnv));
    }

    private Object _dumpEnv(Joe joe, ArgQueue args) {
        interpreter.dumpEnvironment();
        return null;
    }

    //-------------------------------------------------------------------------
    // Configuration and Embedding

    /**
     * Returns Joe's global environment, for querying.
     * @return The environment.
     */
    public GlobalEnvironment getGlobals() {
        return globals;
    }

    /**
     * Gets a global variable's value by name.
     * @param name The name
     * @return The value, if found
     */
    @SuppressWarnings("unused")
    public Optional<Object> getVar(String name) {
        return Optional.ofNullable(globals.getVar(name));
    }

    /**
     * Sets a global variable's value by name.
     * @param name The name
     * @param value The name
     */
    @SuppressWarnings("unused")
    public void setVar(String name, Object value) {
        globals.setVar(name, value);
    }

    /**
     * Installs a package into Joe's global environment.
     * @param pkg The package
     */
    public void installPackage(JoePackage pkg) {
        pkg.install(this);
    }

    /**
     * Installs a native function into Joe's global environment.
     * @param function The function
     */
    public void installGlobalFunction(NativeFunction function) {
        globals.setVar(function.name(), function);
    }

    /**
     * Installs a registered type's proxy into Joe's global environment.
     * @param typeProxy The type proxy.
     */
    public void installType(TypeProxy<?> typeProxy) {
        // FIRST, install the proxy into the proxy table.
        for (var cls : typeProxy.getProxiedTypes()) {
            proxyTable.put(cls, typeProxy);
        }

        // NEXT, install the type into the environment.
        globals.setVar(typeProxy.getTypeName(), typeProxy);
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

    /**
     * Looks for a type proxy by the proxied class, rather than by
     * a value of a proxied class.
     * @param cls The class
     * @return The proxy, or null if not found.
     */
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

    /**
     * Given a value, gets a JoeObject: either an instance of a JoeClass,
     * or a ProxiedValue.
     * @param value The value
     * @return The JoeObject
     */
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

        if (value instanceof ArgQueue args) {
            return args.asList().stream()
                .map(this::codify)
                .collect(Collectors.joining(", "));
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

    /**
     * Gets the script-level type name of the given class.
     * @param cls The class
     * @return The name
     */
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

    /**
     * Calls a JoeCallable value with the given arguments.
     * @param callee A Joe value which must be callable.
     * @param args The arguments to pass to the callable
     * @return The result of calling the callable.
     */
    public Object call(Object callee, Object... args) {
        if (callee instanceof JoeCallable callable) {
            return callable.call(this, new ArgQueue(List.of(args)));
        } else {
            throw expected("callable", callee);
        }
    }

    //-------------------------------------------------------------------------
    // Argument parsing and error handling helpers

    /**
     * Returns a "Wrong number of arguments" JoeError for a method or function
     * with the given signature.  This is primarily used by the arity checker
     * methods, but can also be used by native functions and methods at need.
     * @param signature The signature
     * @return The error, to be thrown.
     */
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
        if (args.remainingArgs() != arity) {
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
        if (args.remainingArgs() < minArity) {
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
        if (args.remainingArgs() < minArity || args.remainingArgs() > maxArity) {
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

    /**
     * Requires that the argument is a Double, and returns it as a
     * double for further processing.
     * @param arg The argument
     * @return the value
     * @throws JoeError if the argument is not a Double.
     */
    public double toDouble(Object arg) {
        if (arg instanceof Double num) {
            return num;
        }

        throw expected("double", arg);
    }

    /**
     * Requires that the argument is a Double, and returns it as an
     * integer for further processing.  Any fractional part is
     * truncated.
     * @param arg The argument
     * @return the value
     * @throws JoeError if the argument is not a Double.
     */
    public int toInteger(Object arg) {
        if (arg instanceof Double num) {
            return num.intValue();
        }

        throw expected("number", arg);
    }

    /**
     * Requires that the argument is a Double, and returns it as an
     * integer in the range 0 to limit - 1 for further processing.  Any
     * fractional part is truncated.
     * @param arg The argument
     * @param limit The maximum index - 1, e.g., the size of a list.
     * @return the value
     * @throws JoeError if the argument is not a Double or is out of range.
     */
    public int toIndex(Object arg, int limit) {
        var value = toInteger(arg);

        if (0 <= value && value < limit) {
            return value;
        }

        throw expected("0 <= index < " + limit, arg);
    }

    /**
     * Requires that the argument is a Keyword, and returns it as
     * such.
     * @param arg The argument
     * @return The keyword
     * @throws JoeError if the argument is not a keyword
     */
    @SuppressWarnings("unused")
    public Keyword toKeyword(Object arg) {
        if (arg instanceof Keyword keyword) {
            return keyword;
        }

        throw expected("keyword", arg);
    }

    /**
     * Requires that the argument is a JoeList, and returns it as
     * such.
     * @param arg The argument
     * @return The list
     * @throws JoeError if the argument is not a JoeList
     */
    public JoeList toList(Object arg) {
        if (arg instanceof JoeList list) {
            return list;
        } else if (arg instanceof List<?> list) {
            return new ListValue(list);
        } else {
            throw expected("List", arg);
        }
    }

    /**
     * Requires that the argument is a String, and returns it as
     * such. Contrast this with {@code stringify()}, which converts
     * the argument to its String representation.  Which to use is
     * sometimes a matter of need, and sometimes a matter of taste.
     * @param arg The argument
     * @return The string
     * @throws JoeError if the argument is not a String
     */
    public String toString(Object arg) {
        if (arg instanceof String string) {
            return string;
        }

        throw expected("string", arg);
    }


    /**
     * Requires that the argument be assignable to a variable of the
     * given class, and returns it as such.
     * @param cls The class
     * @param arg The argument
     * @return The value as cast
     * @param <T> The required value type.
     * @throws JoeError if the argument is not a valid T.
     */
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

    /**
     * Given an arbitrary Java map, wraps it as a JoeMap and ensures that
     * any added key,value pairs are compatible with the given key and
     * value types.  Updates made by the script will update the underlying
     * Java map.  If this is not desired, either use `Joe::readonlyMap()`
     * or return it as a new `MapValue`.
     * @param map The map to wrap
     * @param keyType The type
     * @param valueType The type
     * @return The wrapped map.
     */
    @SuppressWarnings("unused")
    public JoeMap wrapMap(Map<?,?> map, Class<?> keyType, Class<?> valueType) {
        return MapWrapper.wrap(this, map, keyType, valueType);
    }

    /**
     * Given an arbitrary Java map, wraps it to be read-only.  It can
     * be accessed freely at the script level, but not modified.
     * @param map The map to wrap
     * @return The wrapped map
     */
    @SuppressWarnings("unused")
    public JoeMap readonlyMap(Map<?,?> map) {
        return MapWrapper.readOnly(this, map);
    }
}

