package ir.instr;

import ir.ConstInt;
import ir.Value;
import mipsGen.Regs;
import mipsGen.MipsInfo;

import static ir.type.IntegerType.VOID_TYPE;
import static mipsGen.MipsInfo.loadValue;
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
            Regs reg = Regs.v0;
            reg = loadValue(retVal, reg);
            move(Regs.v0, reg);
        }
        writeln("    jr $ra");
    }
}
