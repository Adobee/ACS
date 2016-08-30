package cn.edu.pku.sei.plde.ACS.trace;

import cn.edu.pku.sei.plde.ACS.assertCollect.Asserts;
import cn.edu.pku.sei.plde.ACS.junit.JunitRunner;
import cn.edu.pku.sei.plde.ACS.localization.Localization;
import cn.edu.pku.sei.plde.ACS.localization.Suspicious;
import cn.edu.pku.sei.plde.ACS.type.TypeUtils;
import cn.edu.pku.sei.plde.ACS.utils.*;
import javassist.NotFoundException;
import org.apache.commons.collections.ArrayStack;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.*;

/**
 * Created by yanrunfa on 16/3/30.
 */
public class ErrorLineTracer {
    public Asserts asserts;
    public String classname;
    public String methodName;
    private String methodNameWithParam;
    public int methodStartLine;
    public int methodEndLine;
    public Suspicious suspicious;
    private String code;
    public Map<String,Integer> _commentedTestClass = new HashMap<>();

    public ErrorLineTracer(Suspicious suspicious, Asserts asserts, String classname, String methodName){
        this.asserts = asserts;
        this.classname = classname;
        this.suspicious = suspicious;
        this.methodName = methodName.substring(0, methodName.indexOf('('));
        this.code = FileUtils.getCodeFromFile(asserts._srcPath, classname);
        this.methodNameWithParam = methodName;

    }

    public List<Integer> trace(int defaultErrorLine, boolean isSuccess){
        List<Integer> methodLine =  CodeUtils.getSingleMethodLine(code, methodName, defaultErrorLine);
        if (methodLine.size() != 2){
            return Arrays.asList(defaultErrorLine);
        }
        methodStartLine = methodLine.get(0);
        methodEndLine = methodLine.get(1);

        List<Integer> result = new ArrayList<>();
        if (!isSuccess){
            List<Integer> tracedErrorLine = getErrorLine(defaultErrorLine);
            for (int line: tracedErrorLine){
                if (!suspicious.tracedErrorLine.contains(line)){
                    suspicious.tracedErrorLine.add(line);
                }
            }
            result.addAll(tracedErrorLine);
        }
        else {
            result.add(defaultErrorLine);
        }
        List<Integer> returnList = new ArrayList<>();
        for (int line: result){
            //line = errorLineOutOfSwitch(line, code);
            line = errorLineOutOfElse(line, code);
            String lineString = CodeUtils.getLineFromCode(code, line);
            if (!LineUtils.isIndependentLine(lineString)){
                for (int i= line-1; i > 0; i--){
                    String lineIString = CodeUtils.getLineFromCode(code, i);
                    if (lineIString.contains(";") || LineUtils.isIndependentLine(lineIString)){
                        if (!returnList.contains(i+1)){
                            returnList.add(i +1);
                        }
                        break;
                    }
                }
            }
            else {
                if (!returnList.contains(line)){
                    returnList.add(line);
                }
            }
        }
        for (int i= defaultErrorLine; i> methodStartLine; i--){
            String lineString = CodeUtils.getLineFromCode(code, i);
            if (LineUtils.isIfAndElseIfLine(lineString)){
                if (!returnList.contains(i+1)){
                    returnList.add(i+1);
                }
            }
        }
        return returnList;
    }

    private List<Integer> getErrorLine(int defaultErrorLine){
        List<Integer> result = new ArrayList<>();
        result.addAll(errorLineInConstructor(code, defaultErrorLine));
        result.addAll(errorLineInForLoop(code, methodName));

        List<Integer> lines = getErrorLineFromAssert(asserts);
        if (lines.size() == 0){
            try {
                String trace = TestUtils.getTestTrace(asserts._classpath, asserts._testClasspath, asserts._testClassname, asserts._testMethodName);
                if (trace != null){
                    for (String line: trace.split("\n")){
                        if (line.contains(classname+"."+methodName)){
                            String lineNumber = line.substring(line.indexOf("(")+1, line.indexOf(")")).split(":")[1];
                            lines.add(Integer.valueOf(lineNumber));
                            break;
                        }
                    }
                }
            } catch (NotFoundException e){}
        }
        for (int line: lines){
            if (line>0 && !result.contains(line)){
                result.add(line);
            }
        }
        result.add(defaultErrorLine);
        return result;
    }

