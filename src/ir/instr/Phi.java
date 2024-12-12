package ir.instr;

import ir.BasicBlock;
import ir.ConstInt;
import ir.Value;
import ir.type.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Phi extends Instr {
    public HashMap<BasicBlock, Value> preBBs;

    public Phi(Type type, String name, HashSet<BasicBlock> preBBs) {
        super(type, name);
        this.preBBs = new HashMap<>();
        for (BasicBlock bb : preBBs) {
            this.preBBs.put(bb, new ConstInt(0));
        }
    }

    public void addOperand(BasicBlock bb, Value value) {
        preBBs.put(bb, value);
        value.addUse(this);
    }

    public Value getOperand(BasicBlock bb) {
        return preBBs.get(bb);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  ").append(name).append(" = phi ").append(type).append(" ");
        ArrayList<BasicBlock> bbs = new ArrayList<>(preBBs.keySet());
        for (BasicBlock bb : bbs) {
            if (!bb.equals(bbs.get(0))) {
                sb.append(", ");
            }
            sb.append("[ ").append(preBBs.get(bb).name).append(", %").append(bb.name).append(" ]");
        }
        return sb.toString();
    }
}
