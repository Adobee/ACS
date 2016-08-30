package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import org.junit.Test;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import cn.edu.pku.sei.plde.conqueroverfitting.jdtVisitor.IdentifierCollectVisitor;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.JDTUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.junit.Test;
import java.util.ArrayList;

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
