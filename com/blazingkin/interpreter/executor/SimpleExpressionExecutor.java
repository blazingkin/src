package com.blazingkin.interpreter.executor;

import java.math.BigDecimal;
import java.util.regex.Matcher;

import static com.blazingkin.interpreter.executor.SimpleExpressionParser.parseExpression;
import com.blazingkin.interpreter.Interpreter;
import com.blazingkin.interpreter.variables.Value;
import com.blazingkin.interpreter.variables.Variable;
import com.blazingkin.interpreter.variables.VariableTypes;

@SuppressWarnings("deprecation")
public class SimpleExpressionExecutor {
	
	

	public static double EPSILON = 1E-8; 
	

	public static Value evaluate(SimpleExpression type, String expr){
		switch(type){
		case addition:
		{	String[] splits = expr.split(type.syntax.pattern());
			if (splits.length < 2){
				Interpreter.throwError("Not enough arguments for addition");
			}
			Value accumulator = parseExpression(splits[0]);
			for (int i = 1; i < splits.length; i++){
				accumulator = Variable.addValues(accumulator, parseExpression(splits[i]));
			}
			return accumulator;
		}
		case assignment:
		{	
			String[] splits = expr.split(type.syntax.pattern());
			if (splits.length != 2){
				Interpreter.throwError("Unable to parse assignment operation: "+expr);
			}
			Value right = parseExpression(splits[1]);
			Variable.setValue(splits[0].trim(), right);
			return right;
		}
		case comparison:
		{	String[] splits = expr.split(type.syntax.pattern());
			if (splits.length < 2){
				Interpreter.throwError("Not enough arguments for comparison");
			}
			boolean flag = true;
			Value toCompare = parseExpression(splits[0]);
			for (int i = 1; i < splits.length; i++){
				flag = flag && toCompare.equals(parseExpression(splits[i]));
			}
			return new Value(VariableTypes.Boolean, flag);
		}
		case notEqual:
		{
			String[] splits = expr.split(type.syntax.pattern());
			if (splits.length != 2){
				Interpreter.throwError("Not enough arguments for comparison");
			}
			return Value.bool(parseExpression(splits[0]).equals(parseExpression(splits[1])));
		}
		case division:
		{	String[] splits = expr.split("/");
			if (splits.length != 2){
				Interpreter.throwError("Too many arguments for division");
			}
			return Variable.divVals(parseExpression(splits[0]), parseExpression(splits[1]));
		}
		case multiplication:
		{	String[] splits = expr.split(type.syntax.pattern());
			if (splits.length < 2){
				Interpreter.throwError("Not enough arguments for multiplication");
			}
			Value accumulator = parseExpression(splits[0]);
			for (int i = 1; i < splits.length; i++){
				accumulator = Variable.mulValues(accumulator, parseExpression(splits[i]));
			}
			return accumulator;
		}
		case parenthesis:
		{	
			String replString = expr;
			Matcher mat =  type.syntax.matcher(replString);
			while (mat.find()){
				String toReplace = mat.group().replace("(", "").replace(")", "");
				String tempId = "@"+Executor.getUUID();	// Create a temp variable that starts with @ to store the value
				Variable.setGlobalValue(tempId, parseExpression(toReplace));
				replString = type.syntax.matcher(replString).replaceFirst(tempId);
				mat =  type.syntax.matcher(replString);
			}
			return parseExpression(replString);
		}
		case subtraction:
		{	
			String[] splits = expr.split(type.syntax.pattern());
			if (splits.length != 2){
				Interpreter.throwError("Incorrect number of arguments for subtraction");
			}
			
			return Variable.subValues(parseExpression(splits[0]), parseExpression(splits[1]));
		}
		case exponentiation:
		{
			String[] splits = expr.split(type.syntax.pattern());
			if (splits.length != 2){
				Interpreter.throwError("Unable to parse exponentiation, it should be base ** exponent");
			}
			return Variable.expValues(parseExpression(splits[0]), parseExpression(splits[1]));
		}
		case logarithm:
		{
			String[] splits = expr.split(type.syntax.pattern());
			if (splits.length != 2){
				Interpreter.throwError("Unable to parse logarithm, it should be input __ base");
			}
			return Variable.logValues(parseExpression(splits[0]), parseExpression(splits[1]));
		}
		case approximateComparison:
		{
			String[] splits = expr.split(type.syntax.pattern());
			if (splits.length < 2){
				Interpreter.throwError("Incorrect number of arguments for approximate comparison");
			}
			Value first = parseExpression(splits[0]);
			if (!Variable.isDecimalValue(first)){
				return evaluate(SimpleExpression.comparison, expr);
			}
			BigDecimal frst = Variable.getDoubleVal(first);
			for (int i = 1; i < splits.length; i++){
				Value val = parseExpression(splits[i]);
				if (!Variable.isDecimalValue(val)){
					return evaluate(SimpleExpression.comparison, expr);
				}
				BigDecimal v = Variable.getDoubleVal(val);
				if (Math.abs(frst.subtract(v).doubleValue()) > EPSILON){
					return Value.bool(false);
				}
			}
			return Value.bool(true);
		}
		case greaterThan:
		{
			String[] splits = expr.split(type.syntax.pattern());
			if (splits.length < 2){
				Interpreter.throwError("Not enough arguments for > comparison");
			}
			Value comp = parseExpression(splits[0]);
			if (!Variable.isDecimalValue(comp)){
				Interpreter.throwError("Cannot evaluate > with non-decimal value "+comp);
			}
			BigDecimal cmp = Variable.getDoubleVal(comp);
			boolean flag = true;
			for (int i = 1; i < splits.length; i++){
				Value currval = parseExpression(splits[i]);
				if (!Variable.isDecimalValue(currval)){
					Interpreter.throwError("Cannot evaluate > with non-decimal value "+currval);
				}
				BigDecimal cval = Variable.getDoubleVal(currval);
				flag = flag && ((cmp.subtract(cval)).doubleValue() > 0);
				cmp = cval;
			}
			return Value.bool(flag);
		}
		case greaterThanEqual:
		{
			String[] splits = expr.split(type.syntax.pattern());
			if (splits.length < 2){
				Interpreter.throwError("Not enough arguments for >= comparison");
			}
			Value comp = parseExpression(splits[0]);
			if (!Variable.isDecimalValue(comp)){
				Interpreter.throwError("Cannot evaluate >= with non-decimal value "+comp);
			}
			BigDecimal cmp = Variable.getDoubleVal(comp);
			boolean flag = true;
			for (int i = 1; i < splits.length; i++){
				Value currval = parseExpression(splits[i]);
				if (!Variable.isDecimalValue(currval)){
					Interpreter.throwError("Cannot evaluate >= with non-decimal value "+currval);
				}
				BigDecimal cval = Variable.getDoubleVal(currval);
				flag = flag && ((cmp.subtract(cval)).doubleValue() >= 0);
				cmp = cval;
			}
			return Value.bool(flag);
		}
		case lessThan:
		{
			String[] splits = expr.split(type.syntax.pattern());
			if (splits.length < 2){
				Interpreter.throwError("Not enough arguments for < comparison");
			}
			Value comp = parseExpression(splits[0]);
			if (!Variable.isDecimalValue(comp)){
				Interpreter.throwError("Cannot evaluate < with non-decimal value "+comp);
			}
			BigDecimal cmp = Variable.getDoubleVal(comp);
			boolean flag = true;
			for (int i = 1; i < splits.length; i++){
				Value currval = parseExpression(splits[i]);
				if (!Variable.isDecimalValue(currval)){
					Interpreter.throwError("Cannot evaluate < with non-decimal value "+currval);
				}
				BigDecimal cval = Variable.getDoubleVal(currval);
				flag = flag && ((cmp.subtract(cval)).doubleValue() < 0);
				cmp = cval;
			}
			return Value.bool(flag);
		}
		case lessThanEqual:
		{
			String[] splits = expr.split(type.syntax.pattern());
			if (splits.length < 2){
				Interpreter.throwError("Not enough arguments for <= comparison");
			}
			Value comp = parseExpression(splits[0]);
			if (!Variable.isDecimalValue(comp)){
				Interpreter.throwError("Cannot evaluate <= with non-decimal value "+comp);
			}
			BigDecimal cmp = Variable.getDoubleVal(comp);
			boolean flag = true;
			for (int i = 1; i < splits.length; i++){
				Value currval = parseExpression(splits[i]);
				if (!Variable.isDecimalValue(currval)){
					Interpreter.throwError("Cannot evaluate <= with non-decimal value "+currval);
				}
				BigDecimal cval = Variable.getDoubleVal(currval);
				flag = flag && ((cmp.subtract(cval)).doubleValue() <= 0);
				cmp = cval;
			}
			return Value.bool(flag);
		}
		case modulus:
		{
			String[] splits = expr.split(type.syntax.pattern());
			if (splits.length == 1){
				return Variable.divVals(parseExpression(splits[0]), Value.integer(100));
			}
			if (splits.length != 2){
				Interpreter.throwError("Invalid number of arguments for modulus");
			}
			return Variable.modVals(parseExpression(splits[0]), parseExpression(splits[1]));
		}
		case preorderIncrement:
		{
			String[] splits = expr.split(type.syntax.pattern());
			if (splits.length != 2){	// First is empty, second is the input
				Interpreter.throwError("Invalid format for preorder increment");
			}
			String trimmed = splits[1].trim();
			Variable.setValue(trimmed, Variable.addValues(Variable.getValue(trimmed), Value.integer(1)));
			return Variable.getValue(trimmed);
		}
		case preorderDecrement:
		{
			String[] splits = expr.split(type.syntax.pattern());
			if (splits.length != 2){	// First is empty, second is the input
				Interpreter.throwError("Invalid format for preorder decrement");
			}
			String trimmed = splits[1].trim();
			Variable.setValue(trimmed, Variable.subValues(Variable.getValue(trimmed), Value.integer(1)));
			return Variable.getValue(trimmed);
		}
		case functionCall:
		{
			String methodName = expr.split("\\(")[0].trim();
			Method toCall = Executor.getMethodInCurrentProcess(methodName);
			String justTheArgs = expr.replace(methodName+"(", "");
			justTheArgs = justTheArgs.substring(0, justTheArgs.length()-1);
			if (justTheArgs.length() != 0){
				String[] splits = justTheArgs.split(",");
				Value[] params = new Value[splits.length];
				for (int i = 0; i < splits.length; i++){
					params[i] = parseExpression(splits[i]);
				}
				return Executor.functionCall(toCall, params);
			}
			Value[] emptyArgs = {};
			return Executor.functionCall(toCall, emptyArgs);
		}
		default:
			System.err.println(expr);
			Interpreter.throwError("Simple Expression Executor Inititialized with invalid type");
			break;
		}
		System.err.println(expr);
		Interpreter.throwError("Simple Expression Executor Inititialized with invalid type");
		
		return new Value(VariableTypes.Nil, null);
	}
	
}
