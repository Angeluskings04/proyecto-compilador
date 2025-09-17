package com.compiler.parser.analysis;


import com.compiler.parser.grammar.*;
import java.util.*;

/**
 * StaticAnalyzer
 * --------------
 * Computes FIRST and FOLLOW sets for a grammar.
 */
public class StaticAnalyzer {
    private final Grammar grammar;
    private final Map<Symbol, Set<Symbol>> firstSets = new HashMap<>();
    private final Map<Symbol, Set<Symbol>> followSets = new HashMap<>();

    private final Symbol EPSILON = new Symbol("ε", true);
    private final Symbol EOF = new Symbol("$", true);

    public StaticAnalyzer(Grammar grammar) {
        this.grammar = grammar;
        computeFirstSets();
        computeFollowSets();
    }

    /** Compute FIRST sets */
    private void computeFirstSets() {
        // Initialize
        for (Production p : grammar.getProductions()) {
            firstSets.putIfAbsent(p.getLeft(), new HashSet<>());
            for (Symbol s : p.getRight()) {
                firstSets.putIfAbsent(s, new HashSet<>());
            }
        }
        // Fixed-point algorithm
        boolean changed;
        do {
            changed = false;
            for (Production p : grammar.getProductions()) {
                Set<Symbol> firstA = firstSets.get(p.getLeft());
                int before = firstA.size();
                firstA.addAll(firstOfSequence(p.getRight()));
                if (firstA.size() > before) changed = true;
            }
        } while (changed);
    }

    /** Compute FOLLOW sets */
    private void computeFollowSets() {
        for (Production p : grammar.getProductions()) {
            followSets.putIfAbsent(p.getLeft(), new HashSet<>());
            for (Symbol s : p.getRight()) {
                followSets.putIfAbsent(s, new HashSet<>());
            }
        }
        followSets.get(grammar.getStartSymbol()).add(EOF);

        boolean changed;
        do {
            changed = false;
            for (Production p : grammar.getProductions()) {
                List<Symbol> right = p.getRight();
                for (int i = 0; i < right.size(); i++) {
                    Symbol B = right.get(i);
                    if (!B.isTerminal()) {
                        Set<Symbol> followB = followSets.get(B);
                        int before = followB.size();

                        List<Symbol> beta = right.subList(i + 1, right.size());
                        Set<Symbol> firstBeta = firstOfSequence(beta);
                        followB.addAll(firstBeta);
                        followB.remove(EPSILON);

                        if (beta.isEmpty() || firstBeta.contains(EPSILON)) {
                            followB.addAll(followSets.get(p.getLeft()));
                        }
                        if (followB.size() > before) changed = true;
                    }
                }
            }
        } while (changed);
    }

    /** FIRST of a sequence of symbols */
    private Set<Symbol> firstOfSequence(List<Symbol> symbols) {
        Set<Symbol> result = new HashSet<>();
        if (symbols.isEmpty()) {
            result.add(EPSILON);
            return result;
        }
        for (Symbol s : symbols) {
            result.addAll(firstSets.get(s));
            if (!firstSets.get(s).contains(EPSILON)) {
                result.remove(EPSILON);
                break;
            }
        }
        return result;
    }

    public Map<Symbol, Set<Symbol>> getFirstSets() {
        return firstSets;
    }

    public Map<Symbol, Set<Symbol>> getFollowSets() {
        return followSets;
    }
}
