package com.wjduquette.joe.clark;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.SyntaxError;
import com.wjduquette.joe.Trace;
import com.wjduquette.joe.parser.Expr;
import com.wjduquette.joe.parser.Parser;
import com.wjduquette.joe.parser.Stmt;
import com.wjduquette.joe.scanner.SourceBuffer;
import com.wjduquette.joe.scanner.SourceBuffer.Span;
import com.wjduquette.joe.scanner.Token;
import com.wjduquette.joe.scanner.TokenType;

import static com.wjduquette.joe.clark.Opcode.*;

import java.util.*;

/**
 * The Bert byte-compiler.  This is a single-pass compiler, parsing the
 * source and producing compiled code simultaneously; there is no
 * intermediate form.
 *
 * <p>The compiler uses a Pratt parser for parsing expressions; every
 * {@link TokenType} must be represented in the parser table.  See
 * {@code populateRulesTable} at the bottom of the file.</p>
 */
@SuppressWarnings("unused")
class Compiler {
    //------------------------------------------------------------------------
    // Static Constants

    // The maximum number of local variables in a function.
    public static final int MAX_LOCALS = 256;

    // The maximum number of parameters in a function
    public static final int MAX_PARAMETERS = 255;

    // The name of the script function
    public static final String SCRIPT_NAME = "*script*";

    // The name of a class's "init" method
    public static final String INIT = "init";

    // The `this` variable
    public static final String VAR_THIS = "this";

    // The `super` "variable".
    public static final String VAR_SUPER = "super";

    // The name of a function's varargs parameter.
    public static final String ARGS = "args";

    // The hidden variable used to hold a `foreach` iterator value
    private static final String VAR_ITERATOR = "*switch*";

    // The hidden variable used to hold a `match` value
    private static final String VAR_MATCH = "*match*";

    // The hidden variable used to hold a `switch` value
    private static final String VAR_SWITCH = "*switch*";


    //-------------------------------------------------------------------------
    // Instance Variables

    // The Joe runtime
    private final Joe joe;

    // The errors found during compilation
    private final List<Trace> errors = new ArrayList<>();

    // The source being compiled
    private boolean gotCompleteScript = false;

    // The function currently being compiled.
    private FunctionCompiler current = null;

//    // The type currently being compiled, or null
//    private TypeCompiler currentType = null;
//
//    // The loop currently being compiled, or null
//    private LoopCompiler currentLoop = null;
//
//    // The pattern currently being compiled, or null
//    private PatternCompiler currentPattern = null;

    // Final statement in script; used to allow the final `Stmt.Expression`
    // to return its value.`
    private Stmt finalStatement = null;

    // Used for debugging/dumping
    private transient Disassembler disassembler;
    private transient StringBuilder dump = null;


    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a new compiler
     * @param joe The Joe runtime
     */
    Compiler(Joe joe) {
        this.joe = joe;
    }

    //-------------------------------------------------------------------------
    // Compilation

    /**
     * Compiles the script source as a no-arg function.
     * Compilation errors are accumulated and thrown as a unit.
     * @param scriptName The script's name, e.g., the file name
     * @param source The script's source.
     * @return The script as a `Function`.
     */
    public Function compile(String scriptName, String source) {
        var buffer = new SourceBuffer(scriptName, source);

        // The FunctionCompiler contains the Chunk for the function
        // currently being compiled.  Each `function` or `method`
        // declaration adds a new FunctionCompiler to the stack, so that
        // each has its own Chunk.
        current = new FunctionCompiler(
            null,
            FunctionType.SCRIPT,
            SCRIPT_NAME,
            buffer.all(),
            buffer);

        // NEXT, parse the script.  This will throw a `SyntaxError` if
        // any errors are found.
        errors.clear();
        var statements = parse(buffer);
        finalStatement = statements.isEmpty() ? null : statements.getLast();

        // NEXT, generate the code.
        compile(statements);

        // Take the current chunk and package it as a Function.
        var function = endFunction();

        if (!errors.isEmpty()) {
            throw new SyntaxError("Error while compiling script", errors,
                true);
        }

        return function;
    }

