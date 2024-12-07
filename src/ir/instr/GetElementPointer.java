package ir.instr;

import ir.ConstInt;
import ir.Value;
import ir.type.ArrayType;
import ir.type.PointerType;
import ir.type.Type;
import mipsGen.Regs;
import mipsGen.mipsInfo;

import static mipsGen.mipsInfo.loadAddress;
import static mipsGen.mipsInfo.loadValue;
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
    public String toString() {
        // <result> = getelementptr <ty>, <ty>* <ptrval>{, [inrange] <ty> <idx>}*
        Value pointer = operands.get(0);
        PointerType pointerType = (PointerType) pointer.type;
        Type targetType = pointerType.elementType;
        StringBuilder sb = new StringBuilder();
        sb.append("  ").append(name).append(" = getelementptr ").append(targetType).append(", ")
                .append(pointerType).append(" ").append(pointer.name);
        for (int i = 1; i < operands.size(); i++) {
            sb.append(", ").append(operands.get(i).type).append(" ").append(operands.get(i).name);
        }
        return sb.toString();
    }

    @Override
    public void to_mips() {
        super.to_mips();
        Value pointer = operands.get(0);
        Regs pointer_reg = mipsInfo.value2reg.getOrDefault(this, Regs.k0);
        Regs offset_reg = Regs.k1;

        loadAddress(pointer, pointer_reg);

        Type type = pointer.type;
        for (int i = 1; i < operands.size(); i++) {
            Value offset = operands.get(i);
            if (offset instanceof ConstInt) {
                int offset_val = ((ConstInt) offset).value;
                if (offset_val != 0) {
                    writeln(String.format("    addi $%s, $%s, %d",
                            pointer_reg, pointer_reg, offset_val * type.getElementType().getSize()));
                }
            } else {
                offset_reg = loadValue(offset, offset_reg);
                int k1 = type.getElementType().getSize();
                writeln(String.format("    mul $%s, $%s, %d", offset_reg, offset_reg, k1));
                writeln(String.format("    add $%s, $%s, $k1", pointer_reg, pointer_reg));
            }
            type = new PointerType(type.getElementType().getElementType());
        }

        if (!mipsInfo.value2reg.containsKey(this)) {
            mipsInfo.alloc(type);
            mipsInfo.value2offset.put(this, mipsInfo.cur_offset);
            writeln(String.format("    sw $%s, %d($sp)", pointer_reg, mipsInfo.value2offset.get(this)));
        }
    }
}
