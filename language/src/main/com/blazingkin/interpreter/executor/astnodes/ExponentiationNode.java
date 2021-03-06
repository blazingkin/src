package com.blazingkin.interpreter.executor.astnodes;

import com.blazingkin.interpreter.BLZRuntimeException;
import com.blazingkin.interpreter.Interpreter;
import com.blazingkin.interpreter.expressionabstraction.ASTNode;
import com.blazingkin.interpreter.expressionabstraction.BinaryNode;
import com.blazingkin.interpreter.expressionabstraction.Operator;
import com.blazingkin.interpreter.variables.Context;
import com.blazingkin.interpreter.variables.Value;
import com.blazingkin.interpreter.variables.Variable;

public class ExponentiationNode extends BinaryNode {

	public ExponentiationNode(ASTNode[] args) {
		super(Operator.Exponentiation, args);
		if (args.length != 2){
			Interpreter.throwError("Exponentiation did not have 2 arguments");
		}
	}

	public boolean canModify() {
		return args[0].canModify() || args[1].canModify();
	}
	
	@Override
	public Value execute(Context con) throws BLZRuntimeException {
		return Variable.expValues(args[0].execute(con), args[1].execute(con));
	}

}
