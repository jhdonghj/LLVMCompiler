package irGen;

import ast.AstNode;
import ast.AstType;
import ir.*;
import ir.instr.*;
import ir.type.IntegerType;
import ir.type.PointerType;
import ir.type.Type;
import token.Token;
import token.TokenType;

import java.util.ArrayList;
import java.util.HashMap;

import static ir.type.IntegerType.*;
import static utils.Utils.getType;
import static utils.Utils.singleType;

public class IrGen {
    // Helper functions

    private static Program program;
    private static ArrayList<HashMap<String, Value>> scopes;
    public static ArrayList<Loop> loops;

    private static void enter() {
        scopes.add(new HashMap<>());
    }
    private static void exit() {
        scopes.remove(scopes.size() - 1);
        if (is_global()) cur_func = null;
    }
    public static boolean is_global() {
        return scopes.size() == 1;
    }
    private static void new_value(String name, Value value) {
        scopes.get(scopes.size() - 1).put(name, value);
    }
    private static Value get_value(String name) {
        for (int i = scopes.size() - 1; i > 0; i--) {
            if (scopes.get(i).containsKey(name)) {
                return scopes.get(i).get(name);
            }
        }
        return scopes.get(0).get(name);
    }

    public static Function cur_func;
    public static BasicBlock cur_bb;
    public static void new_bb(BasicBlock bb) {
        cur_func.bbs.add(bb);
        bb.parentFunc = cur_func;
    }
    public static void new_instr(Instr instr) {
        cur_bb.instrs.add(instr);
        instr.parentBB = cur_bb;
    }
    public static void new_globalVariable(GlobalVariable globalVariable) {
        program.addGlobalVariable(globalVariable);
    }
    public static void new_func(Function func) {
        cur_func = func;
        program.addFunction(func);
    }
    public static void new_param(FuncParam param) {
        cur_func.params.add(param);
    }
    public static void new_constString(ConstString constString) {
        program.addConstString(constString);
    }

    public static int bb_cnt = 0, str_cnt = 0, global_cnt = 0;
    public static String new_bb_name() {
        return "bb_1T_" + bb_cnt++;
    }
    public static String new_str_name() {
        return "@s_Z9_" + str_cnt++;
    }
    public static String new_global_name() {
        return "@g_cJ_" + global_cnt++;
    }

    public static ArrayList<AstNode> flatten(AstNode ast, AstType type) {
        if (ast.type != type) {
            ArrayList<AstNode> res = new ArrayList<>();
            res.add(ast);
            return res;
        } else {
            ArrayList<AstNode> lhs = flatten(ast.get(0), type);
            if (ast.size() > 1) {
                lhs.add(flatten(ast.get(1), type).get(0));
                lhs.add(flatten(ast.get(2), type).get(0));
            }
            return lhs;
        }
    }

    public static Value castTo(Value value, Type type) {
        if (value.type.equals(type)) return value;
        if (((IntegerType) value.type).bitWidth < ((IntegerType) type).bitWidth) {
            return new Zext(cur_func.new_var(), value, type);
        } else {
            return new Trunc(cur_func.new_var(), value, type);
        }
    }

    // Main functions

    public static Program generate(AstNode ast) {
        program = new Program();
        scopes = new ArrayList<>();
        loops = new ArrayList<>();
        enter();
        CompUnit(ast);
        return program;
    }

    public static void CompUnit(AstNode ast) {
        for (AstNode son : ast.sons) {
            if (son.type == AstType.Decl) {
                Decl(son);
            } else if (son.type == AstType.FuncDef) {
                FuncDef(son);
            } else {
                MainFuncDef(son);
            }
        }
    }

    public static void Decl(AstNode ast) {
        AstNode son = ast.get(0);
        if (son.type == AstType.ConstDecl) {
            ConstDecl(son);
        } else {
            VarDecl(son);
        }
    }

    public static void ConstDecl(AstNode ast) {
        TokenType bType = BType(ast.get(1));
        for (int i = 2; i < ast.size(); i += 2) {
            ConstDef(ast.get(i), bType);
        }
    }

    public static TokenType BType(AstNode ast) {
        return ast.get(0).token.type;
    }

