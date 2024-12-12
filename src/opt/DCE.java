package opt;

import ir.BasicBlock;
import ir.Function;
import ir.Program;
import ir.Value;
import ir.instr.*;

import java.util.HashSet;

public class DCE {
    private static HashSet<Instr> vis = new HashSet<>();

    public static void run(Program program) {
        for (Function function : program.functions) {
            vis.clear();
            for (BasicBlock bb : function.bbs) {
                for (Instr instr : bb.instrs) {
                    if (isUseful(instr)) {
                        dfs(instr);
                    }
                }
            }
            for (BasicBlock bb : function.bbs) {
                bb.instrs.removeIf(instr -> !vis.contains(instr));
            }
        }
    }

    private static void dfs(Instr instr) {
        if (vis.contains(instr)) return;
        vis.add(instr);
        for (Value operand : instr.operands) {
            if (operand instanceof Instr) {
                dfs((Instr) operand);
            }
        }
    }

    private static boolean isUseful(Instr instr) {
        return instr instanceof Branch || instr instanceof Jump ||
                instr instanceof Return || instr instanceof Call ||
                instr instanceof IOInstr || instr instanceof Store;
    }
}
