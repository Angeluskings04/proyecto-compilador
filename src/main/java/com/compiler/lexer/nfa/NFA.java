package com.compiler.lexer.nfa;

/**
 * Represents a Non-deterministic Finite Automaton (NFA) with a start and end state.
 */
public class NFA {
    /** The initial (start) state of the NFA. */
    public final State startState;

    /** The final (accepting) state of the NFA (tests set isFinal=true explicitly). */
    public final State endState;

    /**
     * Constructs a new NFA with the given start and end states.
     * @param start initial state
     * @param end final state
     */
    public NFA(State start, State end) {
        this.startState = start;
        this.endState = end;
    }

    /** Returns the initial (start) state of the NFA. */
    public State getStartState() {
        return this.startState;
    }
}
