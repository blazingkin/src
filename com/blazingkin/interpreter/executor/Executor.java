package com.blazingkin.interpreter.executor;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import com.blazingkin.interpreter.Interpreter;
import com.blazingkin.interpreter.executor.executionorder.LoopWrapper;
import com.blazingkin.interpreter.executor.instruction.Instruction;
import com.blazingkin.interpreter.executor.instruction.InstructionType;
import com.blazingkin.interpreter.executor.lambda.LambdaParser;
import com.blazingkin.interpreter.executor.lambda.LambdaRegistrar;
import com.blazingkin.interpreter.executor.listener.Event;
import com.blazingkin.interpreter.expressionabstraction.ExpressionExecutor;
import com.blazingkin.interpreter.library.BlzEventHandler;
import com.blazingkin.interpreter.library.StandAloneEventHandler;
import com.blazingkin.interpreter.variables.Context;
import com.blazingkin.interpreter.variables.SystemEnv;
import com.blazingkin.interpreter.variables.Value;
import com.blazingkin.interpreter.variables.Variable;
import com.blazingkin.interpreter.variables.VariableTypes;

public class Executor {

	// Instance objects
	private static BlzEventHandler eventHandler = new StandAloneEventHandler();
	private static ArrayList<Method> methods = new ArrayList<Method>();	// List of all functions within their respective processes
	private static String startingMethod = "main";
	private static ArrayList<Integer> UUIDsUsed = new ArrayList<Integer>();
	private static ArrayList<Event> eventsToBeHandled = new ArrayList<Event>();

	//State Variables
	private static long timeStarted = 0;
	private static int frames = 0;	
	private static boolean immediateMode = false;
	private static boolean closeRequested = false;
	private static boolean breakMode = false;
	private static Value returnBuffer = new Value(VariableTypes.Nil, null);
	
	// Stacks
	private static Stack<RuntimeStackElement> runtimeStack = new Stack<RuntimeStackElement>();
	private static Stack<Process> runningProcesses = new Stack<Process>();	//File Stack
	private static Stack<Method> runningMethods = new Stack<Method>();		//Method Stack
	private static Stack<Context> contextStack = new Stack<Context>();	//Context Stack
	private static Stack<LoopWrapper> loopStack = new Stack<LoopWrapper>();	//Loops
	private static Stack<Integer> processLineStack = new Stack<Integer>();
	
	// This is the main loop
	public static void codeLoop() throws Exception{
		while (!runningProcesses.isEmpty()){			// while we have a thing to do, we will continue to execute
			for (setLine(getStartOfMain());getLine() < getCurrentProcess().getSize();){
				executeCurrentLine();
			}
			popStack();
		}
		cleanup();
	}
	
	public static void executeCurrentLine(){
		Process currentProcess = getCurrentProcess();
		if (currentProcess.isRegistered(getLine())){	// If we've already registered this line, we can just run it
			RegisteredLine line = currentProcess.getRegisteredLine(getLine());
			setLine(getLine()+1);
			line.run();
		}
		else{
			String line = currentProcess.getLine(getLine());
			if (line.trim().length() == 0){
				setLine(getLine()+1);
				return;
			}
			if (line.charAt(0) == '('){
				LambdaParser.parseLambdaExpression(line).getValue();
				setLine(getLine()+1);
				return;
			}
			String split[] = line.split(" ");
			if (split[0].length() > 0 && split[0].substring(0,1).equals(":")){
				Method nM = new Method(currentProcess,getLine(), split[0].substring(1));
				getMethods().add(nM);
				pushToRuntimeStack(nM);
				setLine(getLine()+1);
				return;
			}
			if (split.length == 1 && split[0].equals("")){
				setLine(getLine()+1);
				return;
			}
			setLine(getLine()+1);
			ExpressionExecutor.parseExpression(line);	// If it hasn't been anything so far it must be a simple expression
		}
		if (getEventsToBeHandled().size() > 0 && getCurrentMethod().interuptable){
			currentProcess.lineReturns.add(getLine()+1);
			Executor.executeMethod(getEventsToBeHandled().get(0).method, Variable.getValuesFromList(getEventsToBeHandled().get(0).arguments));
			getEventsToBeHandled().remove(0);
		}
		if (isCloseRequested()){
			getEventHandler().exitProgram("Close was requested");
			cleanup();
			return;
		}
	}
	
