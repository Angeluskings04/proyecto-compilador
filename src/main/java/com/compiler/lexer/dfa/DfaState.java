package com.compiler.lexer.dfa;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.compiler.lexer.nfa.State;

/**
 * DfaState
 * --------
 * Represents a single state in a Deterministic Finite Automaton (DFA).
 * Each DFA state corresponds to a set of states from the original NFA.
 * Provides methods for managing transitions, checking finality, and equality based on NFA state sets.
 */
public class DfaState {
    private static int nextId = 0;

    /** Unique identifier for this DFA state. */
    public final int id;

    /** The set of NFA states this DFA state represents. */
    public final Set<State> nfaStates;

    /** Indicates whether this DFA state is a final (accepting) state. */
    private boolean isFinal;

    /** Map of input symbols to destination DFA states (transitions). */
    private final Map<Character, DfaState> transitions = new HashMap<>();

    /**
     * Constructs a new DFA state.
     * @param nfaStates The set of NFA states that this DFA state represents.
     */
    public DfaState(Set<State> nfaStates) {
        this.id = nextId++;
        this.nfaStates = nfaStates;
        this.isFinal = nfaStates.stream().anyMatch(s -> s.isFinal);
    }

    public int getId() {
    return id;
    }

    /**
     * Returns all transitions from this state.
     * @return Map of input symbols to destination DFA states.
     */
    public Map<Character, DfaState> getTransitions() {
        return transitions;
    }

    /**
     * Adds a transition from this state to another on a given symbol.
     * @param symbol The input symbol for the transition.
     * @param toState The destination DFA state.
     */
    public void addTransition(Character symbol, DfaState toState) {
        transitions.put(symbol, toState);
    }

    /** Two DfaStates are equal if they represent the same set of NFA states. */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DfaState)) return false;
        DfaState other = (DfaState) obj;
        return this.nfaStates.equals(other.nfaStates);
    }

    /** The hash code is based on the set of NFA states. */
    @Override
    public int hashCode() {
        return nfaStates.hashCode();
    }

    /** String representation with id and finality. */
    @Override
    public String toString() {
        return "DfaState(" + id + (isFinal ? ", final" : "") + ")";
    }

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public boolean isFinal() {
        return isFinal;
    }

    /**
     * Gets the transition for a given input symbol.
     * @param symbol The input symbol for the transition.
     * @return The destination DFA state for the transition, or null if none exists.
     */
    public DfaState getTransition(char symbol) {
        return transitions.get(symbol);
    }

    /**
     * Returns the set of NFA states this DFA state represents.
     * @return The set of NFA states.
     */
    public Set<State> getName() {
        return nfaStates;
    }
}