    public static void ConstDef(AstNode ast, TokenType bType) {
        Token ident = ast.get(0).token;
        Initializer initializer = ConstInitVal(ast.get(-1));
        boolean is_array = ast.get(1).token.type == singleType('[');
        int size = is_array ? ConstExp(ast.get(2)) : 1;
        initializer.resize(size);
        Type baseType = getType(bType, false, 0);
        initializer.baseType = baseType;
        initializer.is_array = is_array;
        if (is_global()) {
            GlobalVariable globalVariable = new GlobalVariable(
                new PointerType(initializer.getType()), new_global_name(), initializer
            );
            new_value(ident.value, globalVariable);
        } else if (is_array) {
            Type type = getType(bType, true, size);
            Instr instr = new Allocate(cur_func.new_var(), type, initializer);
            new_value(ident.value, instr);
            Value pointer = instr;
            for (int i = 0; i < size; i++) {
                instr = new GetElementPointer(cur_func.new_var(), pointer, new ConstInt(0), new ConstInt(i));
                Value val = new ConstInt(initializer.get(i));
                val = castTo(val, ((PointerType) instr.type).elementType);
                instr = new Store(cur_func.new_var(), val, instr);
            }
        } else {
            Instr instr = new Allocate(cur_func.new_var(), baseType, initializer);
            new_value(ident.value, instr);
            int value = initializer.get(0);
            Value val = new ConstInt(value);
            val = castTo(val, baseType);
            instr = new Store(cur_func.new_var(), val, instr);
        }
    }

    public static Initializer ConstInitVal(AstNode ast) {
        ArrayList<Integer> values = new ArrayList<>();
        if (ast.get(0).type == AstType.ConstExp || ast.get(0).type == AstType.Exp) {
            values.add(ConstExp(ast.get(0)));
        } else if (ast.get(0).token.type == TokenType.STRCON) {
            String str = ast.get(0).token.value;
            str = str.substring(1, str.length() - 1);
            for (int i = 0; i < str.length(); i++) {
                values.add((int) str.charAt(i));
            }
        } else {
            for (AstNode son : ast.sons) {
                if (son.type == AstType.ConstExp || son.type == AstType.Exp) {
                    values.add(ConstExp(son));
                }
            }
        }
        return new Initializer(values);
    }

    public static void VarDecl(AstNode ast) {
        TokenType bType = BType(ast.get(0));
        for (int i = 1; i < ast.size(); i += 2) {
            VarDef(ast.get(i), bType);
        }
    }

    public static void VarDef(AstNode ast, TokenType bType) {
        Token ident = ast.get(0).token;
        boolean is_array = ast.size() > 1 && ast.get(1).token.type == singleType('[');
        int size = is_array ? ConstExp(ast.get(2)) : 1;
        Type baseType = getType(bType, false, 0);
        if (is_global()) {
            Initializer initializer;
            if (ast.get(-1).type == AstType.InitVal) {
                initializer = ConstInitVal(ast.get(-1));
            } else {
                initializer = new Initializer(new ArrayList<>());
            }
            initializer.baseType = baseType;
            initializer.is_array = is_array;
            initializer.resize(size);
            GlobalVariable globalVariable = new GlobalVariable(
                new PointerType(initializer.getType()), new_global_name(), initializer
            );
            new_value(ident.value, globalVariable);
        } else if (is_array) {
            Type type = getType(bType, true, size);
            Instr instr = new Allocate(cur_func.new_var(), type);
            new_value(ident.value, instr);
            if (ast.get(-1).type == AstType.InitVal) {
                Value pointer = instr;
                ArrayList<Value> initValues = InitVal(ast.get(-1));
                while (initValues.size() > size) initValues.remove(initValues.size() - 1);
                while (initValues.size() < size) initValues.add(new ConstInt(0));
                for (int i = 0; i < size; i++) {
                    instr = new GetElementPointer(cur_func.new_var(), pointer, new ConstInt(0), new ConstInt(i));
                    Value val = initValues.get(i);
                    val = castTo(val, ((PointerType) instr.type).elementType);
                    instr = new Store(cur_func.new_var(), val, instr);
                }
            }
        } else {
            Instr instr = new Allocate(cur_func.new_var(), baseType);
            new_value(ident.value, instr);
            if (ast.get(-1).type == AstType.InitVal) {
                Value value = InitVal(ast.get(-1)).get(0);
                value = castTo(value, baseType);
                instr = new Store(cur_func.new_var(), value, instr);
            }
        }
    }

