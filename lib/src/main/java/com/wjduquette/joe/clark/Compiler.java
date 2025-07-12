package com.wjduquette.joe.clark;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.SyntaxError;
import com.wjduquette.joe.Trace;
import com.wjduquette.joe.nero.*;
import com.wjduquette.joe.parser.*;
import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.SourceBuffer.Span;
import com.wjduquette.joe.scanner.Token;
import com.wjduquette.joe.scanner.TokenType;

import static com.wjduquette.joe.clark.Opcode.*;

import java.util.*;

/**
 * The Clark byte-compiler.  The code is loosely based on the legacy
 * Bert compiler, a single-pass compiler based on Nystrom's `clox`
 * compiler, but Clark works from the standard `Parser`'s AST, and
 * the code-generation API is vastly clearer than Bert's.
 */
@SuppressWarnings({"unused", "RedundantLabeledSwitchRuleCodeBlock", "SameParameterValue"})
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
    private static final Token VAR_ITER = Token.synthetic("*iter*");

    // The hidden variable used to hold a `switch` value
    private static final Token VAR_SWITCH = Token.synthetic("*switch*");

    // The hidden variable used to hold a `match` value
    private static final Token VAR_MATCH = Token.synthetic("*match*");

    // The hidden variable used to hold a `foreach` pattern value
    private static final Token VAR_PATTERN = Token.synthetic("*pattern*");

    // The hidden variable used to hold a `foreach` item value
    private static final Token VAR_ITEM = Token.synthetic("*item*");

    //-------------------------------------------------------------------------
    // Instance Variables

    // The Joe runtime
    private final Joe joe;


    // The errors found during compilation
    private final List<Trace> errors = new ArrayList<>();

    // The source being compiled
    private SourceBuffer buffer = null;
    private boolean gotCompleteScript = false;

    // The function currently being compiled.
    private FunctionInfo current = null;

    // The loop currently being compiled, or null
    private LoopInfo currentLoop = null;

    // The type currently being compiled, or null
    private TypeInfo currentType = null;

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

        // Take the current chunk and package it as a Function.
        Function function;
        try {
            var buff = new SourceBuffer(scriptName, source);
            function = compileScript(buff);
        } catch (SyntaxError ex) {
            throw ex;
        } catch (Exception ex) {
            var context = ex.getStackTrace()[0].toString();
            throw new SyntaxError(
                "Unexpected exception while compiling script: " + ex +
                " at: " + context,
                errors, ex);
        }

        if (!errors.isEmpty()) {
            throw new SyntaxError("Error while compiling script", errors,
                true);
        }

        return function;
    }

    public Function compileScript(SourceBuffer source) {
        this.buffer = source;
        // The FunctionCompiler contains the Chunk for the function
        // currently being compiled.  Each `function` or `method`
        // declaration adds a new FunctionCompiler to the stack, so that
        // each has its own Chunk.
        current = new FunctionInfo(
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
        emit(statements);

        // Take the current chunk and package it as a Function.
        this.buffer = null;
        return endFunction();
    }

    private List<Stmt> parse(SourceBuffer buffer) throws SyntaxError {
        gotCompleteScript = true;

        Parser parser = new Parser(buffer, this::parseError);
        var statements = parser.parseJoe();

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
        lineAtEnd(current.chunk.span);
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

    // Emits the code for a list of statements.
    private void emit(List<Stmt> statements) {
        for (var stmt : statements) {
            emit(stmt);
        }
    }

    // Emits the code for a single statement.  Some typical invariants:
    //
    // - Most statements should leave the stack as they found it; this is
    //   indicated by a "∅" at the end of their stack effects.
    // - Declarations will leave at least one local variable
    //   on the stack (in local scope), but nothing on the "working" part of
    //   the stack.
    // - Some non-declarations are wrapped in a `Stmt.Block` by the Parser.
    //   - These might leave local variables on the stack; they will be
    //     cleared when the block ends.
    //   - I'm planning on removing this pattern in the long run, as it's
    //     less clear.
    private void emit(Stmt stmt) {
        line(stmt.location());

        switch (stmt) {
            case Stmt.Assert s -> {          // Stack effects:
                emit(s.condition());         // cond    ; compute condition
                var end_ = emitJump(JIT);    // ∅       ; JIT end
                emit(s.message());           // msg     ; compute message
                emit(ASSERT);                // ∅       ; throw AssertError
                patchJump(end_);             // ∅       ; end:
            }
            case Stmt.Block s -> {           // Stack effects:
                beginScope();                // ∅           ; begin block scope
                emit(s.statements());        // locals? | ∅ ; compile body
                lineAtEnd(s.span());
                endScope();                  // ∅           ; end block scope
            }
            case Stmt.Break s -> {
                if (currentLoop == null) {
                    error(s.keyword(), "found 'break' outside of any loop.");
                }

                // NEXT, we are jumping out of some number of open scopes, any
                // or all of which might have declared local variables.  Clean
                // them up explicitly.
                popLocals(currentLoop.breakDepth);

                // NEXT, emit the jump to the end of the loop.  It will be
                // patched at the correct time by endLoop().
                currentLoop.breakJumps.emit(JUMP);
            }
            case Stmt.Class s -> {
                // NOTE: The stack effects are written presuming that the
                // class is defined at local scope, as that's harder to
                // track mentally.

                                              // Stack: locals | working
                beginType(Kind.CLASS);        // ∅         ; begin type def
                emit(CLASS, name(s.name()));  // ∅ | c     ; create class
                defineVar(s.name());          // c | ∅     ; define class var

                if (s.superclass() != null) {
                    currentType.hasSupertype = true;
                    beginScope(); // super    // c | ∅      ; begin scope: super
                    emit(s.superclass());     // c | s      ; compute super
                    defineLocal(VAR_SUPER);   // c s | ∅    ; define super var
                    emitGET(s.name());        // c s | c    ; get class
                    emit(INHERIT);            // c s | ∅    ; inherit super
                }

                emitGET(s.name());            // c s | c    ; get class

                // Static Methods
                for (var m : s.staticMethods()) {
                    line(m.span());
                    emitFunction(m);          // c s | c m  ; compile method
                    emitMETHOD(m.name());     // c s | c    ; save method
                }

                // Instance Methods
                currentType.inInstanceMethod = true;
                for (var m : s.methods()) {
                    line(m.span());
                    emitFunction(m);          // c s | c m  ; compile method
                    emitMETHOD(m.name());     // c s | c    ; save method
                }
                currentType.inInstanceMethod = false;

                emit(POP);                    // c s | ∅    ; pop class

                // End super scope
                if (currentType.hasSupertype) {
                    endScope();               // c | ∅      ; end scope super
                }

                // Static Initializer
                if (!s.staticInit().isEmpty()) {
                    var span = buffer.lineSpan(s.classSpan().endLine());
                    var classTrace = constant(new Trace(span,
                        "In class " + s.name().lexeme()));
                    var staticTrace = constant(new Trace(span,
                        "In static initializer"));
                    emit(TRCPUSH, classTrace);
                    emit(TRCPUSH, staticTrace);
                    emit(s.staticInit());     // c | ∅      ; execute static init
                    emit(TRCPOP);
                    emit(TRCPOP);
                }

                endType();                    // c | ∅      ; end type def
            }
            case Stmt.Continue s -> {
                if (currentLoop == null) {
                    error(s.keyword(), "found 'continue' outside of any loop.");
                }

                // NEXT, we are jumping out of some number of open scopes, any
                // or all of which might have declared local variables.  Clean
                // them up explicitly.
                popLocals(currentLoop.continueDepth);

                // NEXT, jump back to the beginning of the loop.
                emitLoop(currentLoop.loopStart);
            }
            case Stmt.Expression e -> {
                emit(e.expr());
                if (e == finalStatement) {
                    // Return the value of the final statement in
                    // the script.  This allows the REPL to
                    // display results conveniently.
                    emit(RETURN);
                } else {
                    emit(POP);
                }
            }
            case Stmt.For s -> {
                // NOTE: the Parser wraps Stmt.For as follows:
                // Stmt.Block[Stmt.For]. Thus, this code needn't
                // create a scope.  This is a confusing pattern;
                // we should manage the scope here.

                // Initializer
                if (s.init() != null) {      // Stack effects:
                    emit(s.init());          // ∅     ; compile initializer
                }

                // Condition
                int start_ = here();         // ∅     ; start:
                int end_ = -1;
                if (s.condition() != null) {
                    emit(s.condition());     // cond  ; compute the condition
                    end_ = emitJump(JIF);    // ∅     ; JIF end
                }

                // Updater
                if (s.updater() != null) {
                    int body_ =              // ∅     ; JUMP body
                        emitJump(JUMP);
                    int updater_ = here();   //       ; updater:
                    emit(s.updater());       // a     ; compute updater
                    emit(POP);               // ∅     ; pop unneeded value
                    emitLoop(start_);        // ∅     ; LOOP start:
                    start_ = updater_;       // ∅     ; Loop back to updater:
                    patchJump(body_);        // ∅     ; body:
                }

                beginLoop(start_);           // ∅     ; begin b/c zone
                emit(s.body());              // ∅     ; compile body
                emitLoop(start_);            // ∅     ; LOOP to updater or start

                if (end_ != -1) {
                    patchJump(end_);         // ∅     ; end:
                }
                endLoop();                   // ∅     ; end b/c zone
            }
            case Stmt.ForEach s -> {
                // NOTE: the Parser wraps Stmt.ForEach as follows:
                // Stmt.Block[Stmt.Var loopVar, Stmt.ForEach]
                //
                // Thus, this code needn't create a scope or
                // define the loop variable.

                // Collection expression     // Stack: locals | working
                emit(s.items());             // ∅ | items  ; compute collection
                emit(ITER);                  // ∅ | it     ; compute iterator
                defineLocal(VAR_ITER);       // it | ∅     ; define *iter*

                // Iteration
                int start_ = here();         // it | ∅     ; start:
                beginLoop(start_);           // it | ∅     ; begin b/c zone
                emit(HASNEXT);               // it | flag  ; got item?
                var end_ = emitJump(JIF);    // it | ∅     ; JIF end
                emit(GETNEXT);               // it | i     ; get next item
                emitSET(s.name());           // it | i     ; set loop var
                emit(POP);                   // it | ∅     ; clear stack

                // Loop Body
                emit(s.body());              // it | ∅     ; compile body
                emitLoop(start_);            // it | ∅     ; LOOP start:

                patchJump(end_);             // it | ∅     ; end:
                endLoop();                   // it | ∅     ; end b/c zone

                // Local *iter* is popped when the enclosing block end.
            }
            case Stmt.ForEachBind s -> {
                // NOTE: the Parser wraps Stmt.ForEachBind as
                // Stmt.Block[Stmt.ForEachBind]. Thus, this code needn't
                // create a scope.
                var vars = s.pattern().getBindings();

                // Setup                     // Stack: locals | working
                emitPATTERN(s.pattern());    // ∅ | p           ; compile pattern
                defineLocal(VAR_PATTERN);    // p | ∅           ; define *pattern*
                emit(s.items());             // p | items       ; compute collection
                emit(ITER);                  // p | it          ; compute iterator
                defineLocal(VAR_ITER);       // p it | ∅        ; define *iter*

                // Iteration
                int start_ = here();         // p it | ∅        ; start:
                beginLoop(start_);           // p it | ∅        ; begin b/c zone
                emit(HASNEXT);               // p it | flag     ; got item?
                var end_ = emitJump(JIF);    // p it | ∅        ; JIF end
                emit(GETNEXT);               // p it | i        ; get next item
                emitGET(VAR_PATTERN);        // p it | i p      ; get *pattern*
                emit(SWAP);                  // p it | p i      ;
                emit(MATCH);                 // p it | vs? flag ; match pattern
                var body_ = emitJump(JIT);   // p it | vs?      ; JIT body
                emitLoop(start_);            // p it | ∅        ; LOOP start

                // Loop Body
                patchJump(body_);            // p it | vs       ; body:
                beginScope();
                defineLocals(vars);          // p it vs | ∅     ; define vars
                emit(s.body());              // p it vs | ∅     ; compile body
                endScope();
                emitLoop(start_);            // p it | ∅        ; loop start

                // End
                patchJump(end_);             // p it | ∅        ; end:
                endLoop();

                // Hidden locals are popped when the enclosing block ends.
            }
            case Stmt.Function s -> {
                emitFunction(s);
                defineVar(s.name());
            }
            case Stmt.If s -> {
                //                    | ∅      ; Initial state
                //       condition    | flag   ; Compute condition
                //       JIF else     | ∅      ; Jump to else branch
                //       thenBranch   | ∅      ; Execute then branch
                //       JUMP end     | ∅      ; Jump to end
                // else: elseBranch   | ∅      ; Execute else branch
                // end:  ...          | ∅      ; end of statement

                emit(s.condition());
                var else_ = emitJump(JIF);
                emit(s.thenBranch());

                int end_ = -1;
                if (s.elseBranch() != null) {
                    end_ = emitJump(JUMP);
                    patchJump(else_);
                    emit(s.elseBranch());
                } else {
                    patchJump(else_);
                }

                if (end_ != -1) patchJump(end_);
            }
            case Stmt.Match s -> {
                // Setup                      // Stack: locals | working
                beginScope();                 // ∅            ; begin scope: match
                emit(s.expr());               // ∅ | m        ; compile match target
                defineLocal(VAR_MATCH);       // m | ∅        ; define *match*

                // Match Cases
                var ends_ = jumpList();
                var next1_ = -1;
                var next2_ = -1;
                for (var c : s.cases()) {
                    var vars = c.pattern().getBindings();

                    patchJump(next1_);        // m | ∅        ; next1:
                    patchJump(next2_);        // m | ∅        ; next2:
                    emitPATTERN(c.pattern()); // m | p        ; compute pattern
                    beginScope();             // m | p        ; begin scope: case
                    emitGET(VAR_MATCH);       // m | p m      ; get *match*
                    emit(MATCH);              // m | vs? flag ; match pattern
                    next1_ = emitJump(JIF);   // m | vs?      ; JIF next1
                    defineLocals(vars);       // m vs | ∅     ; define bindings
                    if (c.guard() != null) {  // m vs | flag  ; compute guard
                        // Just parse as TRUE
                        emit(c.guard());
                    } else {
                        emit(TRUE);
                    }
                    next2_ = emitJump(JIF);   // m vs | ∅     ; JIF next2
                    emit(c.statement());      // m vs | ∅     ; compile body
                    endScope();               // m | ∅        ; end scope: case
                    ends_.emit(JUMP);         // m | ∅        ; JUMP end
                }

                // Default Case
                patchJump(next1_);            // m | ∅        ; next1:
                patchJump(next2_);            // m | ∅        ; next2:
                if (s.matchDefault() != null) {
                    emit(s.matchDefault());   // m | ∅        ; compile default
                }

                // End Of Statement
                patchJumps(ends_);            // m | ∅        ; end:
                endScope();                   // ∅            ; end scope: match
            }
            case Stmt.Record s -> {
                // NOTE: The stack effects are written presuming that the
                // class is defined at local scope, as that's harder to
                // track mentally.

                // Create Record              // Stack: locals | working
                beginType(Kind.RECORD);       // ∅        ; begin type def
                emit(RECORD,                  // ∅ | t    ; create type
                    name(s.name()),
                    constant(s.fields()));
                defineVar(s.name());     // t | ∅    ; define type var
                emitGET(s.name());            // t | t    ; get type

                // Static Methods
                for (var m : s.staticMethods()) {
                    line(m.span());
                    emitFunction(m);          // t | t m  ; compile method
                    emitMETHOD(m.name());     // t | t    ; save method
                }

                // Instance Methods
                currentType.inInstanceMethod = true;
                for (var m : s.methods()) {
                    line(m.span());
                    emitFunction(m);          // t | t m  ; compile method
                    emitMETHOD(m.name());     // t | t    ; save method
                }
                currentType.inInstanceMethod = false;

                emit(POP);                    // t | ∅    ; pop tye

                // End super scope
                if (currentType.hasSupertype) {
                    endScope();               // t | ∅    ; end scope super
                }

                // Static Initializer
                if (!s.staticInit().isEmpty()) {
                    var span = buffer.lineSpan(s.typeSpan().endLine());
                    var typeTrace = constant(new Trace(span,
                        "In type " + s.name().lexeme()));
                    var staticTrace = constant(new Trace(span,
                        "In static initializer"));
                    emit(TRCPUSH, typeTrace);
                    emit(TRCPUSH, staticTrace);
                    emit(s.staticInit());     // t | ∅      ; execute static init
                    emit(TRCPOP);
                    emit(TRCPOP);
                }

                endType();                    // t | ∅    ; end type def
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
                    emit(s.value());
                    emit(RETURN);
                } else {
                    emitReturn(); // Includes initializer magic
                }
            }
            case Stmt.Switch s -> {
                beginScope();
                emit(s.expr());
                defineLocal(VAR_SWITCH);

                // Jump targets
                var ends_ = jumpList();
                int next_ = -1;

                for (var c : s.cases()) {
                    line(c.location());
                    var cases_ = jumpList();

                    // Allow the previous case to jump here if it doesn't match.
                    // next:
                    if (next_ != -1) patchJump(next_);

                    for (var target : c.values()) {
                        // Compute the case target and compare it with the
                        // *switch* value.
                        emit(DUP); // Duplicate the switch value
                        emit(target);
                        emit(EQ);

                        // Jump to the next case if no match.
                        cases_.emit(JIT);
                    }

                    next_ = emitJump(JUMP);
                    patchJumps(cases_);

                    // Parse the case body.
                    emit(c.statement());

                    // No end jump if this the default case
                    ends_.emit(JUMP);
                }

                // next:
                if (next_ != -1) patchJump(next_);

                if (s.switchDefault() != null) {
                    emit(s.switchDefault().statement());
                }

                // Patch all the end jumps.
                patchJumps(ends_);

                // End the scope, removing the "*switch*" variable.
                endScope();
            }
            case Stmt.Throw s -> { // Stack effects:
                emit(s.value());   // value     ; compute error
                emit(THROW);       // ∅         ; Throw it
            }
            case Stmt.Var var -> {
                if (!inGlobalScope()) {
                    // Declare the local before we compute the initializer;
                    // this guarantees that the variable isn't initialized
                    // in terms of itself.
                    declareLocal(var.name());
                }

                // Compile the initial value
                emit(var.value());

                if (inGlobalScope()) {
                    // Define the variable.  We don't worry about whether it
                    // already existed or not
                    defineGlobal(var.name());
                } else {
                    // The value is on the stack; define the variable.
                    defineLocal(var.name());
                }
            }
            case Stmt.VarPattern s -> {
                var vars = s.pattern().getBindings();

                if (!inGlobalScope()) {        // Stack: locals | working
                    declareLocals(vars);       // ∅         ; declare vars
                }

                emitPATTERN(s.pattern());      // ∅ | p     ; compute pattern
                emit(s.target());              // ∅ | p t   ; compute target.

                if (!inGlobalScope()) {
                    emit(Opcode.LOCBIND);       // ∅ | vs    ; match pattern
                    defineLocals(vars);        // vs | ∅    ; define vars
                } else {
                    emit(Opcode.GLOBIND);       // ∅         ; define vars
                }
            }
            case Stmt.While s -> {
                // Setup                   // Stack effects:
                var start_ = here();       // ∅     ; start:
                emit(s.condition());       // cond  ; compute condition

                // Loop
                beginLoop(start_);         // cond  ; begin b/c zone
                int end_ = emitJump(JIF);  // ∅     ; JIF end:
                emit(s.body());            // ∅     ; compile loop body
                emitLoop(start_);          // ∅     ; LOOP start:
                patchJump(end_);           // ∅     ; end:
                endLoop();                 // ∅     ; end b/c zone
            }
        }
    }

    private void emitFunction(Stmt.Function func) {
        this.current = new FunctionInfo(current,
            func.type(), func.name().lexeme(), func.span());

        // Begin the function's scope; no endScope() because `RETURN`
        // does the cleanup.
        beginScope();

        for (var param : func.params()) {
            defineLocal(param);
            current.parameters.add(param.lexeme());
        }

        emit(func.body());

        var compiler = current;  // Save the compiler; endFunction pops it.
        var function = endFunction();
        lineAtEnd(func.span());
        emit(CLOSURE, constant(function));

        // Emit data about the upvalues
        for (int i = 0; i < function.upvalueCount; i++) {
            emit((char)(compiler.upvalues[i].isLocal ? 1 : 0));
            emit(compiler.upvalues[i].index);
        }
    }

    private void emitPATTERN(ASTPattern astPattern) {
        var index = constant(astPattern.getPattern());
        var bindings = constant(astPattern.getBindings().stream()
            .map(Token::lexeme)
            .toList());
        emitList(astPattern.getExprs());
        emit(PATTERN, index, bindings);
    }

    //-------------------------------------------------------------------------
    // Code Generation: Expressions

    // Emits a function's argument list.
    private void emitArgs(List<Expr> args) {
        for (var arg : args) {
            emit(arg);
        }
    }

    // Emits the code for a single expression.
    private void emit(Expr expr) {
        line(expr.location());

        switch (expr) {
            case Expr.Binary e -> {
                var op = switch (e.op().type()) {
                    case TokenType.BANG_EQUAL    -> NE;
                    case TokenType.EQUAL_EQUAL   -> EQ;
                    case TokenType.GREATER       -> GT;
                    case TokenType.GREATER_EQUAL -> GE;
                    case TokenType.IN            -> IN;
                    case TokenType.LESS          -> LT;
                    case TokenType.LESS_EQUAL    -> LE;
                    case TokenType.PLUS          -> ADD;
                    case TokenType.MINUS         -> SUB;
                    case TokenType.NI            -> NI;
                    case TokenType.STAR          -> MUL;
                    case TokenType.SLASH         -> DIV;
                    default -> throw new IllegalStateException(
                        "Unexpected operator: " + e.op());
                };

                                     // Stack effects:
                emit(e.left());      // a        ; compute left
                emit(e.right());     // a b      ; compute right
                emit(op);            // c        ; c = a op b
            }
            case Expr.Call e -> {
                var argc = e.arguments().size();

                emit(e.callee());         // f          ; compute callable
                emitArgs(e.arguments());  // f args...  ; compute arguments
                emit(CALL, (char)argc);   // result     ; result = f(args);
            }
            case Expr.False ignored -> emit(FALSE);
            case Expr.Grouping e -> emit(e.expr());
            case Expr.IndexGet e -> {
                                          // Stack effects:
                emit(e.collection());     // c          ; compute collection
                emit(e.index());          // c i        ; compute index
                emit(INDGET);             // v          ; get v = c[i]
            }
            case Expr.IndexIncrDecr e -> {
                var op = token2incrDecr(e.op());

                if (e.isPre()) { // Pre-increment/decrement
                                          // Stack effects:
                    emit(e.collection()); // c       ; compute collection
                    emit(e.index());      // c i     ; compute index
                    emit(DUP2);           // c i c i ; need it twice
                    emit(INDGET);         // c i x   ; x = c[i]
                    emit(op);             // c i y   ; y = x +/- 1
                    emit(INDSET);         // y       ; c[i] = y
                } else { // Post-increment/decrement
                                          // Stack effects:
                    emit(e.collection()); // c       ; compute collection
                    emit(e.index());      // c i     ; compute index
                    emit(DUP2);           // c i c i ; need it twice
                    emit(INDGET);         // c i x   ; x = c[i]
                    emit(TSET);           // c i x   ; T = x
                    emit(op);             // c i y   ; y = x +/- 1
                    emit(INDSET);         // y       ; c[i] = y
                    emit(POP);            // ∅       ;
                    emit(TGET);           // x       ; x = T
                }
            }
            case Expr.IndexSet e -> {
                // Simple Assignment
                if (e.op().type() == TokenType.EQUAL) {
                    // Stack effects:
                    emit(e.collection()); // c       ; compute collection
                    emit(e.index());      // c i     ; compute index
                    emit(e.value());      // c i x   ; compute value
                    emit(INDSET);         // x       ; c[i] = x
                    return;
                }

                // Assignment with update
                var mathOp = token2updater(e.op());

                emit(e.collection());     // c         ; compute collection
                emit(e.index());          // c i       ; compute index
                emit(DUP2);               // c i c i   ; need it twice
                emit(INDGET);             // c i x     ; x = c[i]
                emit(e.value());          // c i x y   ; compute update value
                emit(mathOp);             // c i z     ; z = x + y
                emit(INDSET);             // z         ; c[i] = z
            }
            case Expr.Lambda e -> emitFunction(e.declaration());
            case Expr.ListLiteral e -> {
                                          // Stack effects
                emitList(e.list());       // list      ; compute list
            }
            case Expr.Literal e -> emitCONST(e.value());
            case Expr.Logical e -> {
                if (e.op().type() == TokenType.AND) {
                                                     // Stack effects:
                    emit(e.left());                  // v      ; compute left
                    int end_ = emitJump(JIFKEEP);    // v      ; JIFKEEP end
                    emit(POP);                       // ∅
                    emit(e.right());                 // v      ; compute right
                    patchJump(end_);                 // v      ; end:
                } else { // OR
                    emit(e.left());                  // v      ; compute left
                    int end_ = emitJump(JITKEEP);    // v      ; JITKEEP end
                    emit(POP);                       // ∅
                    emit(e.right());                 // v      ; compute right
                    patchJump(end_);                 // v      ; end:
                }
            }
            case Expr.MapLiteral e -> {
                                                   // Stack effects:
                emit(MAPNEW);                      // m        ; create map
                for (var i = 0; i < e.entries().size(); i += 2) {
                    emit(e.entries().get(i));      // m k      ; compute key
                    emit(e.entries().get(i + 1));  // m k v    ; compute value
                    emit(MAPPUT);                  // m        ; m[k] = v
                }
            }
            case Expr.Match e -> {
                // Stack effects are written for locals.
                int nVars = e.pattern().getBindings().size();

                // Set up                  // Stack: locals | working
                emit(e.target());          // ∅ | t        ; Compute target
                emitPATTERN(e.pattern());  // ∅ | t p      ; Compute pattern
                emit(SWAP);                // ∅ | p t      ; setup for match

                if (inGlobalScope()) {
                    emit(MATCHG);          // ∅ | flag     ; Do match
                } else {
                    var slot = current.localCount;
                    emit(MATCHL);          // ∅ | vs flag  ; Do match
                    emit(TPUT);            // ∅ | vs       ; T = pop
                    defineLocals(          // ∅ | vs       ; define locals
                        e.pattern().getBindings());
                    emit(LOCMOVE,         // vs | ∅       ; fixup local values
                        (char)slot,
                        (char)nVars);
                    emit(TGET);            // vs | flag    ; push T
                }
            }
            case Expr.Null ignored -> emit(NULL);
            case Expr.PropGet e -> {
                var name = name(e.name());
                                          // Stack effects:
                emit(e.object());         // o      ; compute object
                emit(PROPGET, name);      // a      ; get property name
            }
            case Expr.PropIncrDecr e -> {
                var name = name(e.name());
                var op = token2incrDecr(e.op());

                if (e.isPre()) {
                    // Pre-increment/decrement

                                          // Stack effects:
                    emit(e.object());     // o      ; compute object
                    emit(DUP);            // o o    ; need it twice
                    emit(PROPGET, name);  // o a    ; a = o.name
                    emit(op);             // o b    ; b = a +/- 1
                    emit(PROPSET, name);  // b      ; o.name = b
                } else {
                    // Post-increment/decrement

                                          // Stack effects:
                    emit(e.object());     // o      ; compute object
                    emit(DUP);            // o o    ; need it twice
                    emit(PROPGET, name);  // o a    ; a = o.name
                    emit(TSET);           // o a    ; T = a
                    emit(op);             // o b    ; b = a +/- 1
                    emit(PROPSET, name);  // b      ; o.name = b
                    emit(POP);            // ∅      ;
                    emit(TGET);           // a      ; a = T
                }
            }
            case Expr.PropSet e -> {
                var name = name(e.name());

                // Simple Assignment
                if (e.op().type() == TokenType.EQUAL) {
                                          // Stack effects:
                    emit(e.object());     // o      ; compute object
                    emit(e.value());      // o v    ; compute value
                    emit(PROPSET, name);  // v      ; o.name = v
                    return;
                }

                // Assignment with update
                var mathOp = token2updater(e.op());

                emit(e.object());         // o      ; compute object
                emit(DUP);                // o o    ; object needed twice
                emit(PROPGET, name);      // o a    ; get prop value
                emit(e.value());          // o a b  ; compute update value
                emit(mathOp);             // o c    ; c = a op b
                emit(PROPSET, name);      // c      ; o.name = c
            }
            case Expr.RuleSet e -> {
                // FIRST, compile the rule set.
                var rsc = new RuleSetCompiler(e.ruleSet());
                var ruleset = rsc.compile();

                // Get the exports                // Stack effects
                emit(RULESET, constant(ruleset)); // rsv       ; RuleSetValue
            }
            case Expr.Super e -> {
                if (currentType == null || !currentType.inInstanceMethod) {
                    error(e.keyword(), "Can't use '" + e.keyword().lexeme() +
                        "' outside of a method.");
                } else if (!currentType.hasSupertype) {
                    error(e.keyword(), "Can't use '" + e.keyword().lexeme() +
                        "' in a class with no superclass.");
                }

                var name = name(e.method());

                emitGET(VAR_THIS);     // t        ; get this
                emitGET(VAR_SUPER);    // t s      ; get super
                emit(SUPGET, name);    // m        ; super.name
            }
            case Expr.Ternary e -> {
                                            // Stack effects
                emit(e.condition());        // c     ; compute condition
                int else_ = emitJump(JIF);  //       ; JIF else
                emit(e.trueExpr());         // v     ; compute true value
                int end_ = emitJump(JUMP);  // v     ; JUMP end
                patchJump(else_);           // ∅     ; else:
                emit(e.falseExpr());        // v     ; compute false value
                patchJump(end_);            // v     ; end:
            }
            case Expr.This e -> {
                if (currentType == null || !currentType.inInstanceMethod) {
                    error(e.keyword(),
                        "Can't use '" + e.keyword().lexeme() +
                        "' outside of a method.");
                }
                                     // Stack effects:
                emitGET(VAR_THIS);   // this
            }
            case Expr.True ignored -> emit(TRUE);
            case Expr.Unary e -> {
                var op = switch(e.op().type()) {
                    case TokenType.BANG  -> NOT;
                    case TokenType.MINUS -> NEGATE;
                    default -> throw new IllegalStateException(
                        "Unexpected operator: " + e.op());
                };

                                     // Stack effects:
                emit(e.right());     // a       ; compute right
                emit(op);            // b       ; b = op a
            }
            case Expr.VarGet e -> {
                                     // Stack effects:
                emitGET(e.name());   // value     ; get name
            }
            case Expr.VarIncrDecr e -> {
                var incrDecr = token2incrDecr(e.op());

                if (e.isPre()) { // Pre-increment/decrement
                                          // Stack effects:
                    emitGET(e.name());    // a        ; a = name
                    emit(incrDecr);       // b        ; b = a +/- 1
                    emitSET(e.name());    // b        ; name = b
                } else { // Post-increment/decrement
                                          // Stack effects:
                    emitGET(e.name());    // a        ; a = name
                    emit(TSET);           // a        ; T = a
                    emit(incrDecr);       // b        ; b = a +/- 1
                    emitSET(e.name());    // b        ; name = b
                    emit(POP);            // ∅
                    emit(TGET);           // a        ; a = T
                }
            }
            case Expr.VarSet e -> {
                // Simple Assignment
                if (e.op().type() == TokenType.EQUAL) {
                                          // Stack effects:
                    emit(e.value());      // a        ; compute value
                    emitSET(e.name());    // a        ; name = a
                    return;
                }

                // Assignment with update
                var mathOp = token2updater(e.op());

                                          // Stack effects:
                emitGET(e.name());        // a      ; a = name
                emit(e.value());          // a b    ; compute update value
                emit(mathOp);             // c      ; c = a op b
                emitSET(e.name());        // c      ; name = c
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

    // Returns true if we are at global scope, and false otherwise.
    private boolean inGlobalScope() {
        return current.scopeDepth == 0;
    }

    // Declares and defines the variable with the given name, taking the
    // appropriate action whether we are in the global or a local scope.
    // The variable's value *must* be on the top of the stack before this
    // is called.
    private void defineVar(Token name) {
        if (inGlobalScope()) {
            defineGlobal(name);
        } else {
            defineLocal(name);
        }
    }

    // Defines a global variable with the given name.  The variable's
    // initial value is taken from the top of the stack, and immediately assigned
    // to the named variable in the global environment.
    //
    // emit: initializer      | ∅ → value
    // GLODEF nameIndex       | value → ∅
    private void defineGlobal(Token name) {
        assert inGlobalScope();
        emit(GLODEF, constant(name.lexeme()));
    }

    // Declares a local variable.  Checks for too many locals, and for
    // duplicate declarations in the current scope.  Once this is executed,
    // the variable may no longer be declared, but can not yet be retrieved as
    // it has no value.
    private void declareLocal(Token name) {
        assert !inGlobalScope();
        assert !current.notYetDefined.contains(name.lexeme());

        // Check for too many locals
        if (current.localCount == MAX_LOCALS) {
            error(name, "Too many local variables in function.");
        }

        // Check for duplicate declarations in current scope.
        for (var i = current.localCount - 1; i >= 0; i--) {
            var local = current.locals[i];

            // Stop checking once we get to a lower scope depth.
            if (local.depth != -1 && local.depth < current.scopeDepth) {
                break;
            }

            if (name.lexeme().equals(local.name.lexeme())) {
                error(name, "duplicate variable declaration in this scope.");
            }
        }

        current.notYetDefined.add(name.lexeme());
    }

    // Declares a number of locals at once, e.g., for pattern bindings.
    private void declareLocals(List<Token> names) {
        names.forEach(this::declareLocal);
    }

    // Defines the variable so that it can be referred to in expressions.
    // This constitutes a promise that the value the variable will be placed
    // on the stack before any other instruction executes.  Or, to put it
    // another way, that the code to produce that value will be generated
    // before any other code is generated.
    //
    // Usually this is called either immediately before or immediately after the
    // code that generates the value is generated.
    private void defineLocal(Token name) {
        assert !inGlobalScope();

        current.notYetDefined.remove(name.lexeme());
        current.locals[current.localCount++] =
            new Local(name, current.scopeDepth);
    }

    // Defines a number of locals at once, e.g., for pattern bindings.
    private void defineLocals(List<Token> names) {
        names.forEach(this::defineLocal);
    }

    // defineLocal for hidden variables
    private void defineLocal(String name) {
        defineLocal(Token.synthetic(name));
    }

    // Resolves the named variable and emits a GET instruction.
    private void emitGET(Token name) {
        char getOp;
        int arg = resolveLocal(current, name);

        if (arg != -1) {
            getOp = Opcode.LOCGET;
        } else if ((arg = resolveUpvalue(current, name)) != -1) {
            getOp = Opcode.UPGET;
        } else {
            arg = constant(name.lexeme());
            getOp = Opcode.GLOGET;
        }

        emit(getOp, (char)arg);
    }

    // emitGET for hidden locals.
    private void emitGET(String name) {
        emitGET(Token.synthetic(name));
    }

    // Resolves the named variable and emits a SET instruction.
    private void emitSET(Token name) {
        char setOp;
        int arg = resolveLocal(current, name);

        if (arg != -1) {
            setOp = Opcode.LOCSET;
        } else if ((arg = resolveUpvalue(current, name)) != -1) {
            setOp = Opcode.UPSET;
        } else {
            arg = constant(name.lexeme());
            setOp = Opcode.GLOSET;
        }

        emit(setOp, (char)arg);
    }

    // emitSET for hidden locals.
    private void emitSET(String name) {
        emitSET(Token.synthetic(name));
    }

    // Resolves the name as the name of the local variable in the current
    // scope.  Returns the local's index in the current scope, or -1 if
    // no variable was found.
    private int resolveLocal(FunctionInfo compiler, Token name) {
        if (current.notYetDefined.contains(name.lexeme())) {
            error(name, "Can't read local variable in its own initializer.");
            // Return localCount so that the compiler doesn't look for
            // it as a global.
            return compiler.localCount;
        }

        for (var i = compiler.localCount - 1; i >= 0; i--) {
            var local = compiler.locals[i];
            if (name.lexeme().equals(local.name.lexeme())) {
                return i;
            }
        }
        return -1;
    }

    // Resolves the name as the name of an upvalue.  Returns -1 if the
    // variable is global, and the upvalue index otherwise.  Captures
    // locals as upvalues.
    private int resolveUpvalue(FunctionInfo compiler, Token name) {
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
        FunctionInfo compiler,
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
    // Loop management

    // Begins the loop's break/continue control region.
    private void beginLoop(int loopStart) {
        currentLoop = new LoopInfo(currentLoop);
        currentLoop.breakDepth = current.scopeDepth;
        currentLoop.continueDepth = current.scopeDepth;
        currentLoop.loopStart = loopStart;
    }

    // Ends the loop's break/continue control region, and patches all
    // break jumps
    private void endLoop() {
        patchJumps(currentLoop.breakJumps);
        currentLoop = currentLoop.enclosing;
    }

    //-------------------------------------------------------------------------
    // Type Management

    // Begins a new type definition
    private void beginType(Kind kind) {
        currentType = new TypeInfo(currentType);
        currentType.kind = kind;
    }

    // Ends the type definition
    private void endType() {
        currentType = currentType.enclosing;
    }

    //-------------------------------------------------------------------------
    // Error Handling

    // This method should be used for most compilation errors.
    private void error(Token token, String message) {
        var msg = token.span().isAtEnd()
            ? "Error at end: " + message
            : "Error at '" + token.lexeme() + "': " + message;
        errors.add(new Trace(token.span(), msg));
    }

    // Generates an ad hoc error at a given line.
    @SuppressWarnings("SameParameterValue")
    private void error(String at, String message) {
        var msg = "Error at " + at + ": " + message;
        var span = buffer.lineSpan(current.sourceLine);
        errors.add(new Trace(span, msg));
    }

    //-------------------------------------------------------------------------
    // Code Generation

    // Sets the current source line
    private void line(int line) {
        current.sourceLine = line;
    }

    // Sets the current source line to that of the given token.
    private void line(Token token) {
        line(token.span());
    }

    // Sets the current source line to the start line of the span.
    private void line(Span span) {
        if (span != null) line(span.startLine());
    }

    // Sets the current source line to the end line of the span.
    private void lineAtEnd(Span span) {
        if (span != null) line(span.endLine());
    }

    // Returns the current location in the chunk, e.g., for determining
    // the start of a loop.
    private int here() {
        return current.chunk.codeSize();
    }

    // Adds a constant to the constants table and returns its
    // index.
    private char constant(Object value) {
        return current.chunk.addConstant(value);
    }

    // Adds a constant to the constant table for the token's lexeme,
    // and returns its index.
    private char name(Token name) {
        return constant(name.lexeme());
    }

    // Adds the value to the constants table and emits CONST.
    private void emitCONST(Object value) {
        emit(Opcode.CONST, constant(value));
    }

    // Emits METHOD with the name of the method.  The method's
    // closure must already be on the stack.
    private void emitMETHOD(Token name) {
        emit(METHOD, constant(name.lexeme()));
    }

    // Builds a new ListValue from multiple expressions.
    private void emitList(List<Expr> items) {
        emit(LISTNEW);
        for (var item : items) {
            emit(item);
            emit(LISTADD);
        }
    }

    private int emitJump(char opcode) {
        emit(opcode);
        emit(Character.MAX_VALUE);
        return current.chunk.codeSize() - 1;
    }

    private void emitLoop(int loopStart) {
        emit(Opcode.LOOP);
        int offset = current.chunk.codeSize() - loopStart + 1;
        if (offset < Character.MAX_VALUE) {
            emit((char) offset);
        } else {
            error("loop target", "loop size larger than " +
                Character.MAX_VALUE + ".");
        }
    }

    private JumpList jumpList() {
        return new JumpList();
    }

    // This is a no-op if the offset is -1.
    private void patchJump(int offset) {
        if (offset == -1) {
            return;
        }
        // -1 to adjust for the bytecode for the jump offset itself.
        int jump = current.chunk.codeSize() - offset - 1;

        if (jump > Character.MAX_VALUE) {
            error("jump target", "jump size larger than " +
                Character.MAX_VALUE + ".");
        }

        current.chunk.setCode(offset, (char)jump);
    }

    private void patchJumps(JumpList jumpList) {
        for (var offset : jumpList) {
            patchJump(offset);
        }
    }

    // This function emits the `RETURN` for implicitly returning from
    // a function.  For normal functions it returns `NULL`; for
    // class initializers, it returns the instance that's been
    // initialized.
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

    // Emits a THROW instruction; used to mark unfinished code.
    @SuppressWarnings("unused")
    private void emitTHROW(String message) {
        emitCONST(message);
        emit(THROW);
    }

    private void emit(char... codes) {
        for (var code : codes) {
            current.chunk.write(code, current.sourceLine);
        }
    }

    //-------------------------------------------------------------------------
    // Helper Classes


    // A local variable.
    private static class Local {
        // The name of the variable
        final Token name;

        // Its scope depth
        final int depth;

        // Whether it has been captured as an Upvalue
        boolean isCaptured = false;

        Local(Token name, int depth) {
            this.name = name;
            this.depth = depth;
        }
    }

    // State for the function currently being compiled.
    private class FunctionInfo {
        // The enclosing function, or null.
        final FunctionInfo enclosing;

        // The names of the function's parameters.
        final List<String> parameters = new ArrayList<>();

        // The chunk into which byte-code is compiled.
        final Chunk chunk;

        // Information about the function's local variables.
        // Locals that have been declared but not yet defined are
        // added to notYetDefined to ensure that they aren't used
        // in their own initializers.
        final Set<String> notYetDefined = new HashSet<>();
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

        FunctionInfo(
            FunctionInfo enclosing,
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
                local = new Local(Token.synthetic(VAR_THIS), 0);
            } else {
                local = new Local(Token.synthetic(""), 0);
            }
            locals[localCount++] = local;
        }
    }

    private enum Kind {
        CLASS,
        RECORD
    }

    // Data about the type currently being compiled.
    private static class TypeInfo {
        // The type below this one on the type stack.
        final TypeInfo enclosing;

        // The kind of type.
        Kind kind = Kind.CLASS;

        // Whether this type has a supertype.
        boolean hasSupertype = false;

        // Whether we are in an instance method or not.
        boolean inInstanceMethod = false;

        // Constructor
        TypeInfo(TypeInfo enclosing) {
            this.enclosing = enclosing;
        }
    }

    private class LoopInfo {
        // The enclosing loop, or null if none.
        final LoopInfo enclosing;

        // The scope depth before the entire loop is compiled.
        // Allows break to know how many scopes to end.
        int breakDepth = -1;

        // The scope depth before the loop body is compiled.
        // Allows 'continue' to know how many scopes to end.
        int continueDepth = -1;

        // The jump instruction indices for any `break` statements in the
        // body of the loop.
        final JumpList breakJumps;

        // The chunk index to loop back to on continue
        int loopStart = -1;

        LoopInfo(LoopInfo enclosing) {
            this.enclosing = enclosing;
            this.breakJumps = new JumpList();
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

    private class JumpList extends ArrayList<Integer> {
        public JumpList() {
            // Nothing to do
        }

        public void emit(char jumpCode) {
            add(emitJump(jumpCode));
        }
    }
}
