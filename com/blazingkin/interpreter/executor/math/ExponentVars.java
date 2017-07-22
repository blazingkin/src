package com.blazingkin.interpreter.executor.math;

import java.math.BigDecimal;

import org.nevec.rjm.BigDecimalMath;

import com.blazingkin.interpreter.Interpreter;
import com.blazingkin.interpreter.executor.instruction.InstructionExecutor;
import com.blazingkin.interpreter.executor.lambda.LambdaFunction;
import com.blazingkin.interpreter.variables.Value;
import com.blazingkin.interpreter.variables.Variable;
import com.blazingkin.interpreter.variables.VariableTypes;

@Deprecated
public class ExponentVars implements InstructionExecutor, LambdaFunction {

	public void run(String args[]){
		Value v1 = Variable.getValue(args[0]);
		Value v2 = Variable.getValue(args[1]);
		if ((v1.type == VariableTypes.Integer || v1.type == VariableTypes.Double)
				&& (v2.type == VariableTypes.Integer || v2.type == VariableTypes.Double)){
			BigDecimal d1 = Variable.getDoubleVal(v1);
			BigDecimal d2 = Variable.getDoubleVal(v2);
			Value v = new Value((v1.type == v2.type && v1.type == VariableTypes.Integer)?v1.type:VariableTypes.Double, BigDecimalMath.pow(d1, d2));
			Variable.setValue(args[2], v);
			return;
		}else{
			Interpreter.throwError("Invalid types for exponentiation");
		}

	}

	@Override
	public Value evaluate(String[] args) {
		Value v1 = Variable.getValue(args[0]);
		Value v2 = Variable.getValue(args[1]);
		if ((v1.type == VariableTypes.Integer || v1.type == VariableTypes.Double)
				&& (v2.type == VariableTypes.Integer || v2.type == VariableTypes.Double)){
			BigDecimal d1 = Variable.getDoubleVal(v1);
			BigDecimal d2 = Variable.getDoubleVal(v2);
			return new Value((v1.type == v2.type && v1.type == VariableTypes.Integer)?v1.type:VariableTypes.Double, BigDecimalMath.pow(d1, d2));
		}else{
			Interpreter.throwError("Invalid types for exponentiation");
		}
		return new Value(VariableTypes.Nil, null);
	}
	
}
