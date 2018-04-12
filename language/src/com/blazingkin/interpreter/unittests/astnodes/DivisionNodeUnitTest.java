package com.blazingkin.interpreter.unittests.astnodes;

import com.blazingkin.interpreter.executor.astnodes.DivisionNode;
import com.blazingkin.interpreter.expressionabstraction.ASTNode;
import com.blazingkin.interpreter.expressionabstraction.ValueASTNode;
import com.blazingkin.interpreter.unittests.UnitTestUtil;
import com.blazingkin.interpreter.variables.Value;
import com.blazingkin.interpreter.variables.Context;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class DivisionNodeUnitTest {
    @BeforeClass
	public static void setup(){
		UnitTestUtil.setup();
	}
	
	@After
	public void clear(){
		UnitTestUtil.clearEnv();
    }

    @Test
    public void shouldRequireTwoArgs(){
        ASTNode args[] = {};
        new DivisionNode(args);
        UnitTestUtil.assertLastError("Division did not have 2 arguments");
    }

    @Test
    public void shouldDivideIntegers(){
        ASTNode args[] = {new ValueASTNode("1"), new ValueASTNode("2")};
        Value result = new DivisionNode(args).execute(new Context());
        UnitTestUtil.assertEqual(result, Value.rational(1,2));
    }

    @Test
    public void shouldDivideIntegersAndGetIntegerResult(){
        ASTNode args[] = {new ValueASTNode("20"), new ValueASTNode("10")};
        Value result = new DivisionNode(args).execute(new Context());
        UnitTestUtil.assertEqual(result, Value.integer(2));
    }

    @Test
    public void shouldComplainAboutDividingAString(){
        ASTNode args[] = {new ValueASTNode("\"asdf\""), new ValueASTNode("2")};
        new DivisionNode(args).execute(new Context());
        UnitTestUtil.assertLastError("Could not convert one of asdf or 2 to a dividable type");
    }

}