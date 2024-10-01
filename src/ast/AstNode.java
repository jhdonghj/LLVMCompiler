package ast;

import token.Token;
import utils.IO;

import java.util.ArrayList;

public class AstNode {
    public AstType type;
    public int line;
    public int lstLine;
    public Token token;
    public ArrayList<AstNode> sons = new ArrayList<>();

    public AstNode(AstType type, int line) {
        this.type = type;
        this.line = line;
        this.lstLine = line;
    }

    public AstNode(Token token) {
        type = AstType.Token;
        this.token = token;
        this.line = token.line;
        this.lstLine = token.line;
    }

    public void add(AstNode node) {
        sons.add(node);
        lstLine = node.lstLine;
    }

    public void print() {
        if (type == AstType.Token) {
            IO.writeln(token.toString());
        } else {
            for (AstNode son : sons) {
                son.print();
            }
            if (type != AstType.BlockItem && type != AstType.Decl && type != AstType.BType) {
                IO.writeln("<" + type.toString() + ">");
            }
        }
    }
}
