package cn.edu.pku.sei.plde.conqueroverfitting.equalVariable;

import cn.edu.pku.sei.plde.conqueroverfitting.utils.CodeUtils;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Created by yjxxtd on 4/23/16.
 */
public class EqualVariableCollectTest {
    @Test
    public void testEqualVariableCollect() {
        String path = "filesfortest//Complex.java";
        Set<String> equalVariable = CodeUtils.getEqualVariableInSource(path);

        assertTrue(equalVariable != null);
        assertTrue(equalVariable.size() == 2);
        assertTrue(equalVariable.contains("real"));
        assertTrue(equalVariable.contains("imaginary"));
    }

}
