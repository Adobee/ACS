package cn.edu.pku.sei.plde.conqueroverfitting.sort;

import cn.edu.pku.sei.plde.conqueroverfitting.utils.JDTUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.junit.Test;

import static org.junit.Assert.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by yjxxtd on 4/4/16.
 */
public class VariableSortTest {
    @Test
    public void testVariableSortMath3(){
        Set<String> suspiciousVariableSet = new HashSet<String>();
        suspiciousVariableSet.add("len");
        String statements = "        final int len = a.length;\n" +
                "        if (len != b.length) {\n" +
                "            throw new DimensionMismatchException(len, b.length);\n" +
                "        }\n";
        VariableSort variableSort = new VariableSort(suspiciousVariableSet, statements);
        List<List<String>> sortVariable = variableSort.getSortVariable();
        assertTrue(sortVariable.size() == 1);
        assertTrue(sortVariable.get(0).size() == 1);
        assertTrue(sortVariable.get(0).contains("len"));
    }

    @Test
    public void testVariableSortMath4(){
        Set<String> suspiciousVariableSet = new HashSet<String>();
        suspiciousVariableSet.add("v2D");
        String statements = "               Line line1 = (Line) getHyperplane();\n" +
                "        Line line2 = (Line) subLine.getHyperplane();\n" +
                "\n" +
                "        // compute the intersection on infinite line\n" +
                "        Vector2D v2D = line1.intersection(line2);";
        VariableSort variableSort = new VariableSort(suspiciousVariableSet, statements);
        List<List<String>> sortVariable = variableSort.getSortVariable();
        assertTrue(sortVariable.size() == 1);
        assertTrue(sortVariable.get(0).size() == 1);
        assertTrue(sortVariable.get(0).contains("v2D"));
    }

    @Test
    public void testVariableSortMath25(){
        Set<String> suspiciousVariableSet = new HashSet<String>();
        suspiciousVariableSet.add("currentY");
        suspiciousVariableSet.add("c2");
        String statements = "                           double sx2 = 0;\n" +
                "            double sy2 = 0;\n" +
                "            double sxy = 0;\n" +
                "            double sxz = 0;\n" +
                "            double syz = 0;\n" +
                "\n" +
                "            double currentX = observations[0].getX();\n" +
                "            double currentY = observations[0].getY();\n" +
                "            double f2Integral = 0;\n" +
                "            double fPrime2Integral = 0;\n" +
                "            final double startX = currentX;\n" +
                "            for (int i = 1; i < observations.length; ++i) {\n" +
                "                // one step forward\n" +
                "                final double previousX = currentX;\n" +
                "                final double previousY = currentY;\n" +
                "                currentX = observations[i].getX();\n" +
                "                currentY = observations[i].getY();\n" +
                "\n" +
                "                // update the integrals of f<sup>2</sup> and f'<sup>2</sup>\n" +
                "                // considering a linear model for f (and therefore constant f')\n" +
                "                final double dx = currentX - previousX;\n" +
                "                final double dy = currentY - previousY;\n" +
                "                final double f2StepIntegral = dx * (previousY * previousY + previousY * currentY + currentY * currentY) / 3;\n" +
                "                final double fPrime2StepIntegral = dy * dy / dx;\n" +
                "\n" +
                "                final double x = currentX - startX;\n" +
                "                f2Integral += f2StepIntegral;\n" +
                "                fPrime2Integral += fPrime2StepIntegral;\n" +
                "\n" +
                "                sx2 += x * x;\n" +
                "                sy2 += f2Integral * f2Integral;\n" +
                "                sxy += x * f2Integral;\n" +
                "                sxz += x * fPrime2Integral;\n" +
                "                syz += f2Integral * fPrime2Integral;\n" +
                "            }\n" +
                "\n" +
                "            // compute the amplitude and pulsation coefficients\n" +
                "            double c1 = sy2 * sxz - sxy * syz;\n" +
                "            double c2 = sxy * sxz - sx2 * syz;\n" +
                "            double c3 = sx2 * sy2 - sxy * sxy;\n" +
                "            if ((c1 / c2 < 0) || (c2 / c3 < 0)) {\n" +
                "                final int last = observations.length - 1;\n" +
                "                // Range of the observations, assuming that the\n" +
                "                // observations are sorted.\n" +
                "                final double xRange = observations[last].getX() - observations[0].getX();\n" +
                "                if (xRange == 0) {\n" +
                "                    throw new ZeroException();\n" +
                "                }\n" +
                "                omega = 2 * Math.PI / xRange;\n" +
                "\n" +
                "                double yMin = Double.POSITIVE_INFINITY;\n" +
                "                double yMax = Double.NEGATIVE_INFINITY;\n" +
                "                for (int i = 1; i < observations.length; ++i) {\n" +
                "                    final double y = observations[i].getY();\n" +
                "                    if (y < yMin) {\n" +
                "                        yMin = y;\n" +
                "                    }\n" +
                "                    if (y > yMax) {\n" +
                "                        yMax = y;\n" +
                "                    }\n" +
                "                }\n" +
                "                a = 0.5 * (yMax - yMin);\n" +
                "            } else {";
        VariableSort variableSort = new VariableSort(suspiciousVariableSet, statements);
        List<List<String>> sortVariable = variableSort.getSortVariable();
        assertTrue(sortVariable.size() == 2);
        assertTrue(sortVariable.get(0).size() == 1);
        assertTrue(sortVariable.get(0).contains("c2"));
    }