    private List<Stmt> parse(SourceBuffer buffer) throws SyntaxError {
        gotCompleteScript = true;

        Parser parser = new Parser(buffer, this::parseError);
        var statements = parser.parse();

        // Stop if there was a syntax error.
        if (!errors.isEmpty()) {
            throw new SyntaxError("Syntax error in input, halting.",
                errors, gotCompleteScript);
        }

        return statements;
    }

    private void parseError(Trace trace, boolean incomplete) {
        if (incomplete) gotCompleteScript = false;
        errors.add(trace);
    }

    /**
     * Gets a disassembler dump of the compiled script
     * @param scriptName The script's name, e.g., the file name
     * @param source The script's source.
     * @return The dump.
     */
    public String dump(String scriptName, String source) {
        dump = new StringBuilder();
        disassembler = new Disassembler(joe);

        compile(scriptName, source);

        var output = dump.toString();
        dump = null;
        disassembler = null;
        return output;
    }

    // Completes compilation of the current function and returns it.
    private Function endFunction() {
        emitReturn();
        var function = new Function(
            current.parameters,
            current.chunk,
            current.upvalueCount);
        if (dump != null) {
            dump.append(disassembler.disassemble(function)).append("\n");
        }
        current = current.enclosing;
        return function;
    }

    //-------------------------------------------------------------------------
    // Code Generation: Statements

    private void compile(List<Stmt> statements) {
        for (var stmt : statements) {
            compile(stmt);
        }
    }

    @SuppressWarnings("RedundantLabeledSwitchRuleCodeBlock")
    private void compile(Stmt stmt) {
        setLine(stmt.location());

        switch (stmt) {
            case Stmt.Assert s -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Stmt.Block block -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Stmt.Break aBreak -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Stmt.Class aClass -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Stmt.Continue aContinue -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Stmt.Expression e -> {
                compile(e.expr());
                if (e == finalStatement) {
                    // Return the value of the final statement in
                    // the script.  This allows the REPL to
                    // display results conveniently.
                    emit(RETURN);
                } else {
                    emit(POP);
                }
            }
            case Stmt.For aFor -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Stmt.ForEach forEach -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Stmt.Function function -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Stmt.If anIf -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Stmt.IfLet ifLet -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Stmt.Let let -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Stmt.Match match -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Stmt.Record record -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Stmt.Return aReturn -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Stmt.Switch aSwitch -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Stmt.Throw aThrow -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Stmt.Var var -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Stmt.While aWhile -> {
                throw new UnsupportedOperationException("TODO");
            }
        }
    }

    //-------------------------------------------------------------------------
    // Code Generation: Expressions

    @SuppressWarnings("RedundantLabeledSwitchRuleCodeBlock")
    private void compile(Expr expr) {
        setLine(expr.location());

        switch (expr) {
            case Expr.Assign e -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Expr.Binary binary -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Expr.Call call -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Expr.Get get -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Expr.Grouping grouping -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Expr.IndexGet indexGet -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Expr.IndexSet indexSet -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Expr.Lambda lambda -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Expr.ListLiteral listLiteral -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Expr.Literal literal -> emitConstant(literal.value());
            case Expr.Logical logical -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Expr.MapLiteral mapLiteral -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Expr.PrePostAssign prePostAssign -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Expr.PrePostIndex prePostIndex -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Expr.PrePostSet prePostSet -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Expr.Set set -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Expr.Super aSuper -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Expr.Ternary ternary -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Expr.This aThis -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Expr.Unary unary -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Expr.Variable variable -> {
                throw new UnsupportedOperationException("TODO");
            }
        }
    }


    //-------------------------------------------------------------------------
    // Variable Management

    // Parses a variable name as part of a declaration.  The name can be:
    //
    // - A variable name in a `var` declaration
    // - A function or parameter name in a `function` or `method` declaration.
    //
    // Declares the variable.  If the variable is a global, returns an
    // index to the variable's name in the constants table.  Otherwise,
    // returns 0.
//    private char parseVariable(String errorMessage) {
//        scanner.consume(IDENTIFIER, errorMessage);
//        return addVariable(scanner.previous());
//    }

