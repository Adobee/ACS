package cn.edu.pku.sei.plde.conqueroverfitting.fix;

import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.*;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yanrunfa on 16/6/3.
 */
public class DocumentBaseFilter {
    private Suspicious suspicious;
    private String className;
    private String methodName;
    private String annotation;
    private String classPath;
    private String srcPath;
    private List<String> variableNames = new ArrayList<>();

    public DocumentBaseFilter(Suspicious suspicious){
        this.suspicious = suspicious;
        this.className = suspicious.classname();
        this.methodName = suspicious.functionnameWithoutParam();
        this.classPath = suspicious._classpath;
        this.srcPath = suspicious._srcPath;
        for (VariableInfo info: suspicious.getAllInfo()){
            if (info.isLocalVariable || info.isParameter){
                variableNames.add(info.variableName);
            }
        }
    }


    public boolean filterWithAnnotation(String ifStatement,String fixString, int patchLine){
        String code = FileUtils.getCodeFromFile(srcPath,className);
        String annotation;
        if (fixString.contains("throw ")){
            String exceptionName = fixString.substring(fixString.lastIndexOf(" ")+1, fixString.indexOf("("));
            annotation = AnnotationUtils.getExceptionAnnotation(code,methodName,patchLine,exceptionName);
            if (annotation.equals("")){
                return false;
            }
            if (!exceptionPatchFilter(annotation, getParamFromIfStatement(ifStatement), variableNames)){
                return false;
            }
            return true;
        }
        else if (fixString.contains("return ")){
            /*
            String returnValue = fixString.substring(fixString.indexOf(" ")+1, fixString.indexOf(";"));
            annotation = AnnotationUtils.getReturnAnnotation(code, methodName, patchLine);
            if (annotation.equals("")){
                return false;
            }
            if (!returnPatchFilter(annotation, returnValue)){
                return false;
            }
            return true;*/
        }
        return false;
    }


    public static boolean returnPatchFilter(String annotation, String returnValue){
        if (returnValue.equals("true") || returnValue.equals("false") || returnValue.equals("null")){
            return false;
        }
        if (returnValue.startsWith("\"") && returnValue.endsWith("\"")){
            return false;
        }
        try {
            MathUtils.parseStringValue(returnValue);
            return false;
        }catch (NumberFormatException e){}

        List<String> variables ;
        if (VariableUtils.isExpression(returnValue)){
            variables = CodeUtils.divideParameter(returnValue, 1);
        }
        else {
            variables = Arrays.asList(returnValue);
        }
        for (String variable: variables){
            if (variable.contains(".")){
                variable = variable.split("\\.")[0];
            }
            if (!variable.equals("")&& !StringUtils.isNumeric(variable) &&!annotation.trim().contains(variable.trim())){
                return true;
            }
        }
        return false;
    }

    public static boolean exceptionPatchFilter(String annotation, List<String> ifParam, List<String> variableNames){
        List<String> subjects = AnnotationUtils.Parse(annotation);
        int count = 0;
        for (String param: ifParam){
            if (param.equals("")){
                continue;
            }
            List<String> paramNames = DocumentBaseFilter.splitVariableName(param);
            String name = paramNames.get(paramNames.size()-1);
            for (String subject: subjects){
                if ((" "+subject.replace("<"," ").replace(">"," ")+" ").contains(" "+name+" ")){
                    count ++;
                }
                else if ((" "+subject.replace("<"," ").replace(">"," ")+" ").contains(" "+param+" ")){
                    count ++;
                }
            }
        }
        if (count == ifParam.size()){
            return false;
        }
        for (String variable: variableNames){
            if (variable.equals("this")){
                continue;
            }
            for (String subject: subjects){
                subject = " "+subject.replace("<"," ").replace(">"," ")+" ";
                if (subject.contains(" "+variable.trim()+" ")) {
                    return true;
                }
            }
        }
        return false;

    }

    public static List<String> getParamFromIfStatement(String ifStatement){
        List<String> result = new ArrayList<>();
        if (ifStatement.startsWith("if")){
            ifStatement = ifStatement.substring(ifStatement.indexOf("(")+1,ifStatement.lastIndexOf(")"));
        }
        ifStatement = ifStatement.replace("(","").replace(")","").replace("!","");
        if (!ifStatement.contains("||")){
            result.add(getParamFromPair(ifStatement));
            return result;
        }
        for (String pair: ifStatement.split("\\|\\|")){
            result.add(getParamFromPair(pair));
        }
        return result;
    }

    private static String getParamFromPair(String ifStatement){
        if (ifStatement.contains("instanceof")) {
            return ifStatement.split("instanceof")[0].trim();
        }
        else if (ifStatement.contains(">")){
            return ifStatement.split(">")[0].trim();
        }
        else if (ifStatement.contains("<")){
            return ifStatement.split("<")[0].trim();
        }
        else if (ifStatement.contains("==")){
            return ifStatement.split("==")[0].trim();
        }
        return "";
    }

    public static List<String> splitVariableName(String name){
        List<String> keywords = new ArrayList<>();
        String keyword ="";
        for (Character ch: name.toCharArray()){
            if(!((ch<='Z')&&(ch>='A'))){
                keyword += ch;
            }
            else {
                keywords.add(keyword.toLowerCase());
                keyword = ""+ch;
            }
        }
        keywords.add(keyword.toLowerCase());
        return keywords;
    }
}
