package com.blazingkin.interpreter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import com.blazingkin.interpreter.executor.Executor;
import com.blazingkin.interpreter.executor.executionstack.RuntimeStack;
import com.blazingkin.interpreter.executor.executionstack.RuntimeStackElement;
import com.blazingkin.interpreter.library.BlzEventHandler;
import com.blazingkin.interpreter.repl.REPL;
import com.blazingkin.interpreter.unittests.AllTestsSuite;
import com.blazingkin.interpreter.variables.SystemEnv;
import com.blazingkin.interpreter.variables.Variable;

public class Interpreter {
	public static boolean logging = true;
	
	public static void main(String args[]){
		try{
			switch(args.length){
			case 0:
				printHelp();
				System.exit(-1);
				break;
			default: 
				new Interpreter().run(args);
			break;
			}
		}catch(Exception e){throwError("Unhandled Exception Within Java\nErrorDump:"); e.printStackTrace();}
		
	}
	
	public static void printHelp(){
		System.out.println("How to use BLZ-OSPL:");
		System.out.println("The language home page is at http://blazingk.in/blz-ospl");
/*		System.out.println();
		System.out.println("Compile a pre-blz file (typically .pblz extension)");
		System.out.println("blz-ospl -c *INPATH* *OUTPATH*");
		This feature is deprecated, but will be recreated at a later date*/
		System.out.println();
		System.out.println("Execute a blz file (typically .blz extension)");
		System.out.println("blz-ospl *PATH*");
		System.out.println("The -m flag lets you specify a main function");
		System.out.println();
		System.out.println("Run in immediate mode");
		System.out.println("blz-ospl -i");
		System.out.println();
		System.out.println("Print version number");
		System.out.println("blz-ospl -v");
		System.out.println();
		System.out.println("See this help message");
		System.out.println("blz-ospl -h");
	}
	
	
	public void run(String args[]) throws FileNotFoundException{
		try{
			if (args.length == 0){	
				Interpreter.printHelp();
				System.exit(0);
			}
			if (args[0].charAt(0) == '-'){
				switch(args[0].charAt(1)){
					case 'h':
						Interpreter.printHelp();
						System.exit(0);
						break;
					case 'i':
						REPL.immediateModeLoop(System.in);
						break;
					case 'v':
						System.out.println("blz-ospl v"+Variable.getEnvVariable(SystemEnv.version).value);
						break;
					case 't':
						Result result = JUnitCore.runClasses(AllTestsSuite.class);
						for (Failure failure : result.getFailures()){
							System.out.println(failure.toString());
						}
						System.out.println(result.wasSuccessful() ? result.getRunCount() + " Tests Passed" : "Tests Failed");
						System.exit(result.wasSuccessful() ? 0 : 1);
						break;
				}
				System.exit(0);
			}
			String paths= args[0];
			File pths = new File(paths);
			
			/* If it can't be found, try adding the extension */
			if (!pths.exists() && !paths.endsWith(".blz")) {
				pths = new File(paths + ".blz");
			}
			
			int f = 1;
			List<String> rg = new LinkedList<String>();
			while (f < args.length){
				rg.add(args[f]);
				f++;
			}
			runExecutor(pths, rg);
		}catch(Exception e){
			e.printStackTrace();
			if (!Executor.getCurrentProcess().runningFromFile){
				throwError("Error, Executor was on line "+Executor.getLine()+" in a software environment");
			}else{
				throwError("Error, Executor was on line "+Executor.getLine()+ " in file: "+Executor.getRunningProcesses().peek().readingFrom.getAbsolutePath());
				}		
			}
	}
	
	
	//This is for when you want to run blz code from another program
	public static void executeCodeAsLibrary(String[] code, List<String> args, BlzEventHandler eventHandler) throws Exception{
		try{
			Executor.run(code, args, eventHandler);
		}catch(Exception e){
			if (!Executor.isCloseRequested()){
				throw e;
			}
		}
	}
	
	public static void terminate(){
		Executor.setCloseRequested(true);
	}
	
	public void runExecutor(File path, List<String> args) throws Exception{
		Executor.run(path, args);
	}
	
	public static void throwError(String error){
		thrownErrors.add(new Exception(error));
		if (Executor.isImmediateMode()){
			Executor.getEventHandler().err("There was an issue running your last command\n");
			Executor.getEventHandler().err("Type 'err' to see the error");
			return;
		}
		if (logging){
			if (!Executor.getRunningProcesses().isEmpty()){
				Stack<RuntimeStackElement> reverse = new Stack<RuntimeStackElement>();
				while (!RuntimeStack.runtimeStack.isEmpty()) {
					reverse.push(RuntimeStack.runtimeStack.pop());
				}
				System.err.println("Stack:");
				while (!reverse.isEmpty()) {
					RuntimeStackElement rse = reverse.pop();
					System.err.println((rse.getLineNum() == -1 ? "" : "Line " + rse.getLineNum()) + "\t" + rse.toString());
				}
				System.err.println("Error occurred on line: "+Executor.getLine());
			}
			System.err.println(error);
		}
		Executor.getEventHandler().exitProgram("An Error Occured");
	}

	
	public static Stack<Exception> thrownErrors = new Stack<Exception>();
}