package cn.edu.pku.sei.plde.conqueroverfitting.fix;

import cn.edu.pku.sei.plde.conqueroverfitting.assertCollect.Asserts;
import cn.edu.pku.sei.plde.conqueroverfitting.junit.JunitRunner;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.common.support.Factory;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * Created by yanrunfa on 16/4/1.
 */
public class MethodTwoFixer {
    private final String _classpath;
    private final String _testClassPath;
    private final String _classSrcPath;
    private final String _testSrcPath;
    private final String _className;
    private final String _methodName;
    private Suspicious _suspicious;
    private String _code;
    private String _methodCode;
    private Set<Integer> _errorLines;
    private int _methodStartLine;
    private int _methodEndLine;
    private int _errorTestNum;
    public String correctPatch;
    public int correctStartLine;
    public int correctEndLine;
    public List<String> triedPatch = new ArrayList<>();

    public MethodTwoFixer(Suspicious suspicious){
        _suspicious = suspicious;
        _classpath = suspicious._classpath;
        _testClassPath = suspicious._testClasspath;
        _classSrcPath = suspicious._srcPath;
        _testSrcPath = suspicious._testSrcPath;
        _className = suspicious.classname();
        _methodName = suspicious.functionnameWithoutParam();
        _code = FileUtils.getCodeFromFile(_classSrcPath, suspicious.classname());
        _methodCode  = CodeUtils.getMethodString(_code, suspicious.functionnameWithoutParam());
        _errorLines = suspicious.errorLines();
        List<Integer> methodLines = CodeUtils.getSingleMethodLine(_code, _methodName, _errorLines.iterator().next());
        if (methodLines.size()!= 2){
            _methodStartLine = 0;
            _methodEndLine = 0;
        }else {
            _methodStartLine = methodLines.get(0);
            _methodEndLine = methodLines.get(1);
        }
        _errorTestNum = SuspiciousFixer.FAILED_TEST_NUM;
    }

    public boolean fix(Map<String, List<String>> ifStrings, String project, boolean debug){
        return fix(ifStrings, _errorLines, project, debug);
    }

    public boolean fix(Map<String, List<String>> boundarys, Set<Integer> errorLines, String project, boolean debug){
        for (Map.Entry<String, List<String>> entry: boundarys.entrySet()){
            List<String> ifStrings = entry.getValue();
            ifStrings = TypeUtils.arrayDup(ifStrings);
            for (int errorLine: errorLines){
                List<Integer> ifLines = getIfLine(errorLine);
                if (ifLines.size()!=2){
                    continue;
                }
                for (String ifString: ifStrings){
                    if (ifString.equals("")){
                        continue;
                    }
                    if (ifString.contains(">") && ifString.contains("<") && !ifString.contains("<?>")){
                        continue;
                    }
                    int blockStartLine = ifLines.get(0);
                    int blockEndLine = ifLines.get(1);
                    String ifStatement="";
                    for (int endLine: getLinesCanAdd(blockStartLine, blockEndLine,_code)) {
                        String lastLineString = CodeUtils.getLineFromCode(_code, blockStartLine-1);
                        String wholeLineString = CodeUtils.getWholeLineFromCodeReverse(_code, blockStartLine-1);
                        boolean result = false;
                        if (!LineUtils.isIfAndElseIfLine(wholeLineString)) {
                            /*
                            ifStatement = getIfStatementFromString(ifString);
                            try {
                                result = fixWithAddIf(blockStartLine, endLine, ifStatement,entry.getKey(), false, project, debug);
                            } catch (TimeoutException e){
                                return false;
                            }
                            if (result) {
                                correctStartLine = blockStartLine-1;
                                correctEndLine = endLine;
                                correctPatch = ifStatement;
                                triedPatch.add(ifStatement);
                                return true;
                            }*/
                            continue;
                        }
                        else {
                            String ifEnd = lastLineString.substring(lastLineString.lastIndexOf(')'));
                            lastLineString = lastLineString.substring(0, lastLineString.lastIndexOf(')'));
                            if (!ifFilter(lastLineString, ifString)){
                                continue;
                            }
                            ifStatement =lastLineString+ "&&" + getIfStringFromStatement(getIfStatementFromString(ifString)) + ifEnd;
                            try {
                                result = fixWithAddIf(blockStartLine-1, endLine, ifStatement,entry.getKey(),  true, project, debug);
                            } catch (TimeoutException e){
                                return false;
                            }
                            if (result){
                                correctStartLine = blockStartLine-1;
                                correctEndLine = endLine;
                                correctPatch = ifStatement;
                                triedPatch.add(ifStatement);
                                return true;
                            }else{
                                ifStatement =lastLineString+ "||" +getIfStringFromStatement(ifString) + ifEnd;
                                try {
                                    result = fixWithAddIf(blockStartLine-1, endLine, ifStatement,entry.getKey(),  true, project, debug);
                                } catch (TimeoutException e){
                                    return false;
                                }
                                if (result){
                                    correctStartLine = blockStartLine-1;
                                    correctEndLine = endLine;
                                    correctPatch = ifStatement;
                                    triedPatch.add(ifStatement);
                                    return true;
                                }
                            }
                        }
                    }

                }
            }
        }
        return false;
    }

