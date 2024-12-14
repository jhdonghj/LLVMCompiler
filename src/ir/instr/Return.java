package ir.instr;

import ir.ConstInt;
import ir.Value;
import mipsGen.Regs;
import mipsGen.MipsInfo;

import static ir.type.IntegerType.INT_TYPE;
import static ir.type.IntegerType.VOID_TYPE;
import static mipsGen.MipsInfo.move;
import static utils.IO.writeln;

public class Return extends Instr {
    public Return(String name, Value retVal) {
        super(VOID_TYPE, name);
        if (retVal != null) {
            addOperands(retVal);
        }
    }

    @Override
    public String toString() {
        if (operands.isEmpty()) {
            return "  ret void";
        } else {
            return String.format("  ret %s %s", operands.get(0).type, operands.get(0).name);
        }
    }

    @Override
    public void to_mips() {
        super.to_mips();
        if (!operands.isEmpty()) {
            Value retVal = operands.get(0);
            if (retVal instanceof ConstInt) {
                writeln(String.format("    li $v0, %d", ((ConstInt) retVal).value));
            } else if (MipsInfo.value2reg.containsKey(retVal.name)) {
                move(Regs.v0, MipsInfo.value2reg.get(retVal.name));
//                Regs reg = MipsInfo.value2reg.get(retVal.name);
//                writeln(String.format("    move $v0, $%s", reg));
            } else {
                if (!MipsInfo.value2offset.containsKey(retVal.name)) {
                    MipsInfo.alloc(retVal.type);
                    MipsInfo.value2offset.put(retVal.name, MipsInfo.cur_offset);
                }
                if (retVal.type.getByte() == 4) {
                    writeln(String.format("    lw $v0, %d($sp)", MipsInfo.value2offset.get(retVal.name)));
                } else {
                    writeln(String.format("    lb $v0, %d($sp)", MipsInfo.value2offset.get(retVal.name)));
                }
            }
        }
        for (Regs reg : parentBB.parentFunc.reg2offset.keySet()) {
            MipsInfo.load(INT_TYPE, reg, parentBB.parentFunc.reg2offset.get(reg), Regs.sp);
        }
        writeln("    jr $ra");
    }
}