    private char addVariable(Token name) {
        declareVariable(name);
        if (current.scopeDepth > 0) return 0;    // Local
        return identifierConstant(name);         // Global
    }

    // Creates a hidden variable in the current scope, giving it the
    // value of the next `expression()`.  Hidden variable names should look
    // like "*identifier*", so as not to conflict with real variables.
    @SuppressWarnings("SameParameterValue")
//    private void defineHiddenVariable(String name) {
//        if (current.scopeDepth == 0) {
//            throw new IllegalStateException("Hidden variables must be local.");
//        }
//        var nameToken = Token.synthetic(name);
//        addLocal(nameToken);
//        expression();
//        markVarInitialized();
//    }

    // Adds a string constant to the current chunk's
    // constants table for the given identifier and returns
    // the constant's index.
    private char identifierConstant(Token name) {
        return current.chunk.addConstant(name.lexeme());
    }

    // Emits the instruction to define the variable.
    private void defineVariable(char global) {
        if (current.scopeDepth > 0) {
            // Local
            markVarInitialized();
            return;
        }
        emit(Opcode.GLODEF, global);            // Global
    }

    // Given the variable name, emits the relevant *GET or *SET
    // instruction based on the context.  A *SET instruction will
    // be preceded by the compiled expression to assign to the
    // variable.
    private void getOrSetVariable(Token name, boolean canAssign) {
        // FIRST, get the relevant *SET/*GET opcodes.
        char getOp;
        char setOp;

        int arg = resolveLocal(current, name);

        if (arg != -1) {
            getOp = Opcode.LOCGET;
            setOp = Opcode.LOCSET;
        } else if ((arg = resolveUpvalue(current, name)) != -1) {
            getOp = Opcode.UPGET;
            setOp = Opcode.UPSET;
        } else {
            arg = identifierConstant(name);
            getOp = Opcode.GLOGET;
            setOp = Opcode.GLOSET;
        }
//
//        // NEXT, handle assignment operators
//        if (canAssign && scanner.match(EQUAL)) {
//            expression();
//            emit(setOp, (char)arg);
//        } else if (canAssign && scanner.match(PLUS_EQUAL)) {
//            updateVar(getOp, setOp, (char)arg, Opcode.ADD);
//        } else if (canAssign && scanner.match(MINUS_EQUAL)) {
//            updateVar(getOp, setOp, (char)arg, Opcode.SUB);
//        } else if (canAssign && scanner.match(STAR_EQUAL)) {
//            updateVar(getOp, setOp, (char)arg, Opcode.MUL);
//        } else if (canAssign && scanner.match(SLASH_EQUAL)) {
//            updateVar(getOp, setOp, (char)arg, Opcode.DIV);
//        } else if (canAssign && scanner.match(PLUS_PLUS)) {
//            postIncrDecrVar(getOp, setOp, (char)arg, Opcode.INCR);
//        } else if (canAssign && scanner.match(MINUS_MINUS)) {
//            postIncrDecrVar(getOp, setOp, (char)arg, Opcode.DECR);
//        } else {
//            emit(getOp, (char)arg);
//        }
    }

    // Emits the code to update a variable
    private void updateVar(char getOp, char setOp, char arg, char mathOp) {
        //                | ∅         ; Initial stack
        // *GET    var    | a         ; a = var
        // expr           | a b       ; b = expr
        // mathOp         | c         ; E.g., c = a + b
        // *SET    var    | c         ; var = c, retaining c
        emit(getOp, arg);
//        expression();
        emit(mathOp);
        emit(setOp, arg);
    }

