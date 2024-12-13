package opt;

import ir.BasicBlock;
import ir.Function;
import ir.Program;
import ir.instr.Instr;
import ir.instr.Jump;
import mipsGen.MipsInfo;

public class MergeBB {
    public static void run(Program program) {
        for (Function function : program.functions) {
            for (BasicBlock bb : function.bbs) {
                MipsInfo.fa.put(bb, bb);
                if (bb.instrs.size() == 1) {
                    Instr instr = bb.instrs.get(0);
                    if (instr instanceof Jump jump) {
                        MipsInfo.fa.put(bb, jump.getDestBB());
                    }
                }
            }
        }
    }
}
