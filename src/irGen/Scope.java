package irGen;

import err.ErrHandler;
import err.ErrInfo;
import err.ErrType;
import ir.Function;
import ir.Value;
import utils.IO;

import java.util.HashMap;

public class Scope {
    public static int scope_num = 0;
    public static Scope create_scope(Scope parent) {
        scope_num += 1;
        return new Scope(scope_num, parent);
    }

    /////////////////// non-static ///////////////////

    public int id;
    public HashMap<String, Value> values;
    public Scope parent;
    public Function cur_func;

    public Scope(int id, Scope parent) {
        this.id = id;
        values = new HashMap<>();
        this.parent = parent;
        cur_func = null;
    }

    public Scope enter() {
        Scope scope = create_scope(this);
        scope.cur_func = cur_func;
        return scope;
    }

    public Scope exit() { return parent; }

    public boolean isGlobal() {
        return id == 1;
    }

    public Value find_value(String name) {
        if (values.containsKey(name)) {
            return values.get(name);
        }
        return parent == null ? null : parent.find_value(name);
    }

    public void new_value(Value value, int line) {
        values.put(value.name, value);
    }
}
