package cn.edu.pku.sei.plde.conqueroverfitting.trace;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryWithFreq;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.filter.AbandanTrueValueFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.CodeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.MathUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by yanrunfa on 16-4-16.
 */
public class ExceptionVariable {
    public VariableInfo variable;
    public String name;
    public String type;
    public List<String> values;
    public int level = 1;
    public boolean isExpression;
    private TraceResult traceResult;

    public String getAssertMessage(){
        return traceResult._testClass+"#"+traceResult._testMethod+"#"+traceResult._assertLine;
    }

    public boolean isSuccess(){
        return traceResult.getTestResult();
    }

    public ExceptionVariable(VariableInfo variable,TraceResult traceResult){
        this.traceResult = traceResult;
        this.variable = variable;
        this.level = variable.priority;
        this.name = variable.variableName;
        this.type = variable.getStringType();
        this.isExpression = variable.isExpression;
        if (traceResult.get(variable.variableName)!=null){
            this.values = traceResult.get(variable.variableName);
        }
    }

    public ExceptionVariable(VariableInfo variable, TraceResult traceResult, List<String> values){
        this(variable, traceResult);
        this.values = values;
    }

    public String toString(){
        return variable.getStringType()+" "+variable.variableName+" = "+values.toString();
    }


    public boolean equals(Object obj){
        if (!(obj instanceof ExceptionVariable)){
            return false;
        }
        ExceptionVariable exceptionVariable = (ExceptionVariable) obj;
        for (String value: exceptionVariable.values){
            if (!values.contains(value)){
                return false;
            }
        }
        return this.variable.equals(exceptionVariable.variable) && values.size() == exceptionVariable.values.size();
    }

    /*public boolean judgeTheSame(String[] newValues, String[] thisValues){
        if (newValues.length != thisValues.length){
            return false;
        }
        try{
            for (int i=0; i< newValues.length; i++){
                if (MathUtils.parseStringValue(newValues[i])!=MathUtils.parseStringValue(thisValues[i])){
                    return false;
                }
            }
        } catch (Exception e){
            return false;
        }

        return true;
    }*/