    public static ArrayList<Value> InitVal(AstNode ast) {
        ArrayList<Value> values = new ArrayList<>();
        if (ast.get(0).type == AstType.Exp || ast.get(0).token.type == singleType('{')) {
            for (AstNode son : ast.sons) {
                if (son.type == AstType.Exp) {
                    values.add(Exp(son));
                }
            }
        } else {
            String str = ast.get(0).token.value;
            str = str.substring(1, str.length() - 1);
            for (int i = 0; i < str.length(); i++) {
                values.add(new ConstInt((int) str.charAt(i)));
            }
        }
        return values;
    }

    public static void FuncDef(AstNode ast) {
        Type retType = getType(FuncType(ast.get(0)), false, 0);
        Token ident = ast.get(1).token;
        Function function = new Function(retType, ident.value); // func has been inserted into program within function constructor
        new_value(ident.value, function);
        cur_bb = new BasicBlock(new_bb_name()); // bb has been inserted into func within basic block constructor
        enter();
        if (ast.get(3).type == AstType.FuncFParams) {
            FuncFParams(ast.get(3));
            // i don't know whether to allocate space for function parameters here to meet ssa requirements
            for (FuncParam param : function.params) {
                Instr instr = new Allocate(cur_func.new_var(), param.type);
                new_value(param.origin_name, instr);
                // no need to cast here, type is already correct
                instr = new Store(cur_func.new_var(), param, instr);
            }
        }
        Block(ast.get(-1));
        // ensure last block has return instruction
        if (cur_bb.instrs.isEmpty() || !(cur_bb.instrs.get(cur_bb.instrs.size() - 1) instanceof Return)) {
            Instr instr = new Return(cur_func.new_var(), null);
        }
        exit();
    }

    public static void MainFuncDef(AstNode ast) {
        Function function = new Function(INT_TYPE, "main");
        cur_bb = new BasicBlock(new_bb_name());
        enter();
        Block(ast.get(-1));
        exit();
    }

    public static TokenType FuncType(AstNode ast) {
        return ast.get(0).token.type;
    }

    public static void FuncFParams(AstNode ast) {
        for (int i = 0; i < ast.size(); i += 2) {
            FuncFParam(ast.get(i));
        }
    }

    public static void FuncFParam(AstNode ast) {
        TokenType bType = BType(ast.get(0));
        Token ident = ast.get(1).token;
        boolean is_array = ast.size() > 2;
        Type type = getType(bType, is_array, -1);
        FuncParam funcParam = new FuncParam(type, cur_func.new_var(), ident.value); // funcParam has been inserted into cur_func within funcParam constructor
        // no need to call new_value here, it will be called in FuncDef
    }

    public static void Block(AstNode ast) {
        for (int i = 0; i < ast.size(); i++) {
            if (ast.get(i).type == AstType.BlockItem) {
                BlockItem(ast.get(i));;
            }
        }
    }

    public static void BlockItem(AstNode ast) {
        if (ast.get(0).type == AstType.Decl) {
            Decl(ast.get(0));
        } else {
            Stmt(ast.get(0));
        }
    }

    public static void Stmt(AstNode ast) {
        if (ast.subType == 1) { // if
            If(ast);
        } else if (ast.subType == 2) { // for
            For(ast);
        } else if (ast.subType == 3) { // break / continue
            if (ast.get(0).token.type == TokenType.BREAKTK) { // break
                Instr instr = new Jump(cur_func.new_var(), loops.get(loops.size() - 1).exitBB);
            } else { // continue
                Instr instr = new Jump(cur_func.new_var(), loops.get(loops.size() - 1).stepBB);
            }
        } else if (ast.subType == 4) { // block
            enter();
            Block(ast.get(0));
            exit();
        } else if (ast.subType == 5) { // printf
            Printf(ast);
        } else if (ast.subType == 6) { // return
            if (ast.size() > 2) { // has value
                Value retValue = Exp(ast.get(1));
                retValue = castTo(retValue, cur_func.retType);
                Instr instr = new Return(cur_func.new_var(), retValue);
            } else { // no return value should in void function (error handled in Analyse.java)
                Instr instr = new Return(cur_func.new_var(), null);
            }
        } else if (ast.subType == 7) { // [Exp] ;
            if (ast.size() == 2) {
                Exp(ast.get(0));
            }
        } else if (ast.subType == 8) { // LVal = getint / getchar ( ) ;
            if (ast.get(2).token.type == TokenType.GETINTTK) {
                Value pointer = LValAssign(ast.get(0));
                IOInstr.GetInt value = new IOInstr.GetInt(cur_func.new_var());
                Instr instr = new Store(cur_func.new_var(), castTo(castTo(value, INT_TYPE), ((PointerType) pointer.type).elementType), pointer);
            } else {
                Value pointer = LValAssign(ast.get(0));
                IOInstr.GetChar value = new IOInstr.GetChar(cur_func.new_var());
                Instr instr = new Store(cur_func.new_var(), castTo(castTo(value, CHAR_TYPE), ((PointerType) pointer.type).elementType), pointer);
            }
        } else if (ast.subType == 9) { // LVal = Exp ;
            ForStmt(ast); // same as ForStmt except the last ;
        }
    }

