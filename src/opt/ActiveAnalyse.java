package opt;

import ir.*;
import ir.instr.Instr;
import ir.instr.Phi;
import mipsGen.MipsInfo;

import java.util.HashMap;
import java.util.HashSet;

public class ActiveAnalyse {
    private static HashMap<BasicBlock, HashSet<Value>> defs = new HashMap<>();
    private static HashMap<BasicBlock, HashSet<Value>> uses = new HashMap<>();

    public static void run(Program program) {
        MipsInfo.act_flag = true;
        for (Function function : program.functions) {
            for (BasicBlock bb : function.bbs) {
                defs.put(bb, new HashSet<>());
                uses.put(bb, new HashSet<>());
                for (Instr instr : bb.instrs) {
                    if (instr instanceof Phi phi) {
                        for (Value value : phi.operands) {
                            if (value instanceof Instr || value instanceof FuncParam || value instanceof GlobalVariable) {
                                uses.get(bb).add(value);
                            }
                        }
                    }
                }
                for (Instr instr : bb.instrs) {
                    for (Value value : instr.operands) if (!defs.get(bb).contains(value)) {
                        if (value instanceof Instr || value instanceof FuncParam || value instanceof GlobalVariable) {
                            uses.get(bb).add(value);
                        }
                    }
                    if (!uses.get(bb).contains(instr)) {
                        uses.get(bb).add(instr);
                    }
                }
            }
            boolean changed = false;
            while (true) {
                changed = false;
                for (BasicBlock bb : function.bbs) {
                    HashSet<Value> new_out = new HashSet<>();
                    for (BasicBlock succ : bb.succs) {
                        new_out.addAll(succ.act_in);
                    }
                    HashSet<Value> new_in = new HashSet<>(new_out);
                    new_in.removeAll(defs.get(bb));
                    new_in.addAll(uses.get(bb));
                    if (!new_in.equals(bb.act_in) || !new_out.equals(bb.act_out)) {
                        changed = true;
                    }
                    bb.act_in = new_in; bb.act_out = new_out;
                }
                if (!changed) {
                    break;
                }
            }
//            for (BasicBlock bb : function.bbs) {
//                HashMap<Instr, HashSet<Value>> idefs = new HashMap<>();
//                HashMap<Instr, HashSet<Value>> iuses = new HashMap<>();
//
//            }
        }
    }
}
