package com.blazingkin.interpreter.executor;

import java.util.ArrayList;

public class Method implements RuntimeStackElement {
	
	public int UUID = Executor.getUUID();
	public Process parent;
	public int lineNumber;
	public String functionName;
	public boolean takesVariables = false;
	public boolean interuptable = true;
	public String[] variables = {};
	
	//initLine does not contain the : that makes it a function
	public Method(Process parent, int line, String initLine){
		this.parent = parent;
		lineNumber = line;
		String ln = initLine.split(":")[initLine.split(":").length - 1];
		functionName = ln.split("\\(")[0].trim();
		if (functionName.contains("^")){
			interuptable = false;
			functionName = functionName.replace("^", "");
		}
		if (ln.contains("(") && ln.contains(")")){
			takesVariables = true;
			String vars = ln.split("\\(")[ln.split("\\(").length-1].split("\\)")[0];
			String vNames[] = vars.split(",");
			variables = new String[vNames.length];
			for (int i = 0; i < vNames.length; i++){
				variables[i] = vNames[i].trim();
			}
		}
	}
	
	//Checks to see if this is a specific method object
	public boolean isItThis(String name, int parentID){
		return (functionName.equals(name) && (parentID == parent.UUID));
	}
	
	/**
	 * @param ar - The array to search through
	 * @param s - The name of the target method
	 * @return returns a method or null if it wasn't found
	 */
	public static Method contains(ArrayList<Method> ar, String s){
		for (int i = 0; i < ar.size(); i++){
			if (ar.get(i).functionName.equals(s)){
				return ar.get(i);
			}
				
		}
		return null;
	}

	@Override
	public void onBlockStart() {
		
	}

	@Override
	public void onBlockEnd() {
		if (!Executor.getCurrentProcess().lineReturns.empty()){
			Executor.setLine(Executor.getCurrentProcess().lineReturns.pop());
		}else{
			Executor.popStack();	// If there is nothing else in the current process to run, return to the previous process
		}
	}
	
}
