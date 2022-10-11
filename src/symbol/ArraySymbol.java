package symbol;

import java.util.List;

public class ArraySymbol extends Symbol {

    private boolean isConst; // 是否是常量
    private int dimension; // 0 变量，1 数组，2 二维数组
    private List<Integer> dimLengths; // 每一维数组长度

    public ArraySymbol(String name, boolean isConst, int dimension, List<Integer> dimLengths) { // 数组
        super(name);
        this.isConst = isConst;
        this.dimension = dimension;
        this.dimLengths = dimLengths;
    }

    public boolean isConst() {
        return isConst;
    }

    public int getDimension() {
        return dimension;
    }

    public List<Integer> getDimLengths() {
        return dimLengths;
    }
}
