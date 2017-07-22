package com.blazingkin.interpreter.variables;

import java.awt.MouseInfo;
import java.awt.Toolkit;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nevec.rjm.BigDecimalMath;

import com.blazingkin.interpreter.Interpreter;
import com.blazingkin.interpreter.executor.Executor;
import com.blazingkin.interpreter.executor.Method;
import com.blazingkin.interpreter.executor.SimpleExpressionParser;
import com.blazingkin.interpreter.executor.lambda.LambdaParser;
import com.blazingkin.interpreter.executor.output.graphics.GraphicsExecutor;

public class Variable {
	private static HashMap<Context, HashMap<String, Value>> variables = 
			new HashMap<Context, HashMap<String, Value>>();
	private static Context globalContext = new Context();
	
	
	public static HashMap<String, Value> getContextVariables(Context con){
		if (!variables.containsKey(con)){
			variables.put(con, new HashMap<String, Value>());
		}
		return variables.get(con);
	}
	
	public static Context getGlobalContext(){
		return globalContext;
	}
	public static HashMap<BigInteger, Value> getArray(String arrayName){
		return getArray(arrayName, Executor.getCurrentContext());
	}
	public static HashMap<BigInteger, Value> getGlobalArray(String arrayName){
		return getArray(arrayName, getGlobalContext());
	}
	public static HashMap<BigInteger, Value> getArray(String arrayName, Context context){
		if (!getContextVariables(context).containsKey(arrayName)){
			setValue(arrayName, new Value(VariableTypes.Array, new HashMap<BigInteger, Value>()));
		}
		Value v = getContextVariables(context).get(arrayName);
		if (v.type == VariableTypes.Array && v.value instanceof HashMap<?, ?>){
			@SuppressWarnings("unchecked")
			HashMap<BigInteger, Value> arr = (HashMap<BigInteger, Value>)v.value;
			return arr;
		}else{
			Interpreter.throwError("Attempted to get "+arrayName+" as an array, but it is not one");
			return null;
		}
	}
	
	public static void clearVariables(){
		variables.clear();
		globalContext = new Context();
		Executor.setLine(0);
	}
	
	public static void killContext(Context con){
		if (!con.equals(getGlobalContext())){
			variables.remove(con);
		}
	}
	
	//Gets a local value (the scope of these variables is the function they are declared in
	public static Value getValue(String key){
		return getValue(key, Executor.getCurrentContext());
	}
	
	//This gets a global value (i.e. its scope is the entire program)
	public static Value getGlobalValue(String key){
		return getValue(key, getGlobalContext());
	}
	
	public static Value addValues(Value v1, Value v2){
		if (v1.type == VariableTypes.Integer && v2.type == VariableTypes.Integer){
			return new Value(VariableTypes.Integer, getIntValue(v1).add(getIntValue(v2)));
		}
		if ((v1.type == VariableTypes.Integer || v1.type == VariableTypes.Double) &&
				v2.type == VariableTypes.Integer || v2.type == VariableTypes.Double){
			return new Value(VariableTypes.Double, getDoubleVal(v1).add(getDoubleVal(v2)));
		}
		if (isValRational(v1) && isValRational(v2)){
			BLZRational rat = getRationalVal(v1).add(getRationalVal(v2));
			if (rat.den.equals(BigInteger.ONE)){
				return new Value(VariableTypes.Integer, rat.num);
			}
			return new Value(VariableTypes.Rational, rat);
		}
		Interpreter.throwError("Failed Adding Variables "+v1.value+" and "+v2.value);
		return new Value(VariableTypes.Nil, null);
	}
	
	public static Value subValues(Value v1, Value v2){
		if (v1.type == VariableTypes.Integer && v2.type == VariableTypes.Integer){
			return new Value(VariableTypes.Integer, getIntValue(v1).subtract(getIntValue(v2)));
		}
		if ((v1.type == VariableTypes.Integer || v1.type == VariableTypes.Double) &&
				v2.type == VariableTypes.Integer || v2.type == VariableTypes.Double){
			return new Value(VariableTypes.Double, getDoubleVal(v1).subtract(getDoubleVal(v2)));
		}
		if (isValRational(v1) && isValRational(v2)){
			BLZRational val2 = getRationalVal(v2);
			BLZRational rat = getRationalVal(v1).subtract(val2);
			if (rat.den.equals(BigInteger.ONE)){
				return new Value(VariableTypes.Integer, rat.num);
			}
			return new Value(VariableTypes.Rational, rat);
		}
		Interpreter.throwError("Failed Subtracting Variables "+v1.value+" and "+v2.value);
		return new Value(VariableTypes.Nil, null);
	}
	
