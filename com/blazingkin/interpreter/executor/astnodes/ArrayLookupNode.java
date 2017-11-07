package com.blazingkin.interpreter.executor.astnodes;

import java.math.BigInteger;

import com.blazingkin.interpreter.Interpreter;
import com.blazingkin.interpreter.executor.Executor;
import com.blazingkin.interpreter.expressionabstraction.ASTNode;
import com.blazingkin.interpreter.expressionabstraction.BinaryNode;
import com.blazingkin.interpreter.expressionabstraction.Operator;
import com.blazingkin.interpreter.variables.Context;
import com.blazingkin.interpreter.variables.Value;
import com.blazingkin.interpreter.variables.Variable;
import com.blazingkin.interpreter.variables.VariableTypes;

public class ArrayLookupNode extends BinaryNode {

	public ArrayLookupNode(ASTNode[] args) {
		super(Operator.arrayLookup, args);
		if (args.length != 2){
			Interpreter.throwError("Array Lookup did not have 2 arguments.");
		}
	}
	
	@Override
	public Value execute(Context con){
		if (args[0].getStoreName() == null){
			Value arr = args[0].execute(con);
			if (arr.type == VariableTypes.String){ 
				int index = Variable.getIntValue(args[1].execute(con)).intValue();
				String s = (String) arr.value;
				return new Value(VariableTypes.String, s.substring(index, index+1));
			}
			if (arr.type != VariableTypes.Array){
				Interpreter.throwError("Did not know how to access "+args[0]+" as an array.");
			}
			BigInteger index = Variable.getIntValue(args[1].execute(con));
			return Variable.getValueOfArray(arr, index);
		}
		String name = args[0].getStoreName();
		VariableTypes type = Variable.typeOf(name, con);
		if (type == VariableTypes.Array){
			BigInteger index = Variable.getIntValue(args[1].execute(con));
			return Variable.getValueOfArray(name, index, con);
		}else if (type == VariableTypes.String){ 
			int index = Variable.getIntValue(args[1].execute(con)).intValue();
			String s = (String) Variable.getValue(name).value;
			return new Value(VariableTypes.String, s.substring(index, index+1));
		}else{ /* Assume it is a hash */
			Value key = args[1].execute(con);
			return Variable.getValueOfHash(name, key, con);
		}
	}

}
