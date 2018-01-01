package com.blazingkin.interpreter.unittests;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blazingkin.interpreter.executor.sourcestructures.Process;

public class ProcessUnitTest {

	@BeforeClass
	public static void setup(){
		UnitTestUtil.setup();
	}
	
	
	@Test
	public void TestIncompleteBlockOne() {
		String[] incompleteBlock = {"for i=0, i<20, ++i", "echo i"};
		new Process(incompleteBlock);
		UnitTestUtil.assertLastError("Some blocks not closed!");
	}
	
	@Test
	public void TestIncompleteBlockTwo(){
		String[] incompleteBlock = {"if true", "echo \"bad\""};
		new Process(incompleteBlock);
		UnitTestUtil.assertLastError("Some blocks not closed!");
	}
	
	@Test
	public void TestIncompleteBlockThree(){
		String[] incompleteBlock = {":main", "a = 3"};
		new Process(incompleteBlock);
		UnitTestUtil.assertLastError("Some blocks not closed!");
	}
	
	@Test
	public void TestIncompleteBlockFour(){
		String[] incompleteBlock = {":main", "if true", "a = 3", "end"};
		new Process(incompleteBlock);
		UnitTestUtil.assertLastError("Some blocks not closed!");
	}
	
	@Test
	public void TestCompleteBlockOne(){
		String[] completeBlock = {"a = 2", "echo a"};
		new Process(completeBlock);
		UnitTestUtil.assertNoErrors();
	}
	
	@Test
	public void TestCompleteBlockTwo(){
		String[] completeBlock = {"if true", "a = 3", "end"};
		Process p = new Process(completeBlock);
		UnitTestUtil.assertNoErrors();
		UnitTestUtil.assertEqual(3, p.blockArcs.get(1).end);
	}
	
	@Test
	public void TestCompleteBlockThree(){
		String[] completeBlock = {":main", "if true", "a = 3", "end", "end"};
		Process p = new Process(completeBlock);
		UnitTestUtil.assertNoErrors();
		UnitTestUtil.assertEqual(5, p.blockArcs.get(1).end);
		UnitTestUtil.assertEqual(4, p.blockArcs.get(2).end);
	}
	
	@Test
	public void TestIncompleteConstructorBlock(){
		String[] code = { "constructor Ball", "thing = 2" };
		new Process(code);
		UnitTestUtil.assertLastError("Some blocks not closed!");
	}
	
	@Test
	public void TestConstructorRegistration(){
		String[] code = {"constructor Ball", "color = \"red\"", "end" };
		Process p = new Process(code);
		UnitTestUtil.assertNoErrors();
		UnitTestUtil.assertEqual(p.constructors.size(), 1);
		UnitTestUtil.assertEqual(p.constructors.get(0).name, "Ball");
	}
	
	@Test
	public void TestUnnamedConstructor(){
		String[] code = {"constructor", "color = \"red\"", "end"};
		new Process(code);
		UnitTestUtil.assertLastError("Empty constructor name!");
	}
	
	@After
	public void clearEnv(){
		UnitTestUtil.clearEnv();
	}

}
