package ir;

import java.util.ArrayList;

public class Value {
    public ArrayList<Use> useList;
    public ValueType type;
    public String name;

    public Value(ValueType type, String name) {
        useList = new ArrayList<>();
        this.type = type;
        this.name = name;
    }

    public void addUse(Use use) {
        useList.add(use);
    }
}
