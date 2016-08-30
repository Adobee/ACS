package cn.edu.pku.sei.plde.conqueroverfitting.assertCollect;
import cn.edu.pku.sei.plde.conqueroverfitting.slice.StaticSlice;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.*;
import com.sun.org.apache.bcel.internal.classfile.Code;
import com.sun.xml.internal.ws.api.ha.StickyFeature;
import javassist.NotFoundException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Created by yanrunfa on 16/3/11.
 */
public class Asserts {
    private String _trueClassPath = "";
    public String _classpath;
    public String _srcPath;
    public String _testClasspath;
    public String _testSrcPath;
    public String _testClassname;
    public String _testMethodName;
    private String _code;
    public List<String> _libPath;
    private String _methodCode;
    public int _methodStartLine;
    public int _methodEndLine;
    public List<Integer> _errorAssertLines = new ArrayList<>();
    public List<Integer> _errorThrowLines = new ArrayList<>();
    public int _assertNums;
    public List<String> _asserts;
    public Map<String, Integer> _assertLineMap;
    public Map<Integer,List<String>> _thrownExceptionMap = new HashMap<>();
    public String _project;
    public boolean timeout = false;

    public Asserts(String classpath, String srcPath, String testClasspath, String testSrcPath, String testClassname, String testMethodName, List<String> libPath, String project) {
        _libPath = libPath;
        _classpath = classpath;
        _srcPath = srcPath;
        _testClassname = testClassname;
        _testClasspath = testClasspath;
        _testSrcPath = testSrcPath;
        _testMethodName = testMethodName;
        _project = project;
        _code = FileUtils.getCodeFromFile(_testSrcPath, _testClassname);


        if (!_code.contains(_testMethodName) && _code.contains(" extends ")){
            String extendsClass = _code.split(" extends ")[1].substring(0, _code.split(" extends ")[1].indexOf("{"));
            String className = CodeUtils.getClassNameOfImportClass(_code, extendsClass);
            if (className.equals("")){
                className = CodeUtils.getPackageName(_code)+"."+extendsClass;
            }
            String extendsCode = FileUtils.getCodeFromFile(testSrcPath, className.trim());
            if (!extendsCode.equals("")){
                _code = extendsCode;
            }
        }
        _methodCode = FileUtils.getTestFunctionCodeFromCode(_code,_testMethodName, _testSrcPath);
        List<Integer> methodLines = CodeUtils.getSingleMethodLine(_code,_testMethodName);
        if (methodLines.size() == 2){
            _methodStartLine =methodLines.get(0);
            _methodEndLine = methodLines.get(1);
        }
        else {
            _methodStartLine = 0;
            _methodEndLine = 0;
        }
        _assertLineMap = CodeUtils.getAssertInTest(_code, testMethodName, _methodStartLine);
        _asserts = new ArrayList<>(_assertLineMap.keySet());
        _assertNums = _asserts.size();
        _errorAssertLines = getErrorAssertLine();
    }

    public Asserts(String classpath, String srcPath,  String testClasspath, String testSrcPath, String testClassname, String testMethodName, String project){
        this(classpath, srcPath, testClasspath, testSrcPath, testClassname, testMethodName, new ArrayList<String>(), project);
    }

