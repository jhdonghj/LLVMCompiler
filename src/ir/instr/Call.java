package ir.instr;

import ir.Function;
import ir.Value;

import java.util.ArrayList;

import static ir.type.IntegerType.VOID_TYPE;
import static utils.IO.writeln;

public class Call extends Instr {
    public Call(String name, Function func, Value... args) {
        super(func.retType, name, func);
        addOperands(args);
    }

    public Call(String name, Function func, ArrayList<Value> args) {
        super(func.retType, name, func);
        for (Value arg : args) {
            addOperands(arg);
        }
    }

    @Override
    public void print() {
        StringBuilder sb = new StringBuilder();
        sb.append("  ");
        if (type != VOID_TYPE) {
            sb.append(name).append(" = ");
        }
        sb.append("call ").append(type).append(" @").append(operands.get(0).name).append("(");
        for (int i = 1; i < operands.size(); i++) {
            if (i > 1) {
                sb.append(", ");
            }
            sb.append(operands.get(i).type).append(" ").append(operands.get(i).name);
        }
        sb.append(")");
        writeln(sb.toString());
    }
}
