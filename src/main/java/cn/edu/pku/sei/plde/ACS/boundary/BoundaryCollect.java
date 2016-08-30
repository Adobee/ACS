package cn.edu.pku.sei.plde.ACS.boundary;


import cn.edu.pku.sei.plde.ACS.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.ACS.boundary.model.BoundaryWithFreq;
import cn.edu.pku.sei.plde.ACS.boundary.model.Interval;
import cn.edu.pku.sei.plde.ACS.file.ReadFile;
import cn.edu.pku.sei.plde.ACS.jdtVisitor.BoundaryCollectVisitor;
import cn.edu.pku.sei.plde.ACS.type.TypeEnum;
import cn.edu.pku.sei.plde.ACS.utils.*;
import org.easymock.cglib.transform.impl.InterceptFieldCallback;
import org.eclipse.jdt.core.dom.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoundaryCollect {
    private String rootPath;
    private ArrayList<String> filesPath;
    private ArrayList<BoundaryInfo> boundaryList;

    private boolean isClass;
    public String className;
    private String code;

    public BoundaryCollect(String rootPath, boolean isClass, String className) {
        this.rootPath = rootPath;
        boundaryList = new ArrayList<BoundaryInfo>();
        this.isClass = isClass;
        this.className = className;
        // generateBoundaryList();
    }

    public BoundaryCollect(String rootPath, boolean isClass, String className, String code) {
        this.rootPath = rootPath;
        boundaryList = new ArrayList<BoundaryInfo>();
        this.isClass = isClass;
        this.className = className;
        this.code = code;
    }

    private ArrayList<String> getBoundaryString(){
        filesPath = FileUtils.getJavaFilesInProj(rootPath);
        ArrayList<String> result = new ArrayList<String>();
        for (String filePath : filesPath) {
            try {
                String source = new ReadFile(filePath).getSource();
                if (code!=null && code.replace("\n","").replace(" ","").contains(source.replace("\n","").replace(" ",""))){
                    continue;
                }
                if(source.contains("static") && source.contains("final")){
                    String[] lines = source.split("\\n");
                    for(String line : lines){
                        if(line.contains("static") && line.contains("final")){
                            line = "public class Test { " + line + "}";
                            ASTNode root = JDTUtils.createASTForSource(line, ASTParser.K_COMPILATION_UNIT);
                            if (root == null) {
                                continue;
                            }
                            BoundaryCollectVisitor boundaryCollectVisitor = new BoundaryCollectVisitor();
                            root.accept(boundaryCollectVisitor);
                            ArrayList<BoundaryInfo> boundaryListSub = boundaryCollectVisitor.getBoundaryInfoList();
                            for (BoundaryInfo boundaryInfo : boundaryListSub) {
                                result.add("if ("+boundaryInfo.name+" == "+boundaryInfo.value+"){");
                            }
                        }
                    }
                }
                Pattern ifPattern = Pattern.compile("if\\s*\\(.*?\\)\\s*\\{");
                Matcher ifMatcher = ifPattern.matcher(source);
                while(ifMatcher.find()){
                    result.add(ifMatcher.group(0));
                }
                Pattern whilePattern = Pattern.compile("while\\s*\\(.*?\\)\\s*\\{");
                Matcher whileMatcher = whilePattern.matcher(source);
                while(whileMatcher.find()){
                    result.add(whileMatcher.group(0));
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private void generateBoundaryList() {
        filesPath = FileUtils.getJavaFilesInProj(rootPath);
        for (String filePath : filesPath) {
            try {
                String source = new ReadFile(filePath).getSource();
                if(source.contains("static") && source.contains("final")){
                    String[] lines = source.split("\\n");
                    for(String line : lines){
                        if(line.contains("static") && line.contains("final")){
                            line = "public class Test { " + line + "}";
                            ASTNode root = JDTUtils.createASTForSource(line, ASTParser.K_COMPILATION_UNIT);
                            if (root == null) {
                                return;
                            }
                            BoundaryCollectVisitor boundaryCollectVisitor = new BoundaryCollectVisitor();
                            root.accept(boundaryCollectVisitor);
                            ArrayList<BoundaryInfo> boundaryListSub = boundaryCollectVisitor.getBoundaryInfoList();
                            for (BoundaryInfo boundaryInfo : boundaryListSub) {
                                boundaryInfo.fileName = filePath;
                            }
                            boundaryList.addAll(boundaryListSub);
                        }
                    }
                }
                Pattern ifPattern = Pattern.compile("if\\s*\\(.*?\\)");
                Matcher ifMatcher = ifPattern.matcher(source);
                List<String> result = new ArrayList<String>();
                while(ifMatcher.find()){
                    result.add(ifMatcher.group(0) + "{}");
                }
                Pattern whilePattern = Pattern.compile("while\\s*\\(.*?\\)");
                Matcher whileMatcher = whilePattern.matcher(source);
                while(whileMatcher.find()){
                    result.add(whileMatcher.group(0) + "{}");
                }
                for(String ifExpression : result) {
                    ASTNode root = JDTUtils.createASTForSource(ifExpression, ASTParser.K_STATEMENTS);
                    if (root == null) {
                        return;
                    }
                    BoundaryCollectVisitor boundaryCollectVisitor = new BoundaryCollectVisitor();
                    root.accept(boundaryCollectVisitor);
                    ArrayList<BoundaryInfo> boundaryListSub = boundaryCollectVisitor.getBoundaryInfoList();
                    for (BoundaryInfo boundaryInfo : boundaryListSub) {
                        boundaryInfo.fileName = filePath;
                    }
                    boundaryList.addAll(boundaryListSub);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<BoundaryInfo> getBoundaryList() {
        generateBoundaryList();
        return boundaryList;
    }


    public List<Map.Entry<Interval, Integer>> getBoundaryIntervalMapList(){
        if (className.equalsIgnoreCase("boolean")){
            Map<Interval, Integer> resultMap = new HashMap<>();
            resultMap.put(new Interval(true),  1);
            resultMap.put(new Interval(false), 1);
            return new ArrayList<Map.Entry<Interval, Integer>>(resultMap.entrySet());
        }
        Map<Interval, Integer> intervalCountMap = new HashMap<>();
        ArrayList<String> boundaryStrings = getBoundaryString();
        for (String boundary: boundaryStrings){
            while (CodeUtils.countChar(boundary, '(') > CodeUtils.countChar(boundary, ')')){
                boundary += ")";
            }
            ASTNode root = JDTUtils.createASTForSource(boundary+"}", ASTParser.K_STATEMENTS);
            IfStatement ifStatement = null;
            WhileStatement whileStatement = null;
            Expression expression = null;
            try {
                ifStatement =  (IfStatement) ((List)root.getStructuralProperty(
                        (StructuralPropertyDescriptor)root.structuralPropertiesForType().get(0))).get(0);
                expression = ifStatement.getExpression();
            } catch (Exception e){

            }
            if (ifStatement == null){
                try {
                    whileStatement = (WhileStatement) ((List)root.getStructuralProperty(
                            (StructuralPropertyDescriptor)root.structuralPropertiesForType().get(0))).get(0);
                    expression = whileStatement.getExpression();
                } catch (Exception e){
                    continue;
                }
            }
            if (!(expression instanceof InfixExpression ||
                    expression instanceof InstanceofExpression)){
                continue;
            }
            List<Interval> intervals = getIntervalFromExpression(expression, className, isClass);
            if (intervals != null){
                for (Interval interval: intervals){
                    boolean countedFlag = false;
                    for (Interval exist: intervalCountMap.keySet()){
                        if (exist.equals(interval)){
                            intervalCountMap.put(exist, intervalCountMap.get(exist)+1);
                            countedFlag = true;
                            break;
                        }
                    }
                    if (!countedFlag){
                        intervalCountMap.put(interval, 1);
                    }
                }
            }
        }
        Map<Interval, Integer> reversedNewInterval = new HashMap<>();
        for (Map.Entry<Interval, Integer> entry: intervalCountMap.entrySet()){
            List<Interval> reversedList = entry.getKey().reverse();
            for (Interval reversed: reversedList){
                if (reversed == null){
                    continue;
                }
                if (reversedNewInterval.containsKey(reversed)){
                    reversedNewInterval.put(reversed, reversedNewInterval.get(reversed) +entry.getValue());
                }
                else {
                    reversedNewInterval.put(reversed, entry.getValue());
                }
            }
        }
        intervalCountMap.putAll(reversedNewInterval);
        List<Map.Entry<Interval, Integer>> entries = new ArrayList<Map.Entry<Interval, Integer>>(intervalCountMap.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<Interval, Integer>>() {
            public int compare(Map.Entry<Interval, Integer> obj1 , Map.Entry<Interval, Integer> obj2) {
                if (obj1.getValue().equals(obj2.getValue())){
                    if (obj1.getKey().isValue() && ! obj2.getKey().isValue()){
                        return -1;
                    }
                    if (obj2.getKey().isValue() && ! obj1.getKey().isValue()){
                        return 1;
                    }
                }
                return obj2.getValue() - obj1.getValue();
            }
        });
        if (BoundaryGenerator.GENERATING_VARIABLE!=null){
            RecordUtils.printBoundaryCollectMessage(entries);
        }
        return entries;
    }


    public ArrayList<Interval> getBoundaryInterval(){
        List<Map.Entry<Interval, Integer>> entries = getBoundaryIntervalMapList();
        ArrayList<Interval> result = new ArrayList<>();
        for (int i=0; i< 20; i++){
            if (i<entries.size()){
                result.add(entries.get(i).getKey());
            }
        }
        return result;
    }

    public static List<Interval> getIntervalFromExpression(Expression rawExpression, String className, boolean isClass){
        if (className.equals("Comparable")){
            if (rawExpression instanceof InstanceofExpression){
                return Arrays.asList(new Interval(true));
            }
            return Arrays.asList(new Interval(false));
        }
        if (rawExpression instanceof MethodInvocation && rawExpression.toString().contains(".equals(") && isClass){
            MethodInvocation invocation = (MethodInvocation) rawExpression;
            return Arrays.asList(new Interval(invocation.getExpression().toString()));

        }
        if (rawExpression instanceof InstanceofExpression){
            return Arrays.asList(new Interval(((InstanceofExpression)rawExpression).getRightOperand().toString()));
        }
        if (rawExpression instanceof SimpleName){
            return Arrays.asList(new Interval(true));
        }
        InfixExpression expression;
        try {
            expression = (InfixExpression) rawExpression;
        } catch (ClassCastException e){
            return null;
        }
        if (expression.getOperator().toString().equals("||")){
            List<Interval> leftIntervals = null;
            List<Interval> rightIntervals = null;
            if (expression.getLeftOperand() instanceof InfixExpression){
                leftIntervals = getIntervalFromExpression((InfixExpression) expression.getLeftOperand(), className, isClass);
            }
            if (expression.getRightOperand() instanceof InfixExpression){
                rightIntervals =  getIntervalFromExpression((InfixExpression) expression.getRightOperand(), className, isClass);
            }
            if (leftIntervals == null && rightIntervals == null){
                return null;
            }
            if (leftIntervals == null){
                return rightIntervals;
            }
            if (rightIntervals == null){
                return leftIntervals;
            }
            ArrayList<Interval> intervals = new ArrayList<>(leftIntervals);
            intervals.addAll(rightIntervals);
            Interval.simplify(intervals);
            return intervals;
        }
        else if (expression.getOperator().toString().equals("&&")) {
            List<Interval> leftIntervals = null;
            List<Interval> rightIntervals = null;
            if (expression.getLeftOperand() instanceof InfixExpression){
                leftIntervals = getIntervalFromExpression((InfixExpression) expression.getLeftOperand(), className, isClass);
            }
            if (expression.getRightOperand() instanceof InfixExpression){
                rightIntervals =  getIntervalFromExpression((InfixExpression) expression.getRightOperand(), className, isClass);
            }
            if (leftIntervals == null && rightIntervals == null){
                return null;
            }
            if (leftIntervals == null){
                return rightIntervals;
            }
            if (rightIntervals == null){
                return leftIntervals;
            }
            ArrayList<Interval> intersections = new ArrayList<>();
            for (Interval left: leftIntervals){
                for (Interval right: rightIntervals){
                    Interval intersection = Interval.intersection(left, right);
                    if (intersection != null){
                        intersections.add(intersection);
                    }
                }
            }
            Interval.simplify(intersections);
            return intersections;
        }
        else if (expression.getOperator().toString().contains(">")){
            if (MathUtils.isNumberType(className)){
                try {
                    Double value = MathUtils.parseStringValue(expression.getRightOperand().toString());
                    if (expression.getOperator().toString().contains("=")){
                        return Arrays.asList(new Interval(value, Double.MAX_VALUE, true, false));
                    }
                    else {
                        return Arrays.asList(new Interval(value, Double.MAX_VALUE, false, false));
                    }
                } catch (NumberFormatException e){
                    return null;
                }
            }
            return null;
        }
        else if (expression.getOperator().toString().contains("<")){
            if (MathUtils.isNumberType(className)){
                try {
                    Double value = MathUtils.parseStringValue(expression.getRightOperand().toString());
                    if (expression.getOperator().toString().contains("=")){
                        return Arrays.asList(new Interval(-Double.MAX_VALUE, value, false, true));
                    }
                    else {
                        return Arrays.asList(new Interval(-Double.MAX_VALUE, value, false, false));
                    }
                } catch (NumberFormatException e){
                    return null;
                }
            }
            return null;
        }
        else if (expression.getOperator().toString().equals("==")){
            try {
                Double value = MathUtils.parseStringValue(expression.getRightOperand().toString());
                if (MathUtils.isNumberType(className)){
                    return Arrays.asList(new Interval(value, value, true, true));
                }
            } catch (NumberFormatException e){
                if (className.equals("object")){
                    if (expression.getRightOperand().toString().equals("null")){
                        return Arrays.asList(new Interval(true));
                    }
                }
                else if (isClass){
                    return Arrays.asList(new Interval(expression.getRightOperand().toString()));
                }
                return null;
            }
        }
        else if (expression.getOperator().toString().equals("!=")){
            if (className.equals("object") && expression.getRightOperand().toString().equals("null")){
                return Arrays.asList(new Interval(false));
            }
            else if (isClass){
                return Arrays.asList(new Interval(expression.getRightOperand().toString()));
            }
        }
        return null;
    }


    public ArrayList<BoundaryWithFreq> getBoundaryWithFreqList() {
        generateBoundaryList();
        ArrayList<BoundaryWithFreq> boundaryWithFreqs = new ArrayList<BoundaryWithFreq>();
        //System.out.println("size = " + boundaryWithFreqs.size());
        for (BoundaryInfo boundaryInfo : boundaryList) {
            boolean flag = false;
            for (BoundaryWithFreq boundaryWithFreq : boundaryWithFreqs) {

                if(boundaryWithFreq.value.equals(boundaryInfo.value)){
                    boundaryWithFreq.freq++;
                    boundaryWithFreq.leftClose += boundaryInfo.leftClose;
                    boundaryWithFreq.rightClose += boundaryInfo.rightClose;
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                boundaryWithFreqs.add(new BoundaryWithFreq(boundaryInfo.variableSimpleType, boundaryInfo.isSimpleType,
                        boundaryInfo.otherType, boundaryInfo.value, boundaryInfo.leftClose, boundaryInfo.rightClose, 1));
            }
        }

        if(isClass){
            Iterator<BoundaryWithFreq> iterator = boundaryWithFreqs.iterator();
            while(iterator.hasNext()){
                String value = iterator.next().value;
                if(!value.contains(className) || value.contains("{")){
                    iterator.remove();
                }
            }
        } else{
            Iterator<BoundaryWithFreq> it = boundaryWithFreqs.iterator();
            while (it.hasNext()) {
                BoundaryWithFreq boundaryWithFreq = it.next();
                if (boundaryWithFreq.isSimpleType) {
                    try {
                        int valueSize = boundaryWithFreq.value.length();
                        if (boundaryWithFreq.value.endsWith("L")) {
                            boundaryWithFreq.value = boundaryWithFreq.value.substring(0, valueSize - 1);
                            //boundaryWithFreq.value = boundaryWithFreq.value.replace("L", "");
                        }
                        if (boundaryWithFreq.value.endsWith("l")) {
                            boundaryWithFreq.value = boundaryWithFreq.value.substring(0, valueSize - 1);
                            //boundaryWithFreq.value = boundaryWithFreq.value.replace("l", "");
                        }
                        if (boundaryWithFreq.value.endsWith("D")) {
                            boundaryWithFreq.value = boundaryWithFreq.value.substring(0, valueSize - 1);
                            //boundaryWithFreq.value = boundaryWithFreq.value.replace("D", "");
                        }
                        if (boundaryWithFreq.value.endsWith("d")) {
                            boundaryWithFreq.value = boundaryWithFreq.value.substring(0, valueSize - 1);
                            //boundaryWithFreq.value = boundaryWithFreq.value.replace("d", "");
                        }
                        if (boundaryWithFreq.value.endsWith("F")) {
                            boundaryWithFreq.value = boundaryWithFreq.value.substring(0, valueSize - 1);
                            //boundaryWithFreq.value = boundaryWithFreq.value.replace("F", "");
                        }
                        if (boundaryWithFreq.value.endsWith("f")) {
                            boundaryWithFreq.value = boundaryWithFreq.value.substring(0, valueSize - 1);
                            //boundaryWithFreq.value = boundaryWithFreq.value.replace("f", "");
                        }
                        boundaryWithFreq.dvalue = Double.parseDouble(boundaryWithFreq.value);

                    } catch (Exception e) {
                        if (boundaryWithFreq.value.contains("Integer.MIN_VALUE") || boundaryWithFreq.value.contains("Integer.MIN_VAUE")) {
                            boundaryWithFreq.value = "Integer.MIN_VALUE";
                            boundaryWithFreq.dvalue = Integer.MIN_VALUE;
                        } else if (boundaryWithFreq.value.contains("Integer.MAX_VALUE") || boundaryWithFreq.value.contains("Integer.MAX_VAUE")) {
                            boundaryWithFreq.value = "Integer.MAX_VALUE";
                            boundaryWithFreq.dvalue = Integer.MAX_VALUE;
                        } else if (boundaryWithFreq.value.contains("Double.MIN_VALUE") || boundaryWithFreq.value.contains("Double.MIN_VAUE")) {
                            boundaryWithFreq.value = "Double.MIN_VALUE";
                            boundaryWithFreq.dvalue = Double.MIN_VALUE;
                        } else if (boundaryWithFreq.value.contains("Double.MAX_VALUE") || boundaryWithFreq.value.contains("Double.MAX_VAUE")) {
                            boundaryWithFreq.value = "Double.MAX_VALUE";
                            boundaryWithFreq.dvalue = Double.MAX_VALUE;
                        } else if (boundaryWithFreq.value.contains("Long.MIN_VALUE") || boundaryWithFreq.value.contains("Long.MIN_VAUE")) {
                            boundaryWithFreq.value = "Long.MIN_VALUE";
                            boundaryWithFreq.dvalue = Long.MIN_VALUE;
                        } else if (boundaryWithFreq.value.contains("Long.MAX_VALUE") || boundaryWithFreq.value.contains("Long.MAX_VAUE")) {
                            boundaryWithFreq.value = "Long.MAX_VALUE";
                            boundaryWithFreq.dvalue = Long.MAX_VALUE;
                        } else {
                            it.remove();
                        }
                        continue;

                    }
                }
                if (!(boundaryWithFreq.isSimpleType &&
                        (boundaryWithFreq.variableSimpleType == TypeEnum.INT ||
                                boundaryWithFreq.variableSimpleType == TypeEnum.DOUBLE ||
                                boundaryWithFreq.variableSimpleType == TypeEnum.FLOAT || boundaryWithFreq.variableSimpleType == TypeEnum.DOUBLE.LONG))) {
                    it.remove();
                }
            }
        }


        Collections.sort(boundaryWithFreqs, new Comparator<BoundaryWithFreq>() {
            @Override
            public int compare(BoundaryWithFreq boundaryWithFreq, BoundaryWithFreq t1) {
                if (boundaryWithFreq.freq > t1.freq){
                    return -1;
                }
                if (boundaryWithFreq.freq == t1.freq){
                    return 0;
                }
                return 1;
            }
        });
        int size = boundaryWithFreqs.size();

        for(int i = 10; i < size; i ++){
            boundaryWithFreqs.remove(10);
        }
        return boundaryWithFreqs;
    }
}