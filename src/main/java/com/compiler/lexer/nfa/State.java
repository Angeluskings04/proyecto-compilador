package com.compiler.lexer.nfa;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a state in a Non-deterministic Finite Automaton (NFA).
 * Each state has a unique identifier, a list of transitions to other states,
 * and a flag indicating whether it is a final (accepting) state.
 */
public class State {
    /** Unique identifier for the state. */
    public final int id;

    /** List of transitions from this state to other states. */
    private final List<Transition> transitions;

    /** Indicates whether this state is an accepting (final) state. */
    public boolean isFinal;

    /**
     * Constructs a state with the given id and final flag.
     * @param id unique id
     * @param isFinal whether this is an accepting state
     */
    public State(int id, boolean isFinal) {
        this.id = id;
        this.isFinal = isFinal;
        this.transitions = new ArrayList<>();
    }

    /**
     * Adds a transition to this state's transition list.
     * @param transition transition to add
     */
    public void addTransition(Transition transition) {
        this.transitions.add(transition);
    }

    /**
     * Returns all transitions from this state.
     * @return list of transitions
     */
    public List<Transition> getTransitions() {
        return this.transitions;
    }

    /**
     * Returns the states reachable from this state via epsilon transitions (symbol == null).
     * @return list of states reachable by epsilon transitions
     */
    public List<State> getEpsilonTransitions() {
        List<State> result = new ArrayList<>();
        for (Transition t : transitions) {
            if (t.symbol == null) {
                result.add(t.toState);
            }
        }
        return result;
    }

    /**
     * Returns the states reachable from this state via a transition with the given symbol.
     * @param symbol the symbol for the transition
     * @return list of states reachable by the given symbol
     */
    public List<State> getTransitions(char symbol) {
        List<State> result = new ArrayList<>();
        for (Transition t : transitions) {
            if (t.symbol != null && t.symbol == symbol) {
                result.add(t.toState);
            }
        }
        return result;
    }
}
