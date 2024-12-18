package ir.instr;

import ir.Value;
import mipsGen.MipsInfo;
import mipsGen.Regs;

import static mipsGen.MipsInfo.*;
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
        Regs src = Regs.k0, dest = value2reg.getOrDefault(target.name, Regs.k0);

        src = loadValue(source, src);

        move(dest, src);

        storeValue(this, dest);
    }
}