    private boolean ifFilter(String ifBefore, String newIf){
        ifBefore = ifBefore.replace(" ","");
        if (ifBefore.startsWith("if(") || ifBefore.startsWith("}elseif(")){
            ifBefore = ifBefore.substring(ifBefore.indexOf("(")+1);
        }
        newIf = newIf.replace(" ","");
        newIf = removeBracket(getIfStringFromStatement(newIf));
        if (newIf.equals(ifBefore)){
            return false;
        }
        if (ifBefore.contains(">")){
            try {
                if (ifBefore.contains(">=")){
                    MathUtils.parseStringValue(ifBefore.substring(ifBefore.indexOf("=")+1));
                }
                else {
                    MathUtils.parseStringValue(ifBefore.substring(ifBefore.indexOf(">")+1));
                }
            } catch (NumberFormatException e){
                return false;
            }
            if (newIf.contains(ifBefore.substring(0, ifBefore.indexOf(">")))){
                return true;
            }
        }
        if (ifBefore.contains("<")){
            try {
                if (ifBefore.contains("<=")){
                    MathUtils.parseStringValue(ifBefore.substring(ifBefore.indexOf("=")+1));
                }
                else {
                    MathUtils.parseStringValue(ifBefore.substring(ifBefore.indexOf("<")+1));
                }
            } catch (NumberFormatException e){
                return false;
            }
            if (newIf.contains(ifBefore.substring(0, ifBefore.indexOf("<")))){
                return true;
            }
        }
        return false;
    }

    private static List<Integer> getLinesCanAdd(int startLine, int endLine, String code){
        List<Integer> result = new ArrayList<>();
        int braceCount = 0;
        if (startLine == endLine){
            result.add(endLine);
            return result;
        }
        for (int i= endLine; i> startLine; i--){
            String lineString = CodeUtils.getLineFromCode(code, i);
            if (braceCount == 0){
                result.add(i);
            }
            braceCount += CodeUtils.countChar(lineString,'{');
            braceCount -= CodeUtils.countChar(lineString,'}');
        }
        return result;
    }

    private String getIfStatementFromString(String ifString){
        String statement =  ifString.replace("if (","if (!(");
        statement += "){";
        return statement;
    }

    private String getIfStringFromStatement(String ifStatement){
        return ifStatement.substring(ifStatement.indexOf('(')+1, ifStatement.lastIndexOf(')'));
    }

    private String removeBracket(String ifStatement){
        ifStatement = ifStatement.replace(" ","");
        if (ifStatement.startsWith("(") || ifStatement.startsWith("!(")){
            return getIfStringFromStatement(ifStatement);
        }
        return ifStatement;
    }

