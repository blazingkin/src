package com.blazingkin.interpreter.variables;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashMap;

import com.blazingkin.interpreter.BLZRuntimeException;

public class Context {
	private Context parent;
	public int contextID;
	private static int maxDepth = 500;
	public HashMap<String, Value> variables = new HashMap<String, Value>();
	
	public Context(){
		parent = Variable.getGlobalContext();
		Context p = parent;
		int depth = 0;
		while (! (p == Variable.getGlobalContext()) && depth < maxDepth){
			p = p.parent;
			depth ++;
		}
		if (depth == maxDepth){
			parent = Variable.getGlobalContext();
		}
		contextID = getUID();
		contexts.add(this);
	}
	
	public Context(Context parent){
		contextID = getUID();
		this.parent = parent;
		contexts.add(this);
	}
	
	public int getID(){
		return contextID;
	}
	
	public Context getParentContext(){
		return parent;
	}
	
	public boolean hasValue(String s){
		return variables.containsKey(s);
	}
	

	public Value getValue(String s) throws BLZRuntimeException{
		if (hasValue(s)){
			return variables.get(s);
		}
		
		if (parent != null){
			return parent.getValue(s);
		}else{
			throw new BLZRuntimeException("Could not find a value for "+s );
		}
	}
	
	public boolean inContext(String storeName){
		if (variables.containsKey(storeName)){
			return true;
		}
		if (parent == null || this == parent){
			return false;
		}
		return parent.inContext(storeName);
	}
	
	public void setValue(String storeName, Value value){
		if (hasValue(storeName) || !inContext(storeName) ||
			this == Variable.getGlobalContext() || parent == null){
			variables.put(storeName, value);
		}else{
			parent.setValue(storeName, value);
		}
	}

	public void setValueInPresent(String storeName, Value value){
		variables.put(storeName, value);
	}
	
	
	private static int getUID(){
		return contextCounter++;
	}
	private static int contextCounter = 0;
	
	public int hashCode(){
		return contextID;
	}
	
	public static List<Context> contexts = Collections.synchronizedList(new ArrayList<Context>());
}