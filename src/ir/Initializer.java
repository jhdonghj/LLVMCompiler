package ir;

import java.util.ArrayList;

public class Initializer extends Value {
    public int size;
    public int constInt;
    public String constString;
    public ArrayList<Integer> constArrayInit;
    public Value initValue;
    public ArrayList<Value> arrayInit;

    public Initializer() {
        super(null, null);
    }
}
