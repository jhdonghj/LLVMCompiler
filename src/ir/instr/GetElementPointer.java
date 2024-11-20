package ir.instr;

import ir.Value;
import ir.type.ArrayType;
import ir.type.PointerType;
import ir.type.Type;

import static utils.IO.writeln;

public class GetElementPointer extends Instr {
    public GetElementPointer(String name, Value pointer, Value... offsets) {
        super(null, name, pointer);
        int of_num = offsets.length;
        Type type = pointer.type;
        for (Value offset : offsets) {
            addOperands(offset);
            if (type instanceof ArrayType) {
                type = ((ArrayType) type).elementType;
            } else if (type instanceof PointerType) {
                type = ((PointerType) type).elementType;
            }
        }
        this.type = new PointerType(type);
    }

    @Override
    public void print() {
        // <result> = getelementptr <ty>, <ty>* <ptrval>{, [inrange] <ty> <idx>}*
        Value pointer = operands.get(0);
        PointerType pointerType = (PointerType) pointer.type;
        Type targetType = pointerType.elementType;
        StringBuilder sb = new StringBuilder();
        sb.append("  ").append(name).append(" = getelementptr ").append(targetType).append(", ").append(pointerType).append(" ").append(pointer.name);
        for (int i = 1; i < operands.size(); i++) {
            sb.append(", ").append(operands.get(i).type).append(" ").append(operands.get(i).name);
        }
        writeln(sb.toString());
    }
}
