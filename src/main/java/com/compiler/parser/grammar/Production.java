package com.compiler.parser.analysis;


import java.util.List;

/**
 * Production
 * ----------
 * Represents a grammar production: A -> α
 */
public class Production {
    private final Symbol left;
    private final List<Symbol> right;

    public Production(Symbol left, List<Symbol> right) {
        this.left = left;
        this.right = right;
    }

    public Symbol getLeft() {
        return left;
    }

    public List<Symbol> getRight() {
        return right;
    }

    @Override
    public String toString() {
        return left + " -> " + right;
    }
}
