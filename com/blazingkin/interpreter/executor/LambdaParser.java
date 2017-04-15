package com.blazingkin.interpreter.executor;

public class LambdaParser {

	public static LambdaExpression parseLambdaExpression(String lam){
		if (lam.charAt(0) == '(' && lam.charAt(lam.length()-1) == ')'){
			lam = lam.substring(1, lam.length()-1);
		}
		String[] splits = Executor.parseExpressions(lam);
		String name = splits[0];
		
		String[] args = new String[splits.length-1];
		for (int i = 1; i < splits.length; i++){
			args[i-1] = splits[i];
		}
		for (Instruction in : Instruction.values()){
			if (in.instruction.equals(name) && in.executor instanceof LambdaFunction){
				return new LambdaExpression((LambdaFunction) in.executor, args);
			}
		}
		return LambdaRegistrar.getLambdaExpression(name, args);
	}
	
}
