package cn.edu.pku.sei.plde.conqueroverfitting.visible;

import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.MethodInfo;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import static org.junit.Assert.*;

public class MethodCollectTest {

    private final String projectPath = "filesfortest";
    private final String suspiciousFilePath = new File(
            "filesfortest//FileForTestMethodCollect.java").getAbsolutePath();
    private final String otherFilePath = new File("filesfortest/FileForTestMethodCollect2.java").getAbsolutePath();
    private final String otherFilePath2 = new File("filesfortest/FileForTestMethodCollect3.java").getAbsolutePath();

    @Test
    public void testGetVisibleMethodWithoutParametersInAllClassMap() {

        MethodCollect methodCollect = MethodCollect.GetInstance(projectPath);

        LinkedHashMap<String, ArrayList<MethodInfo>> methodsInClassMap = methodCollect
                .getVisibleMethodWithoutParametersInAllClassMap(suspiciousFilePath);
        assertNotNull(methodsInClassMap);

        assertTrue(methodsInClassMap.containsKey(suspiciousFilePath));
        ArrayList<MethodInfo> methodsInClass = methodsInClassMap
                .get(suspiciousFilePath);
        assertNotNull(methodsInClass);
        assertFalse(methodsInClass.contains((new MethodInfo("test11",
                TypeEnum.BOOLEAN, true, null, true, false, false))));
        assertTrue(methodsInClass.contains((new MethodInfo("test12",
                TypeEnum.STRING, true, null, false, false, false))));

        ArrayList<MethodInfo> methodsInOtherClass = methodsInClassMap
                .get(otherFilePath);
        assertNotNull(methodsInOtherClass);
        assertFalse(methodsInOtherClass.contains((new MethodInfo("test21",
                TypeEnum.BOOLEAN, true, null, true, false, false))));
        assertFalse(methodsInOtherClass.contains((new MethodInfo("test22",
                null, false, "FileForTestVariableCollect", false, false, false))));
        assertFalse(methodsInOtherClass.contains((new MethodInfo("test23",
                null, false, "FileForTestVariableCollect", true, false, false))));
    }

    @Test
    public void testCheckIsStaticMethod() {
        MethodCollect methodCollect = MethodCollect.GetInstance(projectPath);
        assertTrue(methodCollect.checkIsStaticMethod(suspiciousFilePath, "test13"));
        assertFalse(methodCollect.checkIsStaticMethod(suspiciousFilePath, "test14"));
        assertTrue(methodCollect.checkIsStaticMethod(suspiciousFilePath, "test16"));

        assertTrue(methodCollect.checkIsStaticMethod(otherFilePath2, "test31"));
        assertTrue(methodCollect.checkIsStaticMethod(otherFilePath2, "test32"));
        assertTrue(methodCollect.checkIsStaticMethod(otherFilePath2, "test33"));
    }
}
