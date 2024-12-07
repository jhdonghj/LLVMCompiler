package ir;

import ir.instr.Instr;
import ir.type.BBType;

import java.util.ArrayList;

import static irGen.IrGen.new_bb;
import static utils.IO.writeln;

public class BasicBlock extends Value {
    public ArrayList<Instr> instrs;
    public Function parentFunc;

    public BasicBlock(String name) {
        super(new BBType(), name);
        instrs = new ArrayList<>();
        new_bb(this); // auto add to current function
    }

    public void print() {
        writeln(name + ":");
        for (Instr instr : instrs) {
            instr.print();
        }
    }

    public void to_mips() {
        writeln(String.format("%s:", name));
        for (Instr instr : instrs) {
            instr.to_mips();
        }
    }
}
