package com.blazingkin.interpreter.unittests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.blazingkin.interpreter.unittests.astnodes.AdditionNodeUnitTest;
import com.blazingkin.interpreter.unittests.astnodes.ApproximateComparisonNodeUnitTest;
import com.blazingkin.interpreter.unittests.astnodes.ArrayLiteralNodeUnitTest;
import com.blazingkin.interpreter.unittests.astnodes.ArrayLookupNodeUnitTest;
import com.blazingkin.interpreter.unittests.astnodes.AssignmentNodeUnitTest;
import com.blazingkin.interpreter.unittests.astnodes.LessThanNodeUnitTest;
import com.blazingkin.interpreter.unittests.astnodes.LogicalAndNodeUnitTest;
import com.blazingkin.interpreter.unittests.astnodes.LogicalOrNodeUnitTest;
import com.blazingkin.interpreter.unittests.astnodes.WhileNodeUnitTest;

// This is not scalable.... TODO Find a better way
@RunWith(Suite.class)
@Suite.SuiteClasses({
	AdditionNodeUnitTest.class,
	ApproximateComparisonNodeUnitTest.class,
	ArrayLiteralNodeUnitTest.class,
	ArrayLookupNodeUnitTest.class,
	AssignmentNodeUnitTest.class,
	ConstructorUnitTest.class,
	ContextUnitTest.class,
	ExecutorUnitTest.class,
	ExpressionExecutorUnitTest.class,
	ExpressionParserUnitTest.class,
	FunctionCallNodeUnitTest.class,
	LessThanNodeUnitTest.class,
	LogicalAndNodeUnitTest.class,
	LogicalOrNodeUnitTest.class,
	PackageUnitTest.class,
	ProcessUnitTest.class,
	VariableUnitTest.class,
	WhileNodeUnitTest.class
})
public class AllTestsSuite {

}