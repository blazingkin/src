package com.blazingkin.interpreter.executor.instruction;

import com.blazingkin.interpreter.executor.data.*;
import com.blazingkin.interpreter.executor.executionorder.*;
import com.blazingkin.interpreter.executor.input.*;
import com.blazingkin.interpreter.executor.listener.Listener;
import com.blazingkin.interpreter.executor.math.*;
import com.blazingkin.interpreter.executor.output.*;
import com.blazingkin.interpreter.executor.string.*;
import com.blazingkin.interpreter.executor.tensor.*;
import com.blazingkin.interpreter.executor.timing.Wait;

import in.blazingk.blz.packagemanager.ImportPackageInstruction;

@SuppressWarnings("deprecation")
public enum Instruction {
	INVALID("","INVALID", null),													// INVALID - Not for use
	ECHO("ECHO", "ECHO", new Echo()),												// Echo - prints a replacing string
	SAMELINEECHO("SECHO", "SAME LINE ECHO", new SameLineEcho()),
	STORE("SET", "STORE", new Set()),												// Store - Stores an int as a variable
	ADDVARIABLE("ADD", "ADD VARIABLES", new AddVars()),								// Add - adds two replacing strings and sets a variable to them
	SUBTRACTVARIABLE("SUB", "SUBTRACT VARIABLES", new SubVars()),					// Sub - subtract two replacing strings and sets a variable to them
	MULTIPLYVARIABLE("MUL", "MULTIPLE VARIABLES", new MultiplyVars()),				// Mul - multiply two replacing strings and sets a variable to them
	DIVIDEVARIABLE("DIV", "DIVIDE VARIABLES", new DivideVars()),					// Div - divide two replacing strings and sets a variable to them
	POWVARIABLE("POW", "EXPONENTIAL VARIABLES", new ExponentVars()),				// Pow - put one replacing string to the power of another replacing string and sets a variable to them
	NUMINPUT("NIN", "NUM INPUT", new NumInput()),									// Number input - inputs a string and parses to an int stores to a variable
	EXIT("EXT", "EXIT", new Exit()),												// Exit - ends the process
	MOVE("MOV", "MOVE", new MoveData()),											// Move data - copies a variable to another address
	LOGICALAND("AND", "LOGICAL AND", new LogicalAnd()),								// logical and - bitwise and, writes to a variable
	PUSH("PUSH", "PUSH", new Push()),												// push - push an integer onto the stack
	POP("POP", "POP", new Pop()),													// pop - pops an integer into a variable
	PEEK("PEEK", "PEEK", new Peek()),												// peek - peeks an integer into a variable
	RANDOM("RAND", "RANDOM", new RandomImplementor()),								// rand - puts a random number 0-99 into a variable
	STRINGINPUT("STRIN", "STRINGIN", new StringInput()),								// Input String - gets a string through input
	UNSIGN("UNS", "UNSIGN", new Unsign()),											// Unsign - Absolute values a variable
	MODULUS("MOD", "MODULUS", new ModVars()),										// Modulus - Gets the remainder of a difference
	RETURNJUMP("RJP", "RETURN JUMP", new JumpReturn()),								// Return Jump - Jumps, then returns after this at the relevant END
	CALL("CALL", "RETURN JUMP", new JumpReturn()),									// Alias for return jump
	END("END", "END STATEMENT", new End()),											// End - Ends a return jump statement
	CONCATENATE("CON", "CONCATENATE", new ConcatenateStrings()),					// Concatenate Strings - Concatenate Strings, removes "'s
	IF("IF", "IF STATEMENT", new IfBlock()),
	WAIT("WAIT", "WAIT", new Wait()),												//Wait - Waits a given time on this instruction
	ADDLISTENER("ADDLISTENER", "Add Event Listener", new Listener()),									//Add listener - Registers an event listener
	ADDPROCESS("ADDPROCESS", "Add Process", new ChangeProcess()),								//Add Process - Execute code from an external file
	RETURNPROCESS("RETPRO", "Return Process", new ReturnProcess()),								//Return Process - Return to the previous process
	ROUND("ROUND", "Round", new Round()),
	FLOOR("FLOOR", "Floor", new Floor()),
	CEILING("CEIL", "Ceiling", new Ceiling()),
	FILEINPUT("FILEIN", "File In", new FileInput()),
	FILEOUTPUT("FILEOUT", "File Out", new FileOutput()),
	STRINGLENGTH("SLEN", "String Length", new Length(true)),
	ARRAYLENGTH("ALEN", "Array Length", new Length(false)),
	SUBSTRING("SUBS", "Substring", new Substring()),
	Logarithm("LOG", "Logarithm", new Logarithm()),
	SIN("SIN", "Sine", new Trigonometry(TrigFunctions.SIN)),
	COS("COS", "Cosine", new Trigonometry(TrigFunctions.COS)),
	TAN("TAN", "Tangent", new Trigonometry(TrigFunctions.TAN)),
	CSC("CSC", "Cosecant", new Trigonometry(TrigFunctions.CSC)),
	SEC("SEC", "Secant", new Trigonometry(TrigFunctions.SEC)),
	COT("COT", "Cotangent", new Trigonometry(TrigFunctions.COT)),
	ARCSIN("ARCSIN", "Inverse Sine", new Trigonometry(TrigFunctions.ARCSIN)),
	ARCCOS("ARCCOS", "Inverse Cosine", new Trigonometry(TrigFunctions.ARCCOS)),
	ARCTAN("ARCTAN", "Inverse Tangent", new Trigonometry(TrigFunctions.ARCTAN)),
	FORLOOP("FOR", "For", new ForLoop()),
	WHILELOOP("WHILE", "While", new WhileLoop()),
	ENDLOOP("ENDLOOP", "End Loop", new LoopEnd()),
	INTTENSOR("TENSORI", "DECLARE INTEGER TENSOR", new IntTensor()),
	SETTENSORVALUE("TENSORSET", "SET TENSOR VALUE", new SetTensorValue()),
	GETTENSORVALUE("TENSORGET", "GET TENSOR VALUE", new GetTensorValue()),
	TOGGLELOGGING("BLZLOG", "TOGGLE LOGGING", new BLZLogging()),
	DEFINE("DEFINE", "DEFINE LAMBDA EXPRESSION", new Define(true)),
	LAMBDA("LAMBDA", "RETURN LAMBDA EXPRESSION", new Define(false)),
	BREAK("BREAK", "Break from loop", new Break()),
	CONTINUE("CONTINUE", "Continue in loop", new Continue()),
	RETURN("RETURN", "Return value", new ReturnValue()),
	INSTANTIATE("NEW", "INSTANTIATE OBJECT", new InitializeObject()),
	IMPORTPACKAGE("IMPORT", "IMPORT PACKAGE", new ImportPackageInstruction());
	
	
	private Instruction(final String ins, final String name, final InstructionExecutor executor){
		this.instruction = ins;
		this.name = name;
		this.executor = executor;	
	}
	
	public final InstructionExecutor executor;
	public final String instruction;
	public final String name;

}
