package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.internal.expressions.TypeExtension;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by yanrunfa on 5/31/16.
 */
public class MethodUtilTest {
    @Test
    public void testGetMethodReturnType(){
        String methodStatement =
                "public double getSumSquaredErrors() {\n" +
                "    return sumYY - sumXY * sumXY / sumXX;\n" +
                "}";
        Assert.assertEquals(MethodUtils.getMethodReturnType(methodStatement), "double");
    }
}
