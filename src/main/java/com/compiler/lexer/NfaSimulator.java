package com.compiler.lexer;

import java.util.HashSet;
import java.util.Set;

import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;
import com.compiler.lexer.nfa.Transition;

/** Simulator for running input strings on an NFA. */
public class NfaSimulator {

    public NfaSimulator() {}

    /** Runs the input on the NFA and returns true if accepted. */
    public boolean simulate(NFA nfa, String input) {
        Set<State> current = new HashSet<>();
        addEpsilonClosure(nfa.getStartState(), current);

        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            Set<State> next = new HashSet<>();
            for (State s : current) {
                for (Transition t : s.getTransitions()) {
                    if (t.symbol != null && t.symbol == ch) {
                        addEpsilonClosure(t.toState, next);
                    }
                }
            }
            current = next;
            if (current.isEmpty()) return false;
        }

        for (State s : current) {
            if (s.isFinal) return true;
        }
        return false;
    }

    /** Adds to closureSet all states reachable from start via epsilon transitions (including start). */
    private void addEpsilonClosure(State start, Set<State> closureSet) {
        if (closureSet.contains(start)) return;
        closureSet.add(start);
        for (Transition t : start.getTransitions()) {
            if (t.symbol == null) {
                addEpsilonClosure(t.toState, closureSet);
            }
        }
    }
}
