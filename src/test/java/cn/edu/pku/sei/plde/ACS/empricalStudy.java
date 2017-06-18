package cn.edu.pku.sei.plde.ACS;

import cn.edu.pku.sei.plde.ACS.boundary.BoundaryCollect;
import cn.edu.pku.sei.plde.ACS.boundary.model.Interval;
import cn.edu.pku.sei.plde.ACS.fix.DocumentBaseFilter;
import cn.edu.pku.sei.plde.ACS.localization.Suspicious;
import cn.edu.pku.sei.plde.ACS.sort.VariableSort;
import cn.edu.pku.sei.plde.ACS.trace.filter.SearchBoundaryFilter;
import cn.edu.pku.sei.plde.ACS.type.TypeUtils;
import cn.edu.pku.sei.plde.ACS.utils.*;
import cn.edu.pku.sei.plde.ACS.visible.model.VariableInfo;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression;
import org.junit.Test;

import java.io.File;
import java.util.*;

/**
 * Created by yanrunfa on 16/7/15.
 */

class Pair{
    int numerator;
    int denominator;
    float percent;

    Pair(int numerator, int denominator){
        this.numerator = numerator;
        this.denominator = denominator;
        this.percent = numerator/ (float) denominator;
    }

    @Override
    public String toString(){
        return this.numerator + "/" + this.denominator;
    }

    static String  getMedian(List<Pair> list){
        if (list.size() == 0){
            return "";
        }
        if (list.size() == 1){
            return list.get(0).toString();
        }
        if (list.size()%2 == 0){
            Pair left = list.get(list.size()/2-1);
            Pair right = list.get(list.size()/2);
            return new Pair(left.numerator+right.numerator, left.denominator+right.denominator).toString();
        }
        return list.get((list.size()-1)/2).toString();
    }
}
public class empricalStudy {
    private static String NOW_PROJECT;
    private static String NOW_PROJECT_PATH;
    private static int javaCount = 0;
    private static List<Integer> variableRankResult = new ArrayList<>();
    private static List<Integer> variableCountResult = new ArrayList<>();
    private static Map<Integer, Integer> variableLevelResult = new HashMap<>();
    private static Map<Integer, Integer> searchBoundaryResult = new HashMap<>();
    private static Map<Integer, Integer> searchBoundaryUnFitResult = new HashMap<>();
    private static Map<String, Integer> javaDocThrowResult = new HashMap<>();

    private static List<String> javaDocTrueMessage = new ArrayList<>();
    private static List<String> javaDocFalseMessage = new ArrayList<>();
    private static String classpath;
    private static String testClasspath;
    private static String classSrc;
    private static int clasSrcLevel;
    private static String testClassSrc;
    private static String className;
    private static String javaSource;
    private static List<String> baseOperators = Arrays.asList(">","<","==","<=",">=","!=");

    private void resultDataInit(){
        javaDocThrowResult.put("No Throw Annotation",0);
        javaDocThrowResult.put("No Variable In Annotation", 0);
        javaDocThrowResult.put("true",0);
        javaDocThrowResult.put("false", 0);

        variableLevelResult = new HashMap<>();
        variableRankResult = new ArrayList<>();
        variableCountResult = new ArrayList<>();
        searchBoundaryResult = new HashMap<>();
        searchBoundaryUnFitResult = new HashMap<>();

        javaDocTrueMessage = new ArrayList<>();
        javaDocFalseMessage = new ArrayList<>();

        javaCount = 0;

        classpath ="";
        testClasspath = "";
        testClassSrc = "";
        classSrc = "";
        clasSrcLevel = 1;
    }

    @Test
    public void empiricalStudy(){
        //wj begin
        File projects = new File(System.getProperty("user.dir")+"/experiment/repo");
        //File projects = new File("E://Test//");
        //wj end
        for (File project: projects.listFiles()){

            NOW_PROJECT = project.getName();
            //if (!NOW_PROJECT.contains("iosched")){
            //    continue;
            //}
            NOW_PROJECT_PATH = project.getAbsolutePath();

            resultDataInit();
            iterJavaFile(project, 1);
            //recordSearchBoundary(project.getName());
            //recordVariableRank(project.getName());
            recordJavaDoc(project.getName());
            System.out.println(javaCount);

        }
    }