    // Emits the code to post-increment/decrement a variable
    private void postIncrDecrVar(char getOp, char setOp, char arg, char mathOp) {
        //                    | ∅         ; Initial state
        // *GET        var    | a         ; a = var
        // TPUT               | a         ; T = a
        // INCR               | a'        ; a' = a + 1
        // *SET        var    | a'        ; var = a'
        // POP                | ∅         ;
        // TGET               | a         ; push T
        emit(getOp, arg);
        emit(Opcode.TPUT);
        emit(mathOp);
        emit(setOp, arg);
        emit(Opcode.POP);
        emit(Opcode.TGET);
    }

    // Declares the variable.  Checking for duplicate declarations in the
    // current local scope.
    private void declareVariable(Token name) {
        if (current.scopeDepth == 0) return; // Global

        // Check for duplicate declarations in current scope.
        for (var i = current.localCount - 1; i >= 0; i--) {
            var local = current.locals[i];

            // Stop checking once we get to a lower scope depth.
            if (local.depth != -1 && local.depth < current.scopeDepth) {
                break;
            }

            if (name.lexeme().equals(local.name.lexeme())) {
                error(name, "Duplicate variable declaration in this scope.");
            }
        }

        addLocal(name);
    }

    // Adds a local variable with the given name to the current scope.
    private void addLocal(Token name) {
        if (current.localCount == MAX_LOCALS) {
            error(name, "Too many local variables in function.");
        }
        current.locals[current.localCount++] =
            new Local(name);
    }

    // Marks the newest local variable "initialized", so that they can be
    // referred to in expressions.  This is a no-op for global variables.
    private void markVarInitialized() {
        if (current.scopeDepth == 0) return;
        current.locals[current.localCount - 1].depth
            = current.scopeDepth;
    }

    // Marks the N newest local variables "initialized", so that they can be
    // referred to in expressions.  This is a no-op for global variables.
    private void markVarsInitialized(int count) {
        if (current.scopeDepth == 0) return;
        for (var i = 0; i < count; i++) {
            current.locals[current.localCount - 1 - i].depth
                = current.scopeDepth;
        }
    }

    // Resolves the name as the name of the local variable in the current
    // scope.  Returns the local's index in the current scope, or -1 if
    // no variable was found.
    private int resolveLocal(FunctionCompiler compiler, Token name) {
        for (var i = compiler.localCount - 1; i >= 0; i--) {
            var local = compiler.locals[i];
            if (name.lexeme().equals(local.name.lexeme())) {
                if (local.depth == -1) {
                    error(name, "Can't read local variable in its own initializer.");
                }
                return i;
            }
        }
        return -1;
    }

    // Resolves the name as the name of an upvalue.  Returns -1 if the
    // variable is global, and the upvalue index otherwise.  Captures
    // locals as upvalues.
    private int resolveUpvalue(FunctionCompiler compiler, Token name) {
        // FIRST, if there's no enclosing FunctionCompiler, then this is
        // necessarily a global.
        if (compiler.enclosing == null) return -1;

        // NEXT, we already know it isn't in this scope; look for it as a
        // local in the enclosing scope.
        int local = resolveLocal(compiler.enclosing, name);

        if (local != -1) {
            compiler.enclosing.locals[local].isCaptured = true;
            return addUpvalue(compiler, name, (char)local, true);
        }

        // NEXT, it might be defined in a scope that encloses the enclosing
        // scope. That scope might no longer be on the stack, so look for it
        // as an upvalue, not as a local.
        int upvalue = resolveUpvalue(compiler.enclosing, name);
        if (upvalue != -1) {
            return addUpvalue(compiler, name, (char)upvalue, false);
        }

        return -1;
    }

    // Adds an upvalue to the current function.  `index` is the index of the
    // upvalue in this function; `isLocal` is true if the upvalue is defined
    // for this scope, and false if it's for an enclosing scope.
    private int addUpvalue(
        FunctionCompiler compiler,
        Token name,
        char index,
        boolean isLocal) {
        int upvalueCount = compiler.upvalueCount;

        // See if we already know about this upvalue.
        for (var i = 0; i < upvalueCount; i++) {
            UpvalueInfo upvalue = compiler.upvalues[i];
            if (upvalue.index == index && upvalue.isLocal == isLocal) {
                return i;
            }
        }

        if (upvalueCount == MAX_LOCALS) {
            error(name, "Too many closure variables in function.");
            return 0;
        }

        // Allocate a new upvalue.
        compiler.upvalues[upvalueCount] = new UpvalueInfo(index, isLocal);
        return compiler.upvalueCount++;
    }

