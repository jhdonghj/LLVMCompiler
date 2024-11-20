package ir.instr;

import ir.BasicBlock;

import java.util.ArrayList;

import static ir.type.IntegerType.INT_TYPE;

public class Phi extends Instr {
    ArrayList<BasicBlock> incomingBBs;

    public Phi(String name, BasicBlock... incomingBBs) {
        super(INT_TYPE, name, incomingBBs);
    }
}
