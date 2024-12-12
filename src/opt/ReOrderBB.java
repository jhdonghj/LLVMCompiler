package opt;

import ir.BasicBlock;
import ir.Function;
import ir.Program;
import ir.instr.Branch;
import ir.instr.Instr;
import ir.instr.Jump;

import java.util.ArrayList;
import java.util.HashSet;

public class ReOrderBB {
    private static ArrayList<BasicBlock> newBBs;
    private static HashSet<BasicBlock> vis;

    public static void run(Program program) {
        for (Function function : program.functions) {
            newBBs = new ArrayList<>();
            vis = new HashSet<>();
            dfs(function.getEntry());
            function.bbs = newBBs;
        }
    }

    private static void dfs(BasicBlock now) {
        if (vis.contains(now)) return;
        newBBs.add(now);
        vis.add(now);
        Instr last = now.getLastInstr();
        if (last instanceof Branch branch) {
            dfs(branch.getElseBB());
            dfs(branch.getThenBB());
        } else if (last instanceof Jump jump) {
            dfs(jump.getDestBB());
        }
    }
}
