package cn.edu.pku
        .sei.plde.conqueroverfitting.utils;

import org.junit.Test;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * Created by yjxxtd on 4/23/16.
 */
public class VariableUtilsTest {
    @Test
    public void testIsJavaIdentifier(){
        assertFalse(VariableUtils.isJavaIdentifier(".a"));
        assertFalse(VariableUtils.isJavaIdentifier("a*b"));
        assertFalse(VariableUtils.isJavaIdentifier("a.c"));
        assertTrue(VariableUtils.isJavaIdentifier("a2"));
    }
}