    public static void If(AstNode ast) {
        boolean hasElse = ast.size() > 5;
        BasicBlock thenBB = new BasicBlock(new_bb_name());
        if (hasElse) {
            BasicBlock elseBB = new BasicBlock(new_bb_name());
            BasicBlock followBB = new BasicBlock(new_bb_name());
            Cond(ast.get(2), thenBB, elseBB);
            cur_bb = thenBB;
            Stmt(ast.get(4));
            Instr instr = new Jump(cur_func.new_var(), followBB);
            cur_bb = elseBB;
            Stmt(ast.get(6));
            instr = new Jump(cur_func.new_var(), followBB);
            cur_bb = followBB;
        } else {
            BasicBlock followBB = new BasicBlock(new_bb_name());
            Cond(ast.get(2), thenBB, followBB);
            cur_bb = thenBB;
            Stmt(ast.get(4));
            Instr instr = new Jump(cur_func.new_var(), followBB);
            cur_bb = followBB;
        }
    }

    public static void For(AstNode ast) {
        AstNode forStmt1 = null, cond = null, forStmt2 = null, stmt = ast.get(-1);
        int now = 2;
        if (ast.get(now).type == AstType.ForStmt) forStmt1 = ast.get(2); else now--;
        if (ast.get(now + 2).type == AstType.Cond) cond = ast.get(now + 2); else now--;
        if (ast.get(now + 4).type == AstType.ForStmt) forStmt2 = ast.get(now + 4);
        enter();
        if (forStmt1 != null) ForStmt(forStmt1);
        BasicBlock condBB = new BasicBlock(new_bb_name());
        BasicBlock bodyBB = new BasicBlock(new_bb_name());
        BasicBlock stepBB = new BasicBlock(new_bb_name());
        BasicBlock exitBB = new BasicBlock(new_bb_name());
        loops.add(new Loop(condBB, bodyBB, stepBB, exitBB));
        Instr instr = new Jump(cur_func.new_var(), condBB);
        cur_bb = condBB;
        if (cond != null) Cond(cond, bodyBB, exitBB);
        else {
            instr = new Jump(cur_func.new_var(), bodyBB);
        }
        cur_bb = bodyBB;
        Stmt(stmt);
        instr = new Jump(cur_func.new_var(), stepBB);
        cur_bb = stepBB;
        if (forStmt2 != null) ForStmt(forStmt2);
        instr = new Jump(cur_func.new_var(), condBB);
        cur_bb = exitBB;
        loops.remove(loops.size() - 1);
        exit();
    }

