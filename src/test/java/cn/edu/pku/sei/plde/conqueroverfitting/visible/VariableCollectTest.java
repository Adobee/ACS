package cn.edu.pku.sei.plde.conqueroverfitting.visible;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.junit.Test;

import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.VariableCollect;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;

public class VariableCollectTest {

	private final String projectPath = "filesfortest";
	private final String suspiciousFilePath = new File(
			"filesfortest//FileForTestVariableCollect.java").getAbsolutePath();
	private final String otherFilePath = new File(
			"filesfortest//FileForTestVariableCollect2.java")
			.getAbsolutePath();
	private final int suspiciousLineNum = 22;
    private final int suspiciousLineNumInConstructor = 30;

	@Test
	public void testFieldsCollectInClass() {

		VariableCollect variableCollect = VariableCollect.GetInstance(projectPath);

		LinkedHashMap<String, ArrayList<VariableInfo>> fieldsInClassMap = variableCollect
				.getVisibleFieldInAllClassMap(suspiciousFilePath);
		assertNotNull(fieldsInClassMap);

		assertTrue(fieldsInClassMap.containsKey(suspiciousFilePath));
		ArrayList<VariableInfo> fieldsInClass = fieldsInClassMap
				.get(suspiciousFilePath);
		assertNotNull(fieldsInClass);
		assertTrue(fieldsInClass.contains((new VariableInfo("MAX_INTEGER",
				TypeEnum.INT, true, null, true, false))));
		assertTrue(fieldsInClass.contains((new VariableInfo("MIN_INTEGER",
				TypeEnum.INT, true, null, true, false))));
		assertTrue(fieldsInClass.contains((new VariableInfo("MAX_TIME",
				TypeEnum.STRING, true, null, false, false))));
		assertFalse(fieldsInClass.contains((new VariableInfo("variableInnerClass",
				TypeEnum.INT, true, null, true, false))));
		assertTrue(fieldsInClass.contains((new VariableInfo("fileForTestVariableCollect2",
				null, false, "FileForTestVariableCollect2", true, true))));
		
		assertTrue(fieldsInClassMap.containsKey(otherFilePath));
		ArrayList<VariableInfo> fieldsInOtherClass = fieldsInClassMap
				.get(otherFilePath);
		assertTrue(fieldsInOtherClass.contains((new VariableInfo(
				"MAX_INTEGER2", TypeEnum.INT, true, null, true,false))));
		assertTrue(fieldsInOtherClass.contains((new VariableInfo(
				"MIN_INTEGER2", TypeEnum.INT, true, null, true, false))));
		assertFalse(fieldsInOtherClass.contains((new VariableInfo("MAX_TIME2",
				TypeEnum.STRING, true, null, false, false))));
	}

	@Test
	public void testParametersCollectInMethod() {
		VariableCollect variableCollect = VariableCollect.GetInstance(projectPath);
		ArrayList<VariableInfo> parametersInMethodList = variableCollect
				.getVisibleParametersInMethodList(suspiciousFilePath, suspiciousLineNum);
		assertNotNull(parametersInMethodList);

		assertTrue(parametersInMethodList.size() == 3);
		assertTrue(parametersInMethodList.contains((new VariableInfo("a",
				TypeEnum.INT, true, null))));
		assertTrue(parametersInMethodList.contains((new VariableInfo("b",
				TypeEnum.STRING, true, null))));
		assertTrue(parametersInMethodList.contains((new VariableInfo("test2",
				null, false, "FileForTestVariableCollect2"))));

		ArrayList<VariableInfo> parametersInConstructorList = variableCollect
				.getVisibleParametersInMethodList(suspiciousFilePath, suspiciousLineNumInConstructor);
		assertNotNull(parametersInConstructorList);

		assertTrue(parametersInConstructorList.size() == 2);
		assertTrue(parametersInConstructorList.contains((new VariableInfo("m",
				TypeEnum.INT, true, null))));
		assertTrue(parametersInConstructorList.contains((new VariableInfo("n",
				TypeEnum.INT, true, null))));

	}

	@Test
	public void testLocalCollectInMethod() {
		VariableCollect variableCollect = VariableCollect.GetInstance(projectPath);
		ArrayList<VariableInfo> localsInMethodList = variableCollect
				.getVisibleLocalInMethodList(suspiciousFilePath, suspiciousLineNum);
		assertNotNull(localsInMethodList);


		assertTrue(localsInMethodList.contains((new VariableInfo("e", TypeEnum.INT, true, null))));
		assertTrue(localsInMethodList.contains((new VariableInfo("f", TypeEnum.STRING, true, null))));

		ArrayList<VariableInfo> localsInConstructorList = variableCollect
				.getVisibleLocalInMethodList(suspiciousFilePath, suspiciousLineNumInConstructor);
		assertNotNull(localsInConstructorList);


		assertTrue(localsInConstructorList.contains((new VariableInfo("p", TypeEnum.INT, true, null))));
		assertTrue(localsInConstructorList.contains((new VariableInfo("q", TypeEnum.INT, true, null))));
	}
}