	public static Value mulValues(Value v1, Value v2){
		if (v1.type == VariableTypes.Integer && v2.type == VariableTypes.Integer){
			return new Value(VariableTypes.Integer, ((BigInteger)v1.value).multiply((BigInteger)v2.value));
		}
		if (isValRational(v1) && isValRational(v2)){
			BLZRational rat = getRationalVal(v1).multiply(getRationalVal(v2));
			if (rat.den.equals(BigInteger.ONE)){
				return new Value(VariableTypes.Integer, rat.num);
			}
			return new Value(VariableTypes.Rational, rat);
		}
		if ((isValRational(v1) || isValDouble(v1)) && (isValRational(v2) || isValDouble(v2))){
			return new Value(VariableTypes.Double, getDoubleVal(v1).multiply(getDoubleVal(v2)));
		}
		Interpreter.throwError("Failed Multiplying Variables "+v1.value+" and "+v2.value);
		return new Value(VariableTypes.Nil, null);
	}
	
	public static Value modVals(Value val, Value quo) {
		if (val.type == VariableTypes.Integer && quo.type == VariableTypes.Integer){
			return new Value(VariableTypes.Integer, ((BigInteger)val.value).mod((BigInteger)quo.value));
		}
		Interpreter.throwError("Attempted to perform a modulus on non-integers");
		return new Value(VariableTypes.Nil, null);
	}
	
	public static Value divVals(Value top, Value bottom){
		if (top.type == VariableTypes.Integer && bottom.type == VariableTypes.Integer){
			return Value.rational((BigInteger)top.value, (BigInteger)bottom.value);
		}
		if (Variable.isValRational(top) && Variable.isValRational(bottom)){
			BLZRational toprat = Variable.getRationalVal(top);
			BLZRational botrat = Variable.getRationalVal(bottom);
			BLZRational botratopp = (BLZRational) Value.rational(botrat.den, botrat.num).value;
			BLZRational prod = toprat.multiply(botratopp);
			if (prod.den.equals(BigInteger.ONE)){
				return new Value(VariableTypes.Integer, prod.num);
			}
			return new Value(VariableTypes.Rational, prod);
		}
		if ((Variable.isValDouble(top) || Variable.isValRational(top))
				&& (Variable.isValDouble(bottom) || Variable.isValRational(bottom))){
			BigDecimal dtop = Variable.getDoubleVal(top);
			BigDecimal dbot = Variable.getDoubleVal(bottom);
			return new Value(VariableTypes.Double, dtop.divide(dbot, MathContext.DECIMAL128));
		}
		Interpreter.throwError("Could not convert one of "+top+" or "+bottom+" to a dividable type");
		return new Value(VariableTypes.Nil, null);
	}
	
	public static Value expValues(Value v1, Value v2){
		if (isDecimalValue(v1) && isDecimalValue(v2)){
			BigDecimal d1 = getDoubleVal(v1);
			BigDecimal d2 = getDoubleVal(v2);
			return new Value(VariableTypes.Double, powerBig(d1,d2));
		}
		Interpreter.throwError("Failed Taking an Exponent with "+v1.value + " and "+v2.value);
		return new Value(VariableTypes.Nil, null);
	}
	
