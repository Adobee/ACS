package cn.edu.pku.sei.plde.ACS.utils;

import cn.edu.pku.sei.plde.ACS.boundary.model.BoundaryWithFreq;
import cn.edu.pku.sei.plde.ACS.boundary.model.Interval;
import cn.edu.pku.sei.plde.ACS.type.TypeEnum;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * Created by yanrunfa on 16/3/3.
 */
public class MathUtils {

    public static final List<String> numberType = Arrays.asList("INT", "DOUBLE", "FLOAT", "SHORT", "LONG", "int", "double", "float", "short", "long", "Integer", "Double", "Float", "Short", "Long");
    public static final List<Character> number = Arrays.asList('1', '2', '3', '4', '5', '6', '7', '8', '9', '0');

    public static double parseStringValue(String value) throws NumberFormatException {
        value = value.trim();
        if (value.startsWith("(") && value.contains(")") && !value.endsWith(")")) {
            value = value.substring(value.indexOf(")") + 1);
        }
        if (value.contains("-0.0")) {
            return 0.0;
        }
        if (value.contains("Integer.MIN_VALUE")) {
            return Integer.MIN_VALUE;
        }
        if (value.contains("Integer.MAX_VALUE")) {
            return Integer.MAX_VALUE;
        }
        if (value.contains("Long.MAX_VALUE")) {
            return Long.MAX_VALUE;
        }
        if (value.contains("Long.MIN_VALUE")) {
            return Long.MIN_VALUE;
        }
        if (value.contains("Double.MAX_VALUE")) {
            return Double.MAX_VALUE;
        }
        if (value.contains("-Double.MAX_VALUE")) {
            return -Double.MAX_VALUE;
        }
        if (value.contains("Double.MIN_VALUE")) {
            return Double.MIN_VALUE;
        }
        if (value.contains("Infinity")) {
            return Double.POSITIVE_INFINITY;
        }
        if (value.contains("-Infinity")) {
            return Double.NEGATIVE_INFINITY;
        }
        if (value.endsWith(".0")) {
            value = value.substring(0, value.lastIndexOf("."));
        }
        return Double.valueOf(value);
    }

    public static boolean isNumberType(String type) {
        if (numberType.contains(type)) {
            return true;
        }
        return false;
    }

    public static boolean isNumberArray(String type) {
        if (!type.endsWith("[]")) {
            return false;
        }
        type = type.substring(0, type.lastIndexOf("["));
        if (numberType.contains(type)) {
            return true;
        }
        return false;
    }

    public static String getNumberTypeOfArray(String type) {
        return getNumberTypeOfArray(type, true);
    }

    public static String getNumberTypeOfArray(String type, boolean isSimpleType) {
        if (!isNumberArray(type)) {
            return "";
        }
        type = type.substring(0, type.lastIndexOf("["));
        if (isSimpleType) {
            return getSimpleOfNumberType(type);
        } else {
            return getComplexOfNumberType(type);
        }

    }

    public static double getMaxValueOfNumberType(String type) {
        switch (type) {
            case "INT":
            case "int":
            case "Integer":
                return Integer.MAX_VALUE;
            case "double":
            case "Double":
            case "DOUBLE":
                return Double.MAX_VALUE;
            case "SHORT":
            case "short":
            case "Short":
                return Short.MAX_VALUE;
            case "FLOAT":
            case "Float":
            case "float":
                return Float.MAX_VALUE;
            case "LONG":
            case "Long":
            case "long":
                return Long.MAX_VALUE;
            default:
                return Integer.MAX_VALUE;
        }
    }

    public static double getMinValueOfNumberType(String type) {
        switch (type) {
            case "INT":
            case "int":
            case "Integer":
                return Integer.MIN_VALUE;
            case "double":
            case "Double":
            case "DOUBLE":
                return Double.MIN_VALUE;
            case "SHORT":
            case "short":
            case "Short":
                return Short.MIN_VALUE;
            case "FLOAT":
            case "Float":
            case "float":
                return Float.MIN_VALUE;
            case "LONG":
            case "Long":
            case "long":
                return Long.MIN_VALUE;
            default:
                return Integer.MIN_VALUE;
        }
    }

    public static String getSimpleOfNumberType(String type) {
        switch (type) {
            case "INT":
            case "int":
            case "Integer":
                return "int";
            case "double":
            case "Double":
            case "DOUBLE":
                return "double";
            case "SHORT":
            case "short":
            case "Short":
                return "short";
            case "FLOAT":
            case "Float":
            case "float":
                return "float";
            case "LONG":
            case "Long":
            case "long":
                return "long";
            default:
                return "";
        }
    }

