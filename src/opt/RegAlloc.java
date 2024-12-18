package opt;

import ir.*;
import ir.instr.*;
import mipsGen.MipsInfo;
import mipsGen.Regs;
import utils.Pair;

import java.util.*;

import static ir.type.IntegerType.VOID_TYPE;

public class RegAlloc {
    public static HashMap<Instr, HashSet<String>> Idefs = new HashMap<>();
    public static HashMap<Instr, HashSet<String>> Iuses = new HashMap<>();
    public static HashMap<String, HashSet<String>> G = new HashMap<>();
    public static HashSet<Pair<String, String>> moves = new HashSet<>();
    public static ArrayList<String> stack = new ArrayList<>();
    public static ArrayList<HashSet<String>> stackG = new ArrayList<>();
    public static HashMap<String, HashSet<String>> same = new HashMap<>();
    public static HashSet<String> spilled = new HashSet<>();

    public static void run(Program program) {
        for (Function function : program.functions) {
            Idefs.clear(); Iuses.clear(); G.clear(); moves.clear();
            stack.clear(); stackG.clear(); same.clear(); spilled.clear();
            for (int i = 0; i < function.params.size(); i++) {
                FuncParam param = function.params.get(i);
                if (function.hasPrint) {
                    if (i <= 2) {
                        function.value2reg.put(param.name, Regs.a1.get(i));
                    }
                } else if (i <= 3) {
                    function.value2reg.put(param.name, Regs.a0.get(i));
                }
            }
            ArrayList<Regs> regs = getRegs();
            activeAnalyse(function);
            buildGraph(function);
            while (!G.isEmpty()) {
                while (true) {
                    boolean changed = false;
                    // simplify
                    while (true) {
                        boolean flg = false;
                        ArrayList<String> iter = new ArrayList<>(G.keySet());
                        for (String value : iter) {
                            if (G.get(value).size() < regs.size()) {
                                changed = true;
                                flg = true;
                                stack.add(value);
                                stackG.add(new HashSet<>(G.get(value)));
                                remove(value);
                            }
                        }
                        if (!flg) break;
                    }
                    // merge
                    while (true) {
                        boolean flg = false;
                        ArrayList<Pair<String, String>> iter = new ArrayList<>(RegAlloc.moves);
                        for (Pair<String, String> move : iter) {
                            String x = move.first;
                            String y = move.second;
                            HashSet<String> union = new HashSet<>(G.get(x));
                            union.addAll(G.get(y));
                            if (union.size() < regs.size()) {
                                changed = true;
                                flg = true;
                                same.get(x).addAll(same.get(y));
                                for (String z : G.get(y)) {
                                    G.get(z).remove(y);
                                    G.get(z).add(x);
                                    G.get(x).add(z);
                                }
                                remove(y);
                            }
                        }
                        if (!flg) break;
                    }
                    if (!changed) break;
                }
                // spill
                if (!G.isEmpty()) {
                    String spill = null;
                    for (String value : G.keySet()) {
                        if (spill == null || G.get(value).size() > G.get(spill).size()) {
                            spill = value;
                        }
                    }
                    spilled.add(spill);
                    stack.add(spill);
                    stackG.add(new HashSet<>(G.get(spill)));
                    remove(spill);
                }
            }
            // alloc
            for (int i = stack.size() - 1; i >= 0; i--) {
                String value = stack.get(i);
                HashSet<String> adj = stackG.get(i);
                if (spilled.contains(value) || function.value2reg.containsKey(value)) continue;
                ArrayList<Regs> validRegs = new ArrayList<>(regs);
                for (String v : adj) {
                    if (function.value2reg.containsKey(v)) {
                        validRegs.remove(function.value2reg.get(v));
                    }
                }
                function.value2reg.put(value, validRegs.get(0));
            }
            for (int i = stack.size() - 1; i >= 0; i--) {
                String value = stack.get(i);
                HashSet<String> adj = stackG.get(i);
                if (!spilled.contains(value) || function.value2reg.containsKey(value)) continue;
                ArrayList<Regs> validRegs = new ArrayList<>(regs);
                for (String v : adj) {
                    if (function.value2reg.containsKey(v)) {
                        validRegs.remove(function.value2reg.get(v));
                    }
                }
                if (!validRegs.isEmpty()) {
                    function.value2reg.put(value, validRegs.get(0));
                }
            }
            for (String value : same.keySet()) {
                if (function.value2reg.containsKey(value)) {
                    for (String v : same.get(value)) {
                        function.value2reg.put(v, function.value2reg.get(value));
                    }
                }
            }
        }
    }

