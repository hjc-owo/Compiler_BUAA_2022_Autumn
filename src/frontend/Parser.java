package frontend;

import error.Error;
import error.ErrorHandler;
import error.ErrorType;
import node.*;
import symbol.SymbolTable;
import token.Token;
import token.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {
    private List<Token> tokens;
    private int index = 0;
    private CompUnitNode compUnitNode;
    private SymbolTable symbolTable = new SymbolTable();
    private SymbolTable currentSymbolTable = symbolTable;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public void analyze() {
        this.compUnitNode = CompUnit();
    }

    public static Map<NodeType, String> nodeType = new HashMap<NodeType, String>() {{
        put(NodeType.CompUnit, "<CompUnit>\n");
        put(NodeType.Decl, "<Decl>\n");
        put(NodeType.ConstDecl, "<ConstDecl>\n");
        put(NodeType.BType, "<BType>\n");
        put(NodeType.ConstDef, "<ConstDef>\n");
        put(NodeType.ConstInitVal, "<ConstInitVal>\n");
        put(NodeType.VarDecl, "<VarDecl>\n");
        put(NodeType.VarDef, "<VarDef>\n");
        put(NodeType.InitVal, "<InitVal>\n");
        put(NodeType.FuncDef, "<FuncDef>\n");
        put(NodeType.MainFuncDef, "<MainFuncDef>\n");
        put(NodeType.FuncType, "<FuncType>\n");
        put(NodeType.FuncFParams, "<FuncFParams>\n");
        put(NodeType.FuncFParam, "<FuncFParam>\n");
        put(NodeType.Block, "<Block>\n");
        put(NodeType.BlockItem, "<BlockItem>\n");
        put(NodeType.Stmt, "<Stmt>\n");
        put(NodeType.Exp, "<Exp>\n");
        put(NodeType.Cond, "<Cond>\n");
        put(NodeType.LVal, "<LVal>\n");
        put(NodeType.PrimaryExp, "<PrimaryExp>\n");
        put(NodeType.Number, "<Number>\n");
        put(NodeType.UnaryExp, "<UnaryExp>\n");
        put(NodeType.UnaryOp, "<UnaryOp>\n");
        put(NodeType.FuncRParams, "<FuncRParams>\n");
        put(NodeType.MulExp, "<MulExp>\n");
        put(NodeType.AddExp, "<AddExp>\n");
        put(NodeType.RelExp, "<RelExp>\n");
        put(NodeType.EqExp, "<EqExp>\n");
        put(NodeType.LAndExp, "<LAndExp>\n");
        put(NodeType.LOrExp, "<LOrExp>\n");
        put(NodeType.ConstExp, "<ConstExp>\n");
    }};

    public void fillSymbolTable() {
        this.compUnitNode.fillSymbolTable(currentSymbolTable);
    }

    public enum StmtType {
        LValAssignExp, Exp, Block, If, While, Break, Continue, Return, LValAssignGetint, Printf
    }

    private CompUnitNode CompUnit() {
        // CompUnit -> {Decl} {FuncDef} MainFuncDef
        List<DeclNode> declNodes = new ArrayList<>();
        List<FuncDefNode> funcDefNodes = new ArrayList<>();
        MainFuncDefNode mainFuncDefNode;
        while (tokens.get(index + 1).getType() != TokenType.MAINTK && tokens.get(index + 2).getType() != TokenType.LPARENT) {
            DeclNode declNode = Decl();
            declNodes.add(declNode);
        }
        while (tokens.get(index + 1).getType() != TokenType.MAINTK) {
            FuncDefNode funcDefNode = FuncDef();
            funcDefNodes.add(funcDefNode);
        }
        mainFuncDefNode = MainFuncDef();
        return new CompUnitNode(declNodes, funcDefNodes, mainFuncDefNode);
    }

    private DeclNode Decl() {
        // Decl -> ConstDecl | VarDecl
        ConstDeclNode constDeclNode = null;
        VarDeclNode varDeclNode = null;
        if (tokens.get(index).getType() == TokenType.CONSTTK) {
            constDeclNode = ConstDecl();
        } else {
            varDeclNode = VarDecl();
        }
        return new DeclNode(constDeclNode, varDeclNode);
    }

    private ConstDeclNode ConstDecl() {
        // ConstDecl -> 'const' BType ConstDef { ',' ConstDef } ';'
        Token constToken = match(TokenType.CONSTTK);
        BTypeNode bTypeNode = BType();
        List<ConstDefNode> constDefNodes = new ArrayList<>();
        List<Token> commas = new ArrayList<>();
        Token semicnToken;
        constDefNodes.add(ConstDef());
        while (tokens.get(index).getType() == TokenType.COMMA) {
            commas.add(match(TokenType.COMMA));
            constDefNodes.add(ConstDef());
        }
        semicnToken = match(TokenType.SEMICN);
        return new ConstDeclNode(constToken, bTypeNode, constDefNodes, commas, semicnToken);
    }

    private BTypeNode BType() {
        // BType -> 'int'
        Token bTypeToken = match(TokenType.INTTK);
        return new BTypeNode(bTypeToken);
    }

    private ConstDefNode ConstDef() {
        // ConstDef -> Ident { '[' ConstExp ']' } '=' ConstInitVal
        Token ident = match(TokenType.IDENFR);
        List<Token> leftBrackets = new ArrayList<>();
        List<ConstExpNode> constExpNodes = new ArrayList<>();
        List<Token> rightBrackets = new ArrayList<>();
        Token equalToken;
        while (tokens.get(index).getType() == TokenType.LBRACK) {
            leftBrackets.add(match(TokenType.LBRACK));
            constExpNodes.add(ConstExp());
            rightBrackets.add(match(TokenType.RBRACK));
        }
        equalToken = match(TokenType.ASSIGN);
        ConstInitValNode constInitValNode = ConstInitVal();
        return new ConstDefNode(ident, leftBrackets, constExpNodes, rightBrackets, equalToken, constInitValNode);
    }

    private ConstInitValNode ConstInitVal() {
        // ConstInitVal -> ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
        ConstExpNode constExpNode = null;
        Token leftBraceToken = null;
        List<ConstInitValNode> constInitValNodes = new ArrayList<>();
        List<Token> commas = new ArrayList<>();
        Token rightBraceToken = null;
        if (tokens.get(index).getType() == TokenType.LBRACE) {
            leftBraceToken = match(TokenType.LBRACE);
            if (tokens.get(index).getType() != TokenType.RBRACE) {
                constInitValNodes.add(ConstInitVal());
                while (tokens.get(index).getType() != TokenType.RBRACE) {
                    commas.add(match(TokenType.COMMA));
                    constInitValNodes.add(ConstInitVal());
                }
            }
            rightBraceToken = match(TokenType.RBRACE);
        } else {
            constExpNode = ConstExp();
        }
        return new ConstInitValNode(constExpNode, leftBraceToken, constInitValNodes, commas, rightBraceToken);
    }

    private VarDeclNode VarDecl() {
        // VarDecl -> BType VarDef { ',' VarDef } ';'
        BTypeNode bTypeNode = BType();
        List<VarDefNode> varDefNodes = new ArrayList<>();
        List<Token> commas = new ArrayList<>();
        Token semicnToken;
        varDefNodes.add(VarDef());
        while (tokens.get(index).getType() == TokenType.COMMA) {
            commas.add(match(TokenType.COMMA));
            varDefNodes.add(VarDef());
        }
        semicnToken = match(TokenType.SEMICN);
        return new VarDeclNode(bTypeNode, varDefNodes, commas, semicnToken);
    }

    private VarDefNode VarDef() {
        // VarDef -> Ident { '[' ConstExp ']' } [ '=' InitVal ]
        Token ident = match(TokenType.IDENFR);
        List<Token> leftBrackets = new ArrayList<>();
        List<ConstExpNode> constExpNodes = new ArrayList<>();
        List<Token> rightBrackets = new ArrayList<>();
        Token equalToken = null;
        InitValNode initValNode = null;
        while (tokens.get(index).getType() == TokenType.LBRACK) {
            leftBrackets.add(match(TokenType.LBRACK));
            constExpNodes.add(ConstExp());
            rightBrackets.add(match(TokenType.RBRACK));
        }
        if (tokens.get(index).getType() == TokenType.ASSIGN) {
            equalToken = match(TokenType.ASSIGN);
            initValNode = InitVal();
        }
        return new VarDefNode(ident, leftBrackets, constExpNodes, rightBrackets, equalToken, initValNode);
    }

    private InitValNode InitVal() {
        // InitVal -> Exp | '{' [ InitVal { ',' InitVal } ] '}'
        ExpNode expNode = null;
        Token leftBraceToken = null;
        List<InitValNode> initValNodes = new ArrayList<>();
        List<Token> commas = new ArrayList<>();
        Token rightBraceToken = null;
        if (tokens.get(index).getType() == TokenType.LBRACE) {
            leftBraceToken = match(TokenType.LBRACE);
            if (tokens.get(index).getType() != TokenType.RBRACE) {
                initValNodes.add(InitVal());
                while (tokens.get(index).getType() != TokenType.RBRACE) {
                    commas.add(match(TokenType.COMMA));
                    initValNodes.add(InitVal());
                }
            }
            rightBraceToken = match(TokenType.RBRACE);
        } else {
            expNode = Exp();
        }
        return new InitValNode(expNode, leftBraceToken, initValNodes, commas, rightBraceToken);
    }

    private FuncDefNode FuncDef() {
        // FuncDef -> FuncType Ident '(' [FuncFParams] ')' Block
        FuncTypeNode funcTypeNode = FuncType();
        Token ident = match(TokenType.IDENFR);
        Token leftParentToken = match(TokenType.LPARENT);
        FuncFParamsNode funcParamsNode = null;
        if (tokens.get(index).getType() != TokenType.RPARENT) {
            funcParamsNode = FuncFParams();
        }
        Token rightParentToken = match(TokenType.RPARENT);
        BlockNode blockNode = Block();
        return new FuncDefNode(funcTypeNode, ident, leftParentToken, funcParamsNode, rightParentToken, blockNode);
    }

    private MainFuncDefNode MainFuncDef() {
        // MainFuncDef -> 'int' 'main' '(' ')' Block
        Token intToken = match(TokenType.INTTK);
        Token mainToken = match(TokenType.MAINTK);
        Token leftParentToken = match(TokenType.LPARENT);
        Token rightParentToken = match(TokenType.RPARENT);
        BlockNode blockNode = Block();
        return new MainFuncDefNode(intToken, mainToken, leftParentToken, rightParentToken, blockNode);
    }

    private FuncTypeNode FuncType() {
        // FuncType -> 'void' | 'int'
        if (tokens.get(index).getType() == TokenType.VOIDTK) {
            Token voidToken = match(TokenType.VOIDTK);
            return new FuncTypeNode(voidToken);
        } else {
            Token intToken = match(TokenType.INTTK);
            return new FuncTypeNode(intToken);
        }
    }

    private FuncFParamsNode FuncFParams() {
        // FuncFParams -> FuncFParam { ',' FuncFParam }
        List<FuncFParamNode> funcFParamNodes = new ArrayList<>();
        List<Token> commas = new ArrayList<>();
        funcFParamNodes.add(FuncFParam());
        while (tokens.get(index).getType() == TokenType.COMMA) {
            commas.add(match(TokenType.COMMA));
            funcFParamNodes.add(FuncFParam());
        }
        return new FuncFParamsNode(funcFParamNodes, commas);
    }

    private FuncFParamNode FuncFParam() {
        // FuncFParam -> BType Ident [ '[' ']' { '[' ConstExp ']' }]
        BTypeNode bTypeNode = BType();
        Token ident = match(TokenType.IDENFR);
        List<Token> leftBrackets = new ArrayList<>();
        List<Token> rightBrackets = new ArrayList<>();
        List<ConstExpNode> constExpNodes = new ArrayList<>();
        if (tokens.get(index).getType() == TokenType.LBRACK) {
            leftBrackets.add(match(TokenType.LBRACK));
            rightBrackets.add(match(TokenType.RBRACK));
            while (tokens.get(index).getType() == TokenType.LBRACK) {
                leftBrackets.add(match(TokenType.LBRACK));
                constExpNodes.add(ConstExp());
                rightBrackets.add(match(TokenType.RBRACK));
            }
        }
        return new FuncFParamNode(bTypeNode, ident, leftBrackets, rightBrackets, constExpNodes);
    }

    private BlockNode Block() {
        // Block -> '{' { BlockItem } '}'
        Token leftBraceToken = match(TokenType.LBRACE);
        List<BlockItemNode> blockItemNodes = new ArrayList<>();
        while (tokens.get(index).getType() != TokenType.RBRACE) {
            blockItemNodes.add(BlockItem());
        }
        Token rightBraceToken = match(TokenType.RBRACE);
        return new BlockNode(leftBraceToken, blockItemNodes, rightBraceToken);
    }

    private BlockItemNode BlockItem() {
        // BlockItem -> Decl | Stmt
        DeclNode declNode = null;
        StmtNode stmtNode = null;
        if (tokens.get(index).getType() == TokenType.CONSTTK || tokens.get(index).getType() == TokenType.INTTK) {
            declNode = Decl();
        } else {
            stmtNode = Stmt();
        }
        return new BlockItemNode(declNode, stmtNode);
    }

    private StmtNode Stmt() {
        // Stmt -> LVal '=' Exp ';'
        //	| [Exp] ';'
        //	| Block
        //	| 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
        //	| 'while' '(' Cond ')' Stmt
        //	| 'break' ';' | 'continue' ';'
        //	| 'return' [Exp] ';'
        //	| LVal '=' 'getint' '(' ')' ';'
        //	| 'printf' '(' FormatString { ',' Exp } ')' ';'
        if (tokens.get(index).getType() == TokenType.LBRACE) {
            // Block
            BlockNode blockNode = Block();
            return new StmtNode(StmtType.Block, blockNode);
        } else if (tokens.get(index).getType() == TokenType.PRINTFTK) {
            // 'printf' '(' FormatString { ',' Exp } ')' ';'
            Token printfToken = match(TokenType.PRINTFTK);
            Token leftParentToken = match(TokenType.LPARENT);
            Token formatString = match(TokenType.STRCON);
            List<Token> commas = new ArrayList<>();
            List<ExpNode> expNodes = new ArrayList<>();
            while (tokens.get(index).getType() == TokenType.COMMA) {
                commas.add(match(TokenType.COMMA));
                expNodes.add(Exp());
            }
            Token rightParentToken = match(TokenType.RPARENT);
            Token semicnToken = match(TokenType.SEMICN);
            return new StmtNode(StmtType.Printf, printfToken, leftParentToken, formatString, commas, expNodes, rightParentToken, semicnToken);
        } else if (tokens.get(index).getType() == TokenType.IFTK) {
            // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
            Token ifToken = match(TokenType.IFTK);
            Token leftParentToken = match(TokenType.LPARENT);
            CondNode condNode = Cond();
            Token rightParentToken = match(TokenType.RPARENT);
            List<StmtNode> stmtNodes = new ArrayList<>();
            stmtNodes.add(Stmt());
            Token elseToken = null;
            if (tokens.get(index).getType() == TokenType.ELSETK) {
                elseToken = match(TokenType.ELSETK);
                stmtNodes.add(Stmt());
            }
            return new StmtNode(StmtType.If, ifToken, leftParentToken, condNode, rightParentToken, stmtNodes, elseToken);
        } else if (tokens.get(index).getType() == TokenType.WHILETK) {
            // 'while' '(' Cond ')' Stmt
            Token whileToken = match(TokenType.WHILETK);
            Token leftParentToken = match(TokenType.LPARENT);
            CondNode condNode = Cond();
            Token rightParentToken = match(TokenType.RPARENT);
            List<StmtNode> stmtNodes = new ArrayList<>();
            stmtNodes.add(Stmt());
            return new StmtNode(StmtType.While, whileToken, leftParentToken, condNode, rightParentToken, stmtNodes);
        } else if (tokens.get(index).getType() == TokenType.BREAKTK) {
            // 'break' ';'
            Token breakToken = match(TokenType.BREAKTK);
            Token semicnToken = match(TokenType.SEMICN);
            return new StmtNode(StmtType.Break, breakToken, semicnToken);
        } else if (tokens.get(index).getType() == TokenType.CONTINUETK) {
            // 'continue' ';'
            Token continueToken = match(TokenType.CONTINUETK);
            Token semicnToken = match(TokenType.SEMICN);
            return new StmtNode(StmtType.Continue, continueToken, semicnToken);
        } else if (tokens.get(index).getType() == TokenType.RETURNTK) {
            // 'return' [Exp] ';'
            Token returnToken = match(TokenType.RETURNTK);
            ExpNode expNode = null;
            if (tokens.get(index).getType() != TokenType.SEMICN) {
                expNode = Exp();
            }
            Token semicnToken = match(TokenType.SEMICN);
            return new StmtNode(StmtType.Return, returnToken, expNode, semicnToken);
        } else {
            int assign = index, semicn = index;
            for (int i = index; i < tokens.size() && tokens.get(i).getLineNumber() == tokens.get(index).getLineNumber(); i++) {
                if (tokens.get(i).getType() == TokenType.ASSIGN) {
                    assign = i;
                }
            }
            if (assign > index) {
                // LVal '=' Exp ';'
                // LVal '=' 'getint' '(' ')' ';'
                LValNode lValNode = LVal();
                Token assignToken = match(TokenType.ASSIGN);
                if (tokens.get(index).getType() == TokenType.GETINTTK) {
                    Token getintToken = match(TokenType.GETINTTK);
                    Token leftParentToken = match(TokenType.LPARENT);
                    Token rightParentToken = match(TokenType.RPARENT);
                    Token semicnToken = match(TokenType.SEMICN);
                    return new StmtNode(StmtType.LValAssignGetint, lValNode, assignToken, getintToken, leftParentToken, rightParentToken, semicnToken);
                } else {
                    ExpNode expNode = Exp();
                    Token semicnToken = match(TokenType.SEMICN);
                    return new StmtNode(StmtType.LValAssignExp, lValNode, assignToken, expNode, semicnToken);
                }
            } else {
                // [Exp] ';'
                ExpNode expNode = null;
                if (tokens.get(index).getType() == TokenType.IDENFR ||
                        tokens.get(index).getType() == TokenType.PLUS ||
                        tokens.get(index).getType() == TokenType.MINU ||
                        tokens.get(index).getType() == TokenType.NOT ||
                        tokens.get(index).getType() == TokenType.LPARENT ||
                        tokens.get(index).getType() == TokenType.INTCON) {
                    expNode = Exp();
                }
                Token semicnToken = match(TokenType.SEMICN);
                return new StmtNode(StmtType.Exp, expNode, semicnToken);
            }
        }
    }

    private ExpNode Exp() {
        // Exp -> AddExp
        return new ExpNode(AddExp());
    }

    private CondNode Cond() {
        // Cond -> LOrExp
        return new CondNode(LOrExp());
    }

    private LValNode LVal() {
        // LVal -> Ident {'[' Exp ']'}
        Token ident = match(TokenType.IDENFR);
        List<Token> leftBrackets = new ArrayList<>();
        List<ExpNode> expNodes = new ArrayList<>();
        List<Token> rightBrackets = new ArrayList<>();
        while (tokens.get(index).getType() == TokenType.LBRACK) {
            leftBrackets.add(match(TokenType.LBRACK));
            expNodes.add(Exp());
            rightBrackets.add(match(TokenType.RBRACK));
        }
        return new LValNode(ident, leftBrackets, expNodes, rightBrackets);
    }

    private PrimaryExpNode PrimaryExp() {
        // PrimaryExp -> '(' Exp ')' | LVal | Number
        if (tokens.get(index).getType() == TokenType.LPARENT) {
            Token leftParentToken = match(TokenType.LPARENT);
            ExpNode expNode = Exp();
            Token rightParentToken = match(TokenType.RPARENT);
            return new PrimaryExpNode(leftParentToken, expNode, rightParentToken);
        } else if (tokens.get(index).getType() == TokenType.INTCON) {

            NumberNode numberNode = Number();
            return new PrimaryExpNode(numberNode);
        } else {
            LValNode lValNode = LVal();
            return new PrimaryExpNode(lValNode);
        }
    }

    private NumberNode Number() {
        // Number -> IntConst
        return new NumberNode(match(TokenType.INTCON));
    }

    private UnaryExpNode UnaryExp() {
        // UnaryExp -> PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        if (tokens.get(index).getType() == TokenType.IDENFR && tokens.get(index + 1).getType() == TokenType.LPARENT) {
            Token ident = match(TokenType.IDENFR);
            Token leftParentToken = match(TokenType.LPARENT);
            FuncRParamsNode funcRParamsNode = null;
            if (tokens.get(index).getType() != TokenType.RPARENT) {
                funcRParamsNode = FuncRParams();
            }
            Token rightParentToken = match(TokenType.RPARENT);
            return new UnaryExpNode(ident, leftParentToken, funcRParamsNode, rightParentToken);
        } else if (tokens.get(index).getType() == TokenType.PLUS || tokens.get(index).getType() == TokenType.MINU || tokens.get(index).getType() == TokenType.NOT) {
            UnaryOpNode unaryOpNode = UnaryOp();
            UnaryExpNode unaryExpNode = UnaryExp();
            return new UnaryExpNode(unaryOpNode, unaryExpNode);
        } else {
            PrimaryExpNode primaryExpNode = PrimaryExp();
            return new UnaryExpNode(primaryExpNode);
        }
    }

    private UnaryOpNode UnaryOp() {
        // UnaryOp -> '+' | '−' | '!'
        Token token;
        if (tokens.get(index).getType() == TokenType.PLUS) {
            token = match(TokenType.PLUS);
        } else if (tokens.get(index).getType() == TokenType.MINU) {
            token = match(TokenType.MINU);
        } else {
            token = match(TokenType.NOT);
        }
        return new UnaryOpNode(token);
    }

    private FuncRParamsNode FuncRParams() {
        // FuncRParams -> Exp { ',' Exp }
        List<ExpNode> expNodes = new ArrayList<>();
        List<Token> commas = new ArrayList<>();
        expNodes.add(Exp());
        while (tokens.get(index).getType() == TokenType.COMMA) {
            commas.add(match(TokenType.COMMA));
            expNodes.add(Exp());
        }
        return new FuncRParamsNode(expNodes, commas);
    }

    private MulExpNode MulExp() {
        // MulExp -> UnaryExp { ('*' | '/' | '%') UnaryExp }
        List<UnaryExpNode> unaryExpNodes = new ArrayList<>();
        List<Token> operations = new ArrayList<>();
        unaryExpNodes.add(UnaryExp());
        while (tokens.get(index).getType() == TokenType.MULT || tokens.get(index).getType() == TokenType.DIV || tokens.get(index).getType() == TokenType.MOD) {
            if (tokens.get(index).getType() == TokenType.MULT) {
                operations.add(match(TokenType.MULT));
            } else if (tokens.get(index).getType() == TokenType.DIV) {
                operations.add(match(TokenType.DIV));
            } else {
                operations.add(match(TokenType.MOD));
            }
            unaryExpNodes.add(UnaryExp());
        }
        return new MulExpNode(unaryExpNodes, operations);
    }

    private AddExpNode AddExp() {
        // AddExp -> MulExp { ('+' | '−') MulExp }
        List<MulExpNode> mulExpNodes = new ArrayList<>();
        List<Token> operations = new ArrayList<>();
        mulExpNodes.add(MulExp());
        while (tokens.get(index).getType() == TokenType.PLUS || tokens.get(index).getType() == TokenType.MINU) {
            if (tokens.get(index).getType() == TokenType.PLUS) {
                operations.add(match(TokenType.PLUS));
            } else if (tokens.get(index).getType() == TokenType.MINU) {
                operations.add(match(TokenType.MINU));
            }
            mulExpNodes.add(MulExp());
        }
        return new AddExpNode(mulExpNodes, operations);
    }

    private RelExpNode RelExp() {
        // RelExp -> AddExp { ('<' | '>' | '<=' | '>=') AddExp }
        List<AddExpNode> addExpNodes = new ArrayList<>();
        List<Token> operations = new ArrayList<>();
        addExpNodes.add(AddExp());
        while (tokens.get(index).getType() == TokenType.LSS || tokens.get(index).getType() == TokenType.LEQ || tokens.get(index).getType() == TokenType.GRE || tokens.get(index).getType() == TokenType.GEQ) {
            if (tokens.get(index).getType() == TokenType.LSS) {
                operations.add(match(TokenType.LSS));
            } else if (tokens.get(index).getType() == TokenType.LEQ) {
                operations.add(match(TokenType.LEQ));
            } else if (tokens.get(index).getType() == TokenType.GRE) {
                operations.add(match(TokenType.GRE));
            } else {
                operations.add(match(TokenType.GEQ));
            }
            addExpNodes.add(AddExp());
        }
        return new RelExpNode(addExpNodes, operations);
    }

    private EqExpNode EqExp() {
        // EqExp -> RelExp { ('==' | '!=') RelExp }
        List<RelExpNode> relExpNodes = new ArrayList<>();
        List<Token> operations = new ArrayList<>();
        relExpNodes.add(RelExp());
        while (tokens.get(index).getType() == TokenType.EQL || tokens.get(index).getType() == TokenType.NEQ) {
            if (tokens.get(index).getType() == TokenType.EQL) {
                operations.add(match(TokenType.EQL));
            } else {
                operations.add(match(TokenType.NEQ));
            }
            relExpNodes.add(RelExp());
        }
        return new EqExpNode(relExpNodes, operations);
    }

    private LAndExpNode LAndExp() {
        // LAndExp -> EqExp { '&&' EqExp }
        List<EqExpNode> eqExpNodes = new ArrayList<>();
        List<Token> andTokens = new ArrayList<>();
        eqExpNodes.add(EqExp());
        while (tokens.get(index).getType() == TokenType.AND) {
            andTokens.add(match(TokenType.AND));
            eqExpNodes.add(EqExp());
        }
        return new LAndExpNode(eqExpNodes, andTokens);
    }

    private LOrExpNode LOrExp() {
        // LOrExp -> LAndExp { '||' LAndExp }
        List<LAndExpNode> lAndExpNodes = new ArrayList<>();
        List<Token> orTokens = new ArrayList<>();
        lAndExpNodes.add(LAndExp());
        while (tokens.get(index).getType() == TokenType.OR) {
            orTokens.add(match(TokenType.OR));
            lAndExpNodes.add(LAndExp());
        }
        return new LOrExpNode(lAndExpNodes, orTokens);
    }

    private ConstExpNode ConstExp() {
        // ConstExp -> AddExp
        return new ConstExpNode(AddExp());
    }

    private Token match(TokenType tokenType) {
        if (tokens.get(index).getType() == tokenType) {
            return tokens.get(index++);
        } else {
            if (tokenType == TokenType.SEMICN) {
                ErrorHandler.addError(new Error(tokens.get(index - 1).getLineNumber(), ErrorType.i));
                return new Token(TokenType.SEMICN, tokens.get(index - 1).getLineNumber(), ";");
            } else if (tokenType == TokenType.RPARENT) {
                ErrorHandler.addError(new Error(tokens.get(index - 1).getLineNumber(), ErrorType.j));
                return new Token(TokenType.RPARENT, tokens.get(index - 1).getLineNumber(), ")");
            } else if (tokenType == TokenType.RBRACK) {
                ErrorHandler.addError(new Error(tokens.get(index - 1).getLineNumber(), ErrorType.k));
                return new Token(TokenType.RBRACK, tokens.get(index - 1).getLineNumber(), "]");
            }
            return new Token(tokenType, tokens.get(index - 1).getLineNumber(), "");
        }
    }

    public void printParseAns() {
        compUnitNode.print();
    }
}
