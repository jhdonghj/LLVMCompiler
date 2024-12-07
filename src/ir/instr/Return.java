package ir.instr;

import ir.ConstInt;
import ir.Value;
import mipsGen.Regs;
import mipsGen.mipsInfo;

import static ir.type.IntegerType.VOID_TYPE;
import static mipsGen.mipsInfo.loadValue;
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
            } else if (mipsInfo.value2reg.containsKey(retVal)) {
                Regs reg = mipsInfo.value2reg.get(retVal);
                writeln(String.format("    move $v0, $%s", reg));
            } else {
                if (!mipsInfo.value2offset.containsKey(retVal)) {
                    mipsInfo.alloc(retVal.type);
                    mipsInfo.value2offset.put(retVal, mipsInfo.cur_offset);
                }
                if (retVal.type.getByte() == 4) {
                    writeln(String.format("    lw $v0, %d($sp)", mipsInfo.value2offset.get(retVal)));
                } else {
                    writeln(String.format("    lb $v0, %d($sp)", mipsInfo.value2offset.get(retVal)));
                }
            }
        }
        writeln("    jr $ra");
    }
}
