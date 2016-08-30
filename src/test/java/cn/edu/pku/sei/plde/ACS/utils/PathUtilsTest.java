package cn.edu.pku.sei.plde.ACS.utils;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

/**
 * Created by yjxxtd on 4/26/16.
 */
public class PathUtilsTest {

    @Test
    public void testGetVariableInMethod() {
        String bugProject = "Math_2";
        ArrayList<String> paths = PathUtils.getSrcPath(bugProject);
        assertTrue(paths.size() == 4);
        assertTrue(paths.get(0).equals("/target/classes/"));
        assertTrue(paths.get(1).equals("/target/test-classes/"));
        assertTrue(paths.get(2).equals("/src/main/java/"));
        assertTrue(paths.get(3).equals("/src/test/java/"));
    }
}
