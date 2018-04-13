package com.blazingkin.interpreter.executor.instruction;

import com.blazingkin.interpreter.executor.Executor;
import com.blazingkin.interpreter.expressionabstraction.ExpressionExecutor;
import com.blazingkin.interpreter.variables.Value;
import com.blazingkin.interpreter.variables.Context;

public abstract interface InstructionExecutorValue extends InstructionExecutor {
	public default Value run(String line, Context con){
		return run(ExpressionExecutor.parseExpression(line, con));
	}
	
	public abstract Value run(Value val);		//All of the Executors Implement this interface so that they can be referenced from an enum

}
