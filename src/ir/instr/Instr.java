package ir.instr;

import ir.BasicBlock;
import ir.User;
import ir.Value;

public class Instr extends User {
    public BasicBlock parentBB;

    public Instr(String name, Value... operands) {
        super(null, name, operands);
    }
}
