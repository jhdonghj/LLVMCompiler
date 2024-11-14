package ir.instr;

import ir.Initializer;
import ir.ValueType;

public class Allocate extends Instr {
    public ValueType allocType;
    public Initializer initializer;

    public Allocate(String name, ValueType allocType) {
        super(name);
        this.allocType = allocType;
    }
}