	public static Value logValues(Value v1, Value v2){
		if (isDecimalValue(v1) && isDecimalValue(v2)){
			BigDecimal d1 = getDoubleVal(v1);
			BigDecimal d2 = getDoubleVal(v2);
			return new Value(VariableTypes.Double, BigDecimalMath.log(d1.setScale(12, RoundingMode.HALF_UP)).divide(BigDecimalMath.log(d2.setScale(12, RoundingMode.HALF_UP)), MathContext.DECIMAL64));
		}
		if (isDecimalValue(v1) && (v2.type == VariableTypes.String && ((String) v2.value).toLowerCase().equals("e"))){
			return new Value(VariableTypes.Double, BigDecimalMath.log(getDoubleVal(v1).setScale(12, RoundingMode.HALF_UP)));
		}
		System.out.println(v2.type);
		Interpreter.throwError("Failed Taking an Logarithm with "+v1.value + " and "+v2.value);
		return new Value(VariableTypes.Nil, null);
	}
	
	
	private static Pattern squareBracketPattern = Pattern.compile("\\[\\S*\\]");
	private static Pattern curlyBracketPattern = Pattern.compile("\\{\\S*\\}");
	private static Pattern quotePattern = Pattern.compile("\".*\"");
	public static Value getValue(String line, Context con){
		line=line.trim();
		if (line.length() > 0 && line.charAt(0) == '*'){	//See if it's declared as a global variable
			con = getGlobalContext();
		}
		
		if (isInteger(line)){	//If its an integer, then return it
			
			return new Value(VariableTypes.Integer, new BigInteger(line));
		}
		if (isDouble(line)){	//If its a double, then return it
			return new Value(VariableTypes.Double, new BigDecimal(line));
		}
		if (isBool(line)){		//If its a bool, then return it
			return new Value(VariableTypes.Boolean, convertToBool(line));
		}
		if (line.length() > 0 && line.charAt(0) == '(' && line.charAt(line.length()-1) == ')'){
			return LambdaParser.parseLambdaExpression(line).getValue();
		}
		
		
		Matcher quoteMatcher = quotePattern.matcher(line);
		if (quoteMatcher.find()){
			return new Value(VariableTypes.String, line.replace("\"",""));
		}
		Matcher curlyBracketMatcher = curlyBracketPattern.matcher(line);
		if (curlyBracketMatcher.find()){
			String gp = curlyBracketMatcher.group();
			gp = gp.substring(1, gp.length()-1);
			for (SystemEnv env : SystemEnv.values()){
				if (gp.equals(env.name)){
					return getEnvVariable(env);
				}
			}
			Interpreter.throwError("Failed to find an environment variable to match: "+gp);
		}
		Matcher squareBracketMatcher = squareBracketPattern.matcher(line);
		if (squareBracketMatcher.find()){
			String gp = squareBracketMatcher.group();
			gp = gp.substring(1, gp.length()-1);
			Value index = SimpleExpressionParser.parseExpression(gp);
			BigInteger ind = getIntValue(index);
			return getValueOfArray(line.split("\\[")[0], ind, con);
		}
		
		
		if (getContextVariables(con).containsKey(line)){
			return getContextVariables(con).get(line);
		}
		if (con.getParentContext() != getGlobalContext()){
			return getValue(line, con.getParentContext());
		}
		Interpreter.throwError("Unable to find a value for: "+line);
		return new Value(VariableTypes.Nil, null);
	}
	
	
	public static void setValue(String key, Value value){		//sets a local variable
		setValue(key, value, Executor.getCurrentContext());
	}
	
	//Sets a global value (i.e. its scope is the entire program)
	public static void setGlobalValue(String key, Value value){
		setValue(key, value, getGlobalContext());
	}
	
	public static void setValue(String key, Value value, Context con){
		if (key.length() > 0 && key.charAt(0) == '*'){
			con = getGlobalContext();
		}
		Matcher squareBracketMatcher = squareBracketPattern.matcher(key);
		if (squareBracketMatcher.find()){
			String gp = squareBracketMatcher.group();
			gp = gp.substring(1, gp.length()-1);
			Value index = SimpleExpressionParser.parseExpression(gp);
			BigInteger ind = getIntValue(index);
			setValueOfArray(key.split("\\[")[0], ind, value, con);
			return;
		}
		getContextVariables(con).put(key, value);
	}
	

	
	
	public static Value[] getValuesFromList(String[] args){
		Value[] vals = new Value[args.length];
		String[] done = new String[args.length];
		for (int i = 0; i < args.length; i++){
			done[i] = args[i].replace(",", "").replace("(", "").replace(")", "").trim();
			vals[i] = getValue(done[i]);
		}
		return vals;
	}
	


	/*	Checks to see if a variables has been set	
	 */
	public static boolean contains(String key){
		return contains(key, Executor.getCurrentContext());
	}
	
	public static boolean contains(String key, Context con){
		if (key.length() > 0 && key.charAt(0) == '*'){
			con = getGlobalContext();
		}
		return getContextVariables(con).containsKey(key) || getContextVariables(con).containsKey(key.split("\\[")[0]);
	}
	
	


	
	public static boolean isInteger(String s) {
		try{
			new BigInteger(s);
			return true;
		}catch(NumberFormatException e){}
		return false;
	}
	public static boolean isDouble(String s){
		try{
			new BigDecimal(s);
			return true;
		}catch(NumberFormatException e){}
		return false;
	}
	
	
	public static boolean isBool(String s){
		String lower = s.toLowerCase();
		return lower.equals("true") || lower.equals("false");
	}
	
