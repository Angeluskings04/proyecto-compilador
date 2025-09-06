package com.compiler.lexer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;
import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;
import com.compiler.lexer.nfa.Transition;

/**
 * NfaToDfaConverter
 * -----------------
 * Subset construction: cada estado del DFA representa un conjunto de estados del NFA.
 */
public class NfaToDfaConverter {

    public NfaToDfaConverter() {
        // nada que hacer aquí por ahora
    }

    /**
     * Convierte un NFA a DFA usando la construcción por subconjuntos.
     * @param nfa      NFA de entrada
     * @param alphabet Alfabeto (símbolos válidos, sin epsilon)
     * @return DFA resultante
     */
    public static DFA convertNfaToDfa(NFA nfa, Set<Character> alphabet) {
        if (nfa == null || nfa.getStartState() == null) {
            throw new IllegalArgumentException("NFA inválido: startState null");
        }

        // 1) Estado inicial del DFA = epsilon-closure({startNFA})
        Set<State> startSet = new LinkedHashSet<>();
        startSet.add(nfa.getStartState());
        Set<State> startClosure = epsilonClosure(startSet);

        DfaState dfaStart = new DfaState(startClosure);
        List<DfaState> allStates = new ArrayList<>();
        allStates.add(dfaStart);

        // Para no buscar O(n) cada vez, llevamos un índice de "set NFA -> DfaState"
        // Usamos un Map con clave basada en identidad de States (los mismos objetos), con equals por contenido del Set.
        Map<Set<State>, DfaState> seen = new HashMap<>();
        seen.put(new LinkedHashSet<>(startClosure), dfaStart);

        // Cola de trabajo (BFS)
        Deque<DfaState> work = new ArrayDeque<>();
        work.add(dfaStart);

        while (!work.isEmpty()) {
            DfaState current = work.poll();
            Set<State> T = current.getName(); // el conjunto de NFA que representa

            for (char a : alphabet) {
                // 2) move(T, a) y luego epsilon-closure
                Set<State> moveSet = move(T, a);
                if (moveSet.isEmpty()) continue;

                Set<State> U = epsilonClosure(moveSet);
                if (U.isEmpty()) continue;

                // 3) Buscar si ya existe ese conjunto como estado DFA
                DfaState target = findDfaState(allStates, U);
                if (target == null) {
                    target = new DfaState(U);
                    allStates.add(target);
                    work.add(target);
                    // también en índice rápido
                    seen.put(new LinkedHashSet<>(U), target);
                }
                // 4) Añadir transición determinista
                current.addTransition(a, target);
            }
        }

        return new DFA(dfaStart, allStates);
    }

    /**
     * epsilon-closure de un conjunto de estados NFA.
     */
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

    /**
     * move(S, a): estados alcanzables desde S consumiendo el símbolo a (sin epsilon).
     */
    private static Set<State> move(Set<State> states, char symbol) {
        Set<State> out = new LinkedHashSet<>();
        for (State s : states) {
            // Usamos el helper de tu State para obtener transiciones por símbolo
            for (State dst : s.getTransitions(symbol)) {
                out.add(dst);
            }
            // (También funcionaría iterar s.getTransitions() y filtrar por symbol != null && == symbol)
        }
        return out;
    }

    /**
     * Busca un DfaState que represente exactamente el conjunto targetNfaStates.
     * (Útil si no quieres depender del Map 'seen' o si tu implementación de equals/hashCode
     * en DfaState está basada en nfaStates).
     */
    private static DfaState findDfaState(List<DfaState> dfaStates, Set<State> targetNfaStates) {
        for (DfaState d : dfaStates) {
            if (d.getName().equals(targetNfaStates)) {
                return d;
            }
        }
        return null;
    }
}
