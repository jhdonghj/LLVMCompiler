package ir.instr;

import ir.BasicBlock;
import ir.Value;

import static ir.type.IntegerType.VOID_TYPE;
import static utils.IO.writeln;

public class Branch extends Instr {
    public Branch(String name, Value cond, BasicBlock thenBB, BasicBlock elseBB) {
        super(VOID_TYPE, name, cond, thenBB, elseBB);
    }

    @Override
    public void print() {
        // br i1 %cond, label %thenBB, label %elseBB
        Value cond = operands.get(0);
        BasicBlock thenBB = (BasicBlock) operands.get(1);
        BasicBlock elseBB = (BasicBlock) operands.get(2);
        writeln(String.format("  br i1 %s, label %%%s, label %%%s", cond.name, thenBB.name, elseBB.name));
    }
}
