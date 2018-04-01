package com.blazingkin.interpreter.executor.astnodes;

import com.blazingkin.interpreter.Interpreter;
import com.blazingkin.interpreter.expressionabstraction.ASTNode;
import com.blazingkin.interpreter.expressionabstraction.BinaryNode;
import com.blazingkin.interpreter.expressionabstraction.Operator;
import com.blazingkin.interpreter.variables.Context;
import com.blazingkin.interpreter.variables.Value;

public class NotEqualNode extends BinaryNode {

	public NotEqualNode(ASTNode[] args) {
		super(Operator.NotEqual, args);
		if (args.length != 2){
			Interpreter.throwError("Negative Comparison did not have 2 arguments");
		}
	}
	
	@Override
	public Value execute(Context con){
		return Value.bool(!args[0].execute(con).equals(args[1].execute(con)));
	}

	
	
}