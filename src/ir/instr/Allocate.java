package ir.instr;

import ir.Initializer;
import ir.type.PointerType;
import ir.type.Type;

import static utils.IO.writeln;

public class Allocate extends Instr {
    public Type allocType;
    public Initializer initializer;

    public Allocate(String name, Type allocType) {
        super(new PointerType(allocType), name);
        this.allocType = allocType;
    }

    public Allocate(String name, Type allocType, Initializer initializer) {
        super(new PointerType(allocType), name);
        this.allocType = allocType;
        this.initializer = initializer;
    }

    @Override
    public void print() {
        // %name = alloca type
        writeln(String.format("  %s = alloca %s", name, allocType));
    }
}