    private List<Integer> getErrorAssertLine(){
        List<Integer> result = new ArrayList<>();
        File tempJavaFile = FileUtils.copyFile(
                FileUtils.getFileAddressOfJava(_testSrcPath, _testClassname),
                tempJavaPath(_testClassname));
        File originClassFile = new File(FileUtils.getFileAddressOfClass(_testClasspath, _testClassname));
        File backupClassFile = FileUtils.copyFile(originClassFile.getAbsolutePath(), originClassFile.getAbsolutePath()+".AssertsBackup");
        String oldTrace = "";
        while (true){
            int lineNum = 0;
            try{
                List<String> classpaths = new ArrayList<>(_libPath);
                classpaths.add(_classpath);
                String trace = TestUtils.getTestTrace(classpaths, _testClasspath, _testClassname, _testMethodName);
                if (trace == null || trace.equals(oldTrace) || trace.contains("NoClassDefFoundError") || trace.contains("NoSuchMethodError")){
                    break;
                }
                if (trace.equals("timeout")){
                    timeout = true;
                    break;
                }
                oldTrace = trace;
                List<String> thrownExceptions = new ArrayList<>();
                for (String line : trace.split("\n")){
                    if (line.contains(_testClassname) && line.contains(_testMethodName) && line.contains("(") && line.contains(")") && line.contains(":")){
                        lineNum =  Integer.valueOf(line.substring(line.lastIndexOf("(")+1,line.lastIndexOf(")")).split(":")[1]);
                    }
                    if (line.contains("Exception")){
                        thrownExceptions.add(line);
                    }
                }
                if (result.contains(lineNum)) {
                    break;
                }
                String code = FileUtils.getCodeFromFile(tempJavaFile);
                String lineString = CodeUtils.getLineFromCode(code,lineNum).trim();
                if (AssertUtils.isAssertLine(lineString, code)){
                    if (LineUtils.isLineInFor(code, lineNum)){
                        RecordUtils.record("Assert In For: "+_project+" Test: "+_testClassname+"#"+_testMethodName+"#"+lineNum, "assertInFor");
                    }
                    _thrownExceptionMap.put(lineNum, thrownExceptions);
                    result.add(lineNum);
                }
                if (lineString.startsWith("fail(")){
                    int num = lineNum;
                    while (!CodeUtils.getLineFromCode(FileUtils.getCodeFromFile(tempJavaFile),num).trim().startsWith("try")){
                        SourceUtils.commentCodeInSourceFile(tempJavaFile, num);
                        num--;
                    }
                }
                else if (LineUtils.isLineInFailBlock(code, lineNum)){//如果在fail的语句内抛出异常
                    for (int i=lineNum; i< _methodEndLine; i++){
                        if (CodeUtils.getLineFromCode(code, i).trim().startsWith("fail(")){
                            int num = i;
                            while (!CodeUtils.getLineFromCode(FileUtils.getCodeFromFile(tempJavaFile),num).trim().startsWith("try")){
                                SourceUtils.commentCodeInSourceFile(tempJavaFile, num);
                                num--;
                            }
                            _thrownExceptionMap.put(i, thrownExceptions);
                            result.add(i);
                            _errorThrowLines.add(lineNum);
                            break;
                        }
                    }
                }
                else if (!AssertUtils.isAssertLine(lineString,code)){
                    _errorThrowLines.add(lineNum);
                    break;
                }
                SourceUtils.commentCodeInSourceFile(tempJavaFile,lineNum);
                System.out.println(ShellUtils.shellRun(Arrays.asList("javac -Xlint:unchecked -source 1.6 -target 1.6 -cp "+ buildClasspath(Arrays.asList(PathUtils.getJunitPath(),_testClasspath,_classpath)) +" -d "+_testClasspath+" "+ tempJavaFile.getAbsolutePath())));
            }
            catch (NotFoundException e){
                System.out.println("ERROR: Cannot Find Source File: " + _testClassname + " in temp file package\n");
                break;
            }
            catch (IOException e){
                e.printStackTrace();
                break;
            }
        }
        originClassFile.delete();
        tempJavaFile.delete();
        backupClassFile.renameTo(originClassFile);
        return result;
    }

    public List<Integer> dependenceOfAssert(int assertLine){
        Set<Integer> dependences = new HashSet<>();
        dependences.add(assertLine);
        Set<Integer> result = new HashSet<>();
        String assertString = CodeUtils.getLineFromCode(_code, assertLine);
        if (assertString.startsWith("fail(")){
            boolean tryed = false;
            boolean catched = false;
            for (int i=1; i<assertLine; i++){
                if (tryed && catched){
                    break;
                }
                String lastLine = CodeUtils.getLineFromCode(_code, assertLine-i);
                String nextLine = CodeUtils.getLineFromCode(_code, assertLine+i);

                if (!lastLine.contains("try") && !tryed){
                    dependences.add(assertLine-i);
                    result.add(assertLine-i);
                }else {
                    tryed = true;
                }
                if (!nextLine.contains("catch") && !catched){
                    dependences.add(assertLine+i);
                    result.add(assertLine+i);
                }
                else {
                    catched = true;
                }
            }
        }
        int brackets = CodeUtils.countChar(assertString, '(')-CodeUtils.countChar(assertString, ')');
        while (brackets != 0){
            dependences.add(++assertLine);
            assertString = CodeUtils.getLineFromCode(_code, assertLine);
            brackets += CodeUtils.countChar(assertString, '(');
            brackets -= CodeUtils.countChar(assertString, ')');
        }
        while (dependences.size()!=0){
            result.addAll(dependences);
            Set<Integer> newDependences = new HashSet<>();
            for (int dependence: dependences){
                newDependences.addAll(lineStaticAnalysis(dependence));
            }
            dependences.clear();
            dependences.addAll(newDependences);
            if (result.containsAll(newDependences)){
                break;
            }
        }

        result.addAll(dependences);
        return new ArrayList<>(result);
    }

