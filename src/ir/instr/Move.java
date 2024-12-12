package ir.instr;

import ir.Value;
import mipsGen.Regs;

import static mipsGen.MipsInfo.loadValue;
import static mipsGen.MipsInfo.storeValue;
import static utils.IO.writeln;

public class Move extends Instr {
    public Move(Value target, Value source) {
        super(source.type, target.name, target, source);
//        assert target.type.equals(source.type);
    }

    public Value getTarget() {
        return operands.get(0);
    }

    public Value getSource() {
        return operands.get(1);
    }

    public void replaceSource(Phi tempReg) {
        operands.set(1, tempReg);
    }

    @Override
    public String toString() {
        return String.format("  %s = move %s", getTarget().name, getSource().name);
    }

    @Override
    public void to_mips() {
        super.to_mips();
        Value source = getSource(), target = getTarget();
        Regs regs = Regs.k0, regt = Regs.k1;

        regs = loadValue(source, regs);
        regt = loadValue(target, regt);

        writeln(String.format("    move $%s, $%s", regt, regs));

        storeValue(this, regt);
    }
}