    // Increments the scope depth.
    private void beginScope() {
        current.scopeDepth++;
    }

    // Decrements the scope depth by 1, cleaning up any local variables defined
    // in the scope.  Upvalues are closed, other locals are simply popped.
    private void endScope() {
        // FIRST, decrement the scope.
        current.scopeDepth--;

        // NEXT, Determine the number of variables going out of scope, and
        // whether any of them have been captured.
        int varCount = 0;
        char opcode = Opcode.POPN;

        while (current.localCount > 0
            && current.locals[current.localCount - 1].depth > current.scopeDepth)
        {

            if (current.locals[current.localCount -1].isCaptured) {
                // Got at least one captured variable.
                opcode = Opcode.UPCLOSE;
            }
            varCount++;
            current.localCount--;
        }

        // NEXT, generate `POPN` or `UPCLOSE` accordingly.
        if (varCount > 0) {
            emit(opcode, (char)varCount);
        }
    }

    // Pops any locals down to the given depth, closing upvalues as needed.
    // This used by `break` and `continue`; it emits code to pop the
    // requisite number of locals without modifying the compiler's
    // info about locals.
    private void popLocals(int depth) {
        // FIRST, Determine the number of variables going out of scope, and
        // whether any of them have been captured.
        int varCount = 0;
        char opcode = Opcode.POPN;

        var localCount = current.localCount;
        while (localCount > 0) {
            var local = current.locals[localCount - 1];
            if (local.depth <= depth) break;

            if (local.isCaptured) {
                // Got at least one captured variable.
                opcode = Opcode.UPCLOSE;
            }
            varCount++;
            localCount--;
        }

        // NEXT, generate `POPN` or `UPCLOSE` accordingly.
        if (varCount > 0) {
            emit(opcode, (char)varCount);
        }
    }

    //-------------------------------------------------------------------------
    // Parsing Tools

    private void error(Token token, String message) {
        errors.add(new Trace(token.span(), message));
    }

    //-------------------------------------------------------------------------
    // Code Generation

    private void setLine(Span span) {
        if (span != null) setLine(span.startLine());
    }

    private void setLine(int line) {
        current.sourceLine = line;
    }

    private char addConstant(Object value) {
        return current.chunk.addConstant(value);
    }

    private void emitConstant(Object value) {
        emit(Opcode.CONST, current.chunk.addConstant(value));
    }

    private int emitJump(char opcode) {
        emit(opcode);
        emit(Character.MAX_VALUE);
        return current.chunk.codeSize() - 1;
    }

    private void emitLoop(Token token, int loopStart) {
        emit(Opcode.LOOP);
        int offset = current.chunk.codeSize() - loopStart + 1;
        if (offset < Character.MAX_VALUE) {
            emit((char) offset);
        } else {
            error(token, "Loop body too large.");
        }
    }

    private void patchJump(Token token, int offset) {
        // -1 to adjust for the bytecode for the jump offset itself.
        int jump = current.chunk.codeSize() - offset - 1;

        if (jump > Character.MAX_VALUE) {
            error(token, "Too much code to jump over.");
        }

        current.chunk.setCode(offset, (char)jump);
    }

    private void emitReturn() {
        if (current.chunk.type == FunctionType.INITIALIZER) {
            emit(Opcode.LOCGET, (char)0);
        } else {
            emit(Opcode.NULL);
        }
        emit(Opcode.RETURN);
    }

    // Emits a comment instruction; used when debugging the compiler.
    @SuppressWarnings("unused")
    private void emitComment(String comment) {
        var index = current.chunk.addConstant(comment);
        emit(Opcode.COMMENT, index);
    }

    private void emit(char value) {
        current.chunk.write(value, current.sourceLine);
    }

