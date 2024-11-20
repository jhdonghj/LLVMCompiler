package ir.instr;

import ir.BasicBlock;

import static ir.type.IntegerType.VOID_TYPE;
import static utils.IO.writeln;

public class Jump extends Instr {
    public Jump(String name, BasicBlock destBB) {
        super(VOID_TYPE, name, destBB);
    }

    @Override
    public void print() {
        writeln(String.format("  br label %%%s", operands.get(0).name));
    }
}