    public static String getComplexOfNumberType(String type) {
        if (type.endsWith("[]")) {
            type = type.substring(0, type.lastIndexOf("["));
        }
        switch (type) {
            case "INT":
            case "int":
            case "Integer":
                return "Integer";
            case "double":
            case "Double":
            case "DOUBLE":
                return "Double";
            case "SHORT":
            case "short":
            case "Short":
                return "Short";
            case "FLOAT":
            case "Float":
            case "float":
                return "Float";
            case "LONG":
            case "Long":
            case "long":
                return "Long";
            default:
                return "";
        }
    }


    public static List<Integer> changeStringListToInteger(List<String> list) {
        List<Integer> nums = new ArrayList<>();
        for (String var : list) {
            if (!StringUtils.isNumeric(var)) {
                continue;
            }
            try {
                int num = Integer.parseInt(var);
                nums.add(num);
            } catch (Exception e) {
            }
        }
        return nums;
    }


    public static <T> boolean hasInterSection(List<T> firstList, List<T> secondList) {
        for (T value : firstList) {
            if (secondList.contains(value)) {
                return true;
            }
        }
        return false;
    }


    public static boolean isMaxMinValue(String value) {
        return value.contains(String.valueOf(Integer.MAX_VALUE)) ||
                value.contains(String.valueOf(Integer.MIN_VALUE)) ||
                value.contains(String.valueOf(Double.MAX_VALUE)) ||
                value.contains(String.valueOf(Double.MIN_VALUE)) ||
                value.contains(String.valueOf(Long.MAX_VALUE)) ||
                value.contains(String.valueOf(Long.MIN_VALUE)) ||
                value.contains(String.valueOf(Short.MAX_VALUE)) ||
                value.contains(String.valueOf(Short.MIN_VALUE)) ||
                value.contains("-9223372036854775808") ||
                value.contains("9223372036854775807");
    }


    public static boolean allMaxMinValue(List<String> values){
        for (String value: values){
            if (!isMaxMinValue(value)){
                return false;
            }
        }
        return true;
    }

    public static String replaceSpecialNumber(String ifString){
        ifString = ifString.replace(String.valueOf(Integer.MIN_VALUE),"Integer.MIN_VALUE");
        ifString = ifString.replace(String.valueOf(Integer.MAX_VALUE),"Integer.MAX_VALUE");
        ifString = ifString.replace("(int)-2.147483648E9","Integer.MIN_VALUE");
        ifString = ifString.replace("(int)2.147483647E9","Integer.MAX_VALUE");
        ifString = ifString.replace("-2.147483648E9","Integer.MIN_VALUE");
        ifString = ifString.replace("2.147483647E9","Integer.MAX_VALUE");
        ifString = ifString.replace("(long)-9.223372036854776E18","Long.MIN_VALUE");
        ifString = ifString.replace("(long)9.223372036854776E18","Long.MAX_VALUE");
        ifString = ifString.replace("-9.223372036854776E18","Long.MIN_VALUE");
        ifString = ifString.replace("9.223372036854776E18","Long.MAX_VALUE");
        ifString = ifString.replace(String.valueOf(Long.MIN_VALUE),"Long.MIN_VALUE");
        ifString = ifString.replace(String.valueOf(Long.MAX_VALUE),"Long.MAX_VALUE");
        ifString = ifString.replace(String.valueOf(Double.MIN_VALUE),"Double.MIN_VALUE");
        ifString = ifString.replace(String.valueOf(Double.MAX_VALUE),"Double.MAX_VALUE");
        ifString = ifString.replace(String.valueOf(Short.MIN_VALUE),"Short.MIN_VALUE");
        ifString = ifString.replace(String.valueOf(Short.MAX_VALUE),"Short.MAX_VALUE");
        return ifString;
    }

    public static boolean allMaxMinValue(Set<String> values){
        return allMaxMinValue(new ArrayList<String>(values));
    }