	public static Value functionCall(Method m, Value[] values){
		int runtimeStackDepth = runtimeStack.size();
		if (m.parent != getCurrentProcess()){
			pushToRuntimeStack(m.parent);
		}else{
			getCurrentProcess().lineReturns.add(getLine());
		}
		pushToRuntimeStack(m);
		if (m.takesVariables){
			for (int i = 0; i < (m.variables.length > values.length?values.length:m.variables.length); i++){
				Variable.setValue(m.variables[i], values[i]);
			}
		}
		setLine(m.lineNumber);
		while (runtimeStack.size() > runtimeStackDepth){
			executeCurrentLine();
		}
		return returnBuffer;
	}
	
	private static int getStartOfMain(){
		Method start = getMethodInCurrentProcess(startingMethod);
		if (start != null){
			return start.lineNumber;
		}else{
			return 0;
		}
	}
	
	public static Stack<Integer> getProcessLineStack(){
		return processLineStack;
	}
	
	public static Context getCurrentContext(){
		if (contextStack.isEmpty()){
			return Variable.getGlobalContext();
		}
		return contextStack.peek();
	}
	
	public static void pushContext(Context con){
		contextStack.push(con);
	}
	
	public static void executeMethod(Method m){
		pushToRuntimeStack(m);
		setLine(m.lineNumber);
	}
	
	public static void executeMethod(Method m, Value[] values){
		executeMethod(m);
		if (m.takesVariables){
			for (int i = 0; i < (m.variables.length > values.length?values.length:m.variables.length); i++){
				Variable.setValue(m.variables[i], values[i]);
			}
		}
	}
	
	//Run Executor when running from file
	public static void run(File runFile, List<String> args) throws Exception{			// runs the executor
		for (int i = 0; i < args.size(); i+=2){
			String s = args.get(i);
			if (s.substring(0,2).equals("-m")){			// denotation for indicating a starting method
				startingMethod = args.get(i+1);
			}
		}
		pushToRuntimeStack(new Process(runFile));		// puts the file passed to us as the current process
		
		setEventHandler(new StandAloneEventHandler());
		codeLoop();
			
	}
	
	public static void run(ArrayList<String> code, List<String> args, BlzEventHandler handler) throws Exception{
		for (int i = 0; i < args.size(); i+=2){
			String s = args.get(i);
			if (s.substring(0,2).equals("-m")){			// denotation for indicating a starting method
				startingMethod = args.get(i+1);
			}
		}
		pushToRuntimeStack(new Process(code));
		if (startingMethod != null){
			if (!(Method.contains(getMethods(), startingMethod) == null)){
				pushToRuntimeStack(Method.contains(getMethods(), startingMethod));
				Executor.setLine(getCurrentMethod().lineNumber+1);		//if there is a starting method and we can find it, set the line number to it
			}
		}
		setEventHandler(handler);
		codeLoop();
	}
	
	//This cleans the execution environment so that another BLZ program can be run without restarting the Java program
	public static void cleanup(){
		runningProcesses = new Stack<Process>();
		runningMethods = new Stack<Method>();
		contextStack = new Stack<Context>();
		setLoopStack(new Stack<LoopWrapper>());
		setEventHandler(null);
		UUIDsUsed = new ArrayList<Integer>();
		setTimeStarted(0);
		setFrames(0);
		setEventsToBeHandled(new ArrayList<Event>());
		Variable.clearVariables();
		setMethods(new ArrayList<Method>());
		setCloseRequested(false);
		startingMethod = "";
	}
	
