package cn.edu.pku.sei.plde.ACS.trace;

import cn.edu.pku.sei.plde.ACS.localization.Suspicious;
import cn.edu.pku.sei.plde.ACS.sort.VariableSort;
import cn.edu.pku.sei.plde.ACS.trace.filter.AbandanTrueValueFilter;
import cn.edu.pku.sei.plde.ACS.utils.*;
import org.apache.commons.lang.StringUtils;
import org.apache.http.auth.KerberosCredentials;

import java.util.*;

/**
 * Created by yanrunfa on 16/2/21.
 */
public class ExceptionExtractor {
    private Suspicious suspicious;
    private List<TraceResult> traceResults;
    private List<ExceptionVariable> exceptionVariables;
    public ExceptionExtractor(Suspicious suspicious){
        this.suspicious = suspicious;
    }

    public List<ExceptionVariable> extract(Suspicious suspicious, List<TraceResult> traceResults){
        this.traceResults = traceResults;
        exceptionVariables = AbandanTrueValueFilter.abandon(suspicious, traceResults, suspicious.getAllInfo());
        return exceptionVariables;
    }

    public List<List<ExceptionVariable>> sort(){
        List<List<ExceptionVariable>> result = new ArrayList<>();
        List<ExceptionVariable> sortList = new ArrayList<>(exceptionVariables);
        List<List<String>> thisValue = new ArrayList<>();
        for (ExceptionVariable exceptionVariable: exceptionVariables){
            if (exceptionVariable.name.equals("this")){
                if (!thisValue.contains(exceptionVariable.values)){
                    result.add(Arrays.asList(exceptionVariable));
                }
                thisValue.add(exceptionVariable.values);
                sortList.remove(exceptionVariable);
            }
        }
        result.addAll(sortWithMethodOne(sortList, traceResults, suspicious));
        return result;
    }

    private static boolean hasTrueTraceResult(List<TraceResult> traceResults){
        for (TraceResult traceResult: traceResults){
            if (traceResult.getTestResult()){
                return true;
            }
        }
        return false;
    }

    private static List<ExceptionVariable> sortWithMethodTwo(List<ExceptionVariable> exceptionVariables, List<TraceResult> traceResults, Suspicious suspicious){
        String code = FileUtils.getCodeFromFile(suspicious._srcPath, suspicious.classname());
        String statement = CodeUtils.getMethodBodyBeforeLine(code, suspicious.functionnameWithoutParam(), lastLineOfTraceResults(traceResults));
        ExceptionSorter sorter = new ExceptionSorter(suspicious, statement);
        return sorter.sort(exceptionVariables);
    }



    private static List<List<ExceptionVariable>> sortWithMethodOne(List<ExceptionVariable> exceptionVariables,List<TraceResult> traceResults, Suspicious suspicious){
        String code = FileUtils.getCodeFromFile(suspicious._srcPath, suspicious.classname());
        String statement = CodeUtils.getMethodBodyBeforeLine(code, suspicious.functionnameWithoutParam(), lastLineOfTraceResults(traceResults))+CodeUtils.getLineFromCode(code, lastLineOfTraceResults(traceResults));
        Set<String> variables = new HashSet<>();
        for (ExceptionVariable variable: exceptionVariables){
            if (variable.name.endsWith(".null") || variable.name.endsWith(".Comparable")){
                variables.add(variable.name.substring(0, variable.name.lastIndexOf(".")));
            }
            else {
                variables.add(variable.name);
            }
        }
        VariableSort variableSort = new VariableSort(variables, statement);
        List<List<String>> sortedVariable = variableSort.getSortVariable();
        if (sortedVariable.size() == 0){
            return new ArrayList<>();
        }
        //if (sortedVariable.size()>2){
        //    return variableConverse(Arrays.asList(sortedVariable.get(0), sortedVariable.get(1), sortedVariable.get(2)),exceptionVariables);
        //}
        else if (sortedVariable.size()>1){
            return variableConverse(Arrays.asList(sortedVariable.get(0), sortedVariable.get(1)),exceptionVariables);
        }
        return variableConverse(Arrays.asList(sortedVariable.get(0)), exceptionVariables);
    }

    private static List<List<ExceptionVariable>> variableConverse(List<List<String>> sortedVariable, List<ExceptionVariable> exceptionVariables){
        List<List<ExceptionVariable>> result = new ArrayList<>();
        for (List<String> echelon: sortedVariable){
            List<ExceptionVariable> variableEchelon = getExceptionVariableWithName(echelon, exceptionVariables);
            if (variableEchelon.size() != 0){
                result.add(variableEchelon);
            }
        }
        return result;
    }
    private static List<ExceptionVariable> getExceptionVariableWithName(List<String> variableNames, List<ExceptionVariable> exceptionVariables){
        List<ExceptionVariable> result = new ArrayList<>();
        for (String variableName: variableNames){
            result.addAll(getExceptionVariableWithName(variableName, exceptionVariables));
        }
        return result;
    }

    private static List<ExceptionVariable> getExceptionVariableWithName(String variableName, List<ExceptionVariable> exceptionVariables){
        List<ExceptionVariable> result = new ArrayList<>();
        for (ExceptionVariable exceptionVariable: exceptionVariables){
            if (exceptionVariable.name.equals(variableName) || exceptionVariable.name.contains(variableName+".null") || exceptionVariable.name.contains(variableName+".Comparable")){
                result.add(exceptionVariable);
            }
        }
        return result;
    }

    private static int lastLineOfTraceResults(List<TraceResult> traceResults){
        int lastLine = 0;
        for (TraceResult traceResult: traceResults){
            if (traceResult._traceLine > lastLine){
                lastLine = traceResult._traceLine;
            }
        }
        return lastLine;
    }

}
