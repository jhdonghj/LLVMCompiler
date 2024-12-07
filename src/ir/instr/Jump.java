package ir.instr;

import ir.BasicBlock;

import static ir.type.IntegerType.VOID_TYPE;
import static utils.IO.writeln;

public class Jump extends Instr {
    public Jump(String name, BasicBlock destBB) {
        super(VOID_TYPE, name, destBB);
    }

    @Override
    public String toString() {
        return String.format("  br label %%%s", operands.get(0).name);
    }

    @Override
    public void to_mips() {
        super.to_mips();
        writeln(String.format("    j %s", operands.get(0).name));
    }
}