    private void recordVariableRank(String project){
        RecordUtils.record(project+":"+variableLevelResult.toString()+"\n","VariableRankResult");
        int size = map2List(variableLevelResult).size();
        if (variableLevelResult.containsKey(1)){
            RecordUtils.record(project+" level1:"+(variableLevelResult.get(1)/(float)size)+"\n","VariableRankResult");
        }
        if (variableLevelResult.containsKey(2)){
            RecordUtils.record(project+" level1:"+(variableLevelResult.get(2)/(float)size)+"\n","VariableRankResult");
        }
        int rankSum = 0;
        int countSum = 0;
        List<Pair> datas = new ArrayList<>();
        for (int i=0; i< variableRankResult.size(); i++){
            if (variableCountResult.get(i)< variableRankResult.get(i)){
                System.out.println("ERROR");
            }
            datas.add(new Pair(variableRankResult.get(i), variableCountResult.get(i)));
        }
        Collections.sort(datas, new Comparator<Pair>() {
            @Override
            public int compare(Pair pair, Pair t1) {
                if (pair.percent == t1.percent){
                    return 0;
                }
                return pair.percent > t1.percent ? 1 : -1;
            }
        });
        RecordUtils.record(project+":"+
                " Medium: "+Pair.getMedian(datas)+
                " Maximum: "+datas.get(0).toString()+
                " Minimum: "+datas.get(datas.size()-1).toString()+
                "\n","VariableRankResult");

        RecordUtils.record(project+":"+combine(variableRankResult, variableCountResult).toString()+"\n","VariableRankData");
        RecordUtils.record(project+":"+
                " Medium: "+getMedian(variableRankResult)+
                " Maximum: "+getMaximum(variableRankResult)+
                " Average: "+getAverage(variableRankResult)+
                " Length: "+getLength(variableRankResult)+
                "\n","VariableRankResult");

    }

    private void recordJavaDoc(String project){
        RecordUtils.record(project+":"+javaDocThrowResult.toString(),"javaDocThrowResult");
        RecordUtils.record("True :", "javaDocThrowResult");
        for (String message: javaDocTrueMessage){
            RecordUtils.record(message, "javaDocThrowResult");
        }
        RecordUtils.record("False :", "javaDocThrowResult");
        RecordUtils.record("\n", "javaDocThrowResult");
        for (String message: javaDocFalseMessage){
            RecordUtils.record(message, "javaDocThrowResult");
        }
        RecordUtils.record("\n\n", "javaDocThrowResult");
    }

    private void recordSearchBoundary(String project){
        List<Integer> listResult = map2List(searchBoundaryResult);
        RecordUtils.record(project+":"+searchBoundaryResult.toString(), "SearchBoundaryResult");
        RecordUtils.record(project+":"+
                " Medium: "+getMedian(listResult)+
                " Maximum: "+getMaximum(listResult)+
                " Average: "+getAverage(listResult)+
                " Length: "+getLength(listResult)+
                "\n","SearchBoundaryResult");
        listResult = map2List(searchBoundaryUnFitResult);
        RecordUtils.record(project+":"+searchBoundaryUnFitResult.toString(), "searchBoundaryUnFitResult");
        RecordUtils.record(project+":"+
                " Medium: "+getMedian(listResult)+
                " Maximum: "+getMaximum(listResult)+
                " Average: "+getAverage(listResult)+
                " Length: "+getLength(listResult)+
                "\n","searchBoundaryUnFitResult");
    }
    private void iterJavaFile(File pkg, int level){
        if (pkg.getName().endsWith(".java")){
            //if (!pkg.getName().contains("ServiceMethod")){
            //    return;
            //}
            System.out.println("Processing Java File :" + pkg.getName());
            javaSource = FileUtils.getCodeFromFile(pkg);
            javaCount ++;

            statistics(javaSource);
        }
        else if (pkg.isDirectory()){
            if (pkg.getAbsolutePath().endsWith("/java") || pkg.getAbsolutePath().endsWith("/example") || pkg.getAbsolutePath().endsWith("src")){
                if (level <= clasSrcLevel || classSrc.equals("") || pkg.getAbsolutePath().endsWith("/java")){
                    classSrc = pkg.getAbsolutePath();
                    clasSrcLevel = level;
                }
            }
            for (File file: pkg.listFiles()){
                iterJavaFile(file, level+1);
            }
        }
    }

