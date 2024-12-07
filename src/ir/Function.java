package ir;

import ir.type.FunctionType;
import ir.type.Type;
import mipsGen.Regs;
import mipsGen.mipsInfo;

import java.util.ArrayList;

import static irGen.IrGen.new_func;
import static utils.IO.writeln;

public class Function extends Value {
    public ArrayList<BasicBlock> bbs;
    public ArrayList<FuncParam> params;
    public Type retType;
    private int var_cnt;

    public Function(Type retType, String name) {
        super(new FunctionType(), name);
        this.retType = retType;
        bbs = new ArrayList<>();
        params = new ArrayList<>();
        var_cnt = 0;
        new_func(this);
    }

    public String new_var() {
        return "%a" + var_cnt++;
    }

    public void print() {
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
            bb.print();
        }
        writeln("}");
    }

    public void to_mips() {
        writeln(String.format("%s:", name));
        mipsInfo.enter(this);
        for (int i = 0; i < params.size(); i++) {
            FuncParam param = params.get(i);
            if (i <= 3) {
                mipsInfo.value2reg.put(param, Regs.a0.get(i));
            }
            mipsInfo.alloc(param.type);
            mipsInfo.value2offset.put(param, mipsInfo.cur_offset);
        }
        for (BasicBlock bb : bbs) {
            bb.to_mips();
        }
    }
}
