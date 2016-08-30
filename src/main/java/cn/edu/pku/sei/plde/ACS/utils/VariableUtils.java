package cn.edu.pku.sei.plde.ACS.utils;

import cn.edu.pku.sei.plde.ACS.visible.model.VariableInfo;

/**
 * Created by yanrunfa on 16-4-19.
 */
public class VariableUtils {
    public static boolean isExpression(VariableInfo info){
        return isExpression(info.variableName);
    }

    public static boolean isExpression(String variableName){
        return ((variableName.contains("+") ||
                variableName.contains("-") ||
                variableName.contains("*") ||
                variableName.contains("/") ||
                variableName.contains("%") ||
                variableName.contains("&") ||
                variableName.contains("(") ||
                variableName.contains(")") ||
                variableName.contains(">") ||
                variableName.contains("=") ||
                variableName.contains("=") ||
                variableName.contains("<")));

    }

    public static boolean isValue(String var){
        return !isExpression(var) || var.contains("throw ");
    }

    public static boolean isJavaIdentifier(String name){
        if(name == null){
            return false;
        }
        int len = name.length();
        if(len == 0){
            return false;
        }
        if(!Character.isJavaIdentifierStart(name.charAt(0))){
            return false;
        }
        for(int i = 1; i < len; i ++){
            if(!Character.isJavaIdentifierPart(name.charAt(i))){
                return false;
            }
        }
        return true;
    }

}
