package cn.edu.pku.sei.plde.conqueroverfitting.fix;

import cn.edu.pku.sei.plde.conqueroverfitting.assertCollect.Asserts;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryGenerator;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.Interval;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Localization;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.common.container.map.DoubleMap;
import cn.edu.pku.sei.plde.conqueroverfitting.main.TimeLine;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionExtractor;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionVariable;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.TraceResult;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.filter.AbandanTrueValueFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.*;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import com.google.common.collect.Sets;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.lang.StringUtils;


import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by yanrunfa on 16-4-13.
 */
public class SuspiciousFixer {
    public static int FAILED_TEST_NUM = 0;
    public Map<ExceptionVariable, List<String>> boundarysMap = new HashMap<>();
    private Map<VariableInfo, List<String>> trueValues;
    private Map<VariableInfo, List<String>> falseValues;
    private List<TraceResult> traceResults;
    private Suspicious suspicious;
    private List<ExceptionVariable> exceptionVariables;
    private String project;
    private TimeLine timeLine;
    private List<String> methodOneHistory = new ArrayList<>();
    private List<String> methodTwoHistory = new ArrayList<>();
    private List<String> bannedHistory = new ArrayList<>();
    public SuspiciousFixer(Suspicious suspicious, String project, TimeLine timeLine){
        this.suspicious = suspicious;
        this.project = project;
        this.timeLine = timeLine;
        traceResults = suspicious.getTraceResult(project, timeLine);
        trueValues = AbandanTrueValueFilter.getTrueValue(traceResults, suspicious.getAllInfo());
        falseValues = AbandanTrueValueFilter.getFalseValue(traceResults, suspicious.getAllInfo());
        if (FAILED_TEST_NUM == 0){
            FAILED_TEST_NUM = TestUtils.getFailTestNumInProject(project);
        }
    }

    public boolean mainFixProcess(){
        ExceptionExtractor extractor = new ExceptionExtractor(suspicious);
        Map<Integer, List<TraceResult>> traceResultWithLine = traceResultClassify(traceResults);
        Map<Integer, List<TraceResult>> firstToGo = new TreeMap<Integer, List<TraceResult>>(new Comparator<Integer>() {
            @Override
            public int compare(Integer integer, Integer t1) {
                return integer.compareTo(t1);
            }
        });
         for (Map.Entry<Integer, List<TraceResult>> entry: traceResultWithLine.entrySet()){
            if (suspicious.tracedErrorLine.contains(entry.getKey())){
                firstToGo.put(entry.getKey(), entry.getValue());
            }
         }
        for (Map.Entry<Integer, List<TraceResult>> entry: firstToGo.entrySet()){
            if (timeLine.isTimeout()){
                return false;
            }
            if (fixInLineWithTraceResult(entry.getKey(), entry.getValue(), extractor, false)){
                return true;
            }
        }
        for (Map.Entry<Integer, List<TraceResult>> entry: traceResultWithLine.entrySet()){
            if (firstToGo.containsKey(entry.getKey())){
                continue;
            }
            if (timeLine.isTimeout()){
                return false;
            }
            if (fixInLineWithTraceResult(entry.getKey(), entry.getValue(), extractor, true)){
                return true;
            }
        }
        return false;
    }

    private boolean fixInLineWithTraceResult(int line, List<TraceResult> traceResults, ExceptionExtractor extractor, boolean onlyMethod2){
        trueValues = AbandanTrueValueFilter.getTrueValue(traceResults, suspicious.getAllInfo());
        falseValues = AbandanTrueValueFilter.getFalseValue(traceResults, suspicious.getAllInfo());
        exceptionVariables = extractor.extract(suspicious,traceResults);
        List<List<ExceptionVariable>> echelons = extractor.sort();
        for (List<ExceptionVariable> echelon: echelons) {
            Map<String, List<String>> boundarys = new HashMap<>();
            for (Map.Entry<String, List<ExceptionVariable>> assertEchelon : classifyWithAssert(echelon).entrySet()) {
                List<String> ifStrings = getIfStrings(assertEchelon.getValue());
                if (ifStrings.size()<= 0){
                    continue;
                }
                boundarys.put(assertEchelon.getKey(), ifStrings);
            }
            if (!onlyMethod2){
                if (timeLine.isTimeout()){
                    return false;
                }
                Map<String, List<String>> boundaryCopy = new HashMap<>();
                for (Map.Entry<String, List<String>> entry: boundarys.entrySet()){
                    boundaryCopy.put(entry.getKey(), new ArrayList<String>(entry.getValue()));
                }
                String methodOneResult = fixMethodOne(suspicious,boundaryCopy, project, line, false);
                RecordUtils.printRuntimeMessage(suspicious, project, exceptionVariables, echelons, line);
                if (!methodOneResult.equals("")) {
                    printHistoryBoundary(boundarys, methodOneResult);
                    return true;
                }
            }
            if (timeLine.isTimeout()){
                return false;
            }
            AngelicFilter filter = new AngelicFilter(suspicious, project);
            Map<String, List<String>> boundaryCopy = new HashMap<>();
            for (Map.Entry<String, List<String>> entry: boundarys.entrySet()){
                boundaryCopy.put(entry.getKey(), new ArrayList<String>(entry.getValue()));
            }
            String methodTwoResult = fixMethodTwo(suspicious, filter.filter(line,boundaryCopy,traceResults), project, line, false);
            RecordUtils.printRuntimeMessage(suspicious, project, exceptionVariables, echelons, line);
            if (!methodTwoResult.equals("")) {
                printHistoryBoundary(boundarys, methodTwoResult);
                return true;
            }
        }
        return false;
    }




