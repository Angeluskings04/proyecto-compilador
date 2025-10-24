package com.compiler.parser.lr;

import com.compiler.parser.grammar.Production;
import com.compiler.parser.grammar.Symbol;

/**
 * Represents an LR(1) item: [A -> α • β, a]
 */
public class LR1Item {
    public final Production production;
    public final int dotPosition;
    public final Symbol lookahead;

    public LR1Item(Production production, int dotPosition, Symbol lookahead) {
        if (production == null) throw new IllegalArgumentException("production cannot be null");
        if (dotPosition < 0 || dotPosition > production.right.size())
            throw new IllegalArgumentException("invalid dot position");
        if (lookahead == null) throw new IllegalArgumentException("lookahead cannot be null");
        this.production = production;
        this.dotPosition = dotPosition;
        this.lookahead = lookahead;
    }

    public boolean atEnd() {
        return dotPosition >= production.right.size();
    }

    public Symbol nextSymbol() {
        return atEnd() ? null : production.right.get(dotPosition);
    }

    public LR1Item shiftDot() {
        if (atEnd()) return this;
        return new LR1Item(production, dotPosition + 1, lookahead);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof LR1Item)) return false;
        LR1Item o = (LR1Item) obj;
        return production.equals(o.production)
            && dotPosition == o.dotPosition
            && lookahead.equals(o.lookahead);
    }

    @Override
    public int hashCode() {
        int r = production.hashCode();
        r = 31 * r + dotPosition;
        r = 31 * r + lookahead.hashCode();
        return r;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(production.left.name).append(" -> ");
        for (int i = 0; i < production.right.size(); i++) {
            if (i == dotPosition) sb.append("• ");
            sb.append(production.right.get(i).name).append(' ');
        }
        if (dotPosition == production.right.size()) sb.append("• ");
        sb.append(", ").append(lookahead.name).append(']');
        return sb.toString();
    }
}