    private void statistics(String classCode) {
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setSource(classCode.toCharArray());
        CompilationUnit unit = (CompilationUnit) parser.createAST(null);
        if (unit.types().size() <= 0) {
            System.out.println("Parser fail");
            return;
        }
        TypeDeclaration declaration = (TypeDeclaration) unit.types().get(0);
        if (declaration.isInterface()) {
            System.out.println("Interface jumped");
            return;
        }
        List<MethodDeclaration> methodDeclarations = new ArrayList<>();
        methodDeclarations.addAll(Arrays.asList(declaration.getMethods()));
        List<TypeDeclaration> typeDeclarations = new ArrayList<>();
        typeDeclarations.addAll(Arrays.asList(declaration.getTypes()));
        for (TypeDeclaration typeDeclaration: typeDeclarations){
            methodDeclarations.addAll(Arrays.asList(typeDeclaration.getMethods()));
        }
        if (unit.getPackage() != null) {
            className = unit.getPackage().getName().getFullyQualifiedName() + "." + declaration.getName().toString();
        } else {
            className = declaration.getName().toString();
        }
        for (MethodDeclaration method : methodDeclarations) {
            if (method.getBody() == null) {
                System.out.println("Method null body jumped");
                continue;
            }
            List<Statement> statements = method.getBody().statements();
            for (Statement statement : statements) {
                parseStatement(statement, unit, method, classCode);
            }
        }
    }

    private void parseStatement(Statement statement, CompilationUnit unit, MethodDeclaration method, String classCode){
        if (statement instanceof Block){
            List<Statement> statements = ((Block) statement).statements();
            for (Statement s: statements){
                parseStatement(s, unit, method, classCode);
            }
        }
        if (statement instanceof IfStatement) {
            int lineNum = unit.getLineNumber(statement.getStartPosition());
            Expression expression = ((IfStatement) statement).getExpression();
            String methodName = method.getName().getIdentifier();
            Suspicious suspicious = new Suspicious(classpath, testClasspath, classSrc, testClassSrc, className, methodName + "()");
            List<VariableInfo> variables = suspicious.getVariableInfo(lineNum);
            //statisticsVariableRank(classCode, methodName,variables, expression,lineNum);
            //statisticsSearchBoundary(expression, variables);
            statisticsJavaDocFilter(method, (IfStatement) statement, variables);

            parseStatement(((IfStatement) statement).getElseStatement(), unit, method, classCode);
            parseStatement(((IfStatement) statement).getThenStatement(), unit, method, classCode);
        }
    }


    private void statisticsJavaDocFilter(MethodDeclaration methodDeclaration,IfStatement statement, List<VariableInfo> variableInfos){
        Map<String, String> throwAnnotationMap = new HashMap<>();
        Javadoc annotationDoc = methodDeclaration.getJavadoc();
        List<String> variableNames = getVariableNames(variableInfos);
        if (annotationDoc != null) {
            List<TagElement> annotationList = annotationDoc.tags();
            for (TagElement annotation : annotationList) {
                if (annotation.getTagName() != null && annotation.getTagName().equals("@throws")) {
                    String result = "";
                    List<ASTNode> fragements = annotation.fragments();
                    for (ASTNode node : fragements) {
                        if (node instanceof TextElement) {
                            result += node.toString() + " ";
                        }
                    }
                    if (!result.trim().equals("")) {
                        throwAnnotationMap.put(annotation.fragments().get(0).toString(), result.trim());
                    }
                }
            }
        }

        parseJavaDocStatement(statement, throwAnnotationMap, variableNames);

    }

