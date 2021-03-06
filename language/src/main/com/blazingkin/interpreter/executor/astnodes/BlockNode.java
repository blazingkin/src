package com.blazingkin.interpreter.executor.astnodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.blazingkin.interpreter.BLZRuntimeException;
import com.blazingkin.interpreter.executor.Executor;
import com.blazingkin.interpreter.executor.sourcestructures.RegisteredLine;
import com.blazingkin.interpreter.expressionabstraction.ASTNode;
import com.blazingkin.interpreter.expressionabstraction.Operator;
import com.blazingkin.interpreter.expressionabstraction.ValueASTNode;
import com.blazingkin.interpreter.parser.Either;
import com.blazingkin.interpreter.parser.ExpressionParser;
import com.blazingkin.interpreter.parser.ForBlockParser;
import com.blazingkin.interpreter.parser.IfBlockParser;
import com.blazingkin.interpreter.parser.ParseBlock;
import com.blazingkin.interpreter.parser.SourceLine;
import com.blazingkin.interpreter.parser.SyntaxException;
import com.blazingkin.interpreter.parser.TryCatchBlockParser;
import com.blazingkin.interpreter.parser.WhileBlockParser;
import com.blazingkin.interpreter.variables.Context;
import com.blazingkin.interpreter.variables.Value;

public class BlockNode extends ASTNode {

    private static IfBlockParser ifParser = new IfBlockParser();
    private static ForBlockParser forParser = new ForBlockParser();
    private static WhileBlockParser whileParser = new WhileBlockParser();
    private static TryCatchBlockParser tryCatchParser = new TryCatchBlockParser();

    RegisteredLine body[];
    boolean shouldClearReturns;

    public BlockNode(List<Either<SourceLine, ParseBlock>> body) throws SyntaxException {
        this(body, false);
    }

    public BlockNode(boolean shouldClearReturns, RegisteredLine... body){
        this.body = body;
        this.shouldClearReturns = shouldClearReturns;
    }

    public boolean canModify() {
		for (RegisteredLine l : body) {
            if (l.canModify()) {
                return true;
            }
        }
        return false;
	}


    public BlockNode(List<Either<SourceLine, ParseBlock>> body, boolean shouldClearReturns) throws SyntaxException {
        ArrayList<RegisteredLine> lines = new ArrayList<RegisteredLine>();
        for (Either<SourceLine, ParseBlock> line : body){
            if (line.isLeft()){
                /* It is some string, parse it to a RegisteredLine */
                Optional<RegisteredLine> parsed = ExpressionParser.parseLine(line.getLeft().get());
                if (parsed.isPresent()){
                    lines.add(parsed.get());
                }
            } else {
                /* It is some block, parse it to a blocknode */
                ParseBlock block = line.getRight().get();
                if (ifParser.shouldParse(block.getHeader())){
                    lines.add(RegisteredLine.build(ifParser.parseBlock(block), block.lineNumber));
                }else if (forParser.shouldParse(block.getHeader())){
                    lines.add(RegisteredLine.build(forParser.parseBlock(block), block.lineNumber));
                }else if (whileParser.shouldParse(block.getHeader())){
                    lines.add(RegisteredLine.build(whileParser.parseBlock(block), block.lineNumber));
                }else if (tryCatchParser.shouldParse(block.getHeader())){
                    lines.add(RegisteredLine.build(tryCatchParser.parseBlock(block), block.lineNumber));
                }
            }
        }
        this.body = new RegisteredLine[lines.size()];
        this.shouldClearReturns = shouldClearReturns;
        lines.toArray(this.body);
        if (lines.isEmpty()){
            this.body = new RegisteredLine[1];
            this.body[0] = RegisteredLine.build(new ValueASTNode(Value.nil()), -1);
        }
    }

    public Value execute(Context c) throws BLZRuntimeException{
        for (int i = 0; i < body.length - 1; i++){
            body[i].run(c);
            if (Executor.shouldBlockBreak()){
                return Executor.getReturnBuffer();
            }
        }
        Value result = body[body.length - 1].run(c);
        return result;
    }

    public String getStoreName(){
        return "";
    }

    public Operator getOperator(){
        return null;
    }

    public boolean canCollapse(){
        return false;
    }

    public ASTNode collapse(){
        return this;
    }

}