    @Test
    public void testVariableSortMath63(){
        Set<String> suspiciousVariableSet = new HashSet<String>();
        suspiciousVariableSet.add("x");
        suspiciousVariableSet.add("y");
        //suspiciousVariableSet.add("w");
        String statements = "         if ((x == null) || (y == null)) {\n" +
                "            return !((x == null) ^ (y == null));\n" +
                "        }\n" +
                "        if (x.length != y.length) {\n" +
                "            return false;\n" +
                "        }\n" +
                "        for (int i = 0; i < x.length; ++i) {";
        VariableSort variableSort = new VariableSort(suspiciousVariableSet, statements);
        List<List<String>> sortVariable = variableSort.getSortVariable();
        assertTrue(sortVariable.size() == 1);
        assertTrue(sortVariable.get(0).size() == 2);
        assertTrue(sortVariable.get(0).contains("x"));
        assertTrue(sortVariable.get(0).contains("y"));
    }

    @Test
    public void testVariableSortMath99(){
        Set<String> suspiciousVariableSet = new HashSet<String>();
        suspiciousVariableSet.add("lcm");
        suspiciousVariableSet.add("a");
        String statements = "         if (a==0 || b==0){\n" +
                "            return 0;\n" +
                "        " +
                "        int lcm = " +
                "Math.abs(mulAndCheck(a / gcd(a, b), b));";


        VariableSort variableSort = new VariableSort(suspiciousVariableSet, statements);
        List<List<String>> sortVariable = variableSort.getSortVariable();
        assertTrue(sortVariable.size() == 2);
        assertTrue(sortVariable.get(0).size() == 1);
        assertTrue(sortVariable.get(0).contains("lcm"));
    }