    /*public List<BoundaryInfo> boundaryFilter(List<BoundaryInfo> boundaryInfos){
        List<BoundaryInfo> result = new ArrayList<>();
        for (BoundaryInfo info: boundaryInfos){
            if (info.name.equals("serialVersionUID")){
                continue;
            }
            result.add(info);
        }
        return result;
    }
    private String getIntervalWithValue(List<String> valueList, List<BoundaryInfo> boundaryInfos){
        double smallestValue;
        double biggestValue;
        if (valueList.size() > 1){
            smallestValue = MathUtils.parseStringValue(valueList.get(0));
            biggestValue = MathUtils.parseStringValue(valueList.get(0));
            for (String value : valueList) {
                double doubleValue = Double.valueOf(value);
                if (Double.compare(doubleValue, smallestValue) < 0) {
                    smallestValue = doubleValue;
                }
                if (Double.compare(doubleValue, biggestValue) > 0) {
                    biggestValue = doubleValue;
                }
            }
        }
        else {
            smallestValue = MathUtils.parseStringValue(valueList.get(0));
            biggestValue = MathUtils.parseStringValue(valueList.get(0));
        }


        double biggestBoundary = -Double.MAX_VALUE;
        double smallestBoundary = Double.MAX_VALUE;
        BoundaryInfo biggestInfo = null;
        BoundaryInfo smallestInfo = null;
        for (BoundaryInfo info : boundaryInfos) {
            try {
                double doubleValue;
                try {
                    doubleValue = MathUtils.parseStringValue(info.value);
                } catch (NumberFormatException e){
                    System.out.println("ExceptionExtractor: Cannot parse numeric value "+info.value);
                    continue;
                }
                //小于最小值的最大边界值
                if (doubleValue >= biggestBoundary && doubleValue <= smallestValue) {
                    biggestBoundary = doubleValue;
                    if (biggestInfo == null){
                        biggestInfo = info;
                    }
                    if (doubleValue == smallestValue && biggestInfo.value.equals(info.value)){
                        if (info.rightClose == 1){
                            biggestInfo = info;
                        }
                    }
                    if (doubleValue < smallestValue && biggestInfo.value.equals(info.value)){
                        if (info.rightClose == 0){
                            biggestInfo = info;
                        }
                    }
                }
                //大于最大值的最小边界值
                if (doubleValue <= smallestBoundary && doubleValue >= biggestValue) {
                    smallestBoundary = doubleValue;
                    if (smallestInfo == null){
                        smallestInfo = info;
                    }
                    if (doubleValue == biggestValue && smallestInfo.value.equals(info.value)){
                        if (info.leftClose == 1){
                            smallestInfo = info;
                        }
                    }
                    if (doubleValue > biggestValue && smallestInfo.value.equals(info.value)){
                        if (info.leftClose == 0){
                            smallestInfo = info;
                        }
                    }
                }
            } catch (NumberFormatException e){
                e.printStackTrace();
            }
        }
        if (biggestBoundary == -Double.MAX_VALUE && smallestBoundary == Double.MAX_VALUE){
            for (BoundaryInfo info : boundaryInfos) {
                try {
                    double doubleValue = MathUtils.parseStringValue(info.value);
                    //大于最小值的最小边界值
                    if (doubleValue <= smallestBoundary && doubleValue >= smallestValue) {
                        smallestBoundary = doubleValue;
                        if (smallestInfo == null){
                            smallestInfo = info;
                        }
                        if (doubleValue == smallestValue && smallestInfo.value.equals(info.value)){
                            if (info.leftClose == 1){
                                smallestInfo = info;
                            }
                        }
                        if (doubleValue > smallestValue && smallestInfo.value.equals(info.value)){
                            if (info.leftClose == 0){
                                smallestInfo = info;
                            }
                        }
                    }
                    //小于最大值的最大边界值
                    if (doubleValue >= biggestBoundary && doubleValue <= biggestValue) {
                        biggestBoundary = doubleValue;
                        if (biggestInfo == null){
                            biggestInfo = info;
                        }
                        if (doubleValue == biggestValue && biggestInfo.value.equals(info.value)){
                            if (info.rightClose == 1){
                                biggestInfo = info;
                            }
                        }
                        if (doubleValue < biggestValue && biggestInfo.value.equals(info.value)){
                            if (info.rightClose == 0){
                                biggestInfo = info;
                            }
                        }
                    }
                } catch (NumberFormatException e){
                    e.printStackTrace();
                }
            }
            if (biggestBoundary == -Double.MAX_VALUE || smallestBoundary == Double.MAX_VALUE){
                return null;
            }
        }
        String small = String.valueOf(smallestBoundary);
        String big = String.valueOf(biggestBoundary);
        if (biggestInfo != null && biggestInfo.rightClose == 1){
            big = big +']';
        }
        if (smallestInfo != null && smallestInfo.leftClose == 1){
            small = '[' + small;
        }

        return small+"-"+big;
    }

    public List<String> getBoundaryIntervals(List<BoundaryWithFreq> boundaryInfos){
        List<String> valueList = new ArrayList<>(values);
        List<String> result = new ArrayList<>();
        if (name.equals("this")){
            String thisValue = values.iterator().next();
            thisValue = thisValue.substring(thisValue.indexOf('(')+1, thisValue.lastIndexOf(')'));
            String[] thisValues = thisValue.contains(",")?thisValue.split(","):new String[]{thisValue};
            for (BoundaryWithFreq info: boundaryInfos){
                if (info.value.contains("new "+type) && info.value.contains("(") && info.value.contains(")") && !info.value.endsWith("(")){
                    String newValue = info.value.substring(info.value.indexOf('(')+1, info.value.lastIndexOf(')'));
                    String[] newValues = newValue.contains(",")?newValue.split(","):new String[]{newValue};
                    if (judgeTheSame(newValues, thisValues)){
                        if (traceResult.getTestResult()){
                            result.add("!this.equals("+info.value+")");
                            break;
                        }
                        else {
                            result.add("this.equals("+info.value+")");
                            break;
                        }
                    }
                }
            }
            return result;
        }
        for (String value: valueList){
            result.add(value);
        }
        return result;
    }*/
}
