package com.compiler.parser.analysis;


import java.util.ArrayList;
import java.util.List;

/**
 * Grammar
 * -------
 * Represents a context-free grammar (CFG).
 */
public class Grammar {
    private final List<Production> productions = new ArrayList<>();
    private final Symbol startSymbol;

    public Grammar(Symbol startSymbol) {
        this.startSymbol = startSymbol;
    }

    public void addProduction(Production p) {
        productions.add(p);
    }

    public List<Production> getProductions() {
        return productions;
    }

    public Symbol getStartSymbol() {
        return startSymbol;
    }
}
