package ir.instr;

import ir.BasicBlock;
import ir.Function;

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
        if (Function.nxtBB == null || !Function.nxtBB.equals(getDestBB())) {
            writeln(String.format("    j %s", operands.get(0).name));
        }
    }
}