    public static List<Interval> mergetDoubleInterval(ArrayList<Interval> intervals) {
        Collections.sort(intervals, new Comparator<Interval>() {
            @Override
            public int compare(Interval i1, Interval i2) {
                if (i1.leftBoundary() == i2.leftBoundary()) {
                    if (i1.leftClose() && i2.leftClose()) {
                        if (i1.rightClose() == i2.rightClose()) {
                            return 0;
                        }
                        if (i1.rightClose() == false && i2.rightClose() == true) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                    if (i1.leftClose()) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
                return Double.compare(i1.leftBoundary(), i2.leftBoundary());
            }
        });

        List<Interval> result = new ArrayList<Interval>();
        Interval intervalTemp = new Interval(intervals.get(0));

        for (Interval interval : intervals) {
            if (interval.leftBoundary() < intervalTemp.rightBoundary()) {
                if (interval.rightBoundary() > intervalTemp.rightBoundary()) {
                    intervalTemp.setRightBoundary(interval.rightBoundary());
                    intervalTemp.setRightClose(interval.rightClose());
                } else if (interval.rightBoundary() == intervalTemp.rightBoundary()) {
                    if (interval.rightClose() || intervalTemp.rightClose()) {
                        intervalTemp.setRightClose(true);
                    }
                }
            } else if (interval.leftBoundary() == intervalTemp.rightBoundary() && (interval.leftClose() || intervalTemp.rightClose())) {
                intervalTemp.setRightBoundary(interval.rightBoundary());
                intervalTemp.setRightClose(interval.rightClose());
            } else {
                result.add(new Interval(intervalTemp));
                intervalTemp = new Interval(interval);
            }
        }
        result.add(new Interval(intervalTemp));
        return result;
    }

    public static List<Interval> mergetIntInterval(ArrayList<Interval> intervals) {
        Collections.sort(intervals, new Comparator<Interval>() {
            @Override
            public int compare(Interval i1, Interval i2) {
                if (i1.leftBoundary() == i2.leftBoundary()) {
                    if (i1.leftClose() && i2.leftClose()) {
                        if (i1.rightClose() == i2.rightClose()) {
                            return 0;
                        }
                        if (i1.rightClose() == false && i2.rightClose() == true) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                    if (i1.leftClose()) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
                return Double.compare(i1.leftBoundary(), i2.leftBoundary());
            }
        });

        List<Interval> result = new ArrayList<Interval>();
        Interval intervalTemp = new Interval(intervals.get(0));

        for (Interval interval : intervals) {
            if (interval.leftBoundary() < intervalTemp.rightBoundary()) {
                if (interval.rightBoundary() > intervalTemp.rightBoundary()) {
                    intervalTemp.setRightBoundary(interval.rightBoundary());
                    intervalTemp.setRightClose(interval.rightClose());
                } else if (interval.rightBoundary() == intervalTemp.rightBoundary()) {
                    if (interval.rightClose() || intervalTemp.rightClose()) {
                        intervalTemp.setRightClose(true);
                    }
                }
            } else if (interval.leftBoundary() == intervalTemp.rightBoundary() && (interval.leftClose() || intervalTemp.rightClose())) {
                intervalTemp.setRightBoundary(interval.rightBoundary());
                intervalTemp.setRightClose(interval.rightClose());
            } else if ((interval.leftBoundary() == intervalTemp.rightBoundary() + 1) && (interval.leftClose() && intervalTemp.rightClose())) {
                intervalTemp.setRightBoundary(interval.rightBoundary());
                intervalTemp.setRightClose(interval.rightClose());
            } else {
                result.add(new Interval(intervalTemp));
                intervalTemp = new Interval(interval);
            }
        }
        result.add(new Interval(intervalTemp));
        return result;
    }

    public static ArrayList<BoundaryWithFreq> generateInterval(List<BoundaryWithFreq> boundaryWithFreqs, double wrongValue, String type) {
        //List<BoundaryWithFreq> boundaryWithFreqs = new ArrayList<>(boundaryWithFreqs);

        Collections.sort(boundaryWithFreqs, new ComparatorBounaryWithFreqs());

//        Log log = new Log("log//if-long-var1-copy.log");
//        for (BoundaryWithFreq boundaryInfo : boundaryWithFreqs) {
//            log.logSignLine("begin");
//            //log.logStr("name: " + boundaryInfo.name);
//            log.logStr("dvalue: " + boundaryInfo.dvalue);
//            log.logStr("value: " + boundaryInfo.value);
//            log.logStr("type: " + boundaryInfo.variableSimpleType);
//            log.logStr("is " + boundaryInfo.isSimpleType);
//            log.logStr("left " + boundaryInfo.leftClose());
//            log.logStr("right " + boundaryInfo.rightClose());
//            log.logSignLine("end");
//        }

        ArrayList<BoundaryWithFreq> interval = new ArrayList<BoundaryWithFreq>();

        int size = boundaryWithFreqs.size();
        for (BoundaryWithFreq boundaryWithFreq : boundaryWithFreqs) {
            if (boundaryWithFreq.dvalue == wrongValue) {
                interval.add(new BoundaryWithFreq(boundaryWithFreq.variableSimpleType, boundaryWithFreq.isSimpleType, boundaryWithFreq.otherType,
                        boundaryWithFreq.value, 1, 1, boundaryWithFreq.freq));
                interval.add(boundaryWithFreq);
                return interval;
            }
        }

        for (int i = 0; i < size - 1; i++) {
            BoundaryWithFreq boundaryWithFreq0 = new BoundaryWithFreq(boundaryWithFreqs.get(i).variableSimpleType, boundaryWithFreqs.get(i).isSimpleType,
                    boundaryWithFreqs.get(i).otherType, boundaryWithFreqs.get(i).value, boundaryWithFreqs.get(i).leftClose, boundaryWithFreqs.get(i).rightClose, boundaryWithFreqs.get(i).freq);
            BoundaryWithFreq boundaryWithFreq1 = new BoundaryWithFreq(boundaryWithFreqs.get(i + 1).variableSimpleType, boundaryWithFreqs.get(i + 1).isSimpleType,
                    boundaryWithFreqs.get(i + 1).otherType, boundaryWithFreqs.get(i + 1).value, boundaryWithFreqs.get(i + 1).leftClose, boundaryWithFreqs.get(i + 1).rightClose, boundaryWithFreqs.get(i + 1).freq);

            if (boundaryWithFreq0.dvalue < wrongValue && boundaryWithFreq1.dvalue > wrongValue) {
               if(boundaryWithFreq0.leftClose >= boundaryWithFreq0.rightClose){
                    boundaryWithFreq0.leftClose = 1;
                }else{
                    boundaryWithFreq0.leftClose = 0;
                }

                if(boundaryWithFreq1.rightClose >= boundaryWithFreq1.leftClose){
                    boundaryWithFreq1.rightClose = 1;
                }else{
                    boundaryWithFreq1.rightClose = 0;
                }

                interval.add(boundaryWithFreq0);
                interval.add(boundaryWithFreq1);
                return interval;

            }
        }

        if (wrongValue < boundaryWithFreqs.get(0).dvalue) {
            interval.add(new BoundaryWithFreq(TypeEnum.DOUBLE, true, null, MathUtils.getComplexOfNumberType(type)+".MIN_VALUE", 1, 0, 1));
            BoundaryWithFreq boundaryWithFreq1 = new BoundaryWithFreq(boundaryWithFreqs.get(0).variableSimpleType, boundaryWithFreqs.get(0).isSimpleType,
                    boundaryWithFreqs.get(0).otherType, boundaryWithFreqs.get(0).value, boundaryWithFreqs.get(0).leftClose, boundaryWithFreqs.get(0).rightClose, boundaryWithFreqs.get(0).freq);
            if(boundaryWithFreq1.rightClose >= boundaryWithFreq1.leftClose){
                boundaryWithFreq1.rightClose = 1;
            }else{
                boundaryWithFreq1.rightClose = 0;
            }
            interval.add(boundaryWithFreq1);
            return interval;
        }
        if (wrongValue > boundaryWithFreqs.get(size - 1).dvalue) {
            BoundaryWithFreq boundaryWithFreq0 = new BoundaryWithFreq(boundaryWithFreqs.get(size - 1).variableSimpleType, boundaryWithFreqs.get(size - 1).isSimpleType,
                    boundaryWithFreqs.get(size - 1).otherType, boundaryWithFreqs.get(size - 1).value, boundaryWithFreqs.get(size - 1).leftClose, boundaryWithFreqs.get(size - 1).rightClose, boundaryWithFreqs.get(size - 1).freq);
            if(boundaryWithFreq0.leftClose >= boundaryWithFreq0.rightClose){
                boundaryWithFreq0.leftClose = 1;
            }else{
                boundaryWithFreq0.leftClose = 0;
            }
            interval.add(boundaryWithFreq0);
            interval.add(new BoundaryWithFreq(TypeEnum.DOUBLE, true, null, MathUtils.getComplexOfNumberType(type)+".MAX_VALUE", 0, 1, 1));
            return interval;
        }
        return interval;
    }
}


class ComparatorBounaryWithFreqs implements Comparator {
    @Override
    public int compare(Object arg0, Object arg1) {

        BoundaryWithFreq boundaryWithFreq0 = (BoundaryWithFreq) arg0;
        BoundaryWithFreq boundaryWithFreq1 = (BoundaryWithFreq) arg1;
        if (boundaryWithFreq0.isSimpleType && boundaryWithFreq1.isSimpleType) {
            if (boundaryWithFreq0.dvalue < boundaryWithFreq1.dvalue) {
                return -1;
            }
            if (boundaryWithFreq0.dvalue == boundaryWithFreq1.dvalue) {
                return 0;
            }
        }
        return 1;
    }
}