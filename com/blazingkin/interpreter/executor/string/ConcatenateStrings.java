package com.blazingkin.interpreter.executor.string;

import com.blazingkin.interpreter.executor.InstructionExecutor;
import com.blazingkin.interpreter.variables.Value;
import com.blazingkin.interpreter.variables.Variable;
import com.blazingkin.interpreter.variables.VariableTypes;

public class ConcatenateStrings implements InstructionExecutor {
	
	public void run(String[] args){
		String s = "";
		for (int i = 1; i < args.length; i++){
			s = s+args[i];
			if (i != args.length-1){
				s = s + " ";
			}
		}
		String[] splits = s.split("\"");
		String g = "";
			for (int i = 0; i < splits.length; i++){
				if (i%2 == 0){
					g = g+Variable.parseString(splits[i].replace(" ", ""));	
				}else{
					g = g+(splits[i]);
				}
			}
		Variable.setValue(args[0], new Value(VariableTypes.String, g));
	}

}