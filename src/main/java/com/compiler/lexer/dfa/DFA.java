package com.compiler.lexer.dfa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DFA
 * ---
 * Represents a complete Deterministic Finite Automaton (DFA).
 * Contains the start state and a list of all states in the automaton.
 */
public class DFA {
    /**
     * The starting state of the DFA.
     */
    private final DfaState startState;

    /**
     * A list of all states in the DFA.
     */
    private final List<DfaState> allStates;

    /**
     * Constructs a new DFA.
     * @param startState The starting state of the DFA.
     * @param allStates  A list of all states in the DFA.
     */
    public DFA(DfaState startState, List<DfaState> allStates) {
        this.startState = startState;
        // copiamos para evitar modificar desde fuera
        this.allStates = new ArrayList<>(allStates);
    }

    /** Returns the start state of the DFA. */
    public DfaState getStartState() {
        return startState;
    }

    /** Returns an unmodifiable list of all DFA states. */
    public List<DfaState> getAllStates() {
        return Collections.unmodifiableList(allStates);
    }

    /** Adds a state to the DFA (útil al construir por conversión NFA→DFA). */
    public void addState(DfaState state) {
        if (!allStates.contains(state)) {
            allStates.add(state);
        }
    }

    @Override
    public String toString() {
        return "DFA(start=" + startState + ", states=" + allStates.size() + ")";
    }
}