    public List<Integer> lineStaticAnalysis(int analysisLine){
        List<Integer> dependences = new ArrayList<>();
        String lineString = CodeUtils.getLineFromCode(_code, analysisLine);
        if (lineString.contains("=")){
            lineString = lineString.substring(lineString.indexOf("=")+1);
        }
        List<String> params = CodeUtils.divideParameter(lineString, 2, false);
        for (String param: params){
            StaticSlice staticSlice = new StaticSlice(CodeUtils.getMethodBodyFromMethod(_methodCode), param);
            String result = staticSlice.getSliceStatements();

            if (result.equals("")){
                continue;
            }
            for (String line: result.split("\n")){
                if (AssertUtils.isAssertLine(line, _code)){
                    continue;
                }
                int lineNum = CodeUtils.getLineNumOfLineString(_code, line, _methodStartLine);
                if (lineNum!=-1 && lineNum < analysisLine){
                    dependences.add(lineNum);
                }
            }
        }
        return dependences;
    }

    public int errorAssertNum(){
        return _errorAssertLines.size();
    }

    public int errorNum(){
        return _errorAssertLines.size()+_errorThrowLines.size();
    }

    public List<Integer> errorLines(){
        List<Integer> result = new ArrayList<>(_errorAssertLines);
        result.addAll(_errorThrowLines);
        return result;
    }

    public int trueAssertNum(){
        return _assertNums - _errorAssertLines.size();
    }

    private int count(String s,char c){
        int count= 0;
        for (int i=0; i< s.length(); i++){
            if (s.charAt(i)==c){
                count++;
            }
        }
        return count;
    }



    public String getTrueTestFile(){
        if (_errorAssertLines.size()>0){
            return getTrueTestFile(_errorAssertLines);
        }
        else {
            return "";
        }
    }


    public String getTrueTestFile(List<Integer> errorAssertLines){
        if (!_trueClassPath.equals("")){
            return _trueClassPath;
        }
        File tempJavaFile = FileUtils.copyFile(
                FileUtils.getFileAddressOfJava(_testSrcPath, _testClassname),
                FileUtils.tempJavaPath(_testClassname, "Asserts"));

        List<String> assertLines = new ArrayList<>(_asserts);
        for (int assertLine: errorAssertLines){
            String assertString = CodeUtils.getWholeLineFromCode(_code, assertLine);
            if (assertString.contains("fail(")){
                assertString += assertLine;
            }
            assertLines.remove(assertString);
        }
        Set<Integer> lineSet = new HashSet<>();
        for (String assrtString: assertLines){
            int line = _assertLineMap.get(assrtString);
            lineSet.add(line);
            lineSet.addAll(dependenceOfAssert(line));
            lineSet.removeAll(_errorThrowLines);
        }
        try {
            int beginLine = _methodStartLine + 1;
            int endLine = _methodEndLine;
            for (int i=beginLine; i<= endLine; i++){
                String lineString = CodeUtils.getLineFromCode(_code, i);
                if (!lineSet.contains(i) && LineUtils.isCommentable(lineString)){
                    SourceUtils.commentCodeInSourceFile(tempJavaFile, i);
                    if (lineString.contains("fail(") && LineUtils.isLineInFailBlock(_code,i)){
                        int j = i-1;
                        while (!CodeUtils.getLineFromCode(_code, j).contains("try {")){
                            SourceUtils.commentCodeInSourceFile(tempJavaFile, j--);
                        }
                    }
                }
            }
            System.out.println(ShellUtils.shellRun(Arrays.asList("javac -Xlint:unchecked -source 1.6 -target 1.6 -cp "+ buildClasspath(Arrays.asList(PathUtils.getJunitPath(),_testClasspath,_classpath)) +" "+ tempJavaFile.getAbsolutePath())));
        } catch (IOException e){
            e.printStackTrace();
            return "";
        }
        tempJavaFile.delete();
        _trueClassPath = tempJavaFile.getAbsolutePath().substring(0,tempJavaFile.getAbsolutePath().lastIndexOf("."))+".class";
        return _trueClassPath;
    }

    public String buildClasspath(List<String> pathList){
        pathList = new ArrayList<>(pathList);
        if (_libPath!= null){
            pathList.addAll(_libPath);
        }
        String path = "\"";
        path += StringUtils.join(pathList,System.getProperty("path.separator"));
        path += "\"";
        return path;
    }

    private static String tempJavaPath(String classname){
        if (!new File(System.getProperty("user.dir")+"/temp/assert/").exists()){
            new File(System.getProperty("user.dir")+"/temp/assert/").mkdirs();
        }
        return System.getProperty("user.dir")+"/temp/assert/"+classname.substring(classname.lastIndexOf(".")+1)+".java";
    }

    private static String tempClassPath(String classname){
        if (!new File(System.getProperty("user.dir")+"/temp/assert/").exists()){
            new File(System.getProperty("user.dir")+"/temp/assert/").mkdirs();
        }
        return System.getProperty("user.dir")+"/temp/assert/"+classname.substring(classname.lastIndexOf(".")+1)+".class";
    }
}
