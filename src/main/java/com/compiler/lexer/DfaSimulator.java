package com.compiler.lexer;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;

/**
 * DfaSimulator
 * ------------
 * Simula la ejecución de un DFA sobre una cadena de entrada.
 */
public class DfaSimulator {

    public DfaSimulator() { }

    /**
     * Simula el DFA sobre la cadena dada.
     * @param dfa   Autómata determinista
     * @param input Cadena de entrada
     * @return true si la cadena es aceptada; false en caso contrario.
     */
    public boolean simulate(DFA dfa, String input) {
        if (dfa == null || dfa.getStartState() == null) {
            return false;
        }
        DfaState current = dfa.getStartState();

        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            DfaState next = current.getTransition(ch);
            if (next == null) {
                // No hay transición definida para este símbolo: rechaza
                return false;
            }
            current = next;
        }
        // Acepta si el estado actual es final
        return current.isFinal();
    }
}
