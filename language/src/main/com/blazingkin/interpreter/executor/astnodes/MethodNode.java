package com.blazingkin.interpreter.executor.astnodes;

import java.util.ArrayList;
import java.util.Optional;

import com.blazingkin.interpreter.BLZRuntimeException;
import com.blazingkin.interpreter.Interpreter;
import com.blazingkin.interpreter.executor.Executor;
import com.blazingkin.interpreter.executor.executionstack.RuntimeStack;
import com.blazingkin.interpreter.executor.sourcestructures.Process;
import com.blazingkin.interpreter.executor.sourcestructures.RegisteredLine;
import com.blazingkin.interpreter.expressionabstraction.ASTNode;
import com.blazingkin.interpreter.expressionabstraction.Operator;
import com.blazingkin.interpreter.parser.Either;
import com.blazingkin.interpreter.parser.ExpressionParser;
import com.blazingkin.interpreter.parser.ParseBlock;
import com.blazingkin.interpreter.parser.SourceLine;
import com.blazingkin.interpreter.parser.SyntaxException;
import com.blazingkin.interpreter.variables.Context;
import com.blazingkin.interpreter.variables.Value;
import com.blazingkin.interpreter.variables.Variable;

public class MethodNode extends ASTNode {

    String name;
    public String[] variables = {};
    public boolean takesVariables = false;
    public Process parent;
    public BlockNode body;

    public MethodNode(MethodNode other){
        this.parent = other.parent;
        this.name = other.name;
        this.variables = other.variables;
        this.takesVariables = other.takesVariables;
        this.body = other.body;
    }

    public MethodNode(String header, ArrayList<Either<SourceLine, ParseBlock>> body, Process parent) throws SyntaxException {
        this.parent = parent;
        String ln = header.replaceFirst(":", "");
		String[] nameAndArgs = ExpressionParser.parseBindingWithArguments(ln);
		name = nameAndArgs[0];
		if (nameAndArgs.length > 1){
			takesVariables = true;
			variables = new String[nameAndArgs.length - 1];
			for (int i = 0; i < variables.length; i++){
				variables[i] = nameAndArgs[i + 1];
			}
        }
        this.body = new BlockNode(body, true);
    }

    public Optional<Boolean> canModifyCache = Optional.empty();
    public boolean canModify() 
    {
        if (!canModifyCache.isPresent()) {
            canModifyCache = Optional.of(takesVariables && this.body.canModify());
        }
        return canModifyCache.get();
	}

    /* For initializing anonymous functions */
    public MethodNode(String[] argNames, ASTNode body, int lineNum){
        if (argNames.length != 0) {
            takesVariables = true;
            variables = argNames;
        }
        name = "λ";
        this.body = new BlockNode(true, RegisteredLine.build(body, lineNum));
    }

    public Value execute(Context c) throws BLZRuntimeException {
        Interpreter.throwError("Method node was executed without arguments");
        Value empty[] = {};
        return execute(c, empty, false);
    }

    public Value execute(Context c, Value[] values, boolean passByReference) throws BLZRuntimeException{
        boolean pushedParent = false;
        if (parent != null && (RuntimeStack.isEmpty() || RuntimeStack.getProcessStack().peek().UUID != parent.UUID)){
            pushedParent = true;
            RuntimeStack.push(parent);
        }
        Context methodContext;
        if (parent == null){
            methodContext = new Context();
        }else{
            methodContext = new Context(parent.processContext);
        }
        if (takesVariables){
            bindArguments(values, passByReference, methodContext);
        }
        try {
            Value result = body.execute(methodContext);
            Variable.killContext(methodContext);
            if(pushedParent){
                RuntimeStack.pop();
            }
            Executor.setReturnMode(false);
            return result;
        }catch(BLZRuntimeException exception) {
            if (exception.exceptionValue != null){
                throw exception;
            }
            String message = "In "+toString()+"\n"+exception.getMessage();
            if (pushedParent){
                String fileName = RuntimeStack.getProcessStack().peek().toString();
                message = "In " + fileName + "\n"+message;
                RuntimeStack.pop();
            }
            throw new BLZRuntimeException(message, exception.alreadyCaught);
        } catch(StackOverflowError err) {
            String message = "In "+toString()+"\nStack Overflow!";
            if (pushedParent){
                String fileName = RuntimeStack.getProcessStack().peek().toString();
                message = "In " + fileName + "\n"+message;
                RuntimeStack.pop();
            }
            throw new BLZRuntimeException(message);
        }
    }

	public String toString(){
		String args = "";
		for (String s : variables){
			args += s + ", ";
		}
		if (args.length() > 0) {
			args = args.substring(0, args.lastIndexOf(','));
		}
		return "<Method " + name + "(" + args + ")>";
	}

    protected void bindArguments(Value values[], boolean passByReference, Context methodContext){
        int variableCount = (variables.length > values.length?values.length:variables.length);
        if (passByReference){
            for (int i = 0; i < variableCount; i++){
                methodContext.setValueInPresent(variables[i], values[i]);
            }
        }else{
            for (int i = 0; i < variableCount; i++){
                methodContext.setValueInPresent(variables[i], (values[i]).clone());
            }
        }
        /* Bind variables that weren't passed to nil */
        for (int i = variableCount; i < variables.length; i++) {
            methodContext.setValueInPresent(variables[i], Value.nil());
        }
    }



    public Operator getOperator(){
        return Operator.functionCall;
    }

    public String getStoreName(){
        return name;
    }

    public ASTNode collapse(){
        return this;
    }

    public boolean canCollapse(){
        return false;
    }

}