package com.blazingkin.interpreter.executor.input;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.blazingkin.interpreter.executor.Executor;
import com.blazingkin.interpreter.executor.instruction.InstructionExecutor;
import com.blazingkin.interpreter.executor.lambda.LambdaFunction;
import com.blazingkin.interpreter.variables.Value;
import com.blazingkin.interpreter.variables.Variable;
import com.blazingkin.interpreter.variables.VariableTypes;

public class NumInput implements InstructionExecutor, LambdaFunction {
	/*	NumInput
	 * 	Gets a number as input (Only a number)
	 * 
	 */
	public void run(String[] vars){
		try{
			String str = Executor.getEventHandler().getInput();
			if (Variable.isInteger(str)){
				Variable.setValue(vars[0], new Value(VariableTypes.Integer,new BigInteger(str)));
			}else{
				Variable.setValue(vars[0], new Value(VariableTypes.Double,new BigDecimal(str)));
			}
		}catch(Exception e){
			System.err.println("Invalid Input, Please Input A Number");
			run(vars);
		}
	}

	@Override
	public Value evaluate(String[] args) {
		try{
			String str = Executor.getEventHandler().getInput();
			if (Variable.isInteger(str)){
				return new Value(VariableTypes.Integer, new BigInteger(str));
			}else{
				return new Value(VariableTypes.Double, new BigDecimal(str));
			}
		}catch(Exception e){
			System.err.println("Invalid Input, Please Input A Number");
			evaluate(args);
		}
		return evaluate(args);
	}
	
}
