package com.wjduquette.joe;

import com.wjduquette.joe.bert.BertEngine;
import com.wjduquette.joe.types.*;
import com.wjduquette.joe.walker.WalkerEngine;

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
    // Static constants

    /** Constant for selecting the byte-code language engine. */
    public static final String BERT = "bert";

    /** Constant for selecting the AST-walker language engine. */
    public static final String WALKER = "walker";

    /**
     * The set of Joe's reserved words.
     */
    public static final Set<String> RESERVED_WORDS;

    static {
        var set = new TreeSet<String>();
        Collections.addAll(set,
            "assert", "break", "case", "class", "continue", "default",
            "else", "extends", "false", "for", "foreach", "function",
            "if", "in", "let", "match", "method", "ni", "null", "return",
            "static", "super", "switch",
            "this", "throw", "true",
            "var", "while"
        );
        RESERVED_WORDS = Collections.unmodifiableSet(set);
    }

    /**
     * A Java `null`, as a `JoeValue`.
     */
    static final NullValue NULL = new NullValue();

    //-------------------------------------------------------------------------
    // Instance Variables

    private final String engineName;
    private final Engine engine;
    private boolean debug = false;

    // Type Registry
    private final Map<Class<?>, ProxyType<?>> proxyTable = new HashMap<>();
    private final Set<Class<?>> cachedTypes = new HashSet<>();

    // The handler for all script-generated output
    private Consumer<String> outputHandler = this::systemOutHandler;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a clean instance of Joe.  It will include the complete
     * standard library, but nothing else.
     */
    public Joe() {
        this(BERT);
    }

    /**
     * Creates a clean instance of Joe using the given engine type, which
     * must be either {@code Joe.WALKER} (the default) or
     * {@code Joe.BERT}, the experimental byte-engine.
     *
     * <p>The instance include the complete Joe standard library, but
     * nothing else.</p>
     * @param engineType BERT or WALKER
     */
    public Joe(String engineType) {
        this.engineName = engineType;
        switch (engineType) {
            case WALKER -> engine = new WalkerEngine(this);
            case BERT -> engine = new BertEngine(this);
            default -> throw new IllegalArgumentException(
                "Invalid Engine type: '" + engineType + "'.");
        }

        StandardLibrary.PACKAGE.install(this);
    }

    //-------------------------------------------------------------------------
    // Configuration and Embedding

    /**
     * Gets the names of the defined global variables.
     * @return The names
     */
    public Set<String> getVarNames() {
        return engine.getVarNames();
    }

    /**
     * Gets a global variable's value by name.  Returns null if the variable
     * is not found.  Check {@code getVarNames()} to determine whether a
     * global variable is defined or not.
     * @param name The name
     * @return The value
     */
    @SuppressWarnings("unused")
    public Object getVar(String name) {
        return engine.getVar(name);
    }

    /**
     * Sets a global variable's value by name.
     * @param name The name
     * @param value The name
     */
    @SuppressWarnings("unused")
    public void setVar(String name, Object value) {
        engine.setVar(name, value);
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
    void installGlobalFunction(NativeFunction function) {
        engine.setVar(function.name(), function);
    }

    /**
     * Installs a JoeLambda into Joe's global environment as a
     * native function
     * @param name The function's name
     * @param joeLambda The lambda
     */
    public void installGlobalFunction(String name, JoeLambda joeLambda) {
        engine.setVar(name,
            new NativeFunction(name, "function", joeLambda));
    }

    /**
     * Installs a registered type's proxy into Joe's global environment.
     * @param proxyType The type proxy.
     */
    public void installType(ProxyType<?> proxyType) {
        // FIRST, clear the type cache, as things might get looked up
        // differently with the new type.
        cachedTypes.forEach(proxyTable::remove);
        cachedTypes.clear();

        // NEXT, install the proxy into the proxy table.
        for (var cls : proxyType.getProxiedTypes()) {
            proxyTable.put(cls, proxyType);
        }

        // NEXT, install the type into the environment.
        engine.setVar(proxyType.name(), proxyType);
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
            run(resource, source);
        } catch (SyntaxError ex) {
            System.err.println("Could not load script resource '" +
                resource + "' relative to class " +
                cls.getCanonicalName() + ":\n" + ex.getMessage());
            System.err.println(ex.getErrorReport());
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

    /**
     * Gets whether Joe is configured for debugging output.
     * @return true or false
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Sets whether Joe is configured for debugging output.  This
     * is primarily of use to the Joe maintainer.
     * @param flag true or false
     */
    public void setDebug(boolean flag) {
        this.debug = flag;
    }

    /**
     * Gets the name of the execution engine.
     * @return The name.
     */
    public String engineName() {
        return engineName;
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
     * Prints a formatted string to the client's output handler.
     * @param format A String.format format string.
     * @param args The arguments.
     */
    public void printf(String format, Object... args) {
        print(String.format(format, args));
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
     * @param scriptPath The file's path
     * @return The script's result
     * @throws IOException if the file cannot be read.
     * @throws SyntaxError if the script could not be compiled.
     * @throws JoeError on all runtime errors.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Object runFile(String scriptPath)
        throws IOException, SyntaxError, JoeError
    {
        var path = Paths.get(scriptPath);
        byte[] bytes = Files.readAllBytes(path);
        var script = new String(bytes, Charset.defaultCharset());

        return run(path.getFileName().toString(), script);
    }

    /**
     * Executes the script, throwing an appropriate error on failure.
     * The filename is usually the bare file name of the script file,
     * but can be any string relevant to the application, e.g., "%repl%".
     * @param filename The filename
     * @param source The input
     * @return The script's result
     * @throws SyntaxError if the script could not be compiled.
     * @throws JoeError on all runtime errors.
     */
    public Object run(String filename, String source) throws SyntaxError, JoeError {
        return engine.run(filename, source);
    }

    /**
     * Compiles the given file and returns a compilation dump.
     * @param scriptPath The file's path
     * @return The dump's result
     * @throws IOException if the file cannot be read.
     * @throws SyntaxError if the script could not be compiled.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Object dumpFile(String scriptPath)
        throws IOException, SyntaxError
    {
        var path = Paths.get(scriptPath);
        byte[] bytes = Files.readAllBytes(path);
        var script = new String(bytes, Charset.defaultCharset());

        return dump(path.getFileName().toString(), script);
    }

    /**
     * Compiles the script, throwing an appropriate error on failure, and
     * returns a compilation dump.
     * The filename is usually the bare file name of the script file,
     * but can be any string relevant to the application, e.g., "%repl%".
     * @param filename The filename
     * @param source The input
     * @return The dump
     * @throws SyntaxError if the script could not be compiled.
     */
    public String dump(String filename, String source) throws SyntaxError {
        return engine.dump(filename, source);
    }

    /**
     * Checks whether the source "is complete", i.e, whether it represents
     * a complete script.  A script is incomplete if it ends with
     * an incomplete construct, e.g., an unterminated string or block or
     * expression.
     *
     * <p>This is useful in REPLs: if the script is incomplete, the REPL
     * can allow the user to enter additional lines until it is complete.</p>
     * @param source The source text
     * @return true or false
     */
    @SuppressWarnings("unused")
    public boolean isComplete(String source) {
        return engine.isComplete(source);
    }

    //-------------------------------------------------------------------------
    // Internal Support -- for use within this package

    /**
     * Finds the ProxyType for a proxied type.
     *
     * <p>The search goes as follows: the class itself, then each superclass
     * up to (but not including) `Object`; and then, starting again at the
     * class itself, each of the class's interfaces, and so on again up
     * to `Object`.</p>
     *
     * <p>If no proxy is found then the type is deemed to be opaque, and an
     * OpaqueType is created for it.</p>
     *
     * <p>Any successful result is cached, and so the second and subsequent
     * lookups should be quick.</p>
     * @param cls The class
     * @return The ProxyType.
     */
    ProxyType<?> lookupProxy(Class<?> cls) {
        // FIRST, do have a known proxy?
        var proxy = proxyTable.get(cls);
        if (proxy != null) {
            return proxy;
        }

        // NEXT, search for a registered superclass.
        var c = cls.getSuperclass();

        do {
            proxy = proxyTable.get(c);

            if (proxy != null) {
                // If we could only find a proxy for a supertype,
                // cache it so that we don't need to look it up again.
                if (!c.equals(cls)) {
                    proxyTable.put(cls, proxy);
                    cachedTypes.add(cls);
                }
                return proxy;
            }

            c = c.getSuperclass();
        } while (c != null && c != Object.class);

        // NEXT, search for a registered interface.
        c = cls;

        do {
            for (var type : c.getInterfaces()) {
                proxy = proxyTable.get(type);

                if (proxy != null) {
                    proxyTable.put(cls, proxy);
                    cachedTypes.add(cls);
                    return proxy;
                }
            }

            c = c.getSuperclass();
        } while (c != null && c != Object.class);


        // NEXT, create a new OpaqueType.
        var opaque = new OpaqueType(cls);
        proxyTable.put(cls, opaque);
        cachedTypes.add(cls);
        return opaque;
    }

    /**
     * Given a value, gets it as a JoeValue.
     * @param value The value
     * @return The JoeValue
     */
    public JoeValue getJoeValue(Object value) {
        if (value == null) return NULL;
        if (value instanceof JoeValue obj) return obj;

        var proxy = lookupProxy(value.getClass());
        return new TypedValue(this, proxy, value);
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
        return switch(value) {
            case null -> "null";
            case String s -> s;
            case JoeValue obj -> obj.stringify(this);
            default -> getJoeValue(value).stringify(this);
        };
    }

    /**
     * Returns a collection of Joe values as a delimited string.
     * @param delimiter The delimiter
     * @param values The values
     * @return The string
     */
    public String join(String delimiter, Collection<Object> values) {
        return values.stream()
            .map(this::stringify)
            .collect(Collectors.joining(delimiter));
    }

    /**
     * Returns a "typeName 'value'" string using `typeName()` and
     * `stringify()`.
     * @param value The value
     * @return The string.
     */
    public String typedValue(Object value) {
        return value != null
            ? typeName(value) + " '" + stringify(value) + "'"
            : "'null'";
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
            case HasTypeName htn -> htn.typeName();
            default -> {
                // If it's a native type, try to get bind to a type proxy.
                var bound = getJoeValue(value);
                yield bound != null
                    ? bound.typeName()
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
        var proxy = lookupProxy(cls);
        return proxy != null
            ? proxy.name() : cls.getSimpleName();
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
     * Returns true if the object is "falsey", i.e., null or boolean
     * {@code false}, and true otherwise.
     * @param value The value
     * @return true or false
     */
    public static boolean isFalsey(Object value) {
        return !isTruthy(value);
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
        if (Joe.RESERVED_WORDS.contains(name)) {
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
     * Returns true if the callee is callable in the current engine,
     * and false otherwise.
     * @param callee The callee
     * @return true or false
     */
    public boolean isCallable(Object callee) {
        return engine.isCallable(callee);
    }

    /**
     * Calls a JoeCallable value with the given arguments.
     * @param callee A Joe value which must be callable.
     * @param args The arguments to pass to the callable
     * @return The result of calling the callable.
     */
    public Object call(Object callee, Object... args) {
        return engine.call(callee, args);
    }

    //-------------------------------------------------------------------------
    // Argument parsing and error handling helpers

    /**
     * Factory, constructs a JoeError to be thrown by the caller.
     * @param what What kind of value the caller expected
     * @param got The value the caller got
     * @return The error
     */
    public JoeError expected(String what, Object got) {
        var message = "Expected " + what + ", got: " +
            typedValue(got) + ".";
        return new JoeError(message);
    }

    /**
     * Converts the argument to a boolean.
     * @param arg The argument
     * @return the value
     */
    public boolean toBoolean(Object arg) {
        return Joe.isTruthy(arg);
    }

    /**
     * Requires that the argument is a value of type T.
     * @param arg The argument
     * @param cls The desired class T
     * @return The value, cast to class T
     * @param <T> The desired value type
     * @throws JoeError if the argument is incompatible with the desired class.
     */
    @SuppressWarnings({"unused", "unchecked"})
    public <T> T toClass(Object arg, Class<T> cls) {
        if (cls.isAssignableFrom(arg.getClass())) {
            return (T)arg;
        } else {
            throw expected(classTypeName(cls), arg);
        }
    }

    /**
     * Converts a Monica comparator callable to a Java comparator.
     * @param arg The callable
     * @return The comparator
     */
    public Comparator<Object> toComparator(Object arg) {
        if (isCallable(arg)) {
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

        throw expected("number", arg);
    }

    /**
     * Verifies that the argument is either an enum constant of the
     * desired type, a keyword whose name matches an enum
     * constant of the desired type, or a string ditto, and returns the
     * enum constant.
     * @param arg The argument
     * @param cls The enum class
     * @return The constant
     * @param <E> The enum type
     * @throws JoeError if the no enum constant is found.
     */
    @SuppressWarnings({"unused", "unchecked"})
    public <E extends Enum<E>> E toEnum(Object arg, Class<E> cls) {
        if (cls.isAssignableFrom(arg.getClass())) {
            return (E)arg;
        }

        var name = switch (arg) {
            case Keyword k -> k.name();
            case String s -> s;
            default -> null;
        };

        if (name != null) {
            var c = EnumType.valueOf(cls, name);
            if (c != null) {
                return c;
            }
        }

        throw expected(classTypeName(cls), arg);
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
     * Returns the object as a Joe type object.
     * @param arg The argument
     * @return The type
     * @throws JoeError if the argument is not a Joe type object
     */
    public JoeType toJoeType(Object arg) {
        if (arg instanceof JoeType type) {
            return type;
        } else {
            throw expected("Joe type", arg);
        }
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
     * Requires that the argument is a JoeMap, and returns it as
     * such.
     * @param arg The argument
     * @return The map
     * @throws JoeError if the argument is not a JoeMap
     */
    public JoeMap toMap(Object arg) {
        if (arg instanceof JoeMap map) {
            return map;
        } else if (arg instanceof Map<?,?> map) {
            return new MapValue(map);
        } else {
            throw expected("Map", arg);
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

    /**
     * Given an arbitrary Java set, wraps it as a JoeSet and ensures that
     * any added values are compatible with the given value type.  Updates
     * made by the script will update the underlying Java set.  If this is not
     * desired, either use `Joe::readonlySet()` or return it as a
     * new `SetValue`.
     * @param set The set to wrap
     * @param valueType The value type
     * @return The wrapped set.
     */
    @SuppressWarnings("unused")
    public JoeSet wrapSet(Set<?> set, Class<?> valueType) {
        return SetWrapper.wrap(this, set, valueType);
    }

    /**
     * Given an arbitrary Java set, wraps it to be read-only.  It can
     * be accessed freely at the script level, but not modified.
     * @param set The set to wrap
     * @return The wrapped set
     */
    @SuppressWarnings("unused")
    public JoeSet readonlySet(Set<?> set) {
        return SetWrapper.readOnly(this, set);
    }
}

