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

    // The name of all lambda functions
    public static final String LAMBDA_NAME = "*lambda*";

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
            buffer.all());

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
        setLine(current.chunk.span.endLine());
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
                compile(s.condition());
                var endJump = emitJump(JIT);
                compile(s.message());
                emit(ASSERT);
                patchJump(s.keyword(), endJump);
            }
            case Stmt.Block s -> {
                beginScope();
                compile(s.statements());
                setLine(s.span().endLine());
                endScope();
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
            case Stmt.Function s -> {
                // NOTE: The parser returns this for both functions
                // and methods.  Methods aren't yet implemented.
                var global = addVariable(s.name());
                markVarInitialized();
                compileFunction(FunctionType.FUNCTION, s.name().lexeme(),
                    s.params(), s.body(), s.location());
                defineVariable(global);
            }
            case Stmt.If s -> {
                //                    | ∅      ; Initial state
                //       condition    | flag   ; Compute condition
                //       JIF else     | ∅      ; Jump to else branch
                //       thenBranch   | ∅      ; Execute then branch
                //       JUMP end     | ∅      ; Jump to end
                // else: elseBranch   | ∅      ; Execute else branch
                // end:  ...          | ∅      ; end of statement
                compile(s.condition());
                var elseJump = emitJump(JIF);
                compile(s.thenBranch());
                var endJump = emitJump(JUMP);
                patchJump(null, elseJump);
                if (s.elseBranch() != null) compile(s.elseBranch());
                patchJump(null, endJump);
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
            case Stmt.Return s -> {
                if (current.inStaticInitializer) {
                    // TODO: Move this to parser?
                    error(s.keyword(),
                        "Can't return from a static initializer block.");
                }

                if (s.value() != null) {
                    if (current.chunk.type == FunctionType.INITIALIZER) {
                        // Move to parser?
                        error(s.keyword(),
                            "Can't return a value from an initializer.");
                    }
                    compile(s.value());
                    emit(RETURN);
                } else {
                    emitReturn(); // Includes initializer magic
                }
            }
            case Stmt.Switch aSwitch -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Stmt.Throw s -> {
                compile(s.value());
                emit(THROW);
            }
            case Stmt.Var var -> {
                char global = addVariable(var.name());

                if (var.initializer() != null) {
                    compile(var.initializer());
                } else {
                    emit(NULL);
                }

                defineVariable(global);
            }
            case Stmt.While aWhile -> {
                throw new UnsupportedOperationException("TODO");
            }
        }
    }

    private void compileFunction(
        FunctionType type,
        String name,
        List<Token> params,
        List<Stmt> body,
        Span span
    ) {
        this.current = new FunctionCompiler(current, type, name, span);

        // Begin the function's scope; no endScope() because `RETURN`
        // does the cleanup.
        beginScope();

        for (var param : params) {
            var constant = addVariable(param);
            defineVariable(constant);
        }

        compile(body);

        var compiler = current;  // Save the compiler; endFunction pops it.
        var function = endFunction();
        setLine(span.endLine());
        emit(CLOSURE, addConstant(function));

        // Emit data about the upvalues
        for (int i = 0; i < function.upvalueCount; i++) {
            emit((char)(compiler.upvalues[i].isLocal ? 1 : 0));
            emit(compiler.upvalues[i].index);
        }
    }

    //-------------------------------------------------------------------------
    // Code Generation: Expressions

    @SuppressWarnings("RedundantLabeledSwitchRuleCodeBlock")
    private void compile(Expr expr) {
        setLine(expr.location());

        switch (expr) {
            case Expr.Binary e -> {
                compile(e.left());
                compile(e.right());
                switch (e.op().type()) {
                    case TokenType.BANG_EQUAL    -> emit(NE);
                    case TokenType.EQUAL_EQUAL   -> emit(EQ);
                    case TokenType.GREATER       -> emit(GT);
                    case TokenType.GREATER_EQUAL -> emit(GE);
                    case TokenType.IN            -> emit(IN);
                    case TokenType.LESS          -> emit(LT);
                    case TokenType.LESS_EQUAL    -> emit(LE);
                    case TokenType.PLUS          ->  emit(ADD);
                    case TokenType.MINUS         -> emit(SUB);
                    case TokenType.NI            -> emit(NI);
                    case TokenType.STAR          -> emit(MUL);
                    case TokenType.SLASH         -> emit(DIV);
                    default -> throw new IllegalStateException(
                        "Unexpected operator: " + e.op());
                }
            }
            case Expr.Call e -> {
                compile(e.callee());
                e.arguments().forEach(this::compile);
                emit(CALL, (char)e.arguments().size());
            }
            case Expr.Get get -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Expr.Grouping e -> compile(e.expr());
            case Expr.IndexGet indexGet -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Expr.IndexSet indexSet -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Expr.Lambda e -> {
                compileFunction(FunctionType.LAMBDA, LAMBDA_NAME,
                    e.declaration().params(), e.declaration().body(),
                    e.declaration().span());
            }
            case Expr.ListLiteral e -> {
                emit(LISTNEW);
                for (var item : e.list()) {
                    compile(item);
                    emit(LISTADD);
                }
            }
            case Expr.Literal e -> {
                switch (e.value()) {
                    case null -> emit(NULL);
                    case Boolean b -> emit(b ? TRUE : FALSE);
                    default -> emitConstant(e.value());
                }
            }
            case Expr.Logical e -> {
                compile(e.left());
                if (e.op().type() == TokenType.AND) {
                    int endJump = emitJump(JIFKEEP);
                    emit(POP);
                    compile(e.right());
                    patchJump(e.op(), endJump);
                } else { // OR
                    int endJump = emitJump(JITKEEP);
                    emit(POP);
                    compile(e.right());
                    patchJump(e.op(), endJump);
                }
            }
            case Expr.MapLiteral e -> {
                emit(MAPNEW);
                for (var i = 0; i < e.entries().size(); i += 2) {
                    compile(e.entries().get(i));      // Key
                    compile(e.entries().get(i + 1));  // Value
                    emit(MAPPUT);
                }
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
            case Expr.Ternary e -> {
                //                 | cond
                // JIF else        |
                // trueExpr        | a
                // JUMP end        | a
                // else: falseExpr | b
                // end:            | a or b
                compile(e.condition());
                int elseJump = emitJump(JIF);
                compile(e.trueExpr());
                int endJump = emitJump(JUMP);
                patchJump(e.op(), elseJump);
                compile(e.falseExpr());
                patchJump(e.op(), endJump);
            }
            case Expr.This aThis -> {
                throw new UnsupportedOperationException("TODO");
            }
            case Expr.Unary e -> {
                compile(e.right());
                switch(e.op().type()) {
                    case TokenType.BANG  -> emit(NOT);
                    case TokenType.MINUS -> emit(NEGATE);
                    default -> throw new IllegalStateException(
                        "Unexpected operator: " + e.op());
                }
            }
            case Expr.VarGet e -> resolve(e.name()).emitGet();
            case Expr.VarIncrDecr e -> {
                var rv = resolve(e.name());
                var incrDecr = token2incrDecr(e.op());

                if (e.isPre()) {
                    // Pre-increment/decrement
                    //
                    //                    | ∅         ; Initial state
                    // *GET        var    | a         ; a = var
                    // mathOp             | a'        ; e.g., a' = a + 1
                    // *SET        var    | a'        ; var = a'
                    rv.emitGet();
                    emit(incrDecr);
                    rv.emitSet();
                } else {
                    // Post-increment/decrement
                    //                    | ∅         ; Initial state
                    // *GET        var    | a         ; a = var
                    // TPUT               | a         ; T = a
                    // INCR               | a'        ; a' = a + 1
                    // *SET        var    | a'        ; var = a'
                    // POP                | ∅         ;
                    // TGET               | a         ; push T
                    rv.emitGet();
                    emit(TPUT);
                    emit(incrDecr);
                    rv.emitSet();
                    emit(POP);
                    emit(TGET);
                }
            }
            case Expr.VarSet e -> {
                var rv = resolve(e.name());

                // Simple Assignment
                if (e.op().type() == TokenType.EQUAL) {
                    compile(e.value());
                    rv.emitSet();
                    return;
                }

                // Assignment with update
                var mathOp = token2updater(e.op());

                //                | ∅         ; Initial stack
                // *GET    var    | a         ; a = var
                // expr           | a b       ; b = expr
                // mathOp         | c         ; E.g., c = a + b
                // *SET    var    | c         ; var = c, retaining c
                rv.emitGet();
                compile(e.value());
                emit(mathOp);
                rv.emitSet();
            }
        }
    }

    private char token2updater(Token op) {
        return switch(op.type()) {
            case TokenType.PLUS_EQUAL  -> ADD;
            case TokenType.MINUS_EQUAL -> SUB;
            case TokenType.STAR_EQUAL  -> MUL;
            case TokenType.SLASH_EQUAL -> DIV;
            default -> throw new IllegalStateException(
                "Unexpected operator: " + op);
        };
    }

    private char token2incrDecr(Token op) {
        return switch (op.type()) {
            case TokenType.PLUS_PLUS   -> INCR;
            case TokenType.MINUS_MINUS -> DECR;
            default -> throw new IllegalStateException(
                "Unexpected operator: " + op);
        };
    }

    //-------------------------------------------------------------------------
    // Variable Management

    private char addVariable(Token name) {
        declareVariable(name);
        if (current.scopeDepth > 0) return 0;    // Local
        return addConstant(name.lexeme());       // Global
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
//        // FIRST, get the relevant *SET/*GET opcodes.
//        char getOp;
//        char setOp;
//
//        int arg = resolveLocal(current, name);
//
//        if (arg != -1) {
//            getOp = Opcode.LOCGET;
//            setOp = Opcode.LOCSET;
//        } else if ((arg = resolveUpvalue(current, name)) != -1) {
//            getOp = Opcode.UPGET;
//            setOp = Opcode.UPSET;
//        } else {
//            arg = identifierConstant(name);
//            getOp = Opcode.GLOGET;
//            setOp = Opcode.GLOSET;
//        }
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

    private ResolvedVariable resolve(Token name) {
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
            arg = addConstant(name.lexeme());
            getOp = Opcode.GLOGET;
            setOp = Opcode.GLOSET;
        }
        return new ResolvedVariable(getOp, setOp, (char)arg);
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

    // Adds a constant to the constants table and returns its
    // index.
    private char addConstant(Object value) {
        return current.chunk.addConstant(value);
    }

    // Adds the value to the constants table and emits CONST.
    private void emitConstant(Object value) {
        emit(Opcode.CONST, current.chunk.addConstant(value));
    }

    // Given the variable name, emits the relevant *GET
    // instruction based on the context.
    private void emitVarGet(Token name) {
        // FIRST, get the relevant *GET opcode.
        char op;

        int arg = resolveLocal(current, name);

        if (arg != -1) {
            op = Opcode.LOCGET;
        } else if ((arg = resolveUpvalue(current, name)) != -1) {
            op = Opcode.UPGET;
        } else {
            arg = addConstant(name.lexeme());
            op = Opcode.GLOGET;
        }

        emit(op, (char)arg);
    }

    // Given the variable name, emits the relevant *SET
    // instruction based on the context.
    private void emitVarSet(Token name) {
        // FIRST, get the relevant *SET opcode.
        char op;

        int arg = resolveLocal(current, name);

        if (arg != -1) {
            op = Opcode.LOCSET;
        } else if ((arg = resolveUpvalue(current, name)) != -1) {
            op = Opcode.UPSET;
        } else {
            arg = addConstant(name.lexeme());
            op = Opcode.GLOSET;
        }

        emit(op, (char)arg);
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
            Span span
        ) {
            this.enclosing = enclosing;
            this.chunk = new Chunk();
            this.chunk.type = type;
            this.chunk.name = name;
            this.chunk.span = span;

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

    private class ResolvedVariable {
        private final char getOp;
        private final char setOp;
        private final char arg;

        ResolvedVariable(char getOp, char setOp, char arg) {
            this.getOp = getOp;
            this.setOp = setOp;
            this.arg = arg;
        }

        void emitGet() { emit(getOp, arg); }
        void emitSet() { emit(setOp, arg); }
    }
}
