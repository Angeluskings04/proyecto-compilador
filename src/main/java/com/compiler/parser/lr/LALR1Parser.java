package com.compiler.parser.lr;

import java.util.*;
import com.compiler.lexer.Token;
import com.compiler.parser.grammar.Symbol;
import com.compiler.parser.grammar.SymbolType;

/**
 * Simple LALR(1) shift-reduce parser.
 */
public class LALR1Parser {
    private final LALR1Table table;

    public LALR1Parser(LALR1Table table) {
        this.table = table;
    }

    /** Returns the LALR(1) table used by this parser (for tests/debug). */
    public LALR1Table getTable() {
        return this.table;
    }


    public boolean parse(java.util.List<Token> input) {
        // Build input as list of terminal Symbols plus $
        List<Symbol> stream = new ArrayList<>();
        for (Token t : input) {
            Symbol s = table.findTerminal(t.type != null ? t.type : t.lexeme);
            if (s == null) {
                // Try lexeme fallback
                s = table.findTerminal(t.lexeme);
            }
            if (s == null) {
                return false; // unknown token type for this grammar
            }
            stream.add(s);
        }
        stream.add(table.getDollar());

        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(table.getInitialState());
        int ip = 0;

        while (true) {
            int state = stack.peek();
            Symbol a = stream.get(ip);

            LALR1Table.Action act = null;
            Map<Symbol,LALR1Table.Action> row = table.getActionTable().get(state);
            if (row != null) act = row.get(a);

            if (act == null) {
                return false;
            }

            switch (act.type) {
                case SHIFT:
                    stack.push(act.state);
                    ip++;
                    break;
                case REDUCE:
                    int popCount = act.production.right.size();
                    for (int i=0;i<popCount;i++) stack.pop();
                    int s2 = stack.peek();
                    // GOTO
                    Integer next = null;
                    Map<Symbol,Integer> grow = table.getGotoTable().get(s2);
                    if (grow != null) next = grow.get(act.production.left);
                    if (next == null) return false;
                    stack.push(next);
                    break;
                case ACCEPT:
                    return true;
                default:
                    return false;
            }
        }
    }
}