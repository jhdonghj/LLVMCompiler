package ir.instr;

import ir.BasicBlock;
import ir.User;
import ir.Value;
import ir.type.Type;

import java.util.HashSet;

import static irGen.IrGen.new_instr;
import static java.lang.String.format;
import static utils.IO.writeln;

public class Instr extends User {
    public BasicBlock parentBB;
    public HashSet<String> act_in = new HashSet<>();
    public HashSet<String> act_out = new HashSet<>();

    public Instr(Type type, String name, Value... operands) {
        // name = %id
        super(type, name, operands);
        new_instr(this); // auto add to current basic block
    }

    public boolean isJump() {
        return this instanceof Branch || this instanceof Jump || this instanceof Return;
    }

    public String toString() {
        return String.format("%s not implemented", this.getClass().getSimpleName());
    }

    public void to_llvm() {
        writeln(this.toString());
    }

    public void to_mips() {
        writeln(format("    # %s", this));
    }
}
