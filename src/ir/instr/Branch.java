package ir.instr;

import ir.BasicBlock;
import ir.Value;
import mipsGen.Regs;
import mipsGen.mipsInfo;

import static ir.type.IntegerType.VOID_TYPE;
import static utils.IO.writeln;

public class Branch extends Instr {
    public Branch(String name, Value cond, BasicBlock thenBB, BasicBlock elseBB) {
        super(VOID_TYPE, name, cond, thenBB, elseBB);
    }

    @Override
    public String toString() {
        // br i1 %cond, label %thenBB, label %elseBB
        Value cond = operands.get(0);
        BasicBlock thenBB = (BasicBlock) operands.get(1);
        BasicBlock elseBB = (BasicBlock) operands.get(2);
        return String.format("  br i1 %s, label %%%s, label %%%s", cond.name, thenBB.name, elseBB.name);
    }

    @Override
    public void to_mips() {
        super.to_mips();
        Value cond = operands.get(0);
        BasicBlock thenBB = (BasicBlock) operands.get(1);
        BasicBlock elseBB = (BasicBlock) operands.get(2);

        Regs reg;
        if (mipsInfo.value2reg.containsKey(cond)) {
            reg = mipsInfo.value2reg.get(cond);
        } else {
            reg = Regs.k0;
            writeln(String.format("    lw $%s, %d($sp)", reg, mipsInfo.value2offset.get(cond)));
        }
        writeln(String.format("    bne $%s, $0, %s", reg, thenBB.name));
        writeln(String.format("    j %s", elseBB.name));
    }
}
