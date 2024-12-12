package ir;

import ir.type.FunctionType;
import ir.type.Type;
import mipsGen.Regs;
import mipsGen.MipsInfo;

import java.util.ArrayList;

import static irGen.IrGen.new_func;
import static utils.IO.writeln;

public class Function extends Value {
    public ArrayList<BasicBlock> bbs;
    public ArrayList<FuncParam> params;
    public Type retType;
    public boolean hasPrint = false;
    private static int var_cnt = 0;
    public static BasicBlock nxtBB;

    public Function(Type retType, String name) {
        super(new FunctionType(), name);
        this.retType = retType;
        bbs = new ArrayList<>();
        params = new ArrayList<>();
        new_func(this);
    }

    public BasicBlock getEntry() {
        return bbs.get(0);
    }

    public String new_var() {
        return "%a" + var_cnt++;
    }

    public void to_llvm() {
        StringBuilder sb = new StringBuilder();
        sb.append("define dso_local ").append(retType).append(" @").append(name).append("(");
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(params.get(i).type).append(" ").append(params.get(i).name);
        }
        sb.append(") {");
        writeln(sb.toString());
        for (BasicBlock bb : bbs) {
            bb.to_llvm();
        }
        writeln("}");
    }

    public void to_mips() {
        writeln(String.format("%s:", name));
        MipsInfo.enter(this);
        for (int i = 0; i < params.size(); i++) {
            FuncParam param = params.get(i);
            if (hasPrint) {
                if (i <= 2) {
                    MipsInfo.value2reg.put(param.name, Regs.a1.get(i));
                }
            } else {
                if (i <= 3) {
                    MipsInfo.value2reg.put(param.name, Regs.a0.get(i));
                }
            }
            MipsInfo.alloc(param.type);
            MipsInfo.value2offset.put(param.name, MipsInfo.cur_offset);
        }
        for (int i = 0; i < bbs.size(); i++) {
            if (i + 1 < bbs.size()) {
                nxtBB = bbs.get(i + 1);
            } else {
                nxtBB = null;
            }
            bbs.get(i).to_mips();
        }
    }
}
