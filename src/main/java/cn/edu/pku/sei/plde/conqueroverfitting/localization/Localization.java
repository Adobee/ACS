package cn.edu.pku.sei.plde.conqueroverfitting.localization;

import cn.edu.pku.sei.plde.conqueroverfitting.assertCollect.Asserts;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.gzoltar.StatementExt;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.CodeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.RecordUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.TestUtils;
import com.gzoltar.core.components.Statement;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.common.SuspiciousField;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.metric.Metric;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.metric.Ochiai;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.common.synth.TestClassesFinder;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.gzoltar.GZoltarSuspiciousProgramStatements;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.common.library.JavaLibrary;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Created by localization on 16/1/23.
 */

public class Localization  {
    public String classpath;
    public String testClassPath;
    public String[] testClasses;
    public String testSrcPath;
    public String srcPath;
    public String project;
    public List<String> libPaths = new ArrayList<>();

    /**
     * @param classPath the path of project's class file
     * @param testClassPath the path of project's test class file
     */
    public Localization(String classPath, String testClassPath, String testSrcPath, String srcPath, List<String> libPaths, String project) {
        this(classPath, testClassPath, testSrcPath, srcPath);
        this.libPaths = libPaths;
        this.project = project;
    }


    public Localization(String classPath, String testClassPath, String testSrcPath, String srcPath){
        this.classpath = classPath;
        this.testClassPath = testClassPath;
        this.testSrcPath = testSrcPath;
        this.srcPath = srcPath;
        testClasses = new TestClassesFinder().findIn(JavaLibrary.classpathFrom(testClassPath), false);
        Arrays.sort(testClasses);
    }

    public Localization(String classPath, String testClassPath, String testSrcPath, String srcPath, String testClass){
        this.classpath = classPath;
        this.testClassPath = testClassPath;
        this.testSrcPath = testSrcPath;
        this.srcPath = srcPath;
        testClasses = new String[]{testClass};
    }

    public List<StatementExt> getSuspiciousList(){
        return this.getSuspiciousListWithMetric(new Ochiai());
    }


    public List<StatementExt> getSuspiciousListWithSuspiciousnessBiggerThanZero(){
        List<StatementExt> statements = this.getSuspiciousList();
        List<StatementExt> result = new ArrayList<StatementExt>();
        for (StatementExt statement: statements){
            if (statement.getSuspiciousness()>0){
                result.add(statement);
            }
        }
        return result;
    }

    public List<HashMap<SuspiciousField, String>> getSuspiciousListLite() {
        List<StatementExt> statements = this.getSuspiciousListWithSuspiciousnessBiggerThanZero();
        List<HashMap<SuspiciousField, String>> result = new ArrayList<HashMap<SuspiciousField, String>>();
        StatementExt firstline = statements.get(0);
        StatementExt lastline = statements.get(0);
        for (StatementExt statement: statements){
            if (getClassAddressFromStatement(statement).equals(getClassAddressFromStatement(firstline)) && getTargetFunctionFromStatement(statement).equals(getTargetFunctionFromStatement(firstline))){
                firstline = statement.getLineNumber() < firstline.getLineNumber() ? statement : firstline;
                lastline = statement.getLineNumber() > lastline.getLineNumber() ? statement : lastline;
            }else {
                HashMap<SuspiciousField, String> data = new HashMap<SuspiciousField, String>();
                data.put(SuspiciousField.class_address, getClassAddressFromStatement(firstline));
                data.put(SuspiciousField.error_tests, getErrorTestsStringFromStatement(firstline));
                data.put(SuspiciousField.line_number, getLineNumberFromStatement(firstline)+"-"+getLineNumberFromStatement(lastline));
                data.put(SuspiciousField.suspiciousness, getSupiciousnessFromStatement(firstline));
                data.put(SuspiciousField.target_function, getTargetFunctionFromStatement(firstline));
                result.add(data);
                firstline = statement;
                lastline = statement;
            }
        }
        return result;
    }

    public List<Suspicious> getSuspiciousLite(){
        return getSuspiciousLite(true);
    }