    private static void remove(String x) {
        for (String y : G.get(x)) {
            G.get(y).remove(x);
        }
        G.remove(x);
        HashSet<Pair<String, String>> del = new HashSet<>();
        for (Pair<String, String> move : moves) {
            if (move.first.equals(x) || move.second.equals(x)) {
                del.add(move);
            }
        }
        moves.removeAll(del);
    }

    private static void buildGraph(Function function) {
        for (BasicBlock bb : function.bbs) {
            for (Instr instr : bb.instrs) {
                for (String value : Iuses.get(instr)) {
                    G.put(value, new HashSet<>());
                    same.put(value, new HashSet<>());
                    same.get(value).add(value);
                }
                for (String value : Idefs.get(instr)) {
                    G.put(value, new HashSet<>());
                    same.put(value, new HashSet<>());
                    same.get(value).add(value);
                }
                if (instr instanceof Move move) {
                    Value target = move.getTarget();
                    Value source = move.getSource();
                    if (canAssignReg(target) && canAssignReg(source)) {
                        moves.add(new Pair<>(target.name, source.name));
                    }
                }
            }
        }
        for (BasicBlock bb : function.bbs) {
            for (Instr instr : bb.instrs) {
                for (String x : instr.act_out) {
                    for (String y : instr.act_out) {
                        if (x.equals(y)) continue;
                        G.get(x).add(y);
                        G.get(y).add(x);
                    }
                }
                for (String x : instr.act_in) {
                    for (String y : instr.act_in) {
                        if (x.equals(y)) continue;
                        G.get(x).add(y);
                        G.get(y).add(x);
                    }
                }
                for (String x : Idefs.get(instr)) {
                    for (String y : instr.act_out) {
                        if (x.equals(y)) continue;
                        G.get(x).add(y);
                        G.get(y).add(x);
                    }
                }
            }
        }
    }

    public static void activeAnalyse(Function function) {
        MipsInfo.act_flag = true;
        for (BasicBlock bb : function.bbs) {
            for (Instr instr : bb.instrs) {
                Iuses.put(instr, getUse(instr));
                Idefs.put(instr, getDef(instr));
            }
        }
        boolean changed = true;
        while (changed) {
            changed = false;
            for (BasicBlock bb : function.bbs) {
                for (int i = bb.instrs.size() - 1; i >= 0; i--) {
                    Instr instr = bb.instrs.get(i);
                    HashSet<String> new_act_out = new HashSet<>();
                    if (instr instanceof Branch branch) {
                        new_act_out.addAll(branch.getElseBB().instrs.get(0).act_in);
                        new_act_out.addAll(branch.getThenBB().instrs.get(0).act_in);
                    } else if (instr instanceof Jump jump) {
                        new_act_out.addAll(jump.getDestBB().instrs.get(0).act_in);
                    } else if (instr instanceof Return) {

                    } else {
                        new_act_out.addAll(bb.instrs.get(i + 1).act_in);
                    }
                    HashSet<String> new_act_in = new HashSet<>(new_act_out);
                    new_act_in.removeAll(Idefs.get(instr));
                    new_act_in.addAll(Iuses.get(instr));
                    if (!new_act_in.equals(instr.act_in) ||
                            !new_act_out.equals(instr.act_out)) {
                        changed = true;
                    }
                    instr.act_in = new_act_in;
                    instr.act_out = new_act_out;
                }
            }
        }
    }

    private static HashSet<String> getUse(Instr instr) {
        HashSet<String> res = new HashSet<>();
        for (Value value : instr.operands) {
            if (value instanceof Instr || value instanceof FuncParam) {
                res.add(value.name);
            }
        }
        return res;
    }

    private static HashSet<String> getDef(Instr instr) {
        HashSet<String> res = new HashSet<>();
        if (canAssignReg(instr)) res.add(instr.name);
        return res;
    }

    private static boolean canAssignReg(Value value) {
        if (value instanceof Instr instr) {
            return !(instr instanceof Return || instr instanceof Branch ||
                    instr instanceof Jump || instr instanceof Store ||
                    (instr instanceof Call && instr.type.equals(VOID_TYPE)) ||
                    instr instanceof IOInstr.PutString ||
                    instr instanceof IOInstr.PutChar ||
                    instr instanceof IOInstr.PutInt);
        } else {
            return value instanceof FuncParam;
        }
    }

    private static ArrayList<Regs> getRegs() {
        return new ArrayList<>(){{
            add(Regs.gp); add(Regs.fp);
            add(Regs.t0); add(Regs.t1); add(Regs.t2); add(Regs.t3); add(Regs.t4); add(Regs.t5); add(Regs.t6); add(Regs.t7); add(Regs.t8); add(Regs.t9);
            add(Regs.s0); add(Regs.s1); add(Regs.s2); add(Regs.s3); add(Regs.s4); add(Regs.s5); add(Regs.s6); add(Regs.s7);
        }};
    }
}
