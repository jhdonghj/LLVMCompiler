package ir.instr;

import ir.Value;

import static ir.type.IntegerType.VOID_TYPE;
import static utils.IO.writeln;

public class Return extends Instr {
    public Return(String name, Value retVal) {
        super(VOID_TYPE, name);
        if (retVal != null) {
            addOperands(retVal);
        }
    }

    @Override
    public void print() {
        if (operands.size() == 0) {
            writeln("  ret void");
        } else {
            writeln(String.format("  ret %s %s", operands.get(0).type, operands.get(0).name));
        }
    }
}
