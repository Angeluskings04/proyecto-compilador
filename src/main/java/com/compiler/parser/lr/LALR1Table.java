package com.compiler.parser.lr;

import java.util.*;
import com.compiler.parser.grammar.Grammar;
import com.compiler.parser.grammar.Production;
import com.compiler.parser.grammar.Symbol;
import com.compiler.parser.grammar.SymbolType;

/**
 * Builds the LALR(1) parsing table by merging LR(1) states with the same LR(0) cores.
 */
public class LALR1Table {
    private final LR1Automaton automaton;

    // LALR merged states and transitions
    private final List<Set<LR1Item>> lalrStates = new ArrayList<>();
    private final Map<Integer, Map<Symbol,Integer>> lalrTransitions = new HashMap<>();
    private int initialState = 0;

    // ACTION and GOTO tables
    public static final class Action {
        public enum Type { SHIFT, REDUCE, ACCEPT }
        public final Type type;
        public final Integer state; // for SHIFT
        public final Production production; // for REDUCE

        private Action(Type t, Integer s, Production p) { this.type=t; this.state=s; this.production=p; }
        public static Action shift(int s) { return new Action(Type.SHIFT, s, null); }
        public static Action reduce(Production p) { return new Action(Type.REDUCE, null, p); }
        public static Action accept() { return new Action(Type.ACCEPT, null, null); }

        @Override public String toString() {
            switch (type) {
                case SHIFT: return "s"+state;
                case REDUCE: return "r("+production.left.name+"->"+production.right+")";
                case ACCEPT: return "acc";
                default: return "?";
            }
        }

        @Override public boolean equals(Object o) {
            if (!(o instanceof Action)) return false;
            Action a = (Action)o;
            return Objects.equals(type,a.type) && Objects.equals(state,a.state) && Objects.equals(production,a.production);
        }

        @Override public int hashCode() { return Objects.hash(type,state,production); }
    }

    private final Map<Integer, Map<Symbol,Action>> actionTable = new HashMap<>();
    private final Map<Integer, Map<Symbol,Integer>> gotoTable = new HashMap<>();
    private final List<String> conflicts = new ArrayList<>();

    public LALR1Table(LR1Automaton automaton) {
        this.automaton = automaton;
    }

    public void build() {
        // 1) Build LR(1) automaton
        automaton.build();

        // 2) Group LR(1) states by LR(0) cores
        Map<CoreKey, List<Integer>> groups = new LinkedHashMap<>();
        List<Set<LR1Item>> lrStates = automaton.getStates();
        for (int i=0;i<lrStates.size();i++) {
            CoreKey key = new CoreKey(lrStates.get(i));
            groups.computeIfAbsent(key,k->new ArrayList<>()).add(i);
        }

        // 3) Merge states in each group (union of lookaheads for identical (prod,dot))
        Map<Integer,Integer> oldToNew = new HashMap<>();
        for (Map.Entry<CoreKey,List<Integer>> e : groups.entrySet()) {
            Set<LR1Item> merged = new LinkedHashSet<>();
            // index items by (prod,dot)
            Map<ItemCore, Set<Symbol>> laByCore = new HashMap<>();
            for (int old : e.getValue()) {
                for (LR1Item it : lrStates.get(old)) {
                    ItemCore ic = new ItemCore(it.production, it.dotPosition);
                    laByCore.computeIfAbsent(ic,k->new LinkedHashSet<>()).add(it.lookahead);
                }
            }
            for (Map.Entry<ItemCore, Set<Symbol>> kv : laByCore.entrySet()) {
                ItemCore ic = kv.getKey();
                for (Symbol la : kv.getValue()) {
                    merged.add(new LR1Item(ic.production, ic.dotPosition, la));
                }
            }
            int newIndex = lalrStates.size();
            lalrStates.add(merged);
            for (int old : e.getValue()) oldToNew.put(old, newIndex);
        }
        initialState = oldToNew.getOrDefault(0, 0);

        // 4) Rebuild transitions
        for (Map.Entry<Integer, Map<Symbol,Integer>> tr : automaton.getTransitions().entrySet()) {
            int oldFrom = tr.getKey();
            int newFrom = oldToNew.get(oldFrom);
            for (Map.Entry<Symbol,Integer> mv : tr.getValue().entrySet()) {
                int newTo = oldToNew.get(mv.getValue());
                lalrTransitions.computeIfAbsent(newFrom,k->new HashMap<>()).put(mv.getKey(), newTo);
            }
        }

        // 5) Fill ACTION/GOTO
        Grammar G = automaton.getGrammar();
        for (int s=0; s<lalrStates.size(); s++) {
            for (LR1Item it : lalrStates.get(s)) {
                if (!it.atEnd()) {
                    Symbol X = it.nextSymbol();
                    Integer t = optTrans(s, X);
                    if (t != null) {
                        if (X.type == SymbolType.TERMINAL) putAction(s, X, Action.shift(t));
                        else putGoto(s, X, t);
                    }
                } else {
                    // reduce or accept
                    if (it.production.equals(automaton.getAugmentedProduction()) && "$".equals(it.lookahead.name)) {
                        putAction(s, it.lookahead, Action.accept());
                    } else {
                        putAction(s, it.lookahead, Action.reduce(it.production));
                    }
                }
            }
        }
    }

