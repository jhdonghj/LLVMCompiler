package ir;

import ir.type.Type;

import static irGen.IrGen.new_param;

public class FuncParam extends Value {
    public String origin_name;

    public FuncParam(Type type, String name, String origin_name) {
        super(type, name);
        this.origin_name = origin_name;
        new_param(this);
    }
}