	public static void immediateModeLoop(InputStream is){
		System.out.println("blz-ospl "+Variable.getEnvVariable(SystemEnv.version).value +" running in immediate mode:");
		System.out.println("Type 'exit' to exit");
		String in = "";
		Scanner sc = new Scanner(is);
		immediateMode = true;
		Interpreter.thrownErrors.add(new Exception("There have been no exceptions"));
		try{
			do{
				try{
					in = sc.nextLine();
					if (in.equals("err")){
						Interpreter.thrownErrors.peek().printStackTrace();
						continue;
					}
					if (in.equals("exit") || in.equals("quit")){
						break;
					}
					if (in.equals("")){
						continue;
					}
					System.out.println(ExpressionExecutor.parseExpression(in));
				}catch(Exception e){
					Interpreter.thrownErrors.add(e);
					System.err.println("There was an issue running your last command");
					System.err.println("Type 'err' to see the error");
				}
			}while (in.toLowerCase() != "exit");
		}finally{
			sc.close();
		}
	}
	
	//Lots of getters / setters below this point
	public static boolean doesMethodExist(String name){
		return (InstructionType.getInstructionType(name) != Instruction.INVALID) || LambdaRegistrar.isRegisteredLambdaExpression(name);
	}
	
	public static int getUUID(){
		int id;
		do{
			id = (int) (Math.random() * Integer.MAX_VALUE);
		}while(UUIDsUsed.contains(id));
		UUIDsUsed.add(id);
		return id;
	}
	
	public static Stack<LoopWrapper> getLoopStack() {
		return loopStack;
	}
	
	public static void setLoopStack(Stack<LoopWrapper> loopStack) {
		Executor.loopStack = loopStack;
	}
	
	public static boolean isImmediateMode(){
		return immediateMode;
	}
	
	public static BlzEventHandler getEventHandler() {
		return eventHandler;
	}
	
	public static void setEventHandler(BlzEventHandler eventHandler) {
		Executor.eventHandler = eventHandler;
	}
	
	public static ArrayList<Method> getMethods() {
		return methods;
	}
	
	public static void setMethods(ArrayList<Method> methods) {
		Executor.methods = methods;
	}
	
	public static boolean isCloseRequested() {
		return closeRequested;
	}
	
	public static void setCloseRequested(boolean closeRequested) {
		Executor.closeRequested = closeRequested;
	}
	
	public static int getFrames() {
		return frames;
	}
	
	public static void setFrames(int frames) {
		Executor.frames = frames;
	}
	
	public static long getTimeStarted() {
		return timeStarted;
	}
	
	public static void setTimeStarted(long timeStarted) {
		Executor.timeStarted = timeStarted;
	}
	
	public static ArrayList<Event> getEventsToBeHandled() {
		return eventsToBeHandled;
	}
	
	public static void setEventsToBeHandled(ArrayList<Event> eventsToBeHandled) {
		Executor.eventsToBeHandled = eventsToBeHandled;
	}
	
	public static void setLine(int num){				// Sets line within the current process
		Variable.setGlobalValue("*pc", new Value(VariableTypes.Integer, num));
	}
	
	public static int getLine(){
		return (int) Variable.getGlobalValue("*pc").value;
	}
	
	public static Stack<Process> getRunningProcesses(){
		return runningProcesses;
	}
	
	public static Process getCurrentProcess(){
		try{
		return runningProcesses.peek();
		}catch(Exception e){
			return null;
		}
	}
	
	public static Method getCurrentMethod(){
		try{
		return runningMethods.peek();
		}catch(Exception e){
			return null;
		}
	}
	
	public static Method getMethodInCurrentProcess(String methodName){
		for (int i = 0; i < getMethods().size(); i++){
			if (getMethods().get(i).isItThis(methodName, getCurrentProcess().UUID)){
				return getMethods().get(i);
			}
		}
		return null;
	}
	
