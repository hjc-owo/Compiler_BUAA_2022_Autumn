package node;

import frontend.Parser;
import symbol.FuncRParam;
import symbol.SymbolTable;
import token.Token;
import utils.IOUtils;

public class PrimaryExpNode {
    // PrimaryExp -> '(' Exp ')' | LVal | Number

    private Token leftParentToken = null;
    private ExpNode expNode = null;
    private Token rightParentToken = null;
    private LValNode lValNode = null;
    private NumberNode numberNode = null;

    public PrimaryExpNode(Token leftParentToken, ExpNode expNode, Token rightParentToken) {
        this.leftParentToken = leftParentToken;
        this.expNode = expNode;
        this.rightParentToken = rightParentToken;
    }

    public PrimaryExpNode(LValNode lValNode) {
        this.lValNode = lValNode;
    }

    public PrimaryExpNode(NumberNode numberNode) {
        this.numberNode = numberNode;
    }

    public int getValue() {
        if (expNode != null) {
            return expNode.getValue();
        } else if (lValNode != null) {
            return lValNode.getValue();
        } else {
            return numberNode.getValue();
        }
    }

    public void print() {
        if (expNode != null) {
            IOUtils.write(leftParentToken.toString());
            expNode.print();
            IOUtils.write(rightParentToken.toString());
        } else if (lValNode != null) {
            lValNode.print();
        } else {
            numberNode.print();
        }
        IOUtils.write(Parser.nodeType.get(NodeType.PrimaryExp));
    }

    public void fillSymbolTable(SymbolTable currentSymbolTable) {
        if (expNode != null) {
            expNode.fillSymbolTable(currentSymbolTable);
        } else if (lValNode != null) {
            lValNode.fillSymbolTable(currentSymbolTable);
        }
    }

    public FuncRParam getFuncRParam() {
        if (expNode != null) {
            return expNode.getFuncRParam();
        } else if (lValNode != null) {
            return lValNode.getFuncRParam();
        } else {
            return new FuncRParam(null, 0);
        }
    }
}