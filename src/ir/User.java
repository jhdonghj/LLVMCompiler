package ir;

import java.util.ArrayList;

public class User extends Value {
    public ArrayList<Value> operands;

    public User(ValueType type, String name, Value...operands) {
        super(type, name);
        for (Value value : operands) {
            value.addUse(new Use(value, this));
            this.operands.add(value);
        }
    }
}
