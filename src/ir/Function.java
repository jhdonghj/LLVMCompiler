package ir;

import java.util.ArrayList;

public class Function extends Value {
    public ArrayList<BasicBlock> bbs;
    public ArrayList<FuncParam> params;

    public Function(ValueType type, String name) {
        super(type, name);
        bbs = new ArrayList<>();
        params = new ArrayList<>();
    }
}
