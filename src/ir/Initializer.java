package ir;

import ir.type.ArrayType;
import ir.type.Type;

import java.util.ArrayList;

public class Initializer {
    public Type baseType;
    public boolean is_array;
    public ArrayList<Integer> values;

    public Initializer(ArrayList<Integer> values) {
        this.values = values;
    }

    public void resize(int size) {
        while (values.size() < size) {
            values.add(0);
        }
        while (values.size() > size) {
            values.remove(values.size() - 1);
        }
    }

    public int get(int i) {
        return values.get(i);
    }

    public Type getType() {
        if (!is_array) {
            return baseType;
        } else {
            return new ArrayType(values.size(), baseType);
        }
    }

    @Override
    public String toString() {
        // var: i32 0
        // array: [2 x i32] [i32 0, i32 1]
        if (!is_array) {
            return baseType + " " + values.get(0);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(values.size()).append(" x ").append(baseType).append("] ");
            if (values.stream().allMatch(i -> i == 0)) {
                sb.append("zeroinitializer");
            } else {
                sb.append("[");
                for (int i = 0; i < values.size(); i++) {
                    if (i != 0) {
                        sb.append(", ");
                    }
                    sb.append(baseType).append(" ").append(values.get(i));
                }
                sb.append("]");
            }
            return sb.toString();
        }
    }
}
