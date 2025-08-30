package com.compiler.lexer.regex;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Utility class for regular expression parsing using the Shunting Yard algorithm.
 * Inserts explicit concatenation (·) and converts to postfix (RPN).
 */
public class ShuntingYard {

    /**
     * Inserts the explicit concatenation operator ('·') into the regular
     * expression according to standard rules. This makes implicit
     * concatenations explicit for the parser.
     */
    public static String insertConcatenationOperator(String regex) {
        if (regex == null || regex.isEmpty()) return regex;
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < regex.length(); i++) {
            char c1 = regex.charAt(i);
            out.append(c1);
            if (i == regex.length() - 1) break;
            char c2 = regex.charAt(i + 1);

            boolean c1IsOperand = isOperand(c1) || c1 == ')' || c1 == '*' || c1 == '+' || c1 == '?';
            boolean c2IsOperand = isOperand(c2) || c2 == '(';

            if ((isOperand(c1) && isOperand(c2)) ||
                (isOperand(c1) && c2 == '(') ||
                (c1 == ')' && isOperand(c2)) ||
                ((c1 == '*' || c1 == '+' || c1 == '?') && (isOperand(c2) || c2 == '(')) ||
                (c1 == ')' && c2 == '(')) {
                out.append('·');
            }
        }
        return out.toString();
    }

    /** Returns true if c is an operand (letter/digit/underscore/epsilon). */
    private static boolean isOperand(char c) {
        if (Character.isLetterOrDigit(c) || c == '_' || c == 'ε') return true;
        return false;
    }

    /**
     * Converts the given infix regular expression (with explicit '·') to postfix (RPN)
     * using a version of the Shunting Yard algorithm adapted for regex.
     * Supported operators: unary postfix: *, +, ? ; binary: · (concat), | (union)
     * Precedence: unary > · > | ; Left-associative for binary ops.
     */
    public static String toPostfix(String infixRegex) {
        if (infixRegex == null) return "";
        StringBuilder output = new StringBuilder();
        Deque<Character> stack = new ArrayDeque<>();

        java.util.Map<Character,Integer> prec = java.util.Map.of('|', 1, '·', 2);

        for (int i = 0; i < infixRegex.length(); i++) {
            char c = infixRegex.charAt(i);
            if (isOperand(c)) {
                output.append(c);
            } else if (c == '(') {
                stack.push(c);
            } else if (c == ')') {
                while (!stack.isEmpty() && stack.peek() != '(') {
                    output.append(stack.pop());
                }
                if (stack.isEmpty() || stack.peek() != '(') {
                    throw new IllegalArgumentException("Unbalanced parentheses");
                }
                stack.pop(); // remove '('
            } else if (c == '*' || c == '+' || c == '?') {
                // postfix unary: go straight to output
                output.append(c);
            } else if (c == '·' || c == '|') {
                while (!stack.isEmpty() && stack.peek() != '(' &&
                        prec.getOrDefault(stack.peek(), 0) >= prec.getOrDefault(c, 0)) {
                    output.append(stack.pop());
                }
                stack.push(c);
            } else {
                throw new IllegalArgumentException("Unsupported symbol in regex: " + c);
            }
        }
        while (!stack.isEmpty()) {
            char op = stack.pop();
            if (op == '(' || op == ')') throw new IllegalArgumentException("Unbalanced parentheses at end");
            output.append(op);
        }
        return output.toString();
    }
}
