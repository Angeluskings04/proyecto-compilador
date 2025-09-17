package com.compiler.parser.analysis;



/**
 * Symbol
 * ------
 * Represents a symbol in a grammar (terminal or non-terminal).
 */
public class Symbol {
    private final String name;
    private final boolean terminal;

    public Symbol(String name, boolean terminal) {
        this.name = name;
        this.terminal = terminal;
    }

    public String getName() {
        return name;
    }

    public boolean isTerminal() {
        return terminal;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Symbol)) return false;
        Symbol other = (Symbol) obj;
        return this.name.equals(other.name) && this.terminal == other.terminal;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + (terminal ? 1 : 0);
    }
}
