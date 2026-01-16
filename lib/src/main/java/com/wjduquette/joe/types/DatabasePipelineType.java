package com.wjduquette.joe.types;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.ProxyType;
import com.wjduquette.joe.nero.*;

/**
 * A ProxyType for the NeroDatabase.Pipeline type.
 */
public class DatabasePipelineType extends ProxyType<NeroDatabase.Pipeline> {
    /** The type, ready for installation. */
    public static final DatabasePipelineType TYPE = new DatabasePipelineType();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates the proxy.
     */
    DatabasePipelineType() {
        super("DatabasePipeline");

        //**
        // @package joe
        // @type DatabasePipeline
        // The `DatabasePipeline` object allows Joe code to execute
        // [Nero Datalog](../nero/nero.md) [[RuleSet|RuleSets]] on
        // the content of a [[Database]] in a variety of ways.
        // `DatabasePipelines` are created using the [[Database]]
        // `with*` methods.
        proxies(NeroDatabase.Pipeline.class);

        method("check",      this::_check);
        method("debug",      this::_debug);
        method("load",       this::_load);
        method("query",      this::_query);
        method("queryParm",  this::_queryParm);
        method("queryParms", this::_queryParms);
        method("update",     this::_update);
    }

    //-------------------------------------------------------------------------
    // Stringify

    public String stringify(Joe joe, Object value) {
        return name() + "@" + value.hashCode();
    }

    //-------------------------------------------------------------------------
    // Instance Method Implementations

    //**
    // @method check
    // %args validationSchema
    // %result this
    // Verifies that the rule set to execute is compatible with the
    // *validationSchema*, throwing an error if it is not. The schema should
    // be passed as a [[RuleSet]] or Nero script that contains only
    // non-transient `define` declarations.
    private Object _check(NeroDatabase.Pipeline pipeline, Joe joe, Args args) {
        args.exactArity(1, "check(validateSchema)");
        var rules = joe.toRules(args.next());
        var schema = rules.outputSchema();
        pipeline.check(schema);
        return pipeline;
    }

    //**
    // @method debug
    // %args [flag]
    // %result this
    // Sets the pipeline's debug *flag*.  If omitted, the flag defaults to
    // true.
    private Object _debug(NeroDatabase.Pipeline pipeline, Joe joe, Args args) {
        args.arityRange(0, 1, "debug([flag])");
        pipeline.debug(!args.hasNext() || joe.toBoolean(args.next()));
        return pipeline;
    }

    //**
    // @method load
    // %result Database
    // Executes the rule set, adding all results to the database. Throws an
    // error if the facts produced by the rule set are not compatible
    // with the database's current content.
    private Object _load(NeroDatabase.Pipeline pipeline, Joe joe, Args args) {
        args.exactArity(0, "load()");
        pipeline.load();
        return pipeline.database();
    }

    //**
    // @method queryParm
    // %args name, value
    // %result this
    // Defines a query parameter for use by the rule set.  The *name*
    // must be a valid identifier string.
    //
    // All accumulated query parameters will be visible in the rule set
    // as the fields of a `query/...` fact. The `query/...` fact
    // will not appear in the output.
    private Object _queryParm(NeroDatabase.Pipeline pipeline, Joe joe, Args args) {
        args.exactArity(2, "queryParm(name, value)");
        return pipeline.queryParm(joe.toIdentifier(args.next()), args.next());
    }

    //**
    // @method queryParms
    // %args map
    // %result this
    // Defines some number of query parameters for use by the rule set given
    // a *map* from parameter names to values.  The names must all be valid
    // Nero identifiers.
    //
    // All accumulated query parameters will be visible in the rule set
    // as the fields of a `query/...` fact. The `query/...` fact
    // will not appear in the output.
    private Object _queryParms(NeroDatabase.Pipeline pipeline, Joe joe, Args args) {
        args.exactArity(1, "queryParms(map)");
        var map = joe.toMap(args.next());
        for (var e : map.entrySet()) {
            pipeline.queryParm(joe.toIdentifier(e.getKey()), e.getValue());
        }
        return pipeline;
    }

    //**
    // @method query
    // %result Set
    // Executes the rule set on the content of the database, returning
    // the newly inferred facts.  The database will be unchanged.
    private Object _query(NeroDatabase.Pipeline pipeline, Joe joe, Args args) {
        args.exactArity(0, "query()");
        var result = pipeline.query();
        return new SetValue(result.all());
    }

    //**
    // @method update
    // %result Database
    // Updates the database using the rule set, returning the database.
    // Throws an error if the rule set is not compatible with the current
    // content of the database.
    private Object _update(NeroDatabase.Pipeline pipeline, Joe joe, Args args) {
        args.exactArity(0, "update()");
        pipeline.update();
        return pipeline.database();
    }
}
