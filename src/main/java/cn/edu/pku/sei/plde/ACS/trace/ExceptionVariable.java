package cn.edu.pku.sei.plde.ACS.trace;

import cn.edu.pku.sei.plde.ACS.visible.model.VariableInfo;

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
}