    private Map<Integer, List<TraceResult>> traceResultClassify(List<TraceResult> traceResults){
        Map<Integer, List<TraceResult>> result = new TreeMap<Integer, List<TraceResult>>(new Comparator<Integer>() {
            @Override
            public int compare(Integer integer, Integer t1) {
                return integer.compareTo(t1);
            }
        });
        for (TraceResult traceResult: traceResults){
            if (!result.containsKey(traceResult._traceLine)){
                List<TraceResult> results = new ArrayList<>();
                results.add(traceResult);
                result.put(traceResult._traceLine, results);
            }
            else {
                result.get(traceResult._traceLine).add(traceResult);
            }
        }
        return result;
    }


    private Map<String, List<ExceptionVariable>> classifyWithAssert(List<ExceptionVariable> exceptionVariables){
        Map<String, List<ExceptionVariable>> result = new HashMap<>();
        for (ExceptionVariable exceptionVariable: exceptionVariables){
            if (!result.containsKey(exceptionVariable.getAssertMessage())){
                List<ExceptionVariable> variables = new ArrayList<>();
                variables.add(exceptionVariable);
                result.put(exceptionVariable.getAssertMessage(),variables);
            }
            else {
                result.get(exceptionVariable.getAssertMessage()).add(exceptionVariable);
            }
        }
        return result;
    }


    private List<String> getIfStrings(List<ExceptionVariable> exceptionVariables){
        List<String> returnList = new ArrayList<>();
        Map<ExceptionVariable, ArrayList<String>> result = new HashMap<>();
        for (ExceptionVariable exceptionVariable: exceptionVariables){
            ArrayList<String> boundarys = new ArrayList<>(getBoundary(exceptionVariable));
            boolean addedFlag = false;
            for (Map.Entry<ExceptionVariable, ArrayList<String>> entry: result.entrySet()){
                if (entry.getKey().name.equals(exceptionVariable.name)){
                    entry.getValue().removeAll(boundarys);
                    entry.getValue().addAll(boundarys);
                    addedFlag = true;
                    break;
                }
            }
            if (!addedFlag){
                result.put(exceptionVariable, boundarys);
            }
        }
        Set<String> boundaryCombine = new HashSet<>();
        for (List<String> list: result.values()){
            for (String statement: list){
                String ifString = MathUtils.replaceSpecialNumber(getIfStatementFromBoundary(statement));
                if (!returnList.contains(ifString) && !ifString.equals("")){
                    returnList.add(ifString);
                }
            }
        }
        returnList.addAll(boundaryCombine);
        return returnList;
    }

    private String getIfStatementFromBoundary(String boundary){
        return "if ("+ boundary+")";
    }

    private List<String> getBoundary(ExceptionVariable exceptionVariable){
        if (!boundarysMap.containsKey(exceptionVariable)){
            long downLoadStartTime = System.currentTimeMillis();
            List<String> boundarys = BoundaryGenerator.generate(suspicious,exceptionVariable, trueValues, falseValues, project);
            timeLine.addDownloadTime(System.currentTimeMillis()-downLoadStartTime);
            if(timeLine.isTimeout()){
                return new ArrayList<>();
            }
            boundarysMap.put(exceptionVariable, boundarys);
        }
        return boundarysMap.get(exceptionVariable);
    }


    public String fixMethodTwo(Suspicious suspicious, Map<String, List<String>> ifStrings, String project, int errorLine, boolean debug){
        if (ifStrings.size() == 0){
            return "";
        }
        MethodTwoFixer fixer = new MethodTwoFixer(suspicious);
        if (fixer.fix(ifStrings, Sets.newHashSet(errorLine), project, debug)){
            return fixer.correctPatch+"["+fixer.correctStartLine+","+fixer.correctEndLine+"]";
        }
        methodTwoHistory  = fixer.triedPatch;
        return "";
    }


