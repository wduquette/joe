package com.wjduquette.joe.types;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.ProxyType;
import com.wjduquette.joe.nero.FactSet;
import com.wjduquette.joe.nero.Nero;

/**
 * A ProxyType for the Nero.Pipeline type.
 */
public class NeroPipelineType extends ProxyType<Nero.Pipeline> {
    /** The type, ready for installation. */
    public static final NeroPipelineType TYPE = new NeroPipelineType();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates the proxy.
     */
    NeroPipelineType() {
        super("NeroPipeline");

        //**
        // @package joe
        // @type NeroPipeline
        // The `NeroPipeline` object allows Joe code to execute
        // [Nero Datalog](../nero/nero.md) [[RuleSet|RuleSets]] in a
        // variety of ways.  `NeroPipelines` are created using
        // The [[Nero]] `with*` methods.
        proxies(Nero.Pipeline.class);

        method("debug",      this::_debug);
        method("infer",      this::_infer);
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
    // @method debug
    // %args [flag]
    // %result this
    // Sets the pipeline's debug *flag*.  If omitted, the flag defaults to
    // true.
    private Object _debug(Nero.Pipeline pipeline, Joe joe, Args args) {
        args.arityRange(0, 1, "debug([flag])");
        pipeline.debug(!args.hasNext() || joe.toBoolean(args.next()));
        return pipeline;
    }

    //**
    // @method query
    // %args facts
    // %result Set
    // Executes the rule set given a collection of facts, and returns
    // the newly inferred facts.
    private Object _query(Nero.Pipeline pipeline, Joe joe, Args args) {
        args.exactArity(1, "query(facts)");
        var facts = joe.toFacts(args.next());
        var result = pipeline.query(new FactSet(facts));
        return new SetValue(result.all());
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
    private Object _queryParm(Nero.Pipeline pipeline, Joe joe, Args args) {
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
    private Object _queryParms(Nero.Pipeline pipeline, Joe joe, Args args) {
        args.exactArity(1, "queryParms(map)");
        var map = joe.toMap(args.next());
        for (var e : map.entrySet()) {
            pipeline.queryParm(joe.toIdentifier(e.getKey()), e.getValue());
        }
        return pipeline;
    }

    //**
    // @method infer
    // %result Set
    // Executes the rule set and returns a [[Set]] of all inferred
    // [[Fact|Facts]].
    private Object _infer(Nero.Pipeline pipeline, Joe joe, Args args) {
        args.exactArity(0, "infer()");
        return new SetValue(pipeline.infer().all());
    }

    //**
    // @method update
    // %args facts
    // %result Set
    // Executes the rule set given a collection of facts, and returns
    // an updated collection.
    private Object _update(Nero.Pipeline pipeline, Joe joe, Args args) {
        args.exactArity(1, "update(facts)");
        var facts = new FactSet(joe.toFacts(args.next()));
        pipeline.update(facts);
        return new SetValue(facts.all());
    }

}
