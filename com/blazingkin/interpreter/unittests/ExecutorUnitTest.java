package com.blazingkin.interpreter.unittests;

import java.util.ArrayList;

import com.blazingkin.interpreter.executor.Executor;

public class ExecutorUnitTest {

	public static void main(String[] args) {
		String[] p1 = Executor.parseExpressions("THIS (is an (expression \"that\" takes) arguments)");
		String[] p1Check = {"THIS", "(is an (expression \"that\" takes) arguments)"};
		UnitTestUtil.assertEqualArrays(p1, p1Check);
		String[] p2 = Executor.parseExpressions("test \"dafdf adsf\" dafds (dfsadf fdsf \"fdsf fsdaf\")");
		String[] p2Check = {"test", "\"dafdf adsf\"", "dafds", "(dfsadf fdsf \"fdsf fsdaf\")"};
		UnitTestUtil.assertEqualArrays(p2, p2Check);
		String[] p3 = Executor.parseExpressions("this is a test that should be completely split");
		String[] p3Check = {"this", "is", "a", "test", "that", "should", "be", "completely", "split"};
		UnitTestUtil.assertEqualArrays(p3, p3Check);
		
		ArrayList<Integer> uuids = new ArrayList<Integer>();
		for (int i = 0; i < 1000; i++){
			int u = Executor.getUUID();
			if (uuids.contains(u)){
				System.err.println("UUID HAD A COLLISION");
			}
			uuids.add(u);
		}
	}
	


}