    public static void Printf(AstNode ast) {
        ArrayList<Value> values = new ArrayList<>();
        for (AstNode son : ast.sons) {
            if (son.type == AstType.Exp) {
                values.add(Exp(son));
            }
        }
        String format = ast.get(2).token.value;
        format = format.substring(1, format.length() - 1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0, j = 0; i < format.length(); i++) {
            if(format.charAt(i) == '%' && i + 1 < format.length() &&
                    (format.charAt(i + 1) == 'd' || format.charAt(i + 1) == 'c')) {
                if (!sb.isEmpty()) {
                    ConstString str = new ConstString(new_str_name(), sb.toString());
                    Instr instr = new IOInstr.PutString(cur_func.new_var(), str);
                    sb.setLength(0);
                }
                Value value = values.get(j++);
                if (format.charAt(i + 1) == 'd') {
                    Instr instr = new IOInstr.PutInt(cur_func.new_var(), castTo(value, INT_TYPE));
                } else {
                    Instr instr = new IOInstr.PutChar(cur_func.new_var(), castTo(value, CHAR_TYPE));
                }
                i++;
            } else if (format.charAt(i) == '\\') {
                sb.append("\n");
                i++;
            } else {
                sb.append(format.charAt(i));
            }
        }
        if (!sb.isEmpty()) {
            ConstString str = new ConstString(new_str_name(), sb.toString());
            Instr instr = new IOInstr.PutString(cur_func.new_var(), str);
        }
    }

    public static void ForStmt(AstNode ast) {
        Value pointer = LValAssign(ast.get(0));
        Value value = Exp(ast.get(2));
        value = castTo(value, ((PointerType) pointer.type).elementType);
        Instr instr = new Store(cur_func.new_var(), value, pointer);
    }

    public static Value Exp(AstNode ast) {
        return AddExp(ast.get(0));
    }

    public static void Cond(AstNode ast, BasicBlock trueBB, BasicBlock falseBB) {
        LOrExp(ast.get(0), trueBB, falseBB);
    }

    public static Value LValAssign(AstNode ast) {
        // return a pointer to the location of the lvalue
        Token ident = ast.get(0).token;
        Value value = get_value(ident.value);
        if (ast.size() == 1) { // variable
            return value;
        } else { // array
            Value pointer = value;
            Type elementType = ((PointerType) pointer.type).elementType;
            Value offset = Exp(ast.get(2));
            offset = castTo(offset, INT_TYPE);
            if (elementType.isPointer()) {
                pointer = new Load(cur_func.new_var(), pointer);
                return new GetElementPointer(cur_func.new_var(), pointer, offset);
            } else {
                return new GetElementPointer(cur_func.new_var(), pointer, new ConstInt(0), offset);
            }
        }
    }

    public static Value LValValue(AstNode ast) {
        // return the value of the lvalue
        Token ident = ast.get(0).token;
        Value value = get_value(ident.value);
        if (ast.size() == 1) {
            if (!((PointerType) value.type).elementType.isArray()) {
                return new Load(cur_func.new_var(), value);
            } else { // for function parameters
                Value pointer = value;
                return new GetElementPointer(cur_func.new_var(), pointer, new ConstInt(0), new ConstInt(0));
            }
        } else {
            Value pointer = value;
            Value offset = Exp(ast.get(2));
            offset = castTo(offset, INT_TYPE);
            Instr instr;
            if (((PointerType) pointer.type).elementType.isPointer()) {
                pointer = new Load(cur_func.new_var(), pointer);
                instr = new GetElementPointer(cur_func.new_var(), pointer, offset);
            } else {
                instr = new GetElementPointer(cur_func.new_var(), pointer, new ConstInt(0), offset);
            }
            return new Load(cur_func.new_var(), instr);
        }
    }

    public static Value PrimaryExp(AstNode ast) {
        if (ast.size() == 3) {
            return Exp(ast.get(1));
        } else if (ast.get(0).type == AstType.LVal) {
            return LValValue(ast.get(0));
        } else if (ast.get(0).type == AstType.Number) {
            return Number(ast.get(0));
        } else {
            return Character(ast.get(0));
        }
    }

    public static Value Number(AstNode ast) {
        return new ConstInt(Integer.parseInt(ast.get(0).token.value));
    }

    public static Value Character(AstNode ast) {
        String str = ast.get(0).token.value;
        if (str.length() == 3) {
            return new ConstInt(str.charAt(1));
        } else {
            // handle \a, \b, \t, \n, \v, \f, \', \", \\, \0
            return switch (str.charAt(2)) {
                case 'a' -> new ConstInt(7);
                case 'b' -> new ConstInt(8);
                case 't' -> new ConstInt(9);
                case 'n' -> new ConstInt(10);
                case 'v' -> new ConstInt(11);
                case 'f' -> new ConstInt(12);
                case '\'' -> new ConstInt(39);
                case '\"' -> new ConstInt(34);
                case '\\' -> new ConstInt(92);
                default -> new ConstInt(0); // case '0'
            };
        }
    }

