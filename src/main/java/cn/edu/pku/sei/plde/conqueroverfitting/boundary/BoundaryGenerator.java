package cn.edu.pku.sei.plde.conqueroverfitting.boundary;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryWithFreq;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.Interval;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionExtractor;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionVariable;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.TraceResult;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.filter.AbandanTrueValueFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.filter.SearchBoundaryFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.CodeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.InfoUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.MathUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.VariableUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;

/**
 * Created by yanrunfa on 16/2/23.
 */
public class BoundaryGenerator {

    public static ExceptionVariable GENERATING_VARIABLE;

    public static List<String> generate(Suspicious suspicious, ExceptionVariable exceptionVariable, Map<VariableInfo, List<String>> trueValues, Map<VariableInfo, List<String>> falseValues, String project) {
        List<Interval> intervals = new ArrayList<>();
        GENERATING_VARIABLE = exceptionVariable;
        List<Interval> variableBoundary = SearchBoundaryFilter.getInterval(exceptionVariable, project, suspicious);
        for (String value: exceptionVariable.values){
            try {
                if (MathUtils.isMaxMinValue(value)){
                    Double doubleValue = MathUtils.parseStringValue(value);
                    intervals.add(new Interval(doubleValue, doubleValue, true, true));
                    continue;
                }
                for (Interval interval: variableBoundary){
                    if ((interval.containsValue(value)
                            || !TypeUtils.isSimpleType(exceptionVariable.type)) && !intervals.contains(interval)){
                        intervals.add(interval);
                    }
                }
            } catch (NumberFormatException e){
                continue;
            }
        }
        List<String> returnList = new ArrayList<>();
        for (Interval interval: intervals){
            String ifString = generateWithSingleWord(exceptionVariable,interval.toString());
            if (!ifString.equals("") && !ifString.contains("!=")){
                returnList.add(ifString);
            }
        }
        return returnList;
    }

    private static boolean allSpecificValue(List<String> values){
        for (String value: values){
            if (!value.equals("null") && !value.equals("true") && !value.equals("false")){
                return false;
            }
        }
        return true;
    }


    private static String generateWithSingleWord(ExceptionVariable variable, String intervals) {
        if (variable.variable.variableName.equals("this")){
            return intervals.equals("this")?"":"this.equals("+intervals+")";
        }
        if (variable.variable.variableName.equals("return")){
            return intervals;
        }
        if (variable.variable.isAddon){
            if (variable.variable.variableName.endsWith(".Comparable")){
                String variableName = variable.variable.variableName.substring(0,variable.variable.variableName.lastIndexOf("."));
                switch (intervals){
                    case "true":
                        return variableName+" instanceof Comparable<?>";
                    case "false":
                        return "!("+variableName+" instanceof Comparable<?>)";
                }
            }
            if (variable.variable.variableName.endsWith(".null")){
                String variableName = variable.variable.variableName.substring(0,variable.variable.variableName.lastIndexOf("."));
                switch (intervals){
                    case "true":
                        return variableName+" == null";
                    case "false":
                        return variableName+" != null";
                }
            }
        }
        if (MathUtils.isNumberType(variable.variable.getStringType())) {
            if (!intervals.contains(", ")){
                if (intervals.equals("NaN")){
                    return  MathUtils.getComplexOfNumberType(variable.variable.getStringType()) +".isNaN("+variable.variable.variableName+")";
                }
                else {
                    return variable.variable.variableName + "==" + intervals;
                }
            }


            boolean biggestClose = false;
            boolean smallestClose = false;
            String biggest = intervals.split(", ")[0];
            String smallest = intervals.split(", ")[1];
            String varType = MathUtils.getSimpleOfNumberType(variable.variable.getStringType());
            if (biggest.startsWith("[")){
                biggestClose = true;
            }
            biggest = biggest.substring(1);
            if (smallest.endsWith("]")){
                smallestClose = true;
            }
            smallest = smallest.substring(0, smallest.length()-1);
            if (biggest.contains("MIN_VALUE") || biggest.equals("-Double.MAX_VALUE")){
                return variable.variable.variableName + lessSymbol(smallestClose)+"("+varType+")" + smallest;
            }
            if (smallest.contains("MAX_VALUE")){
                return variable.variable.variableName + greaterSymbol(biggestClose)+"("+varType+")" + biggest;
            }
            if (biggest.equals(smallest) && biggestClose && smallestClose){
                return variable.variable.variableName + "==" +smallest;
            }

            double biggestBoundary;
            double smallestBoundary;
            try {
                biggestBoundary = MathUtils.parseStringValue(biggest);
                smallestBoundary = MathUtils.parseStringValue(smallest);
            } catch (Exception e){
                return "";
            }

            if (biggestBoundary > smallestBoundary){
                double temp = biggestBoundary;
                biggestBoundary = smallestBoundary;
                smallestBoundary = temp;
            }

            Map<String, String> interval = new HashMap<>();

            //for (String valueString: variable.values){
            //    double value = MathUtils.parseStringValue(valueString);
            //    if (value < biggestBoundary) {
            //        interval.put("forwardInterval", variable.variable.variableName + lessSymbol(biggestClose)+"("+varType+")" + biggestBoundary);
            //    }
            //    else if (value > smallestBoundary) {
            //        interval.put("backwardInterval", variable.variable.variableName + greaterSymbol(smallestClose)+"("+ varType+")" + smallestBoundary);
            //    }
            //    else if (value <= smallestBoundary && value >= biggestBoundary) {
                    interval.put("innerInterval", "("+variable.variable.variableName + lessSymbol(smallestClose)+"("+varType+")" + smallestBoundary + " && " + variable.variable.variableName + greaterSymbol(biggestClose)+"("+varType+")" + biggestBoundary+")");
            //    }
            //}

            if (interval.size() == 0){
                return "";
            }
            if (interval.size() == 1) {
                return generateWithOneInterval(interval);
            }
            if (interval.size() == 2) {
                return generateWithTwoInterval(interval);
            }
        }
        if (variable.variable.variableName.contains("==")||variable.variable.variableName.contains("!=") || variable.variable.variableName.contains(">") || variable.variable.variableName.contains("<")){
            if (intervals.equals("true")){
                return variable.variable.variableName;
            }
            if (intervals.equals("false")){
                return "!("+variable.variable.variableName+")";
            }
        }
        return variable.variable.variableName + "==" + intervals;
    }

    private static String generateWithOneInterval(Map<String, String> intervals) {
        return (String) intervals.values().toArray()[0];
    }

    private static String generateWithTwoInterval(Map<String, String> intervals) {
        return (String) intervals.values().toArray()[0] + "||" + (String) intervals.values().toArray()[1];
    }

    private static String lessSymbol(boolean close){
        return close?"<=":"<";
    }

    private static String greaterSymbol(boolean close){
        return close?">=":">";
    }

}