    private void emit(char value1, char value2) {
        emit(value1);
        emit(value2);
    }

    //-------------------------------------------------------------------------
    // Helper Classes


    // A local variable.
    private static class Local {
        // The name of the variable
        final Token name;

        // Its scope depth
        int depth = - 1;

        // Whether it has been captured as an Upvalue
        boolean isCaptured = false;

        Local(Token name) {
            this.name = name;
        }
    }

    // State for the function currently being compiled.
    private class FunctionCompiler {
        // The enclosing function, or null.
        final FunctionCompiler enclosing;

        // The names of the function's parameters.
        final List<String> parameters = new ArrayList<>();

        // The chunk into which byte-code is compiled.
        final Chunk chunk;

        // Information about the function's local variables
        final Local[] locals = new Local[MAX_LOCALS];

        // The actual number of local variables
        int localCount = 0;

        // The current scope depth in this function.
        int scopeDepth = 0;

        // The number of upvalues in this function
        int upvalueCount = 0;

        // The locals that have been captured as upvalues.
        final UpvalueInfo[] upvalues = new UpvalueInfo[MAX_LOCALS];

        // Whether we are in a class static initializer block or not.
        boolean inStaticInitializer = false;

        // The current source line
        private int sourceLine = 1;

        FunctionCompiler(
            FunctionCompiler enclosing,
            FunctionType type,
            String name,
            Span span,
            SourceBuffer source
        ) {
            this.enclosing = enclosing;
            this.chunk = new Chunk();
            this.chunk.type = type;
            this.chunk.name = name;
            this.chunk.span = span;
            this.chunk.source = source;


            // Every function has an implicit stack slot for the VM's own use.
            // For methods, this slot will be filled by the instance.
            Local local;
            if (type == FunctionType.METHOD ||
                type == FunctionType.INITIALIZER
            ) {
                local = new Local(Token.synthetic(VAR_THIS));
            } else {
                local = new Local(Token.synthetic(""));
            }
            local.depth = 0;
            locals[localCount++] = local;
        }
    }

    private enum KindOfType {
        CLASS,
        RECORD
    }

    // The type currently being compiled.
    private static class TypeCompiler {
        final TypeCompiler enclosing;
        KindOfType kind = KindOfType.CLASS;
        boolean hasSupertype = false;

        TypeCompiler(TypeCompiler enclosing) {
            this.enclosing = enclosing;
        }
    }

    private static class LoopCompiler {
        // The enclosing loop, or null if none.
        final LoopCompiler enclosing;

        // The scope depth before the entire loop is compiled.
        // Allows break to know how many scopes to end.
        int breakDepth = -1;

        // The scope depth before the loop body is compiled.
        // Allows 'continue' to know how many scopes to end.
        int continueDepth = -1;

        // The jump instruction indices for any `break` statements in the
        // body of the loop.
        List<Character> breakJumps = new ArrayList<>();

        // The chunk index to loop back to on continue
        int loopStart = -1;

        LoopCompiler(LoopCompiler enclosing) {
            this.enclosing = enclosing;
        }
    }

    // Data about a compiled pattern.
    private static class PatternCompiler {
        // The index of the pattern in the constants table
        char patternIndex = 0;

        // The number of constants parsed so far; used to generate
        // Pattern.Constant IDs
        int constantCount = 0;

        // Names of binding variables in this pattern; used to detect
        // duplicates.
        final Set<String> bindingVars = new HashSet<>();

        // Local variables referenced by this pattern's constants.
        // Used to prevent constant expressions from referencing
        // shadowed variables.
        final List<Token> referencedLocals = new ArrayList<>();
    }

    // Compilation information about upvalues.
    static class UpvalueInfo {
        char index;
        boolean isLocal;

        UpvalueInfo(char index, boolean isLocal) {
            this.index = index;
            this.isLocal = isLocal;
        }

        @Override
        public String toString() {
            return "UpvalueInfo[index=" + (int)index + ", isLocal=" + isLocal + "]";
        }
    }
}
