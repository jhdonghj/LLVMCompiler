package opt;

import ir.*;
import ir.instr.*;

import java.util.*;

import static utils.IO.setOut;

public class Mem2Reg {
    private static HashMap<BasicBlock, HashSet<BasicBlock>> CFG;
    private static HashMap<BasicBlock, HashSet<BasicBlock>> rCFG;
    private static HashMap<BasicBlock, HashSet<BasicBlock>> domT;
    private static HashMap<BasicBlock, HashSet<BasicBlock>> DF;
    private static HashSet<BasicBlock> vis;
    private static HashSet<Allocate> allocates;

    public static void run(Program program) {
//        setOut("files/opt0.txt");
//        program.print();
        buildCFG(program);
//        setOut("files/opt1.txt");
//        program.print();
        buildDF(program);
        insertPhi(program);
//        setOut("files/opt2.txt");
//        program.print();
    }

    private static void CFGadd(BasicBlock a, BasicBlock b) {
        CFG.get(a).add(b);
        rCFG.get(b).add(a);
    }

    private static void buildCFG(Program program) {
        for (Function function : program.functions) {
            for (BasicBlock bb : function.bbs) {
                ArrayList<Instr> newInstrs = new ArrayList<>();
                for (Instr instr : bb.instrs) {
                    newInstrs.add(instr);
                    if (instr.isJump()) break;
                }
                bb.instrs = newInstrs;
            }
            ArrayDeque<BasicBlock> queue = new ArrayDeque<>();
            ArrayList<BasicBlock> vis = new ArrayList<>();
            queue.add(function.getEntry());
            while (!queue.isEmpty()) {
                BasicBlock x = queue.poll();
                if (vis.contains(x)) continue;
                vis.add(x);
                Instr last = x.getLastInstr();
                if (last instanceof Branch branch) {
                    queue.add(branch.getThenBB());
                    queue.add(branch.getElseBB());
                } else if (last instanceof Jump jump) {
                    queue.add(jump.getDestBB());
                }
            }
            function.bbs = vis;
        }
        CFG = new HashMap<>();
        rCFG = new HashMap<>();
        for (Function function : program.functions) {
            for (BasicBlock bb : function.bbs) {
                CFG.put(bb, new HashSet<>());
                rCFG.put(bb, new HashSet<>());
            }
        }
        for (Function function : program.functions) {
            ArrayDeque<BasicBlock> queue = new ArrayDeque<>();
            ArrayList<BasicBlock> vis = new ArrayList<>();
            queue.add(function.getEntry());
            while (!queue.isEmpty()) {
                BasicBlock x = queue.poll();
                if (vis.contains(x)) continue;
                vis.add(x);
                Instr last = x.getLastInstr();
                if (last instanceof Branch branch) {
                    CFGadd(x, branch.getThenBB());
                    CFGadd(x, branch.getElseBB());
                    queue.add(branch.getThenBB());
                    queue.add(branch.getElseBB());
                } else if (last instanceof Jump jump) {
                    CFGadd(x, jump.getDestBB());
                    queue.add(jump.getDestBB());
                }
            }
        }
        for (BasicBlock bb : rCFG.keySet()) {
            bb.preds = rCFG.get(bb);
            bb.succs = CFG.get(bb);
        }
//        for (BasicBlock x : CFG.keySet()) {
//            System.out.print("CFG: " + x.name + " -> ");
//            for (BasicBlock y : CFG.get(x)) {
//                System.out.print(y.name + " ");
//            }
//            System.out.println();
//        }
    }

    private static void buildDF(Program program) {
        HashMap<BasicBlock, HashSet<BasicBlock>> domG = new HashMap<>();
        HashMap<BasicBlock, BasicBlock> domTfa = new HashMap<>();
        domT = new HashMap<>();
        DF = new HashMap<>();
        for (Function function : program.functions) {
            for (BasicBlock bb : function.bbs) {
                domG.put(bb, new HashSet<>(function.bbs));
                domT.put(bb, new HashSet<>());
                domTfa.put(bb, null);
                DF.put(bb, new HashSet<>());
                getDomingNodes(function.getEntry(), bb, domG.get(bb));
            }
        }
//        for (BasicBlock bb : domG.keySet()) {
//            System.out.print("Dom: " + bb.name + " -> ");
//            for (BasicBlock dom : domG.get(bb)) {
//                System.out.print(dom.name + " ");
//            }
//            System.out.println();
//        }
        for (Function function : program.functions) {
            for (BasicBlock domer : function.bbs) {
                for (BasicBlock domed : domG.get(domer)) if(!domed.equals(domer)) {
                    boolean flg = true;
                    for (BasicBlock mid : domG.get(domer)) {
                        if (!mid.equals(domer) && !mid.equals(domed) && domG.get(mid).contains(domed)) {
                            flg = false;
                            break;
                        }
                    }
                    if (flg) {
                        domT.get(domer).add(domed);
                        domTfa.put(domed, domer);
                    }
                }
            }
        }
//        for (BasicBlock bb : domT.keySet()) {
//            System.out.print("DomT: " + bb.name + " -> ");
//            for (BasicBlock dom : domT.get(bb)) {
//                System.out.print(dom.name + " ");
//            }
//            System.out.println();
//        }
        for (BasicBlock a : CFG.keySet()) {
            DF.put(a, new HashSet<>());
        }
        for (BasicBlock a : CFG.keySet()) {
            for (BasicBlock b : CFG.get(a)) {
                BasicBlock x = a;
                while (!domT.get(x).contains(b) || x.equals(b)) {
                    DF.get(x).add(b);
                    x = domTfa.get(x);
                }
            }
        }
//        for (BasicBlock bb : DF.keySet()) {
//            System.out.print("DF: " + bb.name + " -> ");
//            for (BasicBlock dom : DF.get(bb)) {
//                System.out.print(dom.name + " ");
//            }
//            System.out.println();
//        }
    }