	public static Method getMethod(String methodName, int processID){
		for (int i = 0; i < getMethods().size(); i++){
			if (getMethods().get(i).isItThis(methodName, processID)){
				return getMethods().get(i);
			}
		}
		return null;	
	}
	
	public static void addProcess(Process p){
		processLineStack.push(getLine());
		pushToRuntimeStack(p);
	}
	
	public static void pushToRuntimeStack(RuntimeStackElement se){
		runtimeStack.push(se);
		if (se instanceof LoopWrapper){
			loopStack.push((LoopWrapper)se);
		}else if (se instanceof Process){
			runningProcesses.push((Process) se);
			contextStack.push(new Context());
		}else if (se instanceof Method){
			runningMethods.push((Method) se);
			contextStack.push(new Context());
		}
		se.onBlockStart();
	}
	
	public static void popStack(){									// This is used to return to the previous process or function
		RuntimeStackElement rse = runtimeStack.pop();
		if (rse instanceof LoopWrapper){
			loopStack.pop();
		}else if(rse instanceof Process){
			runningProcesses.pop();
			Variable.killContext(contextStack.pop());
		}else if(rse instanceof Method){
			runningMethods.pop();
			Variable.killContext(contextStack.pop());
		}
		rse.onBlockEnd();
		if (runtimeStack.empty()){
			Executor.setCloseRequested(true);
			getEventHandler().exitProgram("Reached end of program");
			return;
		}
	}
	
	public static Stack<RuntimeStackElement> getRuntimeStack(){
		return runtimeStack;
	}
	
	public static Stack<Method> getMethodStack(){
		return runningMethods;
	}
	
	public static int getCurrentBlockEnd(){
		return getCurrentProcess().blockArcs.get(getLine()).end;
	}
	
	public static int getCurrentBlockStart(){
		return getCurrentProcess().blockArcs.get(getLine()).start;
	}
	
	//Passes a whole expression
	public static String[] parseExpressions(String exp){
		ArrayList<String> expressions = new ArrayList<String>();
		int start = 0;
		int parensCount = 0;
		String buildingString = "";
		boolean inQuotes = false;
		for (int i = 0; i < exp.length(); i++){
			if (exp.charAt(i) == '(' && !inQuotes){
				if (parensCount == 0){
					start = i;
					if (!buildingString.trim().equals("")){
						expressions.add(buildingString.trim());
						buildingString = "";
					}
				}
				parensCount++;
			}
			

			if (parensCount == 0){
				if (exp.charAt(i) == '\"'){
					if (inQuotes){
						expressions.add(buildingString+"\"");
						buildingString = "";
						inQuotes = !inQuotes;
						continue;
					}
					buildingString = "";
					inQuotes = !inQuotes;
				}
				if (inQuotes){
					buildingString += exp.charAt(i);
					continue;
				}
				if (exp.charAt(i) == ' ' && !buildingString.trim().equals("")){
					expressions.add(buildingString.trim());
					buildingString = "";
					continue;
				}
				if (exp.charAt(i) != '"'){
					buildingString += exp.charAt(i);
				}
			}
			
			if (exp.charAt(i) == ')' && !inQuotes){
				parensCount--;
				if (parensCount == 0){
					expressions.add(exp.substring(start, i+1));
				}
			}
		}
		if (parensCount != 0){
			Interpreter.throwError("Unmatched parens on lambda expression: "+exp);
		}else if(!buildingString.trim().equals("")){
			expressions.add(buildingString.trim());
		}
		String[] express = new String[expressions.size()];
		for (int i = 0; i < express.length; i++){
			express[i] = expressions.get(i);
		}
		return express;
	}

	public static boolean isBreakMode() {
		return breakMode;
	}

	public static void setBreakMode(boolean breakMode) {
		Executor.breakMode = breakMode;
	}
	
	public static void setReturnBuffer(Value v){
		returnBuffer = v;
	}
	
}
