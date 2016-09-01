package cn.edu.pku.sei.plde.ACS.fix;

import cn.edu.pku.sei.plde.ACS.slice.StaticSlice;
import cn.edu.pku.sei.plde.ACS.utils.*;

import org.apache.commons.lang.StringUtils;

import java.util.*;


/**
 * Created by yanrunfa on 16/2/16.
 */

public class ReturnCapturer {
    public final String _classpath;
    public final String _testclasspath;
    public final String _testsrcpath;
    public final String _classSrcPath;
    public String _testClassName;
    public String _testMethodName;
    public String _fileaddress;
    public String _testTrace;
    public String _classCode;
    public String _functionCode;
    public int _errorLineNum;
    public int _assertLine = -1;
    public String _classname;
    public String _methodName;

    /**
     *
     * @param classpath The class path
     * @param testclasspath The test's class path
     * @param testsrcpath the test's source path
     */
    public ReturnCapturer(String classpath, String classSrcPath, String testclasspath, String testsrcpath){
        _classpath = classpath;
        _testclasspath = testclasspath;
        _testsrcpath = testsrcpath;
        _classSrcPath = classSrcPath;
    }
    /**
     *
     * @param testClassName The test's class name to be fixed
     * @param testMethodName the name of test function
     * @return the fix string
     */
    public String getFixFrom(String testClassName, String testMethodName, String classname, String methodName){
        _testClassName = testClassName;
        _testMethodName = testMethodName;
        _classname = classname;
        _methodName = methodName;
        _fileaddress = _testsrcpath + System.getProperty("file.separator") + _testClassName.replace('.',System.getProperty("file.separator").charAt(0))+".java";

        try {
            String retrunString = run();
            if (retrunString.contains("throw")){
                return parseThrowException(retrunString);
            }
            return retrunString;
        } catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    private String parseThrowException(String throwString){
        String code = FileUtils.getCodeFromFile(_classSrcPath, _classname);
        String exception = throwString.substring(throwString.lastIndexOf(" ")+1, throwString.lastIndexOf("()"));
        String exceptionClass = CodeUtils.getClassNameOfImportClass(code, exception);
        if (exceptionClass.equals("")){
            exceptionClass = CodeUtils.getClassNameOfImportClass(_classCode, exception);
            if (exceptionClass.equals("")){
                return throwString;
            }
        }
        String exceptionCode = FileUtils.getCodeFromFile(_classSrcPath, exceptionClass);
        List<Integer> paramCount = CodeUtils.getMethodParamsCountInCode(exceptionCode, exception);
        int minParamCount = Collections.min(paramCount);
        if (minParamCount < 1){
            return throwString;
        }
        String param = "null";
        for (int i=1; i< minParamCount; i++){
            param += ",null";
        }
        return "import "+exceptionClass+";"+"///"+throwString.replace("()","("+param+")");
    }


    public String getFixFrom(String testClassName, String testMethodName, int assertLine, String classname, String methodName){
        _assertLine = assertLine;

        return getFixFrom(testClassName, testMethodName, classname, methodName);
    }

    private String run() throws Exception{
        _testTrace = TestUtils.getTestTrace(_classpath, _testclasspath,_testClassName,_testMethodName);
        if (_testTrace != null && _testTrace.equals("timeout")){
            return "";
        }
        _classCode = FileUtils.getCodeFromFile(_fileaddress);
        _functionCode = FileUtils.getTestFunctionCodeFromCode(_classCode, _testMethodName);
        _errorLineNum = getErrorLineNumFromTestTrace();
        if (_assertLine == -1){
            return fixTest();
        }
        else {
            return getFixFromLine(_assertLine);
        }
    }

    private int getErrorLineNumFromTestTrace(){
        if (_testTrace == null){
            return -1;
        }
        String[] traces = _testTrace.split("\n");
        for (String trace: traces){
            if (trace.contains(_testClassName+"."+_testMethodName)){
                return Integer.valueOf(trace.substring(trace.lastIndexOf(':')+1, trace.lastIndexOf(')')));
            }
        }
        return -1;
    }

    private String fixTest() throws Exception{
        //if (!_functionCode.contains("{") || _functionCode.contains("}")){
        //    return "";
        //}
        //String functionBody = _functionCode.substring(_functionCode.indexOf('{')+1,_functionCode.lastIndexOf('}'));
        String[] functionLines = _functionCode.split("\n");
        boolean annotationFlag = false;
        int errorLineIndex = 0;
        if (_errorLineNum > 0){
            String errorLine = _classCode.split("\n")[_errorLineNum -1].trim();
            for (int i=0; i< functionLines.length; i++){
                String functionLine = functionLines[i].replace("\n","").trim();
                if (functionLine.equals(errorLine)){
                    errorLineIndex = i;
                }
            }
        }
        for (int i=errorLineIndex; i<functionLines.length; i++){
            String detectingLine = functionLines[i].replace("\n","").trim();
            //clean the annotation
            if (detectingLine.contains("*/")){
                detectingLine = detectingLine.substring(detectingLine.indexOf("*/")+1);
                annotationFlag = false;
            }
            if (annotationFlag || detectingLine.startsWith("//")){
                continue;
            }
            if (detectingLine.contains("//")){
                detectingLine = detectingLine.substring(0,detectingLine.indexOf("//"));
            }
            if (detectingLine.contains("/*")){
                detectingLine = detectingLine.substring(0,detectingLine.indexOf("/*"));
                annotationFlag = true;
            }

            if (detectingLine.contains("Exception") && detectingLine.contains("catch")){
                return exceptionProcessing(detectingLine);
            }
            if (detectingLine.contains("assert")){
                return assertProcessing(detectingLine, _functionCode);
            }
        }
        //No Assert And Throw Exception Found
        if (_functionCode.startsWith("(expected")){
            String expectedClass = _functionCode.substring(_functionCode.indexOf("=")+1,_functionCode.indexOf(")"));
            return "throw new " +expectedClass.replace(".class","").trim() + "();";
        }

        System.out.println("No Fix Found for This Test");
        return "";
    }



    private String getFixFromLine(int assertLine){
        String lineString = CodeUtils.getLineFromCode(_classCode, assertLine);
        if (lineString.startsWith("fail(")){
            for (int i = 1; i < _functionCode.split("\n").length; i++){
                String nextLine = CodeUtils.getLineFromCode(_classCode, assertLine+i);
                if (!nextLine.contains("Exception") || !nextLine.contains("catch")){
                    continue;
                }
                return exceptionProcessing(nextLine);
            }
        }
        else if (lineString.contains("assert")){
            try {
                if (!_functionCode.contains("{") || !_functionCode.contains("}")){
                    return "";
                }
                String functionBody = _functionCode.substring(_functionCode.indexOf('{')+1,_functionCode.lastIndexOf('}'));
                String linesBefore = "";
                for (String line: functionBody.split("\n")){
                    if (line.trim().equals(lineString)){
                        break;
                    }
                    linesBefore += line +"\n";
                }
                return assertProcessing(lineString, linesBefore);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else if (_functionCode.startsWith("(expected")){
            String expectedClass = _functionCode.substring(_functionCode.indexOf("=")+1,_functionCode.indexOf(")"));
            return "throw new " +expectedClass.replace(".class","").trim() + "();";
        }
        else if (lineString.startsWith("check")){
            return "";
        }
        else if (lineString.contains("(") && lineString.contains(")") && !lineString.contains("=")){
            String callMethod = lineString.substring(0, lineString.indexOf("(")).trim();
            if (_classCode.contains("void "+callMethod+"(")){
                _functionCode = FileUtils.getTestFunctionCodeFromCode(_classCode, callMethod);
                try {
                    return fixTest();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

    private String exceptionProcessing(String exceptionLine){
        String exceptionName = exceptionLine.substring(exceptionLine.indexOf("(")+1, exceptionLine.indexOf(")")).trim().split(" ")[0];
        return "throw new " + exceptionName + "();";
    }

    private String assertProcessing(String assertLine, String statements) throws Exception{
        String assertType = assertLine.substring(0, assertLine.indexOf('('));
        List<String> parameters = CodeUtils.divideParameter(assertLine, 1, false);
        if (parameters.contains("Assert")){
            parameters.remove("Assert");
        }
        if (parameters.size() == 4){
            parameters.remove(0);
        }
        //if ( parameters.size() <2){
        //    System.out.println(Arrays.toString(parameters.toArray()));
        //    System.out.println("Function divideParameter Fail");
        //    return "";
        //}
        if (assertType.contains("assertEquals") || assertType.contains("assertSame")){
            String callExpression="";
            String returnExpression="";
            List<String> callParam;
            List<String> returnParam;
            String returnString;

            if (parameters.get(1).contains("(") && parameters.get(1).contains(")") && parameters.get(1).contains(_methodName)){
                callExpression = parameters.get(1);
                returnExpression = parameters.get(0);

            }
            else {
                callExpression = parameters.get(0);
                returnExpression = parameters.get(1);
                for (String line: statements.split("\n")){
                    if (line.contains(returnExpression) && line.contains(_methodName) && line.contains("(") && line.contains(")")){
                        callExpression = parameters.get(1);
                        returnExpression = parameters.get(0);
                    }
                }

            }
            callParam = CodeUtils.divideParameter(callExpression,1, false);
            if (callParam.size() == 0){
                callParam.add(callExpression);
            }
            returnParam = CodeUtils.divideParameter(returnExpression, 1, false);
            if (returnParam.size() == 0){
                returnParam.add(returnExpression);
            }
            returnString = "return "+returnExpression;
            if (callExpression.contains(".")){
                String testClass = callExpression.substring(0,callExpression.indexOf("."));
                if (returnExpression.contains(".")){
                    if (returnExpression.substring(0, returnExpression.indexOf(".")).equals(testClass)){
                        returnString = "return " + returnExpression.substring(returnExpression.indexOf(".")+1);
                    }

                }
            }

            //String attachLines = slicingProcess(returnParam, callParam, assertLine);
            String attachLines = staticSlicingProcess(returnParam, callParam, statements);
            String addonFunction = addonFunctions(returnExpression);
            returnString = syncParams(returnString);
            return attachLines + returnString + ";" + addonFunction;
        }
        else if (assertType.contains("assertNull")){
            return "return null;";
        }
        else if (assertType.contains("assertFalse")){
            return "return false;";
        }
        else if (assertType.contains("assertTrue")){
            if (parameters.get(0).contains(">=")){
                String numParam = parameters.get(0).split(">=")[0].contains(_methodName)?parameters.get(0).split(">=")[1]:parameters.get(0).split(">=")[0];
                if (numParam.trim().matches("^(-?\\d+)(\\.\\d+)?$")){
                    return "return "+numParam+";";
                }
            }
            String attachLines = staticSlicingProcess(parameters, new ArrayList<String>(), statements);
            for (String line: attachLines.split("\n")){
                if (LineUtils.isLineInCatchBlock(statements, line)){
                    String lineBefore = statements.split(line)[0];
                    if (lineBefore.contains("\n")){
                        String[] lines = lineBefore.split("\n");
                        for (int i = lines.length-1; i>=0; i--){
                            if (LineUtils.isCatchLine(lines[i])){
                                return "throw new "+ lines[i].substring(lines[i].indexOf('(')+1,lines[i].lastIndexOf(')')).split(" ")[0]+"();";
                            }
                        }
                    }
                }
            }
            return "return true;";
        }
        throw new Exception("Unknown assert type");
    }

    private String addonFunctions(String returnExpression){
        if (!returnExpression.contains("(")|| !returnExpression.contains(")")){
            return "";
        }
        String methodName = returnExpression.substring(0, returnExpression.indexOf("(")).trim();
        String methodCode = CodeUtils.getMethodString(_classCode, methodName);
        if (methodCode.equals("")){
            return "";
        }
        methodCode = methodCode.replace("public ","private ");
        methodCode = methodCode.replace("private ","private static ");
        return ">>>"+methodName+"<<<"+methodCode;
    }

    private String syncParams(String returnString){
        if (!(returnString.contains("(") && returnString.contains(")") && !returnString.contains("()"))){
            return returnString;
        }
        String code = FileUtils.getCodeFromFile(_classSrcPath, _classname);
        String methodCode = CodeUtils.getMethodString(code, _methodName);
        for (String line: methodCode.split("\n")){
            if (line.contains(_methodName) && line.contains("(") && line.contains(")")){
                List<String> params = CodeUtils.getMethodParamsName(line, _methodName);

                List<String> paramsInReturn = CodeUtils.divideParameter(returnString, 1);
                if (params.size()!=paramsInReturn.size()){
                    return returnString;
                }
                for (int i=0; i< params.size(); i++){
                    paramsInReturn.set(i,params.get(i));
                }
                return returnString.substring(0,returnString.indexOf("("))+"("+StringUtils.join(paramsInReturn,",")+")";
            }
        }
        return returnString;
    }

    private String staticSlicingProcess(List<String> returnParam, List<String> callParam, String statements){
        for (int i=0; i<returnParam.size(); i++){
            returnParam.remove("");
            callParam.remove("");
            if (StringUtils.isNumeric(returnParam.get(i))){
                returnParam.remove(i);
            }
        }
        for (int i=0; i<callParam.size(); i++){
            if (StringUtils.isNumeric(callParam.get(i))){
                callParam.remove(i);
            }
        }
        for (int i=0; i<callParam.size(); i++){
            if (returnParam.contains(callParam.get(i))){
                returnParam.remove(callParam.get(i));
            }
        }
        if (returnParam.size() == 0){
            return "";
        }
        String result = "";
        for (String param: returnParam){
            if (param.contains(".")){
                param = param.substring(param.lastIndexOf(".")+1);
            }
            StaticSlice staticSlice = new StaticSlice(statements, param);
            String sliceResult = staticSlice.getSliceStatements();
            List<String> trueResult = new ArrayList<>();
            for (String line: sliceResult.split("\n")){
                if (AssertUtils.isAssertLine(line,_classCode)){
                    continue;
                }
                trueResult.add(line);
            }
            result += StringUtils.join(trueResult,"\n");
        }
        return result;
    }
/*
    private String slicingProcess(List<String> returnParam, List<String> callParam, String assertLine) throws Exception{
        for (int i=0; i<returnParam.size(); i++){
            if (StringUtils.isNumeric(returnParam.get(i))){
                returnParam.remove(i);
            }
        }
        for (int i=0; i<callParam.size(); i++){
            if (StringUtils.isNumeric(callParam.get(i))){
                callParam.remove(i);
            }
        }
        for (int i=0; i<callParam.size(); i++){
           if (returnParam.contains(callParam.get(i))){
               returnParam.remove(callParam.get(i));
          }
        }
        if (returnParam.size() == 0){
            return "";
        }
        int assertLineNum = 0;
        String[] codeLines = _classCode.split("\n");
        for (int i=0;i<codeLines.length; i++){
            if (codeLines[i].contains(assertLine)) {
                assertLineNum = i+1;
            }
        }
        if (assertLineNum == 0){
            throw new NotFoundException("Cannot Found Assert Line :"+assertLine+" in Class "+_testClassName);
        }
        List<String> args = new ArrayList<String>();
        List<Integer> lineToBeAdd = new ArrayList<Integer>();
        String slicePath = Slicer.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        if (System.getProperty("os.name").toLowerCase().startsWith("win") && slicePath.charAt(0) == '/'){
            slicePath = slicePath.substring(1);
        }
        for (String var: returnParam){
            String[] arg = {"java -Xmx2g -jar",slicePath,"-p",Config.TEMP_FILES_PATH+"/test.trace",_testClassName+"."+_testMethodName+":"+assertLineNum+":{"+var+"}"};
            args.add(StringUtils.join(arg," ")+'\n');
        }
        String slicingResult = slicing(_classpath,_testclasspath,_testClassName, args);
        //System.out.print(slicingResult);
        for (String var: returnParam){
            if (slicingResult.contains("Error occurred during initialization of VM")){
                throw new Exception("Slice Initialization Error: \n"+slicingResult);
            }
            if (slicingResult.contains("There was an error while tracing:")){
                throw new Exception("There was an error occurs because of slice, Maybe you should use linux or mac instead of windows.");
            }
            String[] sliceResult = slicingResult.substring(slicingResult.indexOf("{"+var+"}"), slicingResult.indexOf("Computation took",slicingResult.indexOf("{"+var+"}"))).split("\n");
            for (String line: sliceResult){
                if (line.contains(_testClassName+"."+_testMethodName)){
                    lineToBeAdd.add(Integer.valueOf(line.substring(line.indexOf(':')+1,line.indexOf(' '))));
                }
            }
        }
        String result = "";
        lineToBeAdd = new ArrayList<Integer>(new HashSet<Integer>(lineToBeAdd));
        for (int line: lineToBeAdd){
            result += _classCode.split("\n")[line-1].trim() + "\n";
        }
        return result;
    }

    private String slicing(String classpath, String testclasspath, String testClassName, List<String> sliceArgs) throws Exception{
        String tracePath = TracerAgent.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        String junitPath = JUnitCore.class.getProtectionDomain().getCodeSource().getLocation().getFile();

        //small bug of get jar path from class in windows
        if (System.getProperty("os.name").toLowerCase().startsWith("win") && tracePath.charAt(0) == '/' && junitPath.charAt(0) == '/'){
            tracePath = tracePath.substring(1);
            junitPath = junitPath.substring(1);
        }
        String[] args = {
                "java", "-javaagent:"+tracePath+"=tracefile:"+Config.TEMP_FILES_PATH+"/test.trace",
                "-cp","\""+classpath+System.getProperty("path.separator")+testclasspath+System.getProperty("path.separator")+junitPath+"\"",
                "org.junit.runner.JUnitCore",
                testClassName
        };
        String arg = StringUtils.join(args, ' ')+'\n';
        sliceArgs.add(arg);
        String result = ShellUtils.shellRun(sliceArgs);
        File traceFile = new File(Config.TEMP_FILES_PATH+"/test.trace");
        if (traceFile.exists()){
            traceFile.deleteOnExit();
        }
        return result;
    }
*/



}

