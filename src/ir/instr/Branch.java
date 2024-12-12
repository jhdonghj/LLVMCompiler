package ir.instr;

import ir.BasicBlock;
import ir.Function;
import ir.Value;
import mipsGen.Regs;
import mipsGen.MipsInfo;

import static ir.type.IntegerType.VOID_TYPE;
import static utils.IO.writeln;

public class Branch extends Instr {
    public Branch(String name, Value cond, BasicBlock thenBB, BasicBlock elseBB) {
        super(VOID_TYPE, name, cond, thenBB, elseBB);
    }

    public BasicBlock getThenBB() {
        return (BasicBlock) operands.get(1);
    }

    public BasicBlock getElseBB() {
        return (BasicBlock) operands.get(2);
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
        BasicBlock thenBB = getThenBB();
        BasicBlock elseBB = getElseBB();

        Regs reg;
        if (MipsInfo.value2reg.containsKey(cond.name)) {
            reg = MipsInfo.value2reg.get(cond.name);
        } else {
            reg = Regs.k0;
            writeln(String.format("    lw $%s, %d($sp)", reg, MipsInfo.value2offset.get(cond.name)));
        }
        writeln(String.format("    bne $%s, $0, %s", reg, thenBB.name));
        if (Function.nxtBB == null || !Function.nxtBB.equals(elseBB)) {
            writeln(String.format("    j %s", elseBB.name));
        }
    }
}
