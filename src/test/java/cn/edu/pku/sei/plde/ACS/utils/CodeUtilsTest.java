package cn.edu.pku.sei.plde.ACS.utils;

import org.junit.Test;

import java.io.File;

/**
 * Created by yjxxtd on 4/26/16.
 */
public class CodeUtilsTest {

    @Test
    public void testGetMethodAnnotation(){
        String code = FileUtils.getCodeFromFile(new File(System.getProperty("user.dir")+"/filesfortest/CodeTest.code"));
        String methodName = "solve";
        int line = 164;
        String exceptionAnnotation = AnnotationUtils.getExceptionAnnotation(code, methodName, line,"IllegalArgumentException");
        System.out.println(exceptionAnnotation);
        String returnAnnotation = AnnotationUtils.getReturnAnnotation(code, methodName, line);
        System.out.println(returnAnnotation);
    }


    @Test
    public void testGetVariableInMethod(){
//        String methodSrc = "\tpublic int method(int c){\n" +
//                "\t\tint a = f(d);\n" +
//                "\t\treturn b.e;\n" +
//                "\t}";
//        Set<String> suspiciousVariables = new HashSet<String>();
//        suspiciousVariables.add("a");
//        suspiciousVariables.add("g");
//        CodeUtils.getVariableInMethod(methodSrc, suspiciousVariables);
//        assertTrue(suspiciousVariables.size() == 1);
//        assertTrue(suspiciousVariables.contains("a"));
//
//        List<String> list = new ArrayList<String>();
//        for(int i = 1; i <= 106; i ++){
//            list.add("Math_" + i);
//        }
//        for(int i = 1; i <= 27; i ++){
//            list.add("Time_" + i);
//        }
//        for(int i = 1; i <= 26; i ++){
//            list.add("Chart_" + i);
//        }
//        for(int i = 1; i <= 65; i ++){
//            list.add("Lang_" + i);
//        }
//        for(int i = 1; i <= 133; i ++){
//            list.add("Closure_" + i);
//        }
//        FileWriter fw = null;
//        try {
//            fw = new FileWriter("compile.sh");
//            for(String str : list) {
//                fw.write("cd " + str + "\n");
//                fw.write("defects4j compile" + "\n");
//                fw.write("cd .." + "\n");
//            }
//        } catch (Exception e) {
//            System.out.println(e.toString());
//        } finally {
//            if (fw != null)
//                try {
//                    fw.close();
//                } catch (IOException e) {
//                    throw new RuntimeException("关闭失败！");
//                }
//        }
    }

    @Test
    public void testSpreadFor(){
        String code = "public void testFactorial() {" + "for (int i = 1; i < 21; i++) {"
                + "assertEquals(i + \"! \", factorial(i), MathUtils.factorial(i));"
                + "assertEquals(i + \"! \", (double)factorial(i), MathUtils.factorialDouble(i), Double.MIN_VALUE);"
                + "assertEquals(i + \"! \", Math.log((double)factorial(i)), MathUtils.factorialLog(i), 10E-12);" + "}"
                + "assertEquals(\"0\", 1, MathUtils.factorial(0));"
                + "assertEquals(\"0\", 1.0d, MathUtils.factorialDouble(0), 1E-14);"
                + "assertEquals(\"0\", 0.0d, MathUtils.factorialLog(0), 1E-14);" + "}";
        String spreadCode = CodeUtils.spreadFor(code);
        System.out.println(spreadCode);
    }

    


}
