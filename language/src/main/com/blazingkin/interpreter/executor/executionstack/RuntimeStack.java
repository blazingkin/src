package com.blazingkin.interpreter.executor.executionstack;

import java.util.ArrayDeque;

import com.blazingkin.interpreter.BLZRuntimeException;
import com.blazingkin.interpreter.executor.sourcestructures.Process;

public class RuntimeStack {

	public static ThreadLocal<ArrayDeque<Process>> threadProcessStack = new ThreadLocal<ArrayDeque<Process>>() {
		@Override
		protected ArrayDeque<Process> initialValue() {
			return new ArrayDeque<Process>();
		}
	};


	
	public static void push(Process process) throws BLZRuntimeException{
		threadProcessStack.get().push(process);
		process.onBlockStart();
	}

	public static void pushWithoutExecuting(Process process){
		if (process == null){
			return;
		}
		threadProcessStack.get().push(process);
	}
	
	public static void pop(){
		threadProcessStack.get().pop();
	}

	public static boolean isEmpty() {
		return threadProcessStack.get().isEmpty();
	}

	public static ArrayDeque<Process> getProcessStack() {
		return threadProcessStack.get();
	}
	
	public static void cleanup(){
		threadProcessStack.get().clear();
	}
}