    private Integer optTrans(int s, Symbol X) {
        Map<Symbol,Integer> row = lalrTransitions.get(s);
        return row==null?null:row.get(X);
    }

    private void putAction(int s, Symbol a, Action act) {
        Map<Symbol,Action> row = actionTable.computeIfAbsent(s,k->new HashMap<>());
        Action prev = row.get(a);
        if (prev != null && !prev.equals(act)) {
            conflicts.add("Conflict at state "+s+" on "+a.name+": "+prev+" vs "+act);
        } else {
            row.put(a, act);
        }
    }
    private void putGoto(int s, Symbol A, int t) {
        Map<Symbol,Integer> row = gotoTable.computeIfAbsent(s,k->new HashMap<>());
        row.put(A, t);
    }

    // Utilities for LR(0) core keys
    private static final class ItemCore {
        final Production production;
        final int dotPosition;
        ItemCore(Production p, int dot) { this.production=p; this.dotPosition=dot; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof ItemCore)) return false;
            ItemCore k = (ItemCore)o;
            return production.equals(k.production) && dotPosition==k.dotPosition;
        }
        @Override public int hashCode() { return Objects.hash(production, dotPosition); }
    }
    private static final class CoreKey {
        final Set<ItemCore> items;
        CoreKey(Set<LR1Item> state) {
            Set<ItemCore> s = new LinkedHashSet<>();
            for (LR1Item it : state) s.add(new ItemCore(it.production, it.dotPosition));
            this.items = java.util.Collections.unmodifiableSet(s);
        }
        @Override public boolean equals(Object o) {
            if (!(o instanceof CoreKey)) return false;
            return items.equals(((CoreKey)o).items);
        }
        @Override public int hashCode() { return items.hashCode(); }
    }

    // Accessors used in tests
    public List<Set<LR1Item>> getLALRStates() { return lalrStates; }
    public Map<Integer, Map<Symbol,Integer>> getLALRTransitions() { return lalrTransitions; }
    public Map<Integer, Map<Symbol,Action>> getActionTable() { return actionTable; }
    public Map<Integer, Map<Symbol,Integer>> getGotoTable() { return gotoTable; }
    public List<String> getConflicts() { return conflicts; }
    public int getInitialState() { return initialState; }

    // Helper to resolve a terminal by name (for the parser)
    public Symbol findTerminal(String name) {
        for (Symbol t : automaton.getGrammar().getTerminals()) {
            if (t.name.equals(name)) return t;
        }
        if ("$".equals(name)) return new Symbol("$", SymbolType.TERMINAL);
        return null;
    }

    public Symbol getDollar() { return automaton.getDollar(); }
    public Grammar getGrammar() { return automaton.getGrammar(); }
}