    private void parseJavaDocStatement(Statement statement, Map<String, String> throwAnnotationMap, List<String> variableNames){
        if (statement instanceof IfStatement){
            Statement thenStatement =  ((IfStatement) statement).getThenStatement();
            if (thenStatement instanceof Block){
                List<Statement> thenStatements = ((Block) thenStatement).statements();
                if (thenStatements.size()!=1){
                    return;
                }
                thenStatement = thenStatements.get(0);
            }
            // if (throwAnnotationMap.size() == 0){
            //    javaDocThrowResult.put("No Throw Annotation", javaDocThrowResult.get("No Throw Annotation")+1);
            //}
            List<String> ifParam = getVariableNamesFromExpression(((IfStatement) statement).getExpression());
            if (ifParam.size() ==0){
                return;
            }
            if (thenStatement instanceof ThrowStatement) {
                String exceptionName ="";
                if (((ThrowStatement) thenStatement).getExpression() instanceof  ClassInstanceCreation){
                    exceptionName = ((ClassInstanceCreation)((ThrowStatement) thenStatement).getExpression()).getType().toString();
                }
                else if (((ThrowStatement) thenStatement).getExpression() instanceof MethodInvocation){
                    if (((MethodInvocation) ((ThrowStatement) thenStatement).getExpression()).getExpression() == null){
                        return;
                    }
                    exceptionName = ((MethodInvocation) ((ThrowStatement) thenStatement).getExpression()).getExpression().toString();
                }
                if (throwAnnotationMap.containsKey(exceptionName)){
                    String annotation = throwAnnotationMap.get(exceptionName);
                    parsejavaDocThrowResult(annotation, ((IfStatement) statement).getExpression(), variableNames);
                }
                else {
                    javaDocThrowResult.put("No Throw Annotation", javaDocThrowResult.get("No Throw Annotation")+1);
                }
            }
        }

    }

    private void parsejavaDocThrowResult(String annotation, Expression expression, List<String> variableNames){
        if (expression instanceof PrefixExpression){
            expression = ((PrefixExpression) expression).getOperand();
        }
        if (expression instanceof  ParenthesizedExpression){
            expression = ((ParenthesizedExpression) expression).getExpression();
        }
        List<String> subjects = AnnotationUtils.Parse(annotation);
        List<String> ifParam = getVariableNamesFromExpression(expression);
        if (ifParam.size() ==0){
            return;
        }
        //wj begin
        int count = 0;
        for(String subject : subjects) {
            if(subject.equals("")){
                continue;
            }

            for(String param : ifParam){
                if(param.equals("")){
                    continue;
                }

                List<String> paramNames = DocumentBaseFilter.splitVariableName(param);
                String lastName = paramNames.get(paramNames.size()-1);
                String firstName = "";
                String[] words = param.split("\\.");
                if(words.length >= 1){
                    firstName = words[0];
                }

                if ((" "+subject.replace("<"," ").replace(">"," ")+" ").contains(" "+lastName+" ")){
                    count ++;
                }
                else if ((" "+subject.replace("<"," ").replace(">"," ")+" ").contains(" "+param+" ")){
                    count ++;
                }
                else if ((" "+subject.replace("<"," ").replace(">"," ")+" ").contains(" "+firstName+" ")){
                    count ++;
                }
            }
        }

        if(count != 0 && count == subjects.size()){
            javaDocThrowResult.put("true", javaDocThrowResult.get("true")+1);
            javaDocTrueMessage.add(annotation +" / " + expression.toString() +" / "+ subjects.toString() + " / " + ifParam.toString() + " / " + variableNames.toString());
            return;
        }

        for (String variable: variableNames){
            if (variable.equals("this")){
                continue;
            }
            for (String subject: subjects){
                subject = " "+subject.replace("<"," ").replace(">"," ")+" ";
                if (subject.contains(" "+variable.trim()+" ")) {
                    boolean flag = false;
                    for(String param : ifParam){
                        if(param.contains(variable.trim())){
                            flag = true;
                        }
                    }
                    if(flag){
                        continue;
                    }
                    javaDocThrowResult.put("false", javaDocThrowResult.get("false")+1);
                    javaDocFalseMessage.add(annotation +" / " + expression.toString() +" / "+ subjects.toString() + " / " + ifParam.toString() + " / " + variableNames.toString());
                    return;
                }
            }
        }

        // wj end
        //wj comment begin
//        int count = 0;
//        for (String param: ifParam){
//            if (param.equals("")){
//                continue;
//            }
//            List<String> paramNames = DocumentBaseFilter.splitVariableName(param);
//            String name = paramNames.get(paramNames.size()-1);
//            for (String subject: subjects){
//                if ((" "+subject.replace("<"," ").replace(">"," ")+" ").contains(" "+name+" ")){
//                    count ++;
//                }
//                else if ((" "+subject.replace("<"," ").replace(">"," ")+" ").contains(" "+param+" ")){
//                    count ++;
//                }
//            }
//        }
//        if (count == ifParam.size()){
//            javaDocThrowResult.put("true", javaDocThrowResult.get("true")+1);
//            javaDocTrueMessage.add(annotation +" / " + expression.toString() +" / "+ variableNames.toString());
//            return;
//        }
//        for (String variable: variableNames){
//            if (variable.equals("this")){
//                continue;
//            }
//            for (String subject: subjects){
//                subject = " "+subject.replace("<"," ").replace(">"," ")+" ";
//                if (subject.contains(" "+variable.trim()+" ")) {
//                    javaDocThrowResult.put("false", javaDocThrowResult.get("false")+1);
//                    javaDocFalseMessage.add(annotation +" / " + expression.toString() +" / "+ variableNames.toString());
//                    return;
//                }
//            }
//        }
        //wj comment end
        javaDocThrowResult.put("No Variable In Annotation", javaDocThrowResult.get("No Variable In Annotation")+1);
    }