    public String fixMethodOne(Suspicious suspicious,Map<String, List<String>> ifStrings, String project, int errorLine, boolean debug) {
        if (ifStrings.size() == 0){
            return "";
        }
        ReturnCapturer fixCapturer = new ReturnCapturer(suspicious._classpath,suspicious._srcPath, suspicious._testClasspath, suspicious._testSrcPath);
        MethodOneFixer methodOneFixer = new MethodOneFixer(suspicious, project);
        for (Map.Entry<String, List<String>> entry: ifStrings.entrySet()){
            String testClassName = entry.getKey().split("#")[0];
            String testMethodName = entry.getKey().split("#")[1];
            int assertLine = Integer.valueOf(entry.getKey().split("#")[2]);
            Asserts asserts = suspicious._assertsMap.get(testClassName+"#"+testMethodName);
            AssertComment comment = new AssertComment(asserts, assertLine);
            if (assertLine == -1){
                testClassName = suspicious._failTests.get(0).split("#")[0];
                testMethodName = suspicious._failTests.get(0).split("#")[1];
                if (suspicious._assertsMap.containsKey(suspicious._failTests.get(0))){
                    if (suspicious._assertsMap.get(suspicious._failTests.get(0))._errorAssertLines.size()>0){
                        assertLine = suspicious._assertsMap.get(suspicious._failTests.get(0))._errorAssertLines.get(0);
                    }
                }
            }
            if (!CodeUtils.getLineFromCode(FileUtils.getCodeFromFile(suspicious._testSrcPath, testClassName),assertLine).contains("assert")){
                assertLine = -1;
            }
            String fixString = fixCapturer.getFixFrom(testClassName, testMethodName, assertLine, suspicious.classname(), suspicious.functionnameWithoutParam());
            List<Integer> patchLine = errorLine != 0? Arrays.asList(errorLine) : suspicious._errorLineMap.get(testClassName+"#"+testMethodName);
            List<String> ifStatement = entry.getValue();
            List<String> bannedStatement = new ArrayList<>();


            for (String statemnt: ifStatement) {
                if (ifStringFilter(statemnt,fixString, patchLine.get(0))) {
                    bannedStatement.add(statemnt);
                }
            }
            ifStatement.removeAll(bannedStatement);
            bannedHistory.addAll(bannedStatement);


            if (ifStatement.size() == 0){
                return "";
            }
            if (suspicious._isConstructor && fixString.contains("return")){
                continue;
            }
            if (fixString.equals("")){
                continue;
            }
            Patch patch = new Patch(testClassName, testMethodName, suspicious.classname(), patchLine, entry.getValue(), fixString);
            comment.comment();
            boolean result = methodOneFixer.addPatch(patch);
            comment.uncomment();
            if (result){
                break;
            }
        }
        int finalErrorNums = methodOneFixer.fix();
        methodOneHistory = methodOneFixer.triedPatch;
        if (finalErrorNums != -1){
            return methodOneFixer._patches.get(0)._patchString.get(0);
        }
        return "";
    }

    private boolean ifStringFilter(String ifStatement,String fixString, int patchLine){
        DocumentBaseFilter filter = new DocumentBaseFilter(suspicious);
        if (filter.filterWithAnnotation(ifStatement,fixString, patchLine)){
            return true;
        }
        if (ifStatement.contains(">") || ifStatement.contains("<")){
            if (fixString.startsWith("return") && !VariableUtils.isExpression(fixString.split(" ")[1])){
                return true;
            }
        }
        return false;
    }


    private void printHistoryBoundary(Map<String, List<String>> boundary, String ifStatement){
        RecordUtils patchWriter = new RecordUtils("patch");
        patchWriter.write("====================================================\n");
        patchWriter.write("boundary of suspicious: "+suspicious.classname()+"#"+suspicious.functionnameWithoutParam()+"#"+suspicious.getDefaultErrorLine()+"\n");
        patchWriter.write(MathUtils.replaceSpecialNumber(ifStatement)+"\n");
        patchWriter.close();

        RecordUtils writer = new RecordUtils("patch");
        for (Map.Entry<String, List<String>> entry: boundary.entrySet()){
            String assertString = entry.getKey();
            writer.write("====================================================\n");
            writer.write("Tried ifStrings With AssertMessage:"+assertString+"\n");
            for (String ifString: methodOneHistory){
                writer.write(ifString+"\n");
            }
            for (String ifString: methodTwoHistory){
                writer.write(ifString+"\n");
            }
            writer.write("====================================================\n");
            writer.write("Banned ifStrings With AssertMessage:"+assertString+"\n");
            for (String ifString: bannedHistory){
                writer.write(ifString+"\n");
            }
            writer.write("====================================================\n\n");
        }
        writer.close();
    }
}
