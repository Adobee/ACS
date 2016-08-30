package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import cn.edu.pku.sei.plde.conqueroverfitting.gatherer.GathererJava;
import cn.edu.pku.sei.plde.conqueroverfitting.junit.JunitRunner;
import cn.edu.pku.sei.plde.conqueroverfitting.main.Config;
import com.google.common.util.concurrent.Service;
import com.gzoltar.core.GZoltar;
import com.gzoltar.core.instr.testing.TestResult;
import javassist.NotFoundException;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by yanrunfa on 16/3/8.
 */
public class TestUtils {
    /**
     *
     * @param classpath
     * @param testPath
     * @param classname
     * @return
     */
    public static String getTestTrace(List<String> classpath, String testPath, String classname, String functionname) throws NotFoundException{
        ArrayList<String> classpaths = new ArrayList<String>();
        for (String path: classpath){
            classpaths.add(path);
        }
        classpaths.add(testPath);
        GZoltar gzoltar;
        try {
            gzoltar = new GZoltar(System.getProperty("user.dir"));
            gzoltar.setClassPaths(classpaths);
            gzoltar.addPackageNotToInstrument("org.junit");
            gzoltar.addPackageNotToInstrument("junit.framework");
            gzoltar.addTestPackageNotToExecute("junit.framework");
            gzoltar.addTestPackageNotToExecute("org.junit");
            gzoltar.addTestToExecute(classname);
            gzoltar.addClassNotToInstrument(classname);
            ExecutorService service = Executors.newSingleThreadExecutor();
            Future<Boolean> future = service.submit(new GzoltarRunProcess(gzoltar));
            try {
                future.get(Config.GZOLTAR_RUN_TIMEOUT, TimeUnit.SECONDS);
            } catch (InterruptedException e){
                future.cancel(true);
                service.shutdownNow();
                RuntimeUtils.killProcess();
                e.printStackTrace();
                return "timeout";
            } catch (TimeoutException e){
                future.cancel(true);
                service.shutdownNow();
                RuntimeUtils.killProcess();
                e.printStackTrace();
                return "timeout";
            } catch (ExecutionException e){
                service.shutdownNow();
                future.cancel(true);
                RuntimeUtils.killProcess();
                return "timeout";
            }
        } catch (NullPointerException e){
            throw new NotFoundException("Test Class " + classname +  " No Found in Test Class Path " + testPath);
        } catch (IOException e){
            return "";
        }
        List<TestResult> testResults = gzoltar.getTestResults();
        for (TestResult testResult: testResults){
            if (testResult.getName().substring(testResult.getName().lastIndexOf('#')+1).equals(functionname)){
                return testResult.getTrace();
            }
        }
        throw new NotFoundException("No Test Named "+functionname + " Found in Test Class " + classname);
    }
    public static String getTestTrace(String classpath, String testPath, String classname, String functionname) throws NotFoundException{
        return getTestTrace(Arrays.asList(classpath),testPath, classname, functionname);
    }


    public static String getTestTraceFromJunit(String classpath, String testPath, String className, String methodName) {
        String[] arg = {"java","-cp",buildClasspath(classpath, testPath, new ArrayList<String>(), new ArrayList<String>(Arrays.asList(PathUtils.getJunitPath()))) ,"cn.edu.pku.sei.plde.conqueroverfitting.junit.JunitRunner", className+"#"+methodName};
        try {
            return ShellUtils.shellRun(Arrays.asList(StringUtils.join(arg, " ")));
        } catch (IOException e){
            return null;
        }
    }


    public static String getTestTraceFromJunit(String classpath, String testPath,List<String> libPath,  String className, String methodName) {

        String[] arg = {"java","-cp",buildClasspath(classpath, testPath, libPath, new ArrayList<String>(Arrays.asList(PathUtils.getJunitPath()))) ,"cn.edu.pku.sei.plde.conqueroverfitting.junit.JunitRunner", className+"#"+methodName};
        try {
            return ShellUtils.shellRun(Arrays.asList(StringUtils.join(arg, " ")));
        } catch (IOException e){
            return null;
        }
    }


    public static String getDefects4jTestResult(String projectName){
        try {
            String result = ShellUtils.shellRun(Arrays.asList("cd project\n","cd "+projectName+"\n","defects4j test"));
            return result;
        } catch (IOException e){
            return "";
        }
    }

    public static int getFailTestNumInProject(String projectName){
        String testResult = getDefects4jTestResult(projectName);
        if (testResult.equals("")){//error occurs in run
            return Integer.MAX_VALUE;
        }
        if (!testResult.contains("Failing tests:")){
            return Integer.MAX_VALUE;
        }
        int errorNum = 0;
        for (String lineString: testResult.split("\n")){
            if (lineString.contains("Failing tests:")){
                errorNum =  Integer.valueOf(lineString.split(":")[1].trim());
            }
            if (lineString.contains("org.jfree.data.time.junit.MonthTests::testParseMonth")){
                errorNum --;
            }
        }
        return errorNum;
    }



    private static String buildClasspath(String classpath, String testClasspath, List<String> libPaths, List<String> additionalPath){
        if (libPaths.size()!=0){
            additionalPath = new ArrayList<>(additionalPath);
            additionalPath.addAll(libPaths);
        }
        String path = "\"";
        path += classpath;
        path += System.getProperty("path.separator");
        path += testClasspath;
        path += System.getProperty("path.separator");
        path += JunitRunner.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        path += System.getProperty("path.separator");
        path += StringUtils.join(additionalPath,System.getProperty("path.separator"));
        path += "\"";
        return path;
    }
}

class GzoltarRunProcess implements Callable<Boolean> {
    public GZoltar gzoltar;

    public GzoltarRunProcess(GZoltar gzoltar) {
        this.gzoltar = gzoltar;
    }

    public synchronized Boolean call() {
        if (Thread.interrupted()){
            return false;
        }
        gzoltar.run();
        return true;
    }
}