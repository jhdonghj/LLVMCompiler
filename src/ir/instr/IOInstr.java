package ir.instr;

import ir.ConstString;
import ir.Value;
import ir.type.PointerType;
import ir.type.Type;
import mipsGen.Regs;
import mipsGen.mipsInfo;

import static ir.type.IntegerType.*;
import static utils.IO.writeln;
import static mipsGen.mipsInfo.loadValue;

public class IOInstr extends Instr {
    public IOInstr(Type type, String name, Value... operands) {
        super(type, name, operands);
    }

    public static class GetInt extends IOInstr {
        public GetInt(String name) {
            super(INT_TYPE, name);
        }

        @Override
        public String toString() {
            return String.format("  %s = call i32 @getint()", name);
        }

        @Override
        public void to_mips() {
            super.to_mips();
            writeln("    li $v0, 5");
            writeln("    syscall");
            if (mipsInfo.value2reg.containsKey(this)) {
                Regs reg = mipsInfo.value2reg.get(this);
                writeln(String.format("    move $%s, $%s", reg, Regs.v0));
            } else {
                mipsInfo.alloc(new PointerType(INT_TYPE));
                mipsInfo.value2offset.put(this, mipsInfo.cur_offset);
                writeln(String.format("    sw $%s, %d($sp)", Regs.v0, mipsInfo.value2offset.get(this)));
            }
        }
    }

    public static class GetChar extends IOInstr {
        public GetChar(String name) {
            super(INT_TYPE, name);
        }

        @Override
        public String toString() {
            return String.format("  %s = call i32 @getchar()", name);
        }

        @Override
        public void to_mips() {
            super.to_mips();
            writeln("    li $v0, 12");
            writeln("    syscall");
            if (mipsInfo.value2reg.containsKey(this)) {
                Regs reg = mipsInfo.value2reg.get(this);
                writeln(String.format("    move $%s, $%s", reg, Regs.v0));
            } else {
                mipsInfo.alloc(new PointerType(INT_TYPE));
                mipsInfo.value2offset.put(this, mipsInfo.cur_offset);
                writeln(String.format("    sw $%s, %d($sp)", Regs.v0, mipsInfo.value2offset.get(this)));
            }
        }
    }

    public static class PutInt extends IOInstr {
        public PutInt(String name, Value val) {
            super(VOID_TYPE, name, val);
        }

        @Override
        public String toString() {
            return String.format("  call void @putint(i32 %s)", operands.get(0).name);
        }

        @Override
        public void to_mips() {
            super.to_mips();
            Value val = operands.get(0);
            Regs reg = Regs.a0;
            reg = loadValue(val, reg);
            writeln("    li $v0, 1");
            writeln("    syscall");
        }
    }

    public static class PutChar extends IOInstr {
        public PutChar(String name, Value val) {
            super(VOID_TYPE, name, val);
        }

        @Override
        public String toString() {
            return String.format("  call void @putch(i8 %s)", operands.get(0).name);
        }

        @Override
        public void to_mips() {
            super.to_mips();
            Value val = operands.get(0);
            Regs reg = Regs.a0;
            reg = loadValue(val, reg);
            writeln("    li $v0, 11");
            writeln("    syscall");
        }
    }

    public static class PutString extends IOInstr {
        public ConstString str;

        public PutString(String name, ConstString str) {
            super(VOID_TYPE, name);
            this.str = str;
        }

        @Override
        public String toString() {
            // getelementptr in call
            return String.format("  call void @putstr(i8* getelementptr inbounds (%s, %s %s, i32 0, i32 0))",
                    ((PointerType) str.type).elementType, str.type, str.name);
        }

        @Override
        public void to_mips() {
            super.to_mips();
            writeln(String.format("    la $a0, %s", str.name.substring(1)));
            writeln("    li $v0, 4");
            writeln("    syscall");
        }
    }
}
