package com.blazingkin.interpreter.executor.math;

import com.blazingkin.interpreter.Interpreter;
import com.blazingkin.interpreter.executor.instruction.InstructionExecutor;
import com.blazingkin.interpreter.executor.lambda.LambdaFunction;
import com.blazingkin.interpreter.variables.Value;
import com.blazingkin.interpreter.variables.Variable;
import com.blazingkin.interpreter.variables.VariableTypes;

@Deprecated
public class SubVars implements InstructionExecutor, LambdaFunction {
	/*	Subtract
	 * 	Subtracts two numbers and stores them in a third variable
	 */
	public void run(String[] args) {
		Value v1 = Variable.getValue(args[0]);
		Value v2 = Variable.getValue(args[1]);
		if (Variable.isValInt(v1) && Variable.isValInt(v2)){
			int i1 = Variable.getIntValue(v1);
			int i2 = Variable.getIntValue(v2);
			Variable.setValue(args[2], new Value(VariableTypes.Integer, i1-i2));
			return;
		}
		if ((v1.type == VariableTypes.Integer || v1.type == VariableTypes.Double)
				&& (v2.type == VariableTypes.Integer || v2.type == VariableTypes.Double)){
			double d1 = Variable.getDoubleVal(v1);
			double d2 = Variable.getDoubleVal(v2);
			Value v = new Value((v1.type == v2.type && v1.type == VariableTypes.Integer)?v1.type:VariableTypes.Double, d1-d2);
			Variable.setValue(args[2], v);
			return;
		}else{
			Interpreter.throwError("Invalid types for subtraction");
		}
	}

	@Override
	public Value evaluate(String[] args) {
		Value v1 = Variable.getValue(args[0]);
		Value v2 = Variable.getValue(args[1]);
		if (Variable.isValInt(v1) && Variable.isValInt(v2)){
			int i1 = Variable.getIntValue(v1);
			int i2 = Variable.getIntValue(v2);
			return new Value(VariableTypes.Integer, i1-i2);
		}else if ((Variable.isValDouble(v1) || Variable.isValInt(v1))
				&& (Variable.isValDouble(v2) || Variable.isValInt(v2))){
			double d1 = Variable.getDoubleVal(v1);
			double d2 = Variable.getDoubleVal(v2);
			return new Value(VariableTypes.Double, d1-d2);
		}
		Interpreter.throwError("Invalid arguments "+v1.value+" and "+v2.value+" passed to subtracting vars");
		return new Value(VariableTypes.Nil, null);
	}

}
