package com.blazingkin.interpreter.executor.instruction;

import com.blazingkin.interpreter.executor.Executor;
import com.blazingkin.interpreter.expressionabstraction.ExpressionExecutor;
import com.blazingkin.interpreter.expressionabstraction.ExpressionParser;
import com.blazingkin.interpreter.variables.Value;

public interface InstructionExecutorCommaDelimited extends InstructionExecutor {

	public default Value run(String line){
		return run(ExpressionExecutor.extractCommaDelimits(ExpressionParser.parseExpression(line), Executor.getCurrentContext()));
	}
	
	public abstract Value run(Value[] args);
	
}