    private List<Integer> errorLineInForLoop(String code, String methodName){
        List<Integer> result = new ArrayList<>();
        String methodCode = CodeUtils.getMethodString(code, methodName, methodStartLine);
        Map<String, String> methodParams = CodeUtils.getMethodParamsFromDefine(methodCode, methodName);
        Map<String, String> arrayParams = new HashMap<>();
        for (Map.Entry<String, String> entry: methodParams.entrySet()){
            if (TypeUtils.isSimpleArray(entry.getValue())){
                arrayParams.put(entry.getKey(),entry.getValue());
            }
        }
        for (String line: methodCode.split("\n")){
            if (LineUtils.isParameterTraversalForLoop(line, new ArrayList<String>(arrayParams.keySet()))) {
                result.add(CodeUtils.getLineNumOfLineString(code, line, methodStartLine) + 1);
            }
        }
        return result;
    }



    private List<Integer> errorLineInConstructor(String code, int defaultLine){
        List<Integer> result = new ArrayList<>();
        if (CodeUtils.isConstructor(classname, methodName)) {
            result.addAll(CodeUtils.getReturnLine(code, methodName, CodeUtils.getConstructorParamsCount(methodNameWithParam)));
            //List<Integer> methodLine = CodeUtils.getSingleMethodLine(code, methodName, defaultLine);
            //int startLine = methodLine.get(0);
            //while (!CodeUtils.getLineFromCode(code, startLine).contains("{")){
            //    startLine++;
            //}
            //startLine++;
            //if (CodeUtils.getLineFromCode(code, startLine).contains("super")){
            //    startLine++;
            //}
            //result.add(startLine);
        }

        return result;
    }

    private int errorLineOutOfSwitch(int defaultErrorLine, String code){
        String errorLineString = CodeUtils.getLineFromCode(code, defaultErrorLine);
        String lastLineString = CodeUtils.getLineFromCode(code, defaultErrorLine-1);
        if (errorLineString.contains("return ") && lastLineString.contains("case ")){
            for (int i = defaultErrorLine-2; i> asserts._methodStartLine; i--){
                if (CodeUtils.getLineFromCode(code, i).contains("switch ")){
                    defaultErrorLine = i;
                    break;
                }
            }
        }
        return defaultErrorLine;
    }


    private int errorLineOutOfElse(int defaultErrorLine, String code){
        String errorLineString = CodeUtils.getLineFromCode(code, defaultErrorLine);
        int braceCount = CodeUtils.countChar(errorLineString, '}');
        if (errorLineString.contains("else ")){
            for (int i = defaultErrorLine-1; i> methodStartLine; i--){
                String lineString = CodeUtils.getLineFromCode(code, i);
                braceCount += CodeUtils.countChar(lineString, '}');
                braceCount -= CodeUtils.countChar(lineString, '{');
                if (LineUtils.isIfLine(lineString) && braceCount == 0){
                    defaultErrorLine = i;
                    break;
                }
            }
        }
        return defaultErrorLine;
    }

