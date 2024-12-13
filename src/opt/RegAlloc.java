package opt;

import ir.*;
import ir.instr.Call;
import ir.instr.Instr;
import mipsGen.Regs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class RegAlloc {
    public static void run(Program program) {
        HashMap<Function, HashSet<Function>> G = new HashMap<>();
        for (Function function : program.functions) {
            G.put(function, new HashSet<>());
            HashMap<Value, Integer> count = new HashMap<>();
            for (BasicBlock bb : function.bbs) {
                for (Instr instr : bb.instrs) {
                    for (Value operand : instr.operands) {
                        count.put(operand, count.getOrDefault(operand, 0) + 1);
                    }
                    if (instr instanceof Call call) {
                        G.get(function).add(call.getFunction());
                    }
                }
            }
            ArrayList<Integer> cnt = new ArrayList<>(count.values());
            ArrayList<Value> values = new ArrayList<>(count.keySet());
            ArrayList<Integer> id = new ArrayList<>();
            for (int i = 0; i < cnt.size(); i++) {
                id.add(i);
            }
            // sort id by cnt, from big to small
            id.sort((a, b) -> cnt.get(b) - cnt.get(a));
            ArrayList<Regs> validRegs = getRegs();
            int j = 0;
            for (int i : id) {
                Value value = values.get(i);
                if (value instanceof FuncParam) {
                    continue;
                }
                function.value2reg.put(value.name, validRegs.get(j));
                j++;
                if (j >= validRegs.size()) {
                    break;
                }
            }
        }
        while (true) {
            boolean changed = false;
            for (Function function : program.functions) {
                if (function.regClosure.addAll(function.value2reg.values())) {
                    changed = true;
                }
                for (Function callee : G.get(function)) {
                    if (function.regClosure.addAll(callee.regClosure)) {
                        changed = true;
                    }
                }
            }
            if (!changed) {
                break;
            }
        }
    }

    private static ArrayList<Regs> getRegs() {
        return new ArrayList<>(){{
            add(Regs.gp); add(Regs.fp);
            add(Regs.t0); add(Regs.t1); add(Regs.t2); add(Regs.t3); add(Regs.t4); add(Regs.t5); add(Regs.t6); add(Regs.t7); add(Regs.t8); add(Regs.t9);
            add(Regs.s0); add(Regs.s1); add(Regs.s2); add(Regs.s3); add(Regs.s4); add(Regs.s5); add(Regs.s6); add(Regs.s7);
        }};
    }
}
