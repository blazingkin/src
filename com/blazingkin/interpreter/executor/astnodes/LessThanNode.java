package com.blazingkin.interpreter.executor.astnodes;

import java.math.BigDecimal;

import com.blazingkin.interpreter.Interpreter;
import com.blazingkin.interpreter.expressionabstraction.ASTNode;
import com.blazingkin.interpreter.expressionabstraction.BinaryNode;
import com.blazingkin.interpreter.expressionabstraction.Operator;
import com.blazingkin.interpreter.variables.Context;
import com.blazingkin.interpreter.variables.Value;
import com.blazingkin.interpreter.variables.Variable;

public class LessThanNode extends BinaryNode {

	public LessThanNode(ASTNode[] args) {
		super(Operator.LessThan, args);
		if (args.length != 2){
			Interpreter.throwError("Less Than did not have 2 arguments");
		}
	}
	
	@Override
	public Value execute(Context con){
		Value v1 = args[0].execute(con);
		Value v2 = args[1].execute(con);
		if (!Variable.isDecimalValue(v1) || !Variable.isDecimalValue(v2)){
			Interpreter.throwError("One of "+v1+" or "+v2+" is not a decimal value");
		}
		BigDecimal d1 = Variable.getDoubleVal(v1);
		BigDecimal d2 = Variable.getDoubleVal(v2);
		return Value.bool(d1.compareTo(d2) < 0);
	}

}
