package com.compiler;

import java.util.Map;
import java.util.Set;

import com.compiler.lexer.DfaMinimizer;   // <- si aún no lo tienes, comenta esta línea y la sección de minimización
import com.compiler.lexer.DfaSimulator;
import com.compiler.lexer.NfaToDfaConverter;
import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;
import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.regex.RegexParser;

/**
 * Main class for demonstrating regex to NFA, DFA conversion, minimization, and simulation.
 */
public class Main {
    public Main() {}

    public static void main(String[] args) {
        // --- CONFIGURATION ---
        String regex = "a(b|c)*";
        Set<Character> alphabet = Set.of('a', 'b', 'c');
        String[] testStrings = {"a", "ab", "ac", "abbc", "acb", "", "b", "abcabc"};

        System.out.println("Testing Regex: " + regex + "\n");

        // --- STEP 1: Regex -> NFA ---
        RegexParser parser = new RegexParser();
        NFA nfa = parser.parse(regex);
        nfa.endState.isFinal = true; // en tu NFA, endState e isFinal son públicos, OK

        // --- STEP 2: NFA -> DFA ---
        DFA dfa = NfaToDfaConverter.convertNfaToDfa(nfa, alphabet);
        System.out.println("--- Original DFA ---");
        visualizeDfa(dfa);

        // --- STEP 3: DFA Minimization ---
        // Si aún no implementas DfaMinimizer, comenta estas 3 líneas y usa 'dfa' directamente en el simulador.
        DFA minimizedDfa = DfaMinimizer.minimizeDfa(dfa, alphabet);
        System.out.println("--- Minimized DFA ---");
        visualizeDfa(minimizedDfa);

        // --- STEP 4: DFA Simulation ---
        DfaSimulator dfaSimulator = new DfaSimulator();
        System.out.println("--- Testing Simulator with Minimized DFA ---");

        for (String s : testStrings) {
            boolean accepted = dfaSimulator.simulate(minimizedDfa, s);
            System.out.println("String '" + s + "': " + (accepted ? "Accepted" : "Rejected"));
        }
    }

    /**
     * Prints a textual representation of the DFA structure for debugging purposes.
     */
    public static void visualizeDfa(DFA dfa) {
        System.out.println("Start State: D" + dfa.getStartState().getId());
        for (DfaState state : dfa.getAllStates()) {
            StringBuilder sb = new StringBuilder();
            sb.append("State D").append(state.getId());
            if (state.isFinal()) {
                sb.append(" (Final)");
            }
            sb.append(":");
            // Sort transitions by character for consistent output
            state.getTransitions().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    sb.append("\n  --'").append(entry.getKey())
                      .append("'--> D").append(entry.getValue().getId());
                });
            System.out.println(sb.toString());
        }
        System.out.println("------------------------\n");
    }
}