    private void statisticsSearchBoundary(Expression expression, List<VariableInfo> variableInfos){
        VariableInfo info = null;
        String infoType = null;
        boolean isClass = false;
        if (expression instanceof PrefixExpression){
            expression = ((PrefixExpression) expression).getOperand();
        }
        if (expression instanceof  ParenthesizedExpression){
            expression = ((ParenthesizedExpression) expression).getExpression();
        }
        if (expression instanceof InfixExpression) {
            if (!baseOperators.contains(((InfixExpression) expression).getOperator().toString())) {
                return;
            }
            String variableName = ((InfixExpression) expression).getLeftOperand().toString();
            if (variableName.startsWith("this.")){
                variableName = variableName.substring(variableName.indexOf(".")+1);
            }
            info = getNeedsVariable(variableInfos, variableName);
            if (info == null){
                info = getNeedsVariable(variableInfos, ((InfixExpression) expression).getRightOperand().toString());
            }
            if (info != null) {
                isClass = !TypeUtils.isSimpleType(info.getStringType());
            }
            if (((InfixExpression) expression).getRightOperand().toString().equals("null")){
                if (searchBoundaryUnFitResult.containsKey(1)){
                    searchBoundaryUnFitResult.put(1, searchBoundaryUnFitResult.get(1)+1);
                }
                else {
                    searchBoundaryUnFitResult.put(1, 1);
                }
                if (searchBoundaryResult.containsKey(1)){
                    searchBoundaryResult.put(1, searchBoundaryResult.get(1)+1);
                }
                else {
                    searchBoundaryResult.put(1, 1);
                }
                return;
            }
        }
        if (expression instanceof InstanceofExpression){
            InstanceofExpression instanceofExpression = (InstanceofExpression) expression;
            String infoName = instanceofExpression.getLeftOperand().toString();
            info = getNeedsVariable(variableInfos, infoName);
            isClass = true;
        }
        if (expression instanceof MethodInvocation && expression.toString().contains(".equals")){
            MethodInvocation methodInvocation = (MethodInvocation) expression;
            String infoName = methodInvocation.getName().toString();
            info = getNeedsVariable(variableInfos, infoName);
            isClass = true;
        }
        //if (expression instanceof SimpleName){
        //    String infoName = ((SimpleName) expression).getIdentifier();
        //    info = getNeedsVariable(variableInfos, infoName);
        //    isClass = false;
        //}
        if (info == null){
            return;
        }
        if (infoType == null){
            infoType = info.getStringType();
        }
        List<Interval> intervals = BoundaryCollect.getIntervalFromExpression(expression, infoType, isClass);
        if (intervals == null || intervals.size() != 1){
            return;
        }
        System.out.println(expression.toString());
        Interval myInterval = intervals.get(0);
        ArrayList<String> keywords = new ArrayList<>(Arrays.asList("if",info.variableName, info.getStringType()));
        File codePackage = new File("experiment/searchcode/" + StringUtils.join(keywords,"-"));
        if (!codePackage.exists()){
            SearchBoundaryFilter.searchCode(keywords, NOW_PROJECT);
        }
        BoundaryCollect boundaryCollect = new BoundaryCollect(codePackage.getAbsolutePath(), false, info.getStringType(), javaSource);
        List<Map.Entry<Interval, Integer>> boundaryList = boundaryCollect.getBoundaryIntervalMapList();
        List<Map.Entry<Interval, Integer>> filteredIntervals = new ArrayList<>();
        for (int i=0; i< boundaryList.size(); i++){
            if (myInterval.equals(boundaryList.get(i).getKey())){
                int rank = getMinimumRank(boundaryList, i);
                if (searchBoundaryUnFitResult.containsKey(rank)){
                    searchBoundaryUnFitResult.put(rank, searchBoundaryUnFitResult.get(rank)+1);
                }
                else {
                    searchBoundaryUnFitResult.put(rank, 1);
                }
            }
            if (!Interval.hasCommon(boundaryList.get(i).getKey(), myInterval)){
                filteredIntervals.add(boundaryList.get(i));
            }
        }
        boundaryList.removeAll(filteredIntervals);
        for (int i=0; i< boundaryList.size(); i++){
            if (myInterval.equals(boundaryList.get(i).getKey())){
                int rank = getMinimumRank(boundaryList, i);
                if (searchBoundaryResult.containsKey(rank)){
                    searchBoundaryResult.put(rank, searchBoundaryResult.get(rank)+1);
                }
                else {
                    searchBoundaryResult.put(rank, 1);
                }
                return;
            }
        }
        if (searchBoundaryResult.containsKey(Integer.MAX_VALUE)){
            searchBoundaryResult.put(Integer.MAX_VALUE, searchBoundaryResult.get(Integer.MAX_VALUE)+1);
        }
        else {
            searchBoundaryResult.put(Integer.MAX_VALUE, 1);
        }
    }

