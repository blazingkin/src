package com.blazingkin.interpreter.executor.astnodes;

import com.blazingkin.interpreter.Interpreter;
import com.blazingkin.interpreter.expressionabstraction.ASTNode;
import com.blazingkin.interpreter.expressionabstraction.BinaryNode;
import com.blazingkin.interpreter.expressionabstraction.Operator;
import com.blazingkin.interpreter.variables.Context;
import com.blazingkin.interpreter.variables.Value;
import com.blazingkin.interpreter.variables.Variable;

public class DivisionNode extends BinaryNode {

	public DivisionNode(ASTNode[] args) {
		super(Operator.Division, args);
		if (args.length != 2){
			Interpreter.throwError("Division did not have 2 arguments");
		}
	}

	@Override
	public Value execute(Context con){
		return Variable.divVals(args[0].execute(con), args[1].execute(con));
	}
	
}
