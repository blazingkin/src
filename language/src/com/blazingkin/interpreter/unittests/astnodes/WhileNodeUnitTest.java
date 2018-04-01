package com.blazingkin.interpreter.unittests.astnodes;

import static com.blazingkin.interpreter.unittests.UnitTestUtil.assertEqual;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blazingkin.interpreter.executor.astnodes.WhileNode;
import com.blazingkin.interpreter.expressionabstraction.ExpressionParser;
import com.blazingkin.interpreter.expressionabstraction.ValueASTNode;
import com.blazingkin.interpreter.unittests.UnitTestUtil;
import com.blazingkin.interpreter.variables.Context;
import com.blazingkin.interpreter.variables.Value;

public class WhileNodeUnitTest {

	@BeforeClass
	public static void setup(){
		UnitTestUtil.setup();
	}
	
	@After
	public void clear(){
		UnitTestUtil.clearEnv();
	}
	
	@Test
	public void shouldNotRunWithFalseValue() {
		WhileNode wn = new WhileNode(new ValueASTNode(Value.bool(false)), new ValueASTNode(Value.integer(3)));
		UnitTestUtil.assertNil(wn.execute(new Context()));
	}
	
	@Test
	public void shouldReturnLastStatementExecuted() {
		Context testCon = new Context();
		testCon.setValue("a", Value.integer(0));
		WhileNode wn = new WhileNode(ExpressionParser.parseExpression("a < 3"), ExpressionParser.parseExpression("a++"));
		UnitTestUtil.assertEqual(Value.integer(3), wn.execute(testCon));
		assertEqual(testCon.getValue("a"), Value.integer(3));
	}

}