    private void statisticsVariableRank(String classCode, String methodName, List<VariableInfo> variables, Expression expression, int lineNum){
        if (expression instanceof PrefixExpression){
            expression = ((PrefixExpression) expression).getOperand();
        }
        if (expression instanceof  ParenthesizedExpression){
            expression = ((ParenthesizedExpression) expression).getExpression();
        }
        List<String> variableNames = getVariableNames(variables);
        if (variableNames.isEmpty()) {
            return;
        }
        List<String> collectedVariable = getVariableNamesFromExpression(expression);
        if (collectedVariable.size() == 0){
            return;
        }
        String statementsBeforeIf = CodeUtils.getMethodBodyBeforeLine(classCode, methodName, lineNum);
        VariableSort variableSort = new VariableSort(new HashSet<>(variableNames), statementsBeforeIf);
        List<List<String>> sortedVariable = variableSort.getSortVariable();
        int rank = 0;
        for (int i = 0; i < sortedVariable.size(); i++) {
            List<String> level = sortedVariable.get(i);
            for (String var: collectedVariable){
                if (level.contains(var)) {
                    variableRankResult.add(rank+level.indexOf(var)+1);
                    variableCountResult.add(variables.size());
                }
            }
            boolean containFlag = false;
            for (String var: collectedVariable){
                if (level.contains(var)) {
                    if (variableLevelResult.containsKey(i+1)){
                        variableLevelResult.put(i+1, variableLevelResult.get(i+1)+1);
                    }
                    else {
                        variableLevelResult.put(i+1, 1);
                    }
                    containFlag = true;
                }
            }
            if (!containFlag){
                rank += level.size();
            }
        }
    }
    private static List<String> getVariableNamesFromExpression(Expression expression){
        if (expression instanceof PrefixExpression){
            expression = ((PrefixExpression) expression).getOperand();
        }
        if (expression instanceof  ParenthesizedExpression){
            expression = ((ParenthesizedExpression) expression).getExpression();
        }
        if (expression instanceof InfixExpression){
            InfixExpression infixExpression = (InfixExpression) expression;
            if (infixExpression.getOperator().toString().equals("||") || infixExpression.getOperator().toString().equals("&&")){
                List<String> left = getVariableNamesFromExpression(infixExpression.getLeftOperand());
                List<String> right = getVariableNamesFromExpression(infixExpression.getRightOperand());
                List<String> result = new ArrayList<>();
                result.addAll(left);
                result.addAll(right);
                return result;
            }
            else {
                List<String> result = new ArrayList<>();
                if (infixExpression.getLeftOperand() instanceof SimpleName){
                    result.add(infixExpression.getLeftOperand().toString());
                }
                if (infixExpression.getRightOperand() instanceof SimpleName){
                    result.add(infixExpression.getRightOperand().toString());
                }
                return result;
            }
        }
        if (expression instanceof InstanceofExpression){
            return Arrays.asList(((InstanceofExpression) expression).getLeftOperand().toString());
        }
        if (expression instanceof SimpleName){
            return Arrays.asList(((SimpleName) expression).getIdentifier().toString());
        }
        if (expression instanceof MethodInvocation){
            //wj begin
            List<String> ifParam = new ArrayList<String>();
            String expStr = expression.toString();
            if(expStr.contains(".")){
                String[] words = expStr.split("\\.");
                ifParam.add(words[0]);
            }

            ifParam.addAll(Arrays.asList(((MethodInvocation) expression).getName().toString()));
            return ifParam;
            //return Arrays.asList(((MethodInvocation) expression).getName().toString());
            //wj end
        }


        return new ArrayList<>();
    }