    private static void getDomingNodes(BasicBlock now, BasicBlock ban, HashSet<BasicBlock> doming) {
        if (now == ban) return;
        doming.remove(now);
        for (BasicBlock next : CFG.get(now)) {
            if (doming.contains(next)) {
                getDomingNodes(next, ban, doming);
            }
        }
    }

    private static void insertPhi(Program program) {
        for (Function function : program.functions) {
            allocates = new HashSet<>();
            HashMap<Instr, HashSet<BasicBlock>> defb = new HashMap<>();
            for (BasicBlock bb : function.bbs) {
                for (Instr instr : bb.instrs) {
                    if (instr instanceof Allocate allocate && !allocate.type.getElementType().isArray()) {
                        defb.put(allocate, new HashSet<>());
                        allocates.add(allocate);
                    }
                }
            }
            for (BasicBlock bb : function.bbs) {
                for (Instr instr : bb.instrs) {
                    if (instr instanceof Store store &&
                            store.getPtr() instanceof Allocate allocate &&
                            allocates.contains(allocate)) {
                        defb.get(allocate).add(bb);
                    }
                }
            }
            HashSet<Allocate> newAllocates = new HashSet<>(allocates);
            for (Allocate allocate : allocates) {
                if (defb.get(allocate).isEmpty()) {
                    newAllocates.remove(allocate);
                    defb.remove(allocate);
                }
            }
            allocates = newAllocates;

            HashMap<Phi, Allocate> phi2allocate = new HashMap<>();
            for (Allocate allocate : allocates) {
                HashSet<BasicBlock> F = new HashSet<>();
                HashSet<BasicBlock> W = new HashSet<>(defb.get(allocate));
                while (!W.isEmpty()) {
                    BasicBlock x = W.iterator().next();
                    W.remove(x);
                    for (BasicBlock y : DF.get(x)) {
                        if (!F.contains(y)) {
                            Phi phi = new Phi(allocate.allocType, function.new_var(), rCFG.getOrDefault(y, new HashSet<>()));
                            phi.parentBB = y;
                            y.instrs.add(0, phi);
                            phi2allocate.put(phi, allocate);
                            F.add(y);
                            if (!defb.get(allocate).contains(y)) {
                                W.add(y);
                            }
                        }
                    }
                }
//                stack = new Stack<>();
//                dfs(function.getEntry(), allocate, defi.get(allocate), usei.get(allocate));
            }
            HashMap<Allocate, Value> allocate2value = new HashMap<>();
            for (Allocate allocate : allocates) {
                allocate2value.put(allocate, new ConstInt(0));
            }
            vis = new HashSet<>();
            dfs(function.getEntry(), phi2allocate, allocate2value);
        }
    }

    private static void dfs(BasicBlock curBB, HashMap<Phi, Allocate> p2a, HashMap<Allocate, Value> a2v) {
        vis.add(curBB);
        HashMap<Allocate, Value> cur_a2v = new HashMap<>(a2v);
        ArrayList<Instr> newInstrs = new ArrayList<>();
        for (Instr instr : curBB.instrs) {
            if (instr instanceof Load load) {
                if (load.getPtr() instanceof Allocate allocate && cur_a2v.containsKey(allocate)) {
                    updateUser(load, cur_a2v.get(allocate));
                } else {
                    newInstrs.add(instr);
                }
            } else if (instr instanceof Store store) {
                if (store.getPtr() instanceof Allocate allocate && cur_a2v.containsKey(allocate)) {
                    cur_a2v.put(allocate, store.getVal());
                } else {
                    newInstrs.add(instr);
                }
            } else if (instr instanceof Phi phi) {
                if (p2a.containsKey(phi)) {
                    cur_a2v.put(p2a.get(phi), phi);
                }
                newInstrs.add(instr);
            } else if (instr instanceof Allocate) {
                if (!allocates.contains(instr)) {
                    newInstrs.add(instr);
                }
            } else {
                newInstrs.add(instr);
            }
        }
        curBB.instrs = newInstrs;
        for (BasicBlock nextBB : CFG.get(curBB)) {
            for (Instr instr : nextBB.instrs) {
                if (instr instanceof Phi phi && p2a.containsKey(phi)) {
                    phi.addOperand(curBB, cur_a2v.get(p2a.get(phi)));
                }
            }
        }
        for (BasicBlock nextBB : domT.get(curBB)) {
            assert !vis.contains(nextBB);
            if (vis.contains(nextBB)) continue;
            dfs(nextBB, p2a, cur_a2v);
        }
    }

    private static void updateUser(Instr instr, Value value) {
        for (Use use : instr.useList) {
            User user = use.user;
            user.operands.set(user.operands.indexOf(instr), value);
            value.addUse(user);
        }
        instr.useList.clear();
    }
}
