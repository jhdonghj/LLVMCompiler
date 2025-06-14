package ir;

import ir.type.Type;

import java.util.ArrayList;

public class Value {
    public ArrayList<Use> useList;
    public Type type;
    public String name;
    public boolean isConstant;

    public Value(Type type, String name) {
        useList = new ArrayList<>();
        this.type = type;
        this.name = name;
        isConstant = false;
    }

    public void addUse(User user) {
        Use use = new Use(this, user);
        useList.add(use);
    }
}