    public static Value UnaryExp(AstNode ast) {
        if (ast.get(0).type == AstType.PrimaryExp) {
            return PrimaryExp(ast.get(0));
        } else if (ast.get(0).type == AstType.UnaryOp) {
            TokenType op = UnaryOp(ast.get(0));
            Value op1 = UnaryExp(ast.get(1));
            if (op == TokenType.PLUS) {
                return op1;
            } else if (op == TokenType.MINU) {
                return new BinaryOperator(cur_func.new_var(), BinaryOperator.Op.SUB, new ConstInt(0), op1);
            } else {
                return new Icmp(cur_func.new_var(), Icmp.Op.EQ, castTo(op1, INT_TYPE), new ConstInt(0));
            }
        } else {
            Token ident = ast.get(0).token;
            Function func = (Function) get_value(ident.value);
            ArrayList<Value> params = new ArrayList<>();
            if (ast.size() > 3) {
                params = FuncRParams(ast.get(2));
            }
            ArrayList<FuncParam> funcParams = func.params;
            for (int i = 0; i < params.size(); i++) {
                params.set(i, castTo(params.get(i), funcParams.get(i).type));
            }
            return new Call(cur_func.new_var(), func, params);
        }
    }

    public static TokenType UnaryOp(AstNode ast) {
        return ast.get(0).token.type;
    }

    public static ArrayList<Value> FuncRParams(AstNode ast) {
        ArrayList<Value> values = new ArrayList<>();
        for (int i = 0; i < ast.size(); i += 2) {
            values.add(Exp(ast.get(i)));
        }
        return values;
    }

    public static Value MulExp(AstNode ast) {
        ArrayList<AstNode> sons = flatten(ast, AstType.MulExp);
        Value op1 = UnaryExp(sons.get(0));
        for (int i = 1; i < sons.size(); i += 2) {
            op1 = castTo(op1, INT_TYPE);
            Value op2 = UnaryExp(sons.get(i + 1));
            op2 = castTo(op2, INT_TYPE);
            BinaryOperator.Op op =
                    sons.get(i).token.type == TokenType.MULT ? BinaryOperator.Op.MUL :
                    sons.get(i).token.type == TokenType.DIV ? BinaryOperator.Op.SDIV :
                            BinaryOperator.Op.SREM;
            op1 = new BinaryOperator(cur_func.new_var(), op, op1, op2);
        }
        return op1;
    }

    public static Value AddExp(AstNode ast) {
        ArrayList<AstNode> sons = flatten(ast, AstType.AddExp);
        Value op1 = MulExp(sons.get(0));
        for (int i = 1; i < sons.size(); i += 2) {
            op1 = castTo(op1, INT_TYPE);
            Value op2 = MulExp(sons.get(i + 1));
            op2 = castTo(op2, INT_TYPE);
            BinaryOperator.Op op =
                    sons.get(i).token.type == TokenType.PLUS ? BinaryOperator.Op.ADD :
                            BinaryOperator.Op.SUB;
            op1 = new BinaryOperator(cur_func.new_var(), op, op1, op2);
        }
        return op1;
    }

    public static Value RelExp(AstNode ast) {
        ArrayList<AstNode> sons = flatten(ast, AstType.RelExp);
        Value op1 = AddExp(sons.get(0));
        for (int i = 1; i < sons.size(); i += 2) {
            op1 = castTo(op1, INT_TYPE);
            Value op2 = AddExp(sons.get(i + 1));
            op2 = castTo(op2, INT_TYPE);
            Icmp.Op op = sons.get(i).token.type == TokenType.LSS ? Icmp.Op.SLT :
                    sons.get(i).token.type == TokenType.LEQ ? Icmp.Op.SLE :
                    sons.get(i).token.type == TokenType.GRE ? Icmp.Op.SGT :
                            Icmp.Op.SGE;
            op1 = new Icmp(cur_func.new_var(), op, op1, op2);
        }
        return op1;
    }

