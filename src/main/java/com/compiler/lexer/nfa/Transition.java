package com.compiler.lexer.nfa;

/**
 * Represents a transition in a Non-deterministic Finite Automaton (NFA).
 * Each transition consists of a symbol (null for epsilon) and a destination state.
 */
public class Transition {
    /** The symbol that triggers this transition. Null for epsilon transitions. */
    public final Character symbol;

    /** The destination state for this transition. */
    public final State toState;

    /**
     * Constructs a new transition with the given symbol and destination state.
     * @param symbol The symbol for the transition (null for epsilon).
     * @param toState The destination state.
     */
    public Transition(Character symbol, State toState) {
        this.symbol = symbol;
        this.toState = toState;
    }
}
