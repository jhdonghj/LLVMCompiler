package ir;

import ir.instr.Instr;
import ir.type.BBType;

import java.util.ArrayList;
import java.util.HashSet;

import static irGen.IrGen.new_bb;
import static utils.IO.writeln;

public class BasicBlock extends Value {
    public ArrayList<Instr> instrs;
    public Function parentFunc;

    public HashSet<BasicBlock> preds = new HashSet<>();
    public HashSet<BasicBlock> succs = new HashSet<>();
    public HashSet<Value> act_in = new HashSet<>();
    public HashSet<Value> act_out = new HashSet<>();

    public BasicBlock(String name) {
        super(new BBType(), name);
        instrs = new ArrayList<>();
        new_bb(this); // auto add to current function
    }

    public Instr getLastInstr() {
        return instrs.get(instrs.size() - 1);
    }

    public void to_llvm() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(":");
        while (sb.length() < 50) {
            sb.append(" ");
        }
        sb.append("; preds = ");
        for (BasicBlock pred : preds) {
            sb.append(pred.name).append(", ");
        }
        if (!preds.isEmpty()) {
            sb.delete(sb.length() - 2, sb.length());
        }
        writeln(sb.toString());
        for (Instr instr : instrs) {
            instr.to_llvm();
        }
        writeln("");
    }

    public void to_mips() {
        writeln(String.format("%s:", name));
        for (Instr instr : instrs) {
            instr.to_mips();
        }
        writeln("");
    }
}
