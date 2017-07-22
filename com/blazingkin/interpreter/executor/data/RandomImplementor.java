package com.blazingkin.interpreter.executor.data;

import java.math.BigInteger;

import com.blazingkin.interpreter.executor.instruction.InstructionExecutor;
import com.blazingkin.interpreter.executor.lambda.LambdaFunction;
import com.blazingkin.interpreter.variables.Value;
import com.blazingkin.interpreter.variables.Variable;
import com.blazingkin.interpreter.variables.VariableTypes;

public class RandomImplementor implements InstructionExecutor, LambdaFunction {
	/*	Random
	 * Returns a random integer from 0-99
	 * 
	 */
	@Override
	public void run(String[] args) {
		Value v;
		if(args.length == 2){
			BigInteger range = Variable.getIntValue(Variable.getValue(args[1]));
			v = new Value(VariableTypes.Integer, BigInteger.valueOf((long)(Math.random()*range.intValue())));
		}else if (args.length == 3){
			BigInteger lowerBound = Variable.getIntValue(Variable.getValue(args[1]));
			BigInteger upperBound = Variable.getIntValue(Variable.getValue(args[2]));
			BigInteger range = upperBound.subtract(lowerBound);
			v = new Value(VariableTypes.Integer, lowerBound.add(BigInteger.valueOf((long)(Math.random()*range.intValue()))));
		}else{
			v = new Value(VariableTypes.Integer, BigInteger.valueOf((long)(Math.random()*100)));
		}
		Variable.setValue(args[0], v);
	}


	@Override
	public Value evaluate(String[] args) {
		if(args.length == 1){
			BigInteger range = Variable.getIntValue(Variable.getValue(args[0]));
			return new Value(VariableTypes.Integer, BigInteger.valueOf((long)(Math.random()*range.intValue())));
		}else if (args.length == 2){
			BigInteger lowerBound = Variable.getIntValue(Variable.getValue(args[0]));
			BigInteger upperBound = Variable.getIntValue(Variable.getValue(args[1]));
			BigInteger range = upperBound.subtract(lowerBound);
			return new Value(VariableTypes.Integer, lowerBound.add(BigInteger.valueOf((long)(Math.random()*range.intValue()))));
		}
		return new Value(VariableTypes.Integer, BigInteger.valueOf((long)(Math.random()*100)));
	}
}