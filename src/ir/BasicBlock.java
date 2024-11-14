package ir;

import ir.instr.Instr;

import java.util.ArrayList;

public class BasicBlock extends Value {
    public ArrayList<Instr> instrs;
    public Function parentFunc;

    public BasicBlock(String name) {
        super(null, name);
        instrs = new ArrayList<>();
    }
}