    public static Value EqExp(AstNode ast) {
        ArrayList<AstNode> sons = flatten(ast, AstType.EqExp);
        Value op1 = RelExp(sons.get(0));
        for (int i = 1; i < sons.size(); i += 2) {
            op1 = castTo(op1, INT_TYPE);
            Value op2 = RelExp(sons.get(i + 1));
            op2 = castTo(op2, INT_TYPE);
            Icmp.Op op = sons.get(i).token.type == TokenType.EQL ? Icmp.Op.EQ : Icmp.Op.NE;
            op1 = new Icmp(cur_func.new_var(), op, op1, op2);
        }
        return op1;
    }

    public static void LAndExp(AstNode ast, BasicBlock trueBB, BasicBlock falseBB) {
        ArrayList<AstNode> sons = flatten(ast, AstType.LAndExp);
        for (int i = 0; i < sons.size(); i += 2) {
            Value cond = EqExp(sons.get(i));
            if (cond.type != BOOL_TYPE) {
                cond = new Icmp(cur_func.new_var(), Icmp.Op.NE, castTo(cond, INT_TYPE), new ConstInt(0));
            }
            if (i == sons.size() - 1) {
                Instr instr = new Branch(cur_func.new_var(), cond, trueBB, falseBB);
            } else {
                BasicBlock nextBB = new BasicBlock(new_bb_name());
                Instr instr = new Branch(cur_func.new_var(), cond, nextBB, falseBB);
                cur_bb = nextBB;
            }
        }
    }

    public static void LOrExp(AstNode ast, BasicBlock trueBB, BasicBlock falseBB) {
        ArrayList<AstNode> sons = flatten(ast, AstType.LOrExp);
        for (int i = 0; i < sons.size(); i += 2) {
            if (i == sons.size() - 1) {
                LAndExp(sons.get(i), trueBB, falseBB);
            } else {
                BasicBlock nextBB = new BasicBlock(new_bb_name());
                LAndExp(sons.get(i), trueBB, nextBB);
                cur_bb = nextBB;
            }
        }
    }

    public static int LValEval(AstNode ast) {
        Token ident = ast.get(0).token;
        Value value = get_value(ident.value);
        Initializer initializer = value instanceof GlobalVariable ?
                ((GlobalVariable) value).initializer : ((Allocate) value).initializer;
        if (ast.size() == 1) {
            return initializer.get(0);
        } else {
            int offset = ConstExp(ast.get(2));
            return initializer.get(offset);
        }
    }

    public static int PrimaryExpEval(AstNode ast) {
        if (ast.size() == 3) {
            return ConstExp(ast.get(1));
        } else if (ast.get(0).type == AstType.LVal) {
            return LValEval(ast.get(0));
        } else if (ast.get(0).type == AstType.Number) {
            return ((ConstInt) Number(ast.get(0))).value;
        } else {
            return ((ConstInt) Character(ast.get(0))).value;
        }
    }

    public static int UnaryExpEval(AstNode ast) {
        if (ast.get(0).type == AstType.PrimaryExp) {
            return PrimaryExpEval(ast.get(0));
        } else {
            TokenType op = UnaryOp(ast.get(0));
            int res = UnaryExpEval(ast.get(1));
            if (op == TokenType.PLUS) {
                return res;
            } else if (op == TokenType.MINU) {
                return -res;
            } else {
                return res == 0 ? 1 : 0;
            }
        }
    }

    public static int MulExpEval(AstNode ast) {
        ArrayList<AstNode> sons = flatten(ast, AstType.MulExp);
        int res = UnaryExpEval(sons.get(0));
        for (int i = 1; i < sons.size(); i += 2) {
            if (sons.get(i).token.type == TokenType.MULT) {
                res *= UnaryExpEval(sons.get(i + 1));
            } else if (sons.get(i).token.type == TokenType.DIV) {
                res /= UnaryExpEval(sons.get(i + 1));
            } else {
                res %= UnaryExpEval(sons.get(i + 1));
            }
        }
        return res;
    }

    public static int AddExpEval(AstNode ast) {
        ArrayList<AstNode> sons = flatten(ast, AstType.AddExp);
        int res = MulExpEval(sons.get(0));
        for (int i = 1; i < sons.size(); i += 2) {
            if (sons.get(i).token.type == TokenType.PLUS) {
                res += MulExpEval(sons.get(i + 1));
            } else {
                res -= MulExpEval(sons.get(i + 1));
            }
        }
        return res;
    }

    public static int ConstExp(AstNode ast) {
        return AddExpEval(ast.get(0));
    }
}
