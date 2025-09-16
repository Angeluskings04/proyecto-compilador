package com.compiler.lexer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;
import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;

/**
 * NfaToDfaConverter
 * -----------------
 * Construye un DFA a partir de un NFA usando la construcción por subconjuntos.
 * Cada estado del DFA representa un conjunto de estados del NFA.
 */
public class NfaToDfaConverter {

    public NfaToDfaConverter() {}

    /**
     * Convierte un NFA a DFA usando subset construction.
     * @param nfa NFA de entrada
     * @param alphabet Alfabeto (símbolos consumibles; sin epsilon)
     * @return DFA resultante
     */
    public static DFA convertNfaToDfa(NFA nfa, Set<Character> alphabet) {
        if (nfa == null || nfa.getStartState() == null) {
            throw new IllegalArgumentException("NFA inválido: startState null");
        }

        // 1) Estado inicial del DFA = ε-closure({startNFA})
        Set<State> startSet = new LinkedHashSet<>();
        startSet.add(nfa.getStartState());
        Set<State> startClosure = epsilonClosure(startSet);

        DfaState dfaStart = new DfaState(startClosure);
        // Final si hay algún NFA final O si contiene explícitamente el endState del NFA
        dfaStart.setFinal(containsFinal(startClosure) || startClosure.contains(nfa.endState));

        List<DfaState> allStates = new ArrayList<>();
        allStates.add(dfaStart);

        // Índice rápido: conjunto NFA -> DfaState (clave por contenido)
        Map<Set<State>, DfaState> seen = new HashMap<>();
        seen.put(new LinkedHashSet<>(startClosure), dfaStart);

        // 2) BFS sobre los estados del DFA
        Deque<DfaState> work = new ArrayDeque<>();
        work.add(dfaStart);

        while (!work.isEmpty()) {
            DfaState current = work.poll();
            Set<State> T = current.getName(); // conjunto de NFA que representa

            for (char a : alphabet) {
                // move(T, a) y luego ε-closure
                Set<State> moveSet = move(T, a);
                if (moveSet.isEmpty()) continue;

                Set<State> U = epsilonClosure(moveSet);
                if (U.isEmpty()) continue;

                // Reusar o crear estado DFA para U
                Set<State> key = new LinkedHashSet<>(U);
                DfaState target = seen.get(key);
                if (target == null) {
                    target = new DfaState(U);
                    // Final si algún NFA es final O si contiene el endState del NFA
                    target.setFinal(containsFinal(U) || U.contains(nfa.endState));

                    seen.put(key, target);
                    allStates.add(target);
                    work.add(target);
                }

                // Añadir transición determinista
                current.addTransition(a, target);
            }
        }

        return new DFA(dfaStart, allStates);
    }

    /** ε-closure de un conjunto de estados NFA. */
    private static Set<State> epsilonClosure(Set<State> states) {
        Deque<State> stack = new ArrayDeque<>(states);
        Set<State> closure = new LinkedHashSet<>(states);
        while (!stack.isEmpty()) {
            State s = stack.pop();
            for (State e : s.getEpsilonTransitions()) {
                if (closure.add(e)) {
                    stack.push(e);
                }
            }
        }
        return closure;
    }

    /** move(S, a): estados alcanzables desde S consumiendo el símbolo a (sin ε). */
    private static Set<State> move(Set<State> states, char symbol) {
        Set<State> out = new LinkedHashSet<>();
        for (State s : states) {
            for (State dst : s.getTransitions(symbol)) {
                out.add(dst);
            }
        }
        return out;
    }

    /** true si algún estado del NFA en el conjunto es final. */
    private static boolean containsFinal(Set<State> nfaSet) {
        for (State s : nfaSet) {
            if (s.isFinal) return true;
        }
        return false;
    }
}



