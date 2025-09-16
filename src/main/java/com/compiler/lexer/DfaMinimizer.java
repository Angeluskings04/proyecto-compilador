package com.compiler.lexer;

import java.util.*;
import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;

/**
 * Minimización de DFA por refinamiento de particiones (estilo Hopcroft).
 * Previo a minimizar, se completa el DFA con un estado muerto para que
 * la función de transición sea total: δ: Q × Σ → Q.
 */
public class DfaMinimizer {

    public static DFA minimizeDfa(DFA dfa, Set<Character> alphabet) {
        if (dfa == null || dfa.getStartState() == null) {
            throw new IllegalArgumentException("DFA inválido");
        }

        // Snapshot mutable de los estados del DFA original
        List<DfaState> states = new ArrayList<>(dfa.getAllStates());
        if (states.isEmpty()) {
            return new DFA(dfa.getStartState(), states);
        }

        // ===== 0) COMPLETAR DFA (sin ConcurrentModification) =====
        boolean needDead = false;
        for (DfaState s : states) {
            for (char a : alphabet) {
                if (s.getTransition(a) == null) {
                    needDead = true;
                    break;
                }
            }
            if (needDead) break;
        }

        DfaState dead = null;
        if (needDead) {
            // Crear dead y añadirlo a la lista en una sola operación fuera del bucle
            dead = new DfaState(new LinkedHashSet<>()); // conjunto NFA vacío
            dead.setFinal(false);
            List<DfaState> newStates = new ArrayList<>(states);
            newStates.add(dead);
            states = newStates;
        }

        // Rellenar transiciones faltantes y hacer que el dead se auto-bucle
        for (DfaState s : states) {
            for (char a : alphabet) {
                if (s.getTransition(a) == null) {
                    s.addTransition(a, (dead != null) ? dead : s);
                }
            }
        }
        if (dead != null) {
            for (char a : alphabet) {
                dead.addTransition(a, dead);
            }
        }
        // ===== Fin completado =====

        // 1) Partición inicial: finales vs no finales
        Set<DfaState> finals = new LinkedHashSet<>();
        Set<DfaState> nonFinals = new LinkedHashSet<>();
        for (DfaState s : states) {
            if (s.isFinal()) finals.add(s);
            else nonFinals.add(s);
        }
        List<Set<DfaState>> P = new ArrayList<>();
        if (!finals.isEmpty()) P.add(finals);
        if (!nonFinals.isEmpty()) P.add(nonFinals);

        Deque<Set<DfaState>> W = new ArrayDeque<>();
        for (Set<DfaState> block : P) {
            W.add(new LinkedHashSet<>(block));
        }

        // 2) Refinamiento
        while (!W.isEmpty()) {
            Set<DfaState> A = W.poll();
            for (char a : alphabet) {
                Set<DfaState> X = preImage(states, A, a);
                if (X.isEmpty()) continue;

                ListIterator<Set<DfaState>> it = P.listIterator();
                List<Set<DfaState>> toAdd = new ArrayList<>();
                while (it.hasNext()) {
                    Set<DfaState> Y = it.next();
                    Set<DfaState> inter = new LinkedHashSet<>(Y);
                    inter.retainAll(X);
                    if (inter.isEmpty()) continue;

                    Set<DfaState> diff = new LinkedHashSet<>(Y);
                    diff.removeAll(X);
                    if (diff.isEmpty()) continue;

                    // Partir Y en (inter, diff)
                    it.remove();
                    it.add(inter);
                    toAdd.add(diff);

                    if (containsSet(W, Y)) {
                        removeSet(W, Y);
                        W.add(new LinkedHashSet<>(inter));
                        W.add(new LinkedHashSet<>(diff));
                    } else {
                        // Añadir el bloque más pequeño a W
                        if (inter.size() <= diff.size()) {
                            W.add(new LinkedHashSet<>(inter));
                        } else {
                            W.add(new LinkedHashSet<>(diff));
                        }
                    }
                }
                P.addAll(toAdd);
            }
        }

        // 3) Construir DFA mínimo (un estado por bloque de P)
        Map<DfaState, Integer> blockId = new HashMap<>();
        List<DfaState> minStates = new ArrayList<>();

        for (int i = 0; i < P.size(); i++) {
            Set<DfaState> block = P.get(i);
            boolean isFinalBlock = block.stream().anyMatch(DfaState::isFinal);
            DfaState rep = block.iterator().next();
            DfaState newState = new DfaState(rep.getName());
            newState.setFinal(isFinalBlock);
            minStates.add(newState);
            for (DfaState old : block) {
                blockId.put(old, i);
            }
        }

        // 4) Transiciones en el DFA mínimo
        for (int i = 0; i < P.size(); i++) {
            Set<DfaState> block = P.get(i);
            DfaState any = block.iterator().next();
            DfaState newSrc = minStates.get(i);
            for (char a : alphabet) {
                DfaState oldDst = any.getTransition(a);
                Integer j = blockId.get(oldDst);
                if (j == null) continue;
                DfaState newDst = minStates.get(j);
                newSrc.addTransition(a, newDst);
            }
        }

        // 5) Start del mínimo: bloque que contiene al start original
        int startBlock = blockId.get(dfa.getStartState());
        DfaState minStart = minStates.get(startBlock);
        return new DFA(minStart, minStates);
    }

    // --- Helpers ---
    private static Set<DfaState> preImage(List<DfaState> states, Set<DfaState> targetBlock, char a) {
        Set<DfaState> pre = new LinkedHashSet<>();
        for (DfaState s : states) {
            DfaState t = s.getTransition(a);
            if (t != null && targetBlock.contains(t)) {
                pre.add(s);
            }
        }
        return pre;
    }

    private static boolean containsSet(Deque<Set<DfaState>> deque, Set<DfaState> s) {
        for (Set<DfaState> x : deque) {
            if (x.equals(s)) return true;
        }
        return false;
    }

    private static void removeSet(Deque<Set<DfaState>> deque, Set<DfaState> s) {
        Iterator<Set<DfaState>> it = deque.iterator();
        while (it.hasNext()) {
            Set<DfaState> x = it.next();
            if (x.equals(s)) {
                it.remove();
                return;
            }
        }
    }
}