    public List<Suspicious> getSuspiciousLite(boolean jump){
        File suspicousFile = new File(System.getProperty("user.dir")+"/suspicious/"+ FileUtils.getMD5(StringUtils.join(testClasses,"")+classpath+testClassPath+srcPath+testSrcPath)+".sps");

        if (suspicousFile.exists() && jump ){
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(suspicousFile));
                List<Suspicious> result = (List<Suspicious>) objectInputStream.readObject();
                objectInputStream.close();
                //if (result.size()>12){
                //    result = result.subList(0,12);
                //}
                return result;
            }catch (Exception e){
                System.out.println("Reloading Localization Result...");
            }
        }

        List<StatementExt> statements = statementFilter(this.getSuspiciousListWithSuspiciousnessBiggerThanZero());
        List<Suspicious> result = new ArrayList<Suspicious>();
        if (statements.size() == 0){
            return result;
        }
        StatementExt firstline = statements.get(0);
        List<String> lineNumbers = new ArrayList<String>();
        for (StatementExt statement: statements){
            if (getClassAddressFromStatement(statement).equals(getClassAddressFromStatement(firstline)) && getTargetFunctionFromStatement(statement).equals(getTargetFunctionFromStatement(firstline))){
                lineNumbers.add(String.valueOf(statement.getLineNumber()));
            }else {
                result.add(new Suspicious(classpath, testClassPath,srcPath,testSrcPath, getClassAddressFromStatement(firstline), getTargetFunctionFromStatement(firstline), firstline.getSuspiciousness(), firstline.getTests(),firstline.getFailTests(), new ArrayList<String>(lineNumbers),libPaths));
                firstline = statement;
                lineNumbers.clear();
                if (!lineNumbers.contains(String.valueOf(statement.getLineNumber()))){
                    lineNumbers.add(String.valueOf(statement.getLineNumber()));
                }
            }
        }
        if (lineNumbers.size() != 0 && firstline.getTests().size()< 30){
            result.add(new Suspicious(classpath, testClassPath,srcPath,testSrcPath, getClassAddressFromStatement(firstline), getTargetFunctionFromStatement(firstline), firstline.getSuspiciousness(), firstline.getTests(),firstline.getFailTests(), new ArrayList<String>(lineNumbers),libPaths));
        }
        Collections.sort(result, new Comparator<Suspicious>() {
            @Override
            public int compare(Suspicious o1, Suspicious o2) {
                return Double.compare(o2._suspiciousness, o1._suspiciousness);
            }
        });
        if (!jump){
            return result;
        }
        try {
            suspicousFile.createNewFile();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(suspicousFile));
            objectOutputStream.writeObject(result);
            objectOutputStream.close();
        } catch (IOException e){
            e.printStackTrace();
            return new ArrayList<Suspicious>();
        }
        RecordUtils recordUtils = new RecordUtils("Localization");
        for (Suspicious suspicious: result){
            recordUtils.write(suspicious.classname()+"#"+suspicious.functionnameWithoutParam()+"#"+suspicious.getDefaultErrorLine()+"\n");
        }
        recordUtils.close();
        //if (result.size()>12){
        //    result = result.subList(0,12);
        //}
        return result;
    }

    private List<StatementExt> statementFilter(List<StatementExt> statements){
        List<StatementExt> result = new ArrayList<>();
        Map<String, String> errorLineMap = new HashMap<>();
        Map<String, String> testTraceMap = new HashMap<>();
        String packageName = "";

        RecordUtils recordUtils = new RecordUtils("RawLocalization");
        for (StatementExt statementExt: statements){
            recordUtils.write(getClassAddressFromStatement(statementExt)+"#"+getFunctionNameFromStatement(statementExt)+"#"+getLineNumberFromStatement(statementExt)+"\n");
        }
        recordUtils.close();
        for (int i=0; i< 2; i++){
            String[] test = testClasses[0].split("\\.");
            packageName += test[i];
            if (i!=2){
                packageName += ".";
            }
        }

        for (StatementExt statement: statements){
            if (statement.getName().contains("exception") || statement.getName().contains("Exception")){
                continue;
            }
            if (!statement.getLabel().trim().startsWith(packageName.trim())){
                continue;
            }
            /*
            for (String test: statement.getFailTests()) {
                String testClass = test.split("#")[0];
                String testMethod = test.split("#")[1];
                String code = FileUtils.getCodeFromFile(testSrcPath, testClass);
                String methodCode = FileUtils.getTestFunctionCodeFromCode(code, testMethod);
                if (methodCode.equals("")){
                    if (code.contains(" extends ")){
                        String extendsClass = code.split(" extends ")[1].substring(0, code.split(" extends ")[1].indexOf("{"));
                        String className = CodeUtils.getClassNameOfImportClass(code, extendsClass);
                        if (className.equals("")){
                            className = CodeUtils.getPackageName(code)+"."+extendsClass;
                        }
                        String extendsCode = FileUtils.getCodeFromFile(testSrcPath, className.trim());
                        if (!extendsCode.equals("")){
                            code = extendsCode;
                            methodCode = FileUtils.getTestFunctionCodeFromCode(code, testMethod);
                        }
                    }
                }

                if (statements.size() < 50) {
                    String errorAssertCode = "";
                    if (!errorLineMap.containsKey(test)) {
                        Asserts asserts = new Asserts(classpath, srcPath, testClassPath, testSrcPath, testClass, testMethod, libPaths, project);
                        if (asserts.errorLines().size() == 0) {
                            continue;
                        }
                        for (int i : asserts.errorLines()) {
                            errorAssertCode += CodeUtils.getLineFromCode(code, i) + "\n";
                        }
                        errorLineMap.put(test, errorAssertCode);
                    } else {
                        errorAssertCode = errorLineMap.get(test);
                    }
                    if (errorAssertCode.contains(getFunctionNameFromStatement(statement) + "(")) {
                        statement.setSuspiciousWeight(5.0f);
                        result.add(statement);
                    } else if (methodCode.contains(getFunctionNameFromStatement(statement))) {
                        result.add(statement);
                    }
                }
                else {
                    String trace = "";
                    if (!testTraceMap.containsKey(test)){
                        trace = TestUtils.getTestTraceFromJunit(classpath, testClassPath, libPaths, testClass, testMethod);
                        testTraceMap.put(test, trace);
                    }
                    else {
                        trace = testTraceMap.get(test);
                    }
                    boolean firstLineFlag = true;
                    for (String line: trace.split("\n")){
                        if (line.trim().startsWith("at ")){
                            if (line.contains(getClassAddressFromStatement(statement)+"."+getFunctionNameFromStatement(statement)+"(")){
                                if (firstLineFlag){
                                    statement.setSuspiciousWeight(3.0f);
                                }
                                else {
                                    statement.setSuspiciousWeight(3.0f*1.1f);
                                }
                                result.add(statement);
                                break;
                            }
                            firstLineFlag = false;
                        }
                    }
                }
                for (String lineString: methodCode.split("\n")) {
                    if (lineString.contains("(") && lineString.contains(")") && !lineString.contains("=")) {
                        String callMethod = lineString.substring(0, lineString.indexOf("(")).trim();
                        if (code.contains("void " + callMethod + "(")) {
                            result.add(statement);
                            break;
                        }
                    }
                }
                if (!result.contains(statement)){
                    statement.setSuspiciousWeight(0.5f);
                    if (statement.getLabel().contains("$")){
                        statement.setSuspiciousWeight(0.4f);
                    }
                    result.add(statement);
                }
            }*/
            result.add(statement);
        }

        return result;
    }


    public List<HashMap<SuspiciousField, String>> getSuspiciousListLiteWithSpecificLine(){
        List<StatementExt> statements = this.getSuspiciousListWithSuspiciousnessBiggerThanZero();
        List<HashMap<SuspiciousField, String>> result = new ArrayList<HashMap<SuspiciousField, String>>();
        StatementExt firstline = statements.get(0);
        Collection<String> lineNumbers = new ArrayList<String>();
        for (StatementExt statement: statements){
            if (getClassAddressFromStatement(statement).equals(getClassAddressFromStatement(firstline)) && getTargetFunctionFromStatement(statement).equals(getTargetFunctionFromStatement(firstline))){
                lineNumbers.add(String.valueOf(statement.getLineNumber()));
            }else {
                HashMap<SuspiciousField, String> data = new HashMap<SuspiciousField, String>();
                data.put(SuspiciousField.class_address, getClassAddressFromStatement(firstline));
                if (lineNumbers.size() == 1){
                    data.put(SuspiciousField.line_number, (String)lineNumbers.toArray()[0]);
                }else {
                    data.put(SuspiciousField.line_number, StringUtils.join(lineNumbers,"-"));
                }
                data.put(SuspiciousField.error_tests, getErrorTestsStringFromStatement(firstline));
                data.put(SuspiciousField.suspiciousness, getSupiciousnessFromStatement(firstline));
                data.put(SuspiciousField.target_function, getTargetFunctionFromStatement(firstline));
                result.add(data);
                firstline = statement;
                lineNumbers.clear();
                lineNumbers.add(String.valueOf(statement.getLineNumber()));
            }
        }

        return result;
    }


    /**
     *
     * @param metric the suspiciousness calculate metric
     * @return the list of suspicious statement
     */
    public List<StatementExt> getSuspiciousListWithMetric(Metric metric){
        URL[] classpaths = JavaLibrary.classpathFrom(testClassPath);
        classpaths = JavaLibrary.extendClasspathWith(classpath, classpaths);
        if (libPaths != null){
            for (String path: libPaths){
                classpaths = JavaLibrary.extendClasspathWith(path, classpaths);
            }
        }
        GZoltarSuspiciousProgramStatements gZoltar = GZoltarSuspiciousProgramStatements.create(classpaths, testClasses, new Ochiai(),testSrcPath, srcPath, libPaths);
        return gZoltar.sortBySuspiciousness(testClasses);
    }

    /**
     *
     * @param statement target statement
     * @return class address of statement
     */
    public static String getClassAddressFromStatement(Statement statement){
        return statement.getLabel().split("\\{")[0].replace(".head.",".");
    }

    public static String getClassNameFromStatement(Statement statement){
        String classAddress = getClassAddressFromStatement(statement);
        return classAddress.substring(classAddress.lastIndexOf(".")+1);
    }

    /**
     *
     * @param statement target statement
     * @return class address of statement
     */
    public static String getLineNumberFromStatement(Statement statement){
        return String.valueOf(statement.getLineNumber());
    }

    /**
     *
     * @param statement target statement
     * @return class address of statement
     */
    public static String getSupiciousnessFromStatement(Statement statement){
        return String.valueOf(statement.getSuspiciousness());
    }

    /**
     *
     * @param statement target statement
     * @return class address of statement
     */
    public static String getTargetFunctionFromStatement(Statement statement){
        return statement.getLabel().split("\\{")[1].split("\\)")[0]+"\\)";
    }

    public static String getFunctionNameFromStatement(Statement statement){
        return getTargetFunctionFromStatement(statement).split("\\(")[0];
    }

    public static String getErrorTestsStringFromStatement(StatementExt statementExt){
        return StringUtils.join(statementExt.getTests(),"-");
    }
}
