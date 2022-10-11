package node;

import frontend.Parser;
import symbol.SymbolTable;
import utils.IOUtils;

public class CondNode {
    // Cond -> LOrExp

    private LOrExpNode lOrExpNode;

    public CondNode(LOrExpNode lOrExpNode) {
        this.lOrExpNode = lOrExpNode;
    }

    public void print() {
        lOrExpNode.print();
        IOUtils.write(Parser.nodeType.get(NodeType.Cond));
    }

    public void fillSymbolTable(SymbolTable currentSymbolTable) {
        lOrExpNode.fillSymbolTable(currentSymbolTable);
    }
}