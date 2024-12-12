package opt;

import ir.BasicBlock;
import ir.Function;
import ir.Program;
import ir.Value;
import ir.instr.Instr;
import ir.instr.Move;
import ir.instr.Phi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class RemovePhi {
    public static void run(Program program) {
        for (Function function : program.functions) {
            HashSet<Phi> phis = new HashSet<>();
            HashMap<BasicBlock, HashSet<Move>> movesToAdd = new HashMap<>();
            for (BasicBlock bb : function.bbs) {
                for (Instr instr : bb.instrs) {
                    if (instr instanceof Phi phi) {
                        phis.add(phi);
                    }
                }
            }
            function.bbs.forEach(bb -> movesToAdd.put(bb, new HashSet<>()));
            for (Phi phi : phis) {
                for (BasicBlock bb : phi.preBBs.keySet()) {
                    movesToAdd.get(bb).add(new Move(phi, phi.preBBs.get(bb)));
                }
            }
            for (BasicBlock bb : function.bbs) {
                bb.instrs = new ArrayList<>(bb.instrs.stream().filter(instr -> !(instr instanceof Phi)).toList());
            }
            for (BasicBlock bb : movesToAdd.keySet()) {
                HashSet<Move> moves = movesToAdd.get(bb);
                HashMap<Value, Integer> out = new HashMap<>();
                moves.forEach(move -> {
                    if (out.containsKey(move.getSource())) {
                        out.put(move.getSource(), out.get(move.getSource()) + 1);
                    } else {
                        out.put(move.getSource(), 1);
                    }
                    if (!out.containsKey(move.getTarget())) {
                        out.put(move.getTarget(), 0);
                    }
                });
                while (!moves.isEmpty()) {
                    HashSet<Move> toRemove = new HashSet<>();
                    for (Move move : moves) {
                        if (out.get(move.getTarget()) == 0) {
                            bb.instrs.add(bb.instrs.size() - 1, move);
                            toRemove.add(move);
                            out.put(move.getSource(), out.get(move.getSource()) - 1);
                        }
                        if (move.getTarget().equals(move)) {
                            toRemove.add(move);
                            out.put(move.getSource(), out.get(move.getSource()) - 1);
                        }
                    }
                    moves.removeAll(toRemove);
                    if (toRemove.isEmpty()) {
                        Move curMove = moves.iterator().next();
                        Phi tempReg = new Phi(curMove.getTarget().type, function.new_var(), new HashSet<>());
                        Move newMove = new Move(tempReg, curMove.getSource());
                        bb.instrs.add(bb.instrs.size() - 1, newMove);
                        out.put(curMove.getTarget(), 0);
                        for (Move move : moves) {
                            if (move.getSource().equals(curMove.getTarget())) {
                                move.replaceSource(tempReg);
                            }
                        }
                    }
                }
            }
        }
    }
}