    private List<Integer> getErrorLineFromAssert(Asserts asserts){
        List<Integer> assertLines = asserts._errorAssertLines;
        List<Integer> result = new ArrayList<>();
        if (assertLines.size() < 1){
            return result;
        }
        for (int assertLine: assertLines){
            List<Suspicious> suspiciouses = new ArrayList<>();
            File originJavaFile = new File(FileUtils.getFileAddressOfJava(asserts._testSrcPath, asserts._testClassname));
            File originClassFile = new File(FileUtils.getFileAddressOfClass(asserts._testClasspath,asserts._testClassname));
            File backupJavaFile = FileUtils.copyFile(originJavaFile.getAbsolutePath(), originJavaFile.getAbsolutePath()+".ErrorTraceBackup");
            File backupClassFile = FileUtils.copyFile(originClassFile.getAbsolutePath(), originClassFile.getAbsolutePath()+".ErrorTraceBackup");
            FileOutputStream outputStream = null;
            BufferedReader reader  = null;
            try {
                outputStream = new FileOutputStream(originJavaFile);
                reader = new BufferedReader(new FileReader(backupJavaFile));
                String lineString = null;
                List<Integer> functionLine = FileUtils.getTestFunctionLineFromCode(FileUtils.getCodeFromFile(backupJavaFile),asserts._testMethodName);
                int beginLine = functionLine.get(0)+1;
                int endLine = functionLine.get(1);
                int line = 0;
                int tryLine = -1;
                List<Integer> commitedAfter = new ArrayList<>();
                List<Integer> assertDependences = asserts.dependenceOfAssert(assertLine);
                int bracketCount = 0;
                while ((lineString = reader.readLine()) != null) {
                    line++;
                    if (lineString.trim().startsWith("try")){
                        tryLine = line+1;
                    }
                    if (lineString.trim().contains("catch")){
                        tryLine = -1;
                    }
                    if (line >= beginLine && line <= endLine && !assertDependences.contains(line) && LineUtils.isCommentable(lineString)){
                        if (lineString.trim().startsWith("fail(") && tryLine != -1){
                            for (int i= tryLine; i< line; i++){
                                commitedAfter.add(i);
                            }
                        }
                        bracketCount+= CodeUtils.countChar(lineString,'(');
                        bracketCount-= CodeUtils.countChar(lineString,')');
                        lineString = "//"+lineString;
                    }
                    else if (bracketCount > 0){
                        lineString = "//"+lineString;
                        bracketCount+= CodeUtils.countChar(lineString,'(');
                        bracketCount -= CodeUtils.countChar(lineString, ')');
                    }
                    outputStream.write((lineString+"\n").getBytes());

                }
                for (int num: commitedAfter){
                    SourceUtils.commentCodeInSourceFile(originJavaFile, num);
                }
                System.out.println(ShellUtils.shellRun(Arrays.asList("javac -Xlint:unchecked -source 1.6 -target 1.6 -cp "+ buildClasspath(Arrays.asList(PathUtils.getJunitPath())) +" -d "+ asserts._testClasspath+" "+ originJavaFile.getAbsolutePath())));
                new File(System.getProperty("user.dir")+"/temp/"+assertLine).mkdirs();
                System.out.println(ShellUtils.shellRun(Arrays.asList("javac -Xlint:unchecked -source 1.6 -target 1.6 -cp "+ buildClasspath(Arrays.asList(PathUtils.getJunitPath())) +" -d "+System.getProperty("user.dir")+"/temp/"+assertLine+"/ "+ originJavaFile.getAbsolutePath())));
                _commentedTestClass.put(FileUtils.getFileAddressOfClass(System.getProperty("user.dir")+"/temp/"+assertLine,asserts._testClassname), assertLine);
                Localization localization = new Localization(asserts._classpath, asserts._testClasspath, asserts._testSrcPath, asserts._srcPath, asserts._testClassname);
                suspiciouses = localization.getSuspiciousLite(false);

            } catch (NotFoundException e){
                System.out.println("ERROR: Cannot Find Source File: "+ asserts._testClassname +" in Source Path: "+ asserts._testSrcPath);
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }finally {
                if (outputStream != null){
                    try {
                        outputStream.close();
                    } catch (IOException e){
                    }
                }
                if (reader != null){
                    try {
                        reader.close();
                    } catch (IOException e){
                    }
                }
            }
            originJavaFile.delete();
            backupJavaFile.renameTo(originJavaFile);
            originClassFile.delete();
            backupClassFile.renameTo(originClassFile);

            for (Suspicious suspicious: suspiciouses){
                if (suspicious.functionnameWithoutParam().equals(methodName) && suspicious.classname().equals(classname)){
                    result.add(suspicious.getDefaultErrorLine());
                    break;
                }
            }
        }
        return result;
    }

    private String buildClasspath(List<String> additionalPath){
        if (asserts._libPath.size()!=0){
            additionalPath = new ArrayList<>(additionalPath);
            additionalPath.addAll(asserts._libPath);
        }
        String path = "\"";
        path += asserts._classpath;
        path += System.getProperty("path.separator");
        path += asserts._testSrcPath;
        path += System.getProperty("path.separator");
        path += JunitRunner.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        path += System.getProperty("path.separator");
        path += StringUtils.join(additionalPath,System.getProperty("path.separator"));
        path += "\"";
        return path;
    }

}
