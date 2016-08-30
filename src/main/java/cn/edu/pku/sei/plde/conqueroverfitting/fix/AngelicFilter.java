package cn.edu.pku.sei.plde.conqueroverfitting.fix;

import cn.edu.pku.sei.plde.conqueroverfitting.assertCollect.Asserts;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.TraceResult;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.*;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by yanrunfa on 5/4/16.
 */
public class AngelicFilter {
    private Suspicious suspicious;
    private final String classPath;
    private final String srcPath;
    private final String testClassPath;
    private final String testSrcPath;
    private final String className;
    private final String methodName;
    private final String code;
    private final String project;

    private String testClassName;
    private String testMethodName;
    private File javaBackup;
    private File classBackup;
    private File targetFile;

    public AngelicFilter(Suspicious suspicious, String project){
        this.suspicious = suspicious;
        this.project = project;
        classPath = suspicious._classpath;
        srcPath = suspicious._srcPath;
        testClassPath = suspicious._testClasspath;
        testSrcPath = suspicious._testSrcPath;
        className = suspicious.classname();
        methodName = suspicious.functionnameWithoutParam();
        code = FileUtils.getCodeFromFile(srcPath, className);
        targetFile = new File(FileUtils.getFileAddressOfJava(srcPath, className));
    }

    public Map<String, List<String>> filter(int targetLine, Map<String, List<String>> boundary, List<TraceResult> traceResults){
        Map<String, List<String>> result = new HashMap<>();
        for (Map.Entry<String, List<String>> entry: boundary.entrySet()){
            if (!filterSingle(targetLine, entry.getKey(), traceResults)){
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private boolean filterSingle(int targetLine, String assertMessage,List<TraceResult> traceResults){
        String targetLineString = CodeUtils.getLineFromCode(code, targetLine-1);
        if (!LineUtils.isIfLine(targetLineString)){
            return false;
        }
        testClassName = assertMessage.split("#")[0];
        testMethodName = assertMessage.split("#")[1];
        String ifExpression = LineUtils.getIfExpression(targetLineString);
        String value = getExpressionValye(ifExpression, traceResults);
        if (value.equals("")){
            return false;
        }
        String newIfString;
        if (value.equals("true")){
            newIfString = getNewIfString(targetLineString, true);
        }
        else {
            newIfString = getNewIfString(targetLineString, false);
        }
        backup();
        SourceUtils.insertIfStatementToSourceFile(targetFile, newIfString,targetLine-1,-1,true);
        make();
        Asserts asserts = new Asserts(classPath,srcPath, testClassName, testSrcPath, testClassName, testMethodName, project);
        recovery();
        int errAssertAfterFix = asserts.errorNum();
        if (errAssertAfterFix != 0){
            return true;
        }
        return false;
    }

    private String getNewIfString(String preIfString, boolean value){
        String ifEnd = preIfString.substring(preIfString.lastIndexOf(")"));
        String ifStart = preIfString.substring(0, preIfString.lastIndexOf(')'));
        if (value){
            return ifStart + "&& false "+ifEnd;
        }
        else {
            return ifStart + "|| true "+ifEnd;
        }
    }

    private String getExpressionValye(String ifExpression, List<TraceResult> traceResults){

        for (TraceResult traceResult: traceResults){
            if (!traceResult._testClass.equals(testClassName) || !traceResult._testMethod.equals(testMethodName)){
                continue;
            }
            if (traceResult.getResultMap().containsKey(ifExpression)){
                List<String> values = traceResult.getResultMap().get(ifExpression);
                if (values.size() == 1 &&(values.get(0).equals("true") || values.get(0).equals("false"))){
                    return values.get(0);
                }
            }
        }
        return "";
    }

    public void recovery(){
        FileUtils.copyFile(classBackup.getAbsolutePath(), FileUtils.getFileAddressOfClass(classPath, className));
        FileUtils.copyFile(javaBackup.getAbsolutePath(), FileUtils.getFileAddressOfJava(srcPath, className));

    }

    private void backup(){
        classBackup = new File(FileUtils.tempClassPath(className,"AngelicFilter"));
        FileUtils.copyFile(FileUtils.getFileAddressOfClass(classPath, className),classBackup.getAbsolutePath());

        javaBackup = new File(FileUtils.tempJavaPath(className,"AngelicFilter"));
        FileUtils.copyFile(FileUtils.getFileAddressOfJava(srcPath, className),javaBackup.getAbsolutePath());
    }

    private void make(){
        try {
            System.out.println(ShellUtils.shellRun(Arrays.asList("javac -Xlint:unchecked -source 1.6 -target 1.6 -cp "+ buildClasspath(Arrays.asList(PathUtils.getJunitPath())) +" -d "+ classPath+" "+ targetFile.getAbsolutePath())));
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private String buildClasspath(List<String> additionalPath){
        if (suspicious._libPath.size()!=0){
            additionalPath = new ArrayList<>(additionalPath);
            additionalPath.addAll(suspicious._libPath);
        }
        String path = "\"";
        path += classPath;
        path += System.getProperty("path.separator");
        path += testClassPath;
        path += System.getProperty("path.separator");
        path += StringUtils.join(additionalPath,System.getProperty("path.separator"));
        path += "\"";
        return path;
    }
}
