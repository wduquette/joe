package com.wjduquette.joe.nero;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class contains the Nero Abstract Syntax Tree types.
 */
class NeroAST {
    private NeroAST() {} // not instantiable

    //-------------------------------------------------------------------------
    // Clauses

    public sealed interface Clause permits FactClause, RuleClause {}

    public record FactClause(AtomItem item) implements Clause {
        public Atom asFact() {
            return item.asFact();
        }

        @Override public String toString() {
            return item + ".";
        }
    }

    public record RuleClause(AtomItem head, List<Item> body) implements Clause {
        public Rule asRule() {
            var realBody = body.stream().map(Item::asBodyItem).toList();
            return new Rule(head.asFact(), realBody);
        }

        @Override public String toString() {
            var bodyString = body.stream().map(Item::toString)
                .collect(Collectors.joining(", "));
            return head + " :- " + bodyString + ".";
        }
    }

    //-------------------------------------------------------------------------
    // Items

    public sealed interface Item permits AtomItem, ComparisonItem {
        BodyItem asBodyItem();
    }

    public record AtomItem(
        Token relation,
        List<TermToken> terms,
        boolean negated
    ) implements Item {
        /**
         * Gets a list of the variable names used in the item.
         * @return The list
         */
        public List<String> getVariableNames() {
            return terms.stream()
                .filter(t -> t instanceof VariableToken)
                .map(TermToken::toString)
                .toList();
        }

        /**
         * Converts the item to a normal atom.  It's an error if the
         * item's negated flag is set.
         * TODO: Consider renaming `asAtom`. Might be the head of a rule.
         * @return The atom.
         */
        public Atom asFact() {
            if (negated) {
                throw new IllegalStateException("Atom is negated; cannot be a fact.");
            }
            return toAtom();
        }

        /**
         * Converts the item to a Rule's BodyItem, according to the item's
         * negated flag.
         * @return The BodyItem
         */
        @Override
        public BodyItem asBodyItem() {
            return negated ? new BodyItem.Negated(toAtom())
                           : new BodyItem.Normal(toAtom());
        }

        // Convert the time to an Atom, as used by the engine.
        private Atom toAtom() {
            var realTerms = terms.stream().map(TermToken::asTerm).toList();
            return new Atom(relation.lexeme(), realTerms);
        }

        @Override public String toString() {
            var termString = terms.stream().map(TermToken::toString)
                .collect(Collectors.joining(", "));
            return (negated ? "not " : "") +
                relation.lexeme() + "(" + termString + ")";
        }
    }

    public record ComparisonItem() implements Item {
        @Override
        public BodyItem asBodyItem() {
            throw new UnsupportedOperationException("TODO");
        }
    }

    //-------------------------------------------------------------------------
    // Terms

    /**
     * A term in an atom, either a constant or a variable.
     */
    public sealed interface TermToken
        permits ConstantToken, VariableToken
    {
        /** Converts the token to a `Term` as used in the engine. */
        Term asTerm();
    }

    /**
     * A token representing a constant term: a keyword, string, or
     * integer.
     * @param value The value token
     */
    public record ConstantToken(Token value) implements TermToken {
        @Override public Term asTerm() {
            return new Term.Constant(value.literal());
        }

        @Override public String toString() {
            return value.lexeme();
        }
    }

    /**
     * A token representing a variable term: an identifier.
     * @param name The token token
     */
    public record VariableToken(Token name) implements TermToken {
        @Override public Term asTerm() {
            return new Term.Variable(name.lexeme());
        }

        @Override public String toString() {
            return name.lexeme();
        }
    }



}
