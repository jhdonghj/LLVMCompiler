package opt;

import ir.BasicBlock;
import ir.Function;
import ir.Program;
import ir.instr.Call;
import ir.instr.Instr;

import java.util.HashMap;
import java.util.HashSet;

public class FuncRegClosure {
    public static HashMap<Function, HashSet<Function>> G = new HashMap<>();

    public static void run(Program program) {
        for (Function function : program.functions) {
            G.put(function, new HashSet<>());
            for (BasicBlock bb : function.bbs) {
                for (Instr instr : bb.instrs) {
                    if (instr instanceof Call call) {
                        G.get(function).add(call.getFunction());
                    }
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
}
