package com.compiler.lexer.regex;

import java.util.Stack;

import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;
import com.compiler.lexer.nfa.Transition;

/**
 * Builds an NFA from an infix regular expression using Thompson's construction.
 */
public class RegexParser {

    private int nextId = 0;
    private State newState(boolean isFinal) {
        return new State(nextId++, isFinal);
    }

    public NFA parse(String infixRegex) {
        String withConcat = ShuntingYard.insertConcatenationOperator(infixRegex);
        String postfix = ShuntingYard.toPostfix(withConcat);
        Stack<NFA> stack = new Stack<>();
        for (int i = 0; i < postfix.length(); i++) {
            char c = postfix.charAt(i);
            if (isOperand(c)) {
                stack.push(createNfaForCharacter(c));
            } else if (c == '·') {
                handleConcatenation(stack);
            } else if (c == '|') {
                handleUnion(stack);
            } else if (c == '*') {
                handleKleeneStar(stack);
            } else if (c == '+') {
                handlePlus(stack);
            } else if (c == '?') {
                handleOptional(stack);
            } else {
                throw new IllegalArgumentException("Unexpected token in postfix: " + c);
            }
        }
        if (stack.size() != 1) throw new IllegalStateException("Invalid postfix: stack size = " + stack.size());
        return stack.pop();
    }

    private void handleOptional(Stack<NFA> stack) {
        NFA f = stack.pop();
        State s = newState(false);
        State a = newState(false);
        // ε to fragment and ε to accept
        s.addTransition(new Transition(null, f.startState));
        s.addTransition(new Transition(null, a));
        // fragment end ε to accept
        f.endState.addTransition(new Transition(null, a));
        stack.push(new NFA(s, a));
    }

    private void handlePlus(Stack<NFA> stack) {
        NFA f = stack.pop();
        State s = newState(false);
        State a = newState(false);
        // must take fragment once
        s.addTransition(new Transition(null, f.startState));
        // loop from fragment end back to start, and to accept
        f.endState.addTransition(new Transition(null, f.startState));
        f.endState.addTransition(new Transition(null, a));
        stack.push(new NFA(s, a));
    }

    private NFA createNfaForCharacter(char c) {
    State s = newState(false);
    State a = newState(false);

    if (c == 'ε') {
        // Épsilon real: transición con símbolo null
        s.addTransition(new Transition(null, a));
    } else {
        // Símbolo normal
        s.addTransition(new Transition(c, a));
    }
    return new NFA(s, a);
}


    private void handleConcatenation(Stack<NFA> stack) {
        NFA f2 = stack.pop();
        NFA f1 = stack.pop();
        // connect f1.end -> ε -> f2.start
        f1.endState.addTransition(new Transition(null, f2.startState));
        stack.push(new NFA(f1.startState, f2.endState));
    }

    private void handleUnion(Stack<NFA> stack) {
        NFA f2 = stack.pop();
        NFA f1 = stack.pop();
        State s = newState(false);
        State a = newState(false);
        s.addTransition(new Transition(null, f1.startState));
        s.addTransition(new Transition(null, f2.startState));
        f1.endState.addTransition(new Transition(null, a));
        f2.endState.addTransition(new Transition(null, a));
        stack.push(new NFA(s, a));
    }

    private void handleKleeneStar(Stack<NFA> stack) {
        NFA f = stack.pop();
        State s = newState(false);
        State a = newState(false);
        // enter or skip
        s.addTransition(new Transition(null, f.startState));
        s.addTransition(new Transition(null, a));
        // loop and exit
        f.endState.addTransition(new Transition(null, f.startState));
        f.endState.addTransition(new Transition(null, a));
        stack.push(new NFA(s, a));
    }

    /** Returns true if c is an operand (letter/digit/_/epsilon). */
    private boolean isOperand(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == 'ε';
    }
}