    @Test
    public void testVariableSortLang24(){
        Set<String> suspiciousVariableSet = new HashSet<String>();
        suspiciousVariableSet.add("hasDecPoint");
        suspiciousVariableSet.add("i");
        suspiciousVariableSet.add("sz");
        String statements ="        if (StringUtils.isEmpty(str)) {\n" +
                "            return false;\n" +
                "        }\n" +
                "        char[] chars = str.toCharArray();\n" +
                "        int sz = chars.length;\n" +
                "        boolean hasExp = false;\n" +
                "        boolean hasDecPoint = false;\n" +
                "        boolean allowSigns = false;\n" +
                "        boolean foundDigit = false;\n" +
                "        // deal with any possible sign up front\n" +
                "        int start = (chars[0] == '-') ? 1 : 0;\n" +
                "        if (sz > start + 1) {\n" +
                "            if (chars[start] == '0' && chars[start + 1] == 'x') {\n" +
                "                int i = start + 2;\n" +
                "                if (i == sz) {\n" +
                "                    return false; // str == \"0x\"\n" +
                "                }\n" +
                "                // checking hex (it can't be anything else)\n" +
                "                for (; i < chars.length; i++) {\n" +
                "                    if ((chars[i] < '0' || chars[i] > '9') && (chars[i] < 'a' || chars[i] > 'f') && (chars[i] < 'A' || chars[i] > 'F')) {\n" +
                "                        return false;\n" +
                "                    }\n" +
                "                }\n" +
                "                return true;\n" +
                "            }\n" +
                "        }\n" +
                "        sz--; // don't want to loop to the last char, check it afterwords\n" +
                "              // for type qualifiers\n" +
                "        int i = start;" +
                "        while (i < sz || (i < sz + 1 && allowSigns && !foundDigit)) {\n" +
                "            if (chars[i] >= '0' && chars[i] <= '9') {\n" +
                "                foundDigit = true;\n" +
                "                allowSigns = false;\n" +
                "\n" +
                "            } else if (chars[i] == '.') {\n" +
                "                if (hasDecPoint || hasExp) {\n" +
                "                    // two decimal points or dec in exponent   \n" +
                "                    return false;\n" +
                "                }\n" +
                "                hasDecPoint = true;\n" +
                "            } else if (chars[i] == 'e' || chars[i] == 'E') {\n" +
                "                // we've already taken care of hex.\n" +
                "                if (hasExp) {\n" +
                "                    // two E's\n" +
                "                    return false;\n" +
                "                }\n" +
                "                if (!foundDigit) {\n" +
                "                    return false;\n" +
                "                }\n" +
                "                hasExp = true;\n" +
                "                allowSigns = true;\n" +
                "            } else if (chars[i] == '+' || chars[i] == '-') {\n" +
                "                if (!allowSigns) {\n" +
                "                    return false;\n" +
                "                }\n" +
                "                allowSigns = false;\n" +
                "                foundDigit = false; // we need a digit after the E\n" +
                "            } else {\n" +
                "                return false;\n" +
                "            }\n" +
                "            i++;\n" +
                "        }\n" +
                "        if (i < chars.length) {\n" +
                "            if (chars[i] >= '0' && chars[i] <= '9') {\n" +
                "                // no type qualifier, OK\n" +
                "                return true;\n" +
                "            }\n" +
                "            if (chars[i] == 'e' || chars[i] == 'E') {\n" +
                "                // can't have an E at the last byte\n" +
                "                return false;\n" +
                "            }\n" +
                "            if (chars[i] == '.') {\n" +
                "                if (hasDecPoint || hasExp) {\n" +
                "                    // two decimal points or dec in exponent\n" +
                "                    return false;\n" +
                "                }\n" +
                "                // single trailing decimal point after non-exponent is ok\n" +
                "                return foundDigit;\n" +
                "            }\n" +
                "            if (!allowSigns && (chars[i] == 'd' || chars[i] == 'D'|| chars[i] == 'f'|| chars[i] == 'F')) {\n" +
                "                return foundDigit;\n" +
                "            }\n" +
                "            if (chars[i] == 'l' || chars[i] == 'L') {\n" +
                "                // not allowing L with an exponent or decimal point";



        VariableSort variableSort = new VariableSort(suspiciousVariableSet, statements);
        List<List<String>> sortVariable = variableSort.getSortVariable();
        assertTrue(sortVariable.size() == 1);
        assertTrue(sortVariable.get(0).size() == 1);
        assertTrue(sortVariable.get(0).contains("hasDecPoint"));
    }

}