	public static boolean isFraction(String s){
		String[] splits = s.split("/");
		if (splits.length == 2){
			return isInteger(splits[0]) && isInteger(splits[1]);
		}
		return false;
	}
	
	public static Value convertToFraction(String s){
		String[] splits = s.split("/");
		return Value.rational(Integer.parseInt(splits[0]), Integer.parseInt(splits[1]));
	}
	
	public static boolean convertToBool(String s){
		String lower = s.toLowerCase();
		if (lower.equals("true")){
			return true;
		}
		return false;
	}
	
	public static Value getEnvVariable(SystemEnv se){
		switch(se){
		case windowSizeX:
			return Value.integer(GraphicsExecutor.jf.getWidth());
		case windowSizeY:
			return Value.integer(GraphicsExecutor.jf.getHeight());
		case FPS:
			return Value.integer(GraphicsExecutor.lastFPS);
		case time:
			return Value.doub((double)System.currentTimeMillis());
		case osName:
			return new Value(VariableTypes.String, System.getProperty("os.name"));
		case osVersion:
			return new Value(VariableTypes.String, System.getProperty("os.version"));
		case windowPosX:
			return Value.integer(GraphicsExecutor.jf.getLocation().x);
		case windowPosY:
			return Value.integer(GraphicsExecutor.jf.getLocation().y);
		case screenResX:
			return Value.doub(Toolkit.getDefaultToolkit().getScreenSize().getWidth());
		case screenResY:
			return Value.doub(Toolkit.getDefaultToolkit().getScreenSize().getHeight());
		case cursorPosX:
			return Value.integer(MouseInfo.getPointerInfo().getLocation().x);
		case cursorPosY:
			return Value.integer(MouseInfo.getPointerInfo().getLocation().y);
		case processUUID:
			if (Executor.getCurrentProcess() == null){
				return Value.integer(-1);
			}
			return Value.integer(Executor.getCurrentProcess().UUID);
		case processesRunning:
			return Value.integer(Executor.getRunningProcesses().size());
		case methodStack:
			String stackString = "";
			Method[] stck = new Method[Executor.getMethodStack().size()];
			Executor.getMethodStack().copyInto(stck);
			for (Method m : stck){
				stackString = m.functionName + "\n" + stackString;
			}
			stackString = stackString.trim();
			return new Value(VariableTypes.String, stackString);
		case lineReturns:
			if (Executor.getCurrentProcess() == null){
				return Value.integer(-1);
			}
			return Value.integer(Executor.getCurrentProcess().lineReturns.size());
		case version:
			//TODO update this every time
			return new Value(VariableTypes.String, "2.2.2");
		case runningFileLocation:
			if (Executor.getCurrentProcess() == null || !Executor.getCurrentProcess().runningFromFile){
				return new Value(VariableTypes.String, "SOFTWARE");
			}
			return new Value(VariableTypes.String, Executor.getCurrentProcess().readingFrom.getParentFile().getAbsolutePath());
		case runningFileName:
			if (Executor.getCurrentProcess() == null || !Executor.getCurrentProcess().runningFromFile){
				return new Value(VariableTypes.String, "SOFTWARE");
			}
			return new Value(VariableTypes.String, Executor.getCurrentProcess().readingFrom.getName());
		case runningFilePath:
			if (Executor.getCurrentProcess() == null || !Executor.getCurrentProcess().runningFromFile){
				return new Value(VariableTypes.String, "SOFTWARE");
			}
			return new Value(VariableTypes.String, Executor.getCurrentProcess().readingFrom.getAbsolutePath());
		case newline:
			return new Value(VariableTypes.String, "\n");
		case alt:
			return new Value(VariableTypes.String, "ALT");
		case back:
			return new Value(VariableTypes.String, "BACKSPACE");
		case control:
			return new Value(VariableTypes.String, "CONTROL");
		case shift:
			return new Value(VariableTypes.String, "SHIFT");
		case systemKey:
			return new Value(VariableTypes.String, "SYSTEM_KEY");
		case tab:
			return new Value(VariableTypes.String, "\t");
		case caps:
			return new Value(VariableTypes.String, "CAPS_LOCK");
		case boundCursorPosX:
			return Value.integer(MouseInfo.getPointerInfo().getLocation().x - GraphicsExecutor.jf.getLocation().x - GraphicsExecutor.jf.getInsets().left);
		case boundCursorPosY:
			return Value.integer(MouseInfo.getPointerInfo().getLocation().y - GraphicsExecutor.jf.getLocation().y -  GraphicsExecutor.jf.getInsets().top);
		case space:
			return new Value(VariableTypes.String, " ");
		case euler:
			return Value.doub(Math.E);
		case pi:
			return Value.doub(Math.PI);
		case context:
			return Value.integer(Executor.getCurrentContext().getID());
		case nil:
			return new Value(VariableTypes.Nil, null);
		default:
			return new Value(VariableTypes.Nil, null);
		}
	}
	