    private boolean fixWithAddIf(int ifStartLine, int ifEndLine, String ifStatement,String testMessage, boolean replace, String project, boolean debug) throws TimeoutException{
        String testClassName = testMessage.split("#")[0];
        String testMethodName = testMessage.split("#")[1];
        int assertLine = Integer.valueOf(testMessage.split("#")[2]);
        Asserts asserts = _suspicious._assertsMap.get(testClassName+"#"+testMethodName);
        AssertComment comment = new AssertComment(asserts, assertLine);
        List<String> thrownExceptions = new ArrayList<>();
        if (asserts._thrownExceptionMap.containsKey(assertLine)){
            thrownExceptions = asserts._thrownExceptionMap.get(assertLine);
        }
        if (!canBeEqualsNull(ifStatement, thrownExceptions)){
        //    return false;
        }
        comment.comment();

        File targetJavaFile = new File(FileUtils.getFileAddressOfJava(_classSrcPath, _className));
        File targetClassFile = new File(FileUtils.getFileAddressOfClass(_classpath, _className));
        File javaBackup = FileUtils.copyFile(targetJavaFile.getAbsolutePath(), FileUtils.tempJavaPath(_className,"MethodTwoFixer"));
        File classBackup = FileUtils.copyFile(targetClassFile.getAbsolutePath(), FileUtils.tempClassPath(_className,"MethodTwoFixer"));
        SourceUtils.insertIfStatementToSourceFile(targetJavaFile, ifStatement, ifStartLine, ifEndLine, replace);
        try {
            targetClassFile.delete();
            System.out.println(ShellUtils.shellRun(Arrays.asList("javac -Xlint:unchecked -source 1.6 -target 1.6 -cp "+ buildClasspath(Arrays.asList(PathUtils.getJunitPath())) +" -d "+_classpath+" "+ targetJavaFile.getAbsolutePath())));
        }
        catch (IOException e){
            FileUtils.copyFile(classBackup, targetClassFile);
            FileUtils.copyFile(javaBackup, targetJavaFile);
            comment.uncomment();
            return false;
        }
        if (!targetClassFile.exists()){ //编译不成功
            FileUtils.copyFile(classBackup, targetClassFile);
            FileUtils.copyFile(javaBackup, targetJavaFile);
            comment.uncomment();
            return false;
        }

        Asserts assertsAfterFix = new Asserts(_classpath,_classSrcPath, _testClassPath, _testSrcPath, testClassName, testMethodName, project);
        if (assertsAfterFix.timeout){
            comment.uncomment();
            return false;
        }
        int errAssertAfterFix = assertsAfterFix.errorNum();
        int errAssertBeforeFix = asserts.errorNum();
        System.out.print("Method 2 try patch: "+ifStatement+" in line: "+ifStartLine);
        if (errAssertAfterFix < errAssertBeforeFix || errAssertAfterFix == 0) {
            int errorTestNumAfterFix = TestUtils.getFailTestNumInProject(project);
            if (errorTestNumAfterFix == Integer.MAX_VALUE){
                comment.uncomment();
                throw new TimeoutException();
            }
            if (errorTestNumAfterFix < _errorTestNum){
                System.out.println(" fix success");
                if (debug){
                    FileUtils.copyFile(classBackup, targetClassFile);
                    FileUtils.copyFile(javaBackup, targetJavaFile);
                }
                SuspiciousFixer.FAILED_TEST_NUM = errorTestNumAfterFix;
                RecordUtils.recordIf(_code, ifStatement,ifStartLine,ifEndLine,replace,project);
                comment.uncomment();
                return true;
            }
        }
        FileUtils.copyFile(classBackup, targetClassFile);
        FileUtils.copyFile(javaBackup, targetJavaFile);
        comment.uncomment();
        System.out.println(" fix fail");
        return false;
    }


    private List<Integer> getIfLine(int errorLine){
        int braceCount = 0;
        for (int i= errorLine-1; i>_methodStartLine; i--){
            String lineString = CodeUtils.getLineFromCode(_code, i);
            braceCount += CodeUtils.countChar(lineString, '{');
            braceCount -= CodeUtils.countChar(lineString, '}');
        }
        if (braceCount > 0){
            return getBraceArea(errorLine);
        }
        else {
            return Arrays.asList(errorLine-1, errorLine);
        }
    }

    private boolean changeIfArea(List<Integer> area, int errorLine){
        int startLine = area.get(0);
        int endLine = area.get(1);
        if (errorLine - startLine >= errorLine - endLine){
            area.set(1, endLine+1);
        }
        else {
            area.set(0, startLine-1);
        }
        return area.get(0) > _methodStartLine && area.get(1) < _methodEndLine;
    }

    private List<Integer> getBraceArea(int errorLine){
        List<Integer> result = new ArrayList<>();
        int bracket = 0;
        for (int i = 0; i < _methodEndLine-_methodStartLine; i++){
            String lineString = CodeUtils.getLineFromCode(_code, errorLine-i);
            if (lineString.contains("{") || lineString.contains("}")){
                if (bracket == 0){
                    result.add(errorLine-i+1);
                    break;
                }
            }
            bracket += CodeUtils.countChar(lineString, '}');
            bracket -= CodeUtils.countChar(lineString, '{');
        }
        bracket = 0;
        for (int i = 0; i<_methodEndLine-_methodStartLine; i++){
            String lineString = CodeUtils.getLineFromCode(_code, errorLine+i);
            if (result.size() != 0){
                if (lineString.contains("}")){
                    if (bracket == 0){
                        result.add(errorLine+i);
                        break;
                    }
                }
                bracket += CodeUtils.countChar(lineString, '{');
                bracket -= CodeUtils.countChar(lineString, '}');
            }
        }
        return result;
    }

    private String buildClasspath(List<String> additionalPath){
        String path = "\"";
        path += _classpath;
        path += System.getProperty("path.separator");
        path += _testClassPath;
        path += System.getProperty("path.separator");
        path += JunitRunner.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        path += System.getProperty("path.separator");
        path += StringUtils.join(additionalPath,System.getProperty("path.separator"));
        path += System.getProperty("path.separator");
        path += StringUtils.join(_suspicious._libPath,System.getProperty("path.separator"));
        path += "\"";
        return path;
    }

    private static boolean canBeEqualsNull(String ifString, List<String> thrownExceptions){
        if (!ifString.contains("null")){
            return true;
        }
        for (String exception: thrownExceptions){
            if (exception.contains("java.lang.NullPointerException")){
                return true;
            }
        }
        return false;
    }


}
