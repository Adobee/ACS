package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.Interval;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Created by yjxxtd on 4/24/16.
 */
public class ExpressionUtilsTest {
    @Test
    public void testGetExpressionsInMethod(){
        String source = "    public int test(){\n" +
                "        int a = 1, b = 1, c = 1, d = 1;\n" +
                "        if(a >= 0){\n" +
                "            \n" +
                "        }\n" +
                "        if(a + 1 >= 0){\n" +
                "            \n" +
                "        }\n" +
                "        String str = \"\";\n" +
                "        if(str.contains(\"\")){\n" +
                "            \n" +
                "        }\n" +
                "        int e = a + b; return c + d;\n" +
                "    }";
        Set<String> expressionSet = ExpressionUtils.getExpressionsInMethod(source);
        assertTrue(expressionSet.size() == 2);
        assertTrue(expressionSet.contains("a + 1"));
        assertTrue(expressionSet.contains("c + d"));
    }

    @Test
    public void testMath105(){
        String source = "    public double getSumSquaredErrors() {\n" +
                "        return sumYY - sumXY * sumXY / sumXX;\n" +
                "    }";
        Set<String> expressionSet = ExpressionUtils.getExpressionsInMethod(source);
        assertTrue(expressionSet.size() == 1);
        assertTrue(expressionSet.contains("sumYY - sumXY * sumXY / sumXX"));
    }



}
