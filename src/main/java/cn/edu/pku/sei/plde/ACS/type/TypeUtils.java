package cn.edu.pku.sei.plde.ACS.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yanrunfa on 16/3/3.
 */
public class TypeUtils {
    public static final List<String> simpleType = Arrays.asList(
            "BYTE","Byte","byte",
            "SHORT","Short","short",
            "INT","Integer","int",
            "LONG","Long","long",
            "FLOAT","Float","float",
            "DOUBLE","Double","double",
            "CHARACTER","Character","char",
            "BOOLEAN","Boolean","bool","boolean",
            "NULL","null");


    public static final List<String> containerType = Arrays.asList(
            "Collection","List","ArrayList","Vector","Map","HashTable","HashMap",
            "Iterator","ListIterator","Set","Queue","TreeMap","LinkedList","PriorityQueue",
            "LinkedHashMap","HashSet","TreeSet","LinkedHashSet"
    );

    public static boolean isSimpleType(String type){
        return simpleType.contains(type);
    }


    public static boolean isArray(String type){
        return type.endsWith("[]");
    }

    public static boolean isContainer(String type){
        for (String container: containerType){
            if (container.toLowerCase().equals(type.toLowerCase())){
                return true;
            }
        }
        return false;
    }

    public static boolean isSimpleArray(String type){
        if (!type.endsWith("[]")){
            return false;
        }
        type = type.substring(0, type.lastIndexOf("["));
        return isSimpleType(type);
    }

    public static String getTypeOfSimpleArray(String type){
        if (!isSimpleArray(type)){
            return "";
        }
        return type.substring(0, type.lastIndexOf("["));
    }


    public static TypeEnum getTypeEnumOfSimpleType(String type){
        if (isSimpleArray(type)){
            type = getTypeOfSimpleArray(type);
        }
        switch (type){
            case "BYTE":
            case "Byte":
            case "byte":
                return TypeEnum.BYTE;
            case "SHORT":
            case "Short":
            case "short":
                return TypeEnum.SHORT;
            case "INT":
            case "Integer":
            case "int":
                return TypeEnum.INT;
            case "LONG":
            case "Long":
            case "long":
                return TypeEnum.LONG;
            case "FLOAT":
            case "Float":
            case "float":
                return TypeEnum.FLOAT;
            case "DOUBLE":
            case "Double":
            case "double":
                return TypeEnum.DOUBLE;
            case "CHARACTER":
            case "Character":
            case "char":
                return TypeEnum.CHARACTER;
            case "BOOLEAN":
            case "Boolean":
            case "bool":
                return TypeEnum.BOOLEAN;
            case "STRING":
            case "String":
                return TypeEnum.STRING;
            case "NULL":case "null":
                return TypeEnum.NULL;
        }
        return null;
    }

    public static boolean isComplexType(String type){
        if (type.endsWith("[]")){
            return false;
        }
        if (simpleType.contains(type)){
            return false;
        }
        return true;
    }

    public static boolean isArrayFromName(String name){
        return name.endsWith("[i]");
    }


    public static List<String> arrayDup(List<String> array){
        List<String> result = new ArrayList<>();
        if (array == null){
            return new ArrayList<>();
        }
        for (String value: array){
            if (!result.contains(value)){
                result.add(value);
            }
        }
        return result;
    }
}
