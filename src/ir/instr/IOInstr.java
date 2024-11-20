package ir.instr;

import ir.ConstString;
import ir.Value;
import ir.type.PointerType;
import ir.type.Type;

import static ir.type.IntegerType.*;
import static utils.IO.writeln;

public class IOInstr extends Instr {
    public IOInstr(Type type, String name, Value... operands) {
        super(type, name, operands);
    }

    public static class GetInt extends IOInstr {
        public GetInt(String name) {
            super(INT_TYPE, name);
        }

        @Override
        public void print() {
            writeln(String.format("  %s = call i32 @getint()", name));
        }
    }

    public static class GetChar extends IOInstr {
        public GetChar(String name) {
            super(INT_TYPE, name);
        }

        @Override
        public void print() {
            writeln(String.format("  %s = call i32 @getchar()", name));
        }
    }

    public static class PutInt extends IOInstr {
        public PutInt(String name, Value val) {
            super(VOID_TYPE, name, val);
        }

        @Override
        public void print() {
            writeln(String.format("  call void @putint(i32 %s)", operands.get(0).name));
        }
    }

    public static class PutChar extends IOInstr {
        public PutChar(String name, Value val) {
            super(VOID_TYPE, name, val);
        }

        @Override
        public void print() {
            writeln(String.format("  call void @putch(i8 %s)", operands.get(0).name));
        }
    }

    public static class PutString extends IOInstr {
        public ConstString str;

        public PutString(String name, ConstString str) {
            super(VOID_TYPE, name);
            this.str = str;
        }

        @Override
        public void print() {
            // getelementptr in call
            writeln(String.format("  call void @putstr(i8* getelementptr inbounds (%s, %s %s, i32 0, i32 0))",
                    ((PointerType) str.type).elementType, str.type, str.name));
        }
    }
}