    private static List<String> getVariableNames(List<VariableInfo> variableInfos){
        List<String> result = new ArrayList<>();
        for (VariableInfo info: variableInfos){
            result.add(info.variableName);
        }
        return result;
    }

    private static VariableInfo getNeedsVariable(List<VariableInfo> variableInfos, String variableName){
        for (VariableInfo info: variableInfos){
            if (info.variableName.equals(variableName)){
                return info;
            }
        }
        return null;
    }

    private static int getMedian(List<Integer> list){
        Collections.sort(list);
        List<Integer> copy = new ArrayList<>(list);
        if (copy.contains(Integer.MAX_VALUE)){
            copy = copy.subList(0, copy.indexOf(Integer.MAX_VALUE));
        }
        if (copy.size() == 0){
            return 0;
        }
        if (copy.size() == 1){
            return copy.get(0);
        }
        if (copy.size()%2 == 0){
            return (copy.get(copy.size()/2-1) + copy.get(copy.size()/2)) /2;
        }
        return copy.get((copy.size()-1)/2);
    }

    private static int getMaximum(List<Integer> list){
        List<Integer> copy = new ArrayList<>(list);
        if (copy.contains(Integer.MAX_VALUE)){
            copy = copy.subList(0, copy.indexOf(Integer.MAX_VALUE));
        }
        if (copy.size() == 0){
            return 0;
        }
        return Collections.max(copy);
    }


    private static float getAverage(List<Integer> list){
        long sum = 0;
        int size = 0;
        for (int value: list){
            if (value != Integer.MAX_VALUE){
                sum += value;
                size ++;
            }
        }
        return sum/(float)size;
    }

    private static int getLength(List<Integer> list){
        int size = 0;
        for (int value: list){
            if (value != Integer.MAX_VALUE){
                size++;
            }
        }
        return size;
    }

    private static List<Integer> map2List(Map<Integer, Integer> map){
        List<Integer> result = new ArrayList<>();
        ArrayList<Integer> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys);
        for (int key: keys){
            for (int i=0; i< map.get(key); i++){
                result.add(key);
            }
        }
        return result;
    }

    private static List<String> combine(List<Integer> numerator, List<Integer> denominator){
        List<String> result = new ArrayList<>();
        for (int i=0; i< numerator.size(); i++){
            if (numerator.get(i) != Integer.MAX_VALUE){
                result.add(numerator.get(i)+"/"+denominator.get(i));
            }
        }
        return result;
    }

    private static int getMinimumRank(List<Map.Entry<Interval, Integer>> list, int rank){
        for (int i=0; i< rank; i++){
            if (list.get(i).getValue().equals(list.get(rank).getValue())){
                return i+1;
            }
        }
        return rank+1;
    }
}
