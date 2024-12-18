package ir.instr;

import config.Config;
import ir.BasicBlock;
import ir.Function;
import mipsGen.MipsInfo;

import static ir.type.IntegerType.VOID_TYPE;
import static utils.IO.writeln;

public class Jump extends Instr {
    public Jump(String name, BasicBlock destBB) {
        super(VOID_TYPE, name, destBB);
    }

    public BasicBlock getDestBB() {
        return (BasicBlock) operands.get(0);
    }

    @Override
    public String toString() {
        return String.format("  br label %%%s", operands.get(0).name);
    }

    @Override
    public void to_mips() {
        super.to_mips();
        BasicBlock destBB = getDestBB();
        if (Function.nxtBB == null || !Function.nxtBB.equals(destBB)) {
            writeln(String.format("    j %s", destBB.name));
        }
    }
}