	public static void clearLocalVariables(Context con){
		getContextVariables(con).clear();
	}
	
	//Parses an array value
	public static Value getValueOfArray(String arrayName, BigInteger index){
		return getValueOfArray(arrayName, index, Executor.getCurrentContext());
	}
	
	public static Value getValueOfArray(String arrayName, BigInteger index, Context con){
		if (getArray(arrayName, con).containsKey(index)){
			return getArray(arrayName, con).get(index);	
		}
		return new Value(VariableTypes.Nil, null);
	}
	
	//Sets the value of an array
	public static void setValueOfArray(String key,BigInteger index, Value value){
		setValueOfArray(key,index,value,Executor.getCurrentContext());
	}
	
	public static void setValueOfArray(String key,BigInteger index, Value value, Context con){
		getArray(key, con).put(index, value);
	}
	
	public static boolean isValInt(Value v){
		if (v.type == VariableTypes.Rational){
			BLZRational rat = (BLZRational) v.value;
			return rat.den.equals(BigInteger.ONE);
		}
		return v.type == VariableTypes.Integer;
	}
	public static boolean isValDouble(Value v){
		return  v.type == VariableTypes.Double;
	}
	
	
	/**
	 * @param v - The value to check
	 * @return if the value is a real number (as in the mathematical set)
	 */
	public static boolean isDecimalValue(Value v){
		return v.type == VariableTypes.Integer || v.type == VariableTypes.Rational || v.type == VariableTypes.Double;
	}
	
	public static boolean isValRational(Value v){
		return v.type == VariableTypes.Integer || v.type == VariableTypes.Rational;
	}
	
	public static BLZRational getRationalVal(Value v){
		if (v.type == VariableTypes.Rational){
			return (BLZRational) v.value;
		}
		if (v.type == VariableTypes.Integer){
			return new BLZRational((BigInteger)v.value, BigInteger.ONE);
		}
		Interpreter.throwError("Attemted an illegal cast to a rational");
		return new BLZRational(BigInteger.ZERO, BigInteger.ZERO);
	}
	
	public static BigDecimal getDoubleVal(Value v){
		try{
			if (isValInt(v) || v.value instanceof Integer){
				return new BigDecimal(((BigInteger) v.value));
			}
			if (v.type == VariableTypes.Rational){
				BLZRational rat = (BLZRational) v.value;
				BigDecimal num = new BigDecimal(rat.num);
				BigDecimal den = new BigDecimal(rat.den);
				return num.divide(den, MathContext.DECIMAL128);
			}
			return (BigDecimal)v.value;
		}catch(Exception e){
			Interpreter.throwError("Attempted to cast "+v.value+" to a double and failed!");
			return BigDecimal.ZERO;
		}
	}
	public static BigInteger getIntValue(Value v){
		try{
			if (isValDouble(v) || v.value instanceof Double){
				return BigInteger.valueOf((long)((double) v.value));
			}
			if (v.type == VariableTypes.Rational){
				BLZRational rat = (BLZRational) v.value;
				return rat.num.divide(rat.den); 
			}
			return (BigInteger) v.value;
		}catch(Exception e){
			Interpreter.throwError("Attempted to cast "+v.value+" to an int and failed!");
			return BigInteger.ZERO;
		}
	}
	
	public static BigDecimal powerBig(BigDecimal base, BigDecimal exponent) {

	    BigDecimal ans=  new BigDecimal(1.0);
	    BigDecimal k=  new BigDecimal(1.0);
	    BigDecimal t=  new BigDecimal(-1.0);
	    BigDecimal no=  new BigDecimal(0.0);

	    if (exponent != no) {
	        BigDecimal absExponent =  exponent.signum() > 0 ? exponent : t.multiply(exponent);
	        while (absExponent.signum() > 0){
	            ans =ans.multiply(base);
	            absExponent = absExponent.subtract(BigDecimal.ONE);
	        }

	        if (exponent.signum() < 0) {
	            // For negative exponent, must invert
	            ans = k.divide(ans);
	        }
	    } else {
	        // exponent is 0
	        ans = k;
	    }

	    return ans;
	}

	
	
}
