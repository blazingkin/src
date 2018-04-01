package com.blazingkin.interpreter.executor.data;

import com.blazingkin.interpreter.executor.Executor;
import com.blazingkin.interpreter.executor.executionstack.RuntimeStack;
import com.blazingkin.interpreter.executor.instruction.InstructionExecutor;
import com.blazingkin.interpreter.executor.sourcestructures.Constructor;
import com.blazingkin.interpreter.executor.sourcestructures.Method;
import com.blazingkin.interpreter.variables.BLZObject;
import com.blazingkin.interpreter.variables.Value;
import com.blazingkin.interpreter.variables.Variable;
import com.blazingkin.interpreter.variables.VariableTypes;

public class InitializeObject implements InstructionExecutor {

	@Override
	public Value run(String line) {
		String[] args = line.split(" ");
		if (args.length == 1){
			Variable.setValue(line, Value.obj(new BLZObject()));
			return Variable.getVariableValue(line);
		}
		Constructor constructor = Executor.getConstructor(args[1]);
		BLZObject newObj = new BLZObject();
		Variable.setValue(args[0], new Value(VariableTypes.Object, newObj));
		setReferences(constructor, newObj);
		int startLine = Executor.getLine();
		Executor.setLine(constructor.getLineNum());
		
		RuntimeStack.pushContext(newObj.objectContext);
			int depth = RuntimeStack.runtimeStack.size();
			RuntimeStack.push(constructor);
			while (RuntimeStack.runtimeStack.size() > depth){
				Executor.executeCurrentLine();
			}
		RuntimeStack.popContext();
		
		Executor.setLine(startLine);
		return Variable.getVariableValue(args[0]);
	}

	/* Set all the 'this' references 
	 * as well as global function references */
	private void setReferences(Constructor constructor, BLZObject newObj){
		Variable.setValue(constructor.getName(), Value.obj(newObj), newObj.objectContext);
		Variable.setValue("this", Value.obj(newObj), newObj.objectContext);
		for (Method m : constructor.getParent().methods){
			Variable.setValue(m.functionName, new Value(VariableTypes.Method, m), newObj.objectContext);
		}
		for (Method m : constructor.getParent().importedMethods){
			Variable.setValue(m.functionName, new Value(VariableTypes.Method, m), newObj.objectContext);
		}
	}
	
}