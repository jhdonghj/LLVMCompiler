package ir;

import ir.type.Type;

import java.util.ArrayList;

public class User extends Value {
    public ArrayList<Value> operands;

    public User(Type type, String name, Value...operands) {
        super(type, name);
        this.operands = new ArrayList<>();
        for (Value value : operands) {
            value.addUse(this);
            this.operands.add(value);
        }
    }

    public void addOperands(Value...operands) {
        for (Value value : operands) {
            value.addUse(this);
            this.operands.add(value);
        }
    }
}
