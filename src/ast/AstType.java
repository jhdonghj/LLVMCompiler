package ast;

public enum AstType {
    CompUnit, Decl, ConstDecl, BType, ConstDef, ConstInitVal,
    VarDecl, VarDef, InitVal, FuncDef, MainFuncDef, FuncType,
    FuncFParams, FuncFParam, Block, BlockItem, Stmt, ForStmt,
    Exp, Cond, LVal, PrimaryExp, Number, Character,
    UnaryExp, UnaryOp, FuncRParams, MulExp, AddExp, RelExp, 
    EqExp, LAndExp, LOrExp, ConstExp, Token
}
