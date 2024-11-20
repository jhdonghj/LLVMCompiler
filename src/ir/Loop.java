package ir;

public class Loop {
    public BasicBlock condBB;
    public BasicBlock bodyBB;
    public BasicBlock stepBB;
    public BasicBlock exitBB;

    public Loop(BasicBlock condBB, BasicBlock bodyBB, BasicBlock stepBB, BasicBlock exitBB) {
        this.condBB = condBB;
        this.bodyBB = bodyBB;
        this.stepBB = stepBB;
        this.exitBB = exitBB;
    }
}
