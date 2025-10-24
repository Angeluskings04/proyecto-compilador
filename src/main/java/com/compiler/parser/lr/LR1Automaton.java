package com.compiler.parser.lr;

import java.util.*;
import com.compiler.parser.grammar.Grammar;
import com.compiler.parser.grammar.Production;
import com.compiler.parser.grammar.Symbol;
import com.compiler.parser.grammar.SymbolType;
import com.compiler.parser.syntax.StaticAnalyzer;

/**
 * Builds the canonical collection of LR(1) item sets and transitions.
 */
public class LR1Automaton {
    private final Grammar grammar;
    private final List<Set<LR1Item>> states = new ArrayList<>();
    private final Map<Integer, Map<Symbol, Integer>> transitions = new HashMap<>();

    // Augmented symbols
    private final Symbol augmentedStart;
    private final Production augmentedProd;
    private final Symbol dollar;

    private final StaticAnalyzer analyzer;

    public LR1Automaton(Grammar grammar) {
        this.grammar = grammar;
        this.analyzer = new StaticAnalyzer(grammar);
        // create $ terminal
        this.dollar = new Symbol("$", SymbolType.TERMINAL);
        // create augmented start S' -> S
        this.augmentedStart = new Symbol(grammar.getStartSymbol().name+"'", SymbolType.NON_TERMINAL);
        this.augmentedProd = new Production(augmentedStart, java.util.List.of(grammar.getStartSymbol()));
    }

    public Grammar getGrammar() { return grammar; }
    public List<Set<LR1Item>> getStates() { return states; }
    public Map<Integer, Map<Symbol,Integer>> getTransitions() { return transitions; }
    public Symbol getDollar() { return dollar; }
    public Production getAugmentedProduction() { return augmentedProd; }

    public void build() {
        if (!states.isEmpty()) return;
        // I0 = closure({ S' -> • S, $})
        Set<LR1Item> I0core = new LinkedHashSet<>();
        I0core.add(new LR1Item(augmentedProd, 0, dollar));
        Set<LR1Item> I0 = closure(I0core);
        states.add(I0);
        Queue<Integer> work = new ArrayDeque<>();
        work.add(0);

        // symbols to consider: all grammar symbols (terms + nonterms)
        Set<Symbol> symbols = new LinkedHashSet<>();
        symbols.addAll(grammar.getNonTerminals());
        symbols.addAll(grammar.getTerminals());

        while (!work.isEmpty()) {
            int i = work.remove();
            Set<LR1Item> I = states.get(i);
            for (Symbol X : symbols) {
                Set<LR1Item> J = goTo(I, X);
                if (J.isEmpty()) continue;
                int j = indexOfState(J);
                if (j < 0) {
                    j = states.size();
                    states.add(J);
                    work.add(j);
                }
                transitions.computeIfAbsent(i,k->new HashMap<>()).put(X, j);
            }
        }
    }

    public Set<LR1Item> closure(Set<LR1Item> I) {
        Set<LR1Item> C = new LinkedHashSet<>(I);
        boolean changed;
        do {
            changed = false;
            List<LR1Item> snapshot = new ArrayList<>(C);
            for (LR1Item it : snapshot) {
                Symbol B = it.nextSymbol();
                if (B != null && B.type == SymbolType.NON_TERMINAL) {
                    // beta = symbols after B
                    List<Symbol> beta = it.production.right.subList(it.dotPosition + 1, it.production.right.size());
                    // FIRST(beta a)
                    Set<Symbol> lookaheads = firstOfSequence(beta, it.lookahead);
                    for (Production p : grammar.getProductions()) {
                        if (!p.left.equals(B)) continue;
                        for (Symbol a : lookaheads) {
                            if (isEpsilon(a)) continue;
                            LR1Item ni = new LR1Item(p, 0, a);
                            if (C.add(ni)) changed = true;
                        }
                    }
                }
            }
        } while (changed);
        return C;
    }

    public Set<LR1Item> goTo(Set<LR1Item> I, Symbol X) {
        Set<LR1Item> moved = new LinkedHashSet<>();
        for (LR1Item it : I) {
            if (X.equals(it.nextSymbol())) {
                moved.add(it.shiftDot());
            }
        }
        return moved.isEmpty() ? java.util.Collections.emptySet() : closure(moved);
    }

    private int indexOfState(Set<LR1Item> J) {
        for (int idx = 0; idx < states.size(); idx++) {
            if (states.get(idx).equals(J)) return idx;
        }
        return -1;
    }

    private boolean isEpsilon(Symbol s) {
        return s != null && "ε".equals(s.name);
    }

    /**
     * FIRST of a sequence beta followed by a terminal a (beta may be empty).
     * Uses StaticAnalyzer FIRST sets and handles epsilon propagation.
     */
    private Set<Symbol> firstOfSequence(List<Symbol> beta, Symbol a) {
        Map<Symbol, Set<Symbol>> firstSets = analyzer.getFirstSets();
        Set<Symbol> result = new LinkedHashSet<>();
        boolean allNullable = true;
        for (Symbol X : beta) {
            Set<Symbol> f = firstSets.getOrDefault(X, Set.of());
            for (Symbol t : f) {
                if (!isEpsilon(t)) result.add(t);
            }
            if (!f.contains(new Symbol("ε", SymbolType.TERMINAL))) {
                allNullable = false;
                break;
            }
        }
        if (beta.isEmpty() || allNullable) {
            result.add(a);
        }
        return result;
    }
}