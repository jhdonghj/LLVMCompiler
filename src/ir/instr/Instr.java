package ir.instr;

import ir.BasicBlock;
import ir.User;
import ir.Value;
import ir.type.Type;

import static irGen.IrGen.new_instr;
import static java.lang.String.format;
import static utils.IO.writeln;

public class Instr extends User {
    public BasicBlock parentBB;
//    public int id;

    public Instr(Type type, String name, Value... operands) {
        // name = %id
        super(type, name, operands);
//        id = Integer.parseInt(name.substring(1));
        new_instr(this); // auto add to current basic block
    }

    public String toString() {
        return "not implemented";
    }

    public void print() {
        writeln(this.toString());
    }

    public void to_mips() {
        writeln(format("    # %s", this));
    }
}
