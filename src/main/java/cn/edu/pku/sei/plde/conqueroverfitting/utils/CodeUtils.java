package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import cn.edu.pku.sei.plde.conqueroverfitting.file.ReadFile;
import cn.edu.pku.sei.plde.conqueroverfitting.jdtVisitor.ConstructorDeclarationCollectVisitor;
import cn.edu.pku.sei.plde.conqueroverfitting.jdtVisitor.EqualFieldCollectVisitor;
import cn.edu.pku.sei.plde.conqueroverfitting.jdtVisitor.EqualFieldCollectVisitor;
import cn.edu.pku.sei.plde.conqueroverfitting.jdtVisitor.IdentifierCollectVisitor;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.gzoltar.StatementExt;
import cn.edu.pku.sei.plde.conqueroverfitting.localizationInConstructor.model.ConstructorDeclarationInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.MethodInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import com.sun.xml.internal.ws.api.model.MEP;
import javassist.NotFoundException;
import org.apache.commons.collections.ArrayStack;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.internal.utils.FileUtil;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

import org.eclipse.jdt.internal.core.dom.NaiveASTFlattener;
import org.omg.CORBA.Object;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.w3c.dom.NodeList;

import java.io.*;
import java.util.*;

/**
 * Created by yanrunfa on 16/3/2.
 */
public class CodeUtils {

    public static Set<String> getEqualVariableInSource(String path){
        Set<String> equalVariable = new HashSet<String>();
        String source = new ReadFile(path).getSource();
        ASTNode root = JDTUtils.createASTForSource(source, ASTParser.K_COMPILATION_UNIT);
        EqualFieldCollectVisitor equalFieldCollectVisitor = new EqualFieldCollectVisitor();
        root.accept(equalFieldCollectVisitor);
        return equalFieldCollectVisitor.getEqualVariable();
    }

    public static int countParamsOfConstructorInTest(String filePath, String methodName, String newClass) throws Exception{
        String code = FileUtils.getCodeFromFile(filePath);
        String method = FileUtils.getTestFunctionCodeFromCode(code,methodName);
        for (String line: method.split("\n")){
            if (!line.contains("new") || !line.contains(newClass) ) {
                continue;
            }
            List<String> parameters = getConstructorParams(line);
            return parameters.size();
        }
        return -1;
    }


    public static int countParamsOfConstructorInAssert(String assertLine){
        return getConstructorParams(assertLine).size();
    }


    public static List<String> getMethodParams(String line, String methodName){
        if (!line.contains(methodName)){
            return new ArrayList<>();
        }
        List<String> params = divideParameter(line, 1);
        for (String param: params){
            if (param.contains(methodName)){
                return getMethodParams(param, methodName);
            }
        }
        return params;
    }

    public static Map<String, String> getMethodParamsInCode(String code, String methodName, int innerLine){
        String methodCode = getMethodString(code, methodName, innerLine);
        return getMethodParamsFromDefine(methodCode, methodName);
    }

    public static List<Integer> getMethodParamsCountInCode(String code, String methodName) {
        List<Integer> result = new ArrayList<>();
        List<String> methodCodes = getMethodStrings(code, methodName);
        for (String methodCode: methodCodes){
            result.add(getMethodParamsFromDefine(methodCode, methodName).size());
        }
        return result;
    }

    public static Map<String, String> getMethodParamsFromDefine(String methodCode, String methodName){
        Map<String, String> result = new HashMap<>();
        for (String line: methodCode.split("\n")){
            if (line.contains(" "+methodName+"(")){
                List<String> params = Arrays.asList(line.substring(line.indexOf("(")+1, line.lastIndexOf(")")).split(","));
                for (String param: params){
                    param = param.trim();
                    if (!param.contains(" ")){
                        continue;
                    }
                    result.put(param.split(" ")[1], param.split(" ")[0]);
                }
                break;
            }
        }
        return result;
    }


    public static List<String> getMethodParamsName(String line, String methodName){
        List<String> params = getMethodParams(line, methodName);
        List<String> result = new ArrayList<>();
        for (String param: params){
            if (param.contains(" ")){
                param = param.substring(param.lastIndexOf(" "));
                result.add(param.trim());
            }
        }
        return result;
    }

    private static List<String> getConstructorParams(String line){
        List<String> params = divideParameter(line, 1);
        for (String param: params){
            if (param.contains("new ")){
                return getConstructorParams(param);
            }
        }
        return params;
    }

    public static String getClassNameOfVariable(VariableInfo info, String filePath) {
        String code = FileUtils.getCodeFromFile(filePath);
        String infoType = info.getStringType();
        if (infoType.contains("<")){
            infoType = infoType.substring(0, infoType.indexOf("<"));
        }
        List<String> packages = FileUtils.getPackageImportFromCode(code);
        for (String packageName: packages){
            if (getClassNameFromPackage(packageName).equals(infoType)){
                if (packageName.startsWith("package ") || packageName.startsWith("import ")){
                    packageName = packageName.split(" ")[1];
                    packageName = packageName.substring(0, packageName.length()-1);
                }
                return packageName;
            }
        }
        String selfPackage = "";
        for (String line: code.split("\n")){
            if (line.startsWith("package ") || line.startsWith("import ")){
                selfPackage = line.split(" ")[1];
                selfPackage = selfPackage.substring(0, selfPackage.length()-1);
            }
            if (line.trim().startsWith("public class "+info.getStringType())){
                if (!selfPackage.equals("")){
                    return selfPackage+"."+info.getStringType();
                }
            }
        }
        return "";
    }

    public static String getPackageName(String code) {
        for (String line : code.split("\n")) {
            if (line.startsWith("package ")) {
                return line.split(" ")[1].substring(0, line.split(" ")[1].length()-1).trim();
            }
        }
        return "";
    }

    public static String getClassNameOfImportClass(String code, String className){
        List<String> packages = FileUtils.getPackageImportFromCode(code);
        for (String packageName: packages){
            if (getClassNameFromPackage(packageName).equals(className)){
                if (packageName.startsWith("import")){
                    packageName = packageName.substring(packageName.indexOf(" "));
                }
                if (packageName.endsWith(";")){
                    packageName = packageName.substring(0, packageName.length()-1);
                }
                return packageName;
            }
        }
        return "";
    }

    public static String getClassNameFromPackage(String packageName){
        String name = packageName.substring(packageName.lastIndexOf(".")+1);
        if (name.endsWith(";")){
            return name.substring(0, name.length()-1);
        }
        return name;
    }

    public static List<String> getAssertInTest(String code, String testMethodName){
        List<String> result = new ArrayList<>();
        String functionCode = CodeUtils.getMethodBody(code, testMethodName);
        int bracket  = 0;
        String line = "";
        for (String lineString: functionCode.split("\n")){
            if (bracket != 0){
                bracket += countChar(lineString,'(');
                bracket -= countChar(lineString,')');
                line += lineString.trim();
                if (bracket == 0){
                    result.add(line);
                }
                continue;
            }
            if (AssertUtils.isAssertLine(lineString, code)){
                line = lineString.trim();
                bracket += countChar(lineString,'(');
                bracket -= countChar(lineString,')');
                if (bracket == 0){
                    result.add(line);
                }
            }
        }
        return result;
    }

    public static Map<String, Integer> getAssertInTest(String code, String testMethodName, int methodStartLine){
        Map<String, Integer> result = new HashMap<>();
        String methodCode = FileUtils.getTestFunctionBodyFromCode(code, testMethodName);
        int bracket  = 0;
        String line = "";
        int assertStartLine = 0;
        for (String lineString: methodCode.split("\n")){
            methodStartLine++;
            if (bracket != 0){
                bracket += countChar(lineString,'(');
                bracket -= countChar(lineString,')');
                line += lineString.trim();
                if (bracket == 0){
                    if (line.contains("fail(")){
                        line = line+assertStartLine;
                    }
                    result.put(line, assertStartLine);
                }
                continue;
            }
            if (AssertUtils.isAssertLine(lineString, code)){
                line = lineString.trim();
                bracket += countChar(lineString,'(');
                bracket -= countChar(lineString,')');
                if (bracket == 0){
                    if (line.contains("fail(")){
                        line = line + methodStartLine;
                    }
                    result.put(line, methodStartLine);
                }
                else {
                    assertStartLine = methodStartLine;
                }
            }
        }
        return result;
    }

    public static List<String> getAssertInTest(String testSrcPath, String testClassname, String testMethodName){
        String code = FileUtils.getCodeFromFile(FileUtils.getFileAddressOfJava(testSrcPath, testClassname));
        return getAssertInTest(code, testMethodName);
    }

    public static String getLineFromCode(String code, int line){
        int lineNum = 0;
        for (String lineString: code.split("\n")){
            lineNum++;
            if (lineNum == line){
                return lineString.trim();
            }
        }
        return "";
    }

    public static String getWholeLineFromCode(String code, int line){
        String result = "";
        int brackets;
        do {
            result += getLineFromCode(code, line++);
            brackets = CodeUtils.countChar(result, '(') - CodeUtils.countChar(result, ')');
        } while (brackets != 0);
        return result;
    }

    public static String getWholeLineFromCodeReverse(String code, int line){
        String result = "";
        int brackets;
        do {
            result = getLineFromCode(code, line--)+result;
            brackets = CodeUtils.countChar(result, ')') - CodeUtils.countChar(result, '(');
        } while (brackets != 0 && line > 0);
        return result;
    }

    public static List<String> divideParameter(String line, int level){
        return divideParameter(line, level, true);
    }

    public static List<String> divideParameter(String line, int level, boolean dividePoint, List<Character> custom){
        line = line.replace("(double)", "").replace("(int)","");
        List<String> result = new ArrayList<String>();
        int bracketCount = 0;
        int startPoint = 0;
        for (int i=0;i<line.length();i++){
            char ch = line.charAt(i);
            if (ch == ',' && bracketCount <= level){
                if (startPoint != i ){
                    result.add(line.substring(startPoint,i));
                }
                startPoint = i+1;
            }
            else if (ch == '('){
                if (bracketCount != 0 && level > 1){
                    result.add(line.substring(startPoint,i));
                }
                if (++bracketCount <= level){
                    startPoint = i + 1;
                }
            }
            else if (ch == '[' ){
                if (++bracketCount <= level){
                    if (startPoint != i){
                        result.add(line.substring(startPoint,i));
                    }
                    startPoint = i + 1;
                }
            }
            else if (ch == ')' || ch == ']'){
                if (bracketCount-- <= level){
                    if (startPoint != i){
                        result.add(line.substring(startPoint,i));
                    }
                    startPoint = i + 1;
                }
            }
            else if (ch == '+' || ch == '-' || ch == '/' || ch == '*') {
                if (bracketCount < level) {
                    if (startPoint != i) {
                        result.add(line.substring(startPoint, i));
                    }
                    startPoint = i + 1;
                }
            }
            else if (ch == '.' && dividePoint){
                if (bracketCount < level){
                    if (startPoint != i && !StringUtils.isNumeric(line.substring(startPoint, i))){
                        result.add(line.substring(startPoint, i));
                        startPoint = i + 1;
                    }
                }
            }
            else if (custom.contains(ch) && bracketCount <= level){
                if (startPoint != i ){
                    result.add(line.substring(startPoint,i));
                }
                startPoint = i+1;
            }
        }
        List<String> returnList = new ArrayList<>();
        for (String param: result){
            returnList.add(param.trim());
        }
        return returnList;
    }


    public static List<String> divideParameter(String line, int level, boolean dividePoint ){
        return divideParameter(line, level, dividePoint, new ArrayList<Character>());
    }


    private static List<MethodDeclaration> getAllMethod(String code){
        return getAllMethod(code, false);
    }

    private static List<MethodDeclaration> getAllMethod(String code, boolean getInnerClassMethod){
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setSource(code.toCharArray());
        CompilationUnit unit = (CompilationUnit) parser.createAST(null);
        if (unit.types().size() == 0){
            return new ArrayList<>();
        }
        TypeDeclaration declaration = (TypeDeclaration) unit.types().get(0);
        List<MethodDeclaration> methodDeclarations = new ArrayList<>();
        methodDeclarations.addAll(Arrays.asList(declaration.getMethods()));
        if (getInnerClassMethod){
            for (TypeDeclaration typeDeclaration: declaration.getTypes()){
                methodDeclarations.addAll(Arrays.asList(typeDeclaration.getMethods()));
            }
        }
        return methodDeclarations;
    }

    private static List<FieldDeclaration> getAllField(String code){
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setSource(code.toCharArray());
        CompilationUnit unit = (CompilationUnit) parser.createAST(null);
        if (unit.types().size() == 0){
            return new ArrayList<>();
        }
        TypeDeclaration declaration = (TypeDeclaration) unit.types().get(0);
        FieldDeclaration varDec[] = declaration.getFields();
        return Arrays.asList(varDec);
    }

    public static Map<String, String> getAllFieldAndType(String code){
        Map<String, String> result = new HashMap<>();
        for (FieldDeclaration field: getAllField(code)){
            String type = field.getType().toString();
            String fieldDec = field.fragments().get(0).toString();
            if (fieldDec.contains("=")){
                String variableName = fieldDec.split("=")[0];
                result.put(variableName, type);
            }
        }
        return result;
    }

    public static List<String> getAllFieldName(String code){
        List<String> result = new ArrayList<>();
        for (FieldDeclaration field : getAllField(code)) {
            String fieldDec = field.fragments().get(0).toString();
            if (fieldDec.contains("=")){
                result.add(fieldDec.split("=")[0]);
            }
            else {
                result.add(fieldDec);
            }
        }
        return result;
    }

    public static boolean hasField(String code, String field){
        return getAllFieldName(code).contains(field);
    }

    public static boolean hasFieldWithType(String code, String field, String type){
        if (type.contains(".")){
            type = type.substring(type.lastIndexOf(".")+1);
        }
        for (Map.Entry<String, String> entry: getAllFieldAndType(code).entrySet()){
            if (entry.getKey().equals(field) && entry.getValue().equals(type)){
                return true;
            }
        }
        return false;
    }



    private static List<MethodDeclaration> getMethod(String code, String methodName) {
        methodName = methodName.trim();
        List<MethodDeclaration> result = new ArrayList<>();
        for (MethodDeclaration method : getAllMethod(code, true)) {
            if (method.getName().getIdentifier().equals(methodName)) {
                result.add(method);
            }
        }
        return result;
    }



    public static List<String> getAllMethodName(String code, boolean getInnerClassMethod){
        List<String> result = new ArrayList<>();
        for (MethodDeclaration method : getAllMethod(code, getInnerClassMethod)) {
            result.add(method.getName().getIdentifier());
        }
        return result;
    }

    public static boolean hasMethod(String code, String method){
        return getAllMethodName(code, false).contains(method);
    }

    public static String getMethodString(String code, String methodName){
        List<MethodDeclaration> declarations = getMethod(code, methodName);
        if (declarations.size() == 0){
            return "";
        }
        NaiveASTFlattener printer = new NaiveASTFlattener();
        declarations.get(0).accept(printer);
        String method =  printer.getResult();
        while (method.trim().startsWith("/**")){
            method = method.substring(method.indexOf("*/")+2);
        }
        return method.trim();
    }

    /**
     * Important Alert: insensitive with \n
     * @param code
     * @param methodName
     * @return
     */
    public static String getMethodBody(String code, String methodName){
        String methodString = getMethodString(code, methodName);
        if (!methodString.contains("{") || !methodString.contains("}")){
            return "";
        }
        methodString = methodString.substring(methodString.indexOf("{")+1, methodString.lastIndexOf("}"));
        return methodString.trim();
    }

    public static List<String> getMethodStrings(String code, String methodName){
        List<String> result = new ArrayList<>();
        List<MethodDeclaration> declarations = getMethod(code, methodName);
        for (MethodDeclaration method : declarations) {
            result.add(method.toString());
        }
        return result;
    }

    public static String getMethodString(String code, String methodName, int methodLine){
        methodName = methodName.trim();
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setSource(code.toCharArray());
        CompilationUnit unit = (CompilationUnit) parser.createAST(null);
        List<MethodDeclaration> declarations = getMethod(code, methodName);
        for (MethodDeclaration method : declarations) {
            int startLine = unit.getLineNumber(method.getStartPosition()) -1;
            int endLine = unit.getLineNumber(method.getStartPosition()+method.getLength()) -1;
            if (startLine <= methodLine && endLine >= methodLine){
                return method.toString();
            }
        }
        return "";
    }

    public static Javadoc getMethodAnnotation(String code, String methodName, int methodLine){
        methodName = methodName.trim();
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setSource(code.toCharArray());
        CompilationUnit unit = (CompilationUnit) parser.createAST(null);
        List<MethodDeclaration> declarations = getMethod(code, methodName);
        for (MethodDeclaration method : declarations) {
            int startLine = unit.getLineNumber(method.getStartPosition()) -1;
            int endLine = unit.getLineNumber(method.getStartPosition()+method.getLength()) -1;
            if (startLine <= methodLine && endLine >= methodLine){
                return method.getJavadoc();
            }
        }
        return null;
    }

    public static String getMethodBody(String code, String methodName, int methodStartLine){
        String methodString = getMethodString(code, methodName, methodStartLine);
        methodString = methodString.substring(methodString.indexOf("{")+1, methodString.lastIndexOf("}"));
        while (methodString.startsWith("\n")){
            methodString = methodString.substring(1);
        }
        while (methodString.endsWith("\n")){
            methodString = methodString.substring(0, methodString.length()-1);
        }
        return methodString;
    }

    public static String getMethodBodyBeforeLine(String code, String methodName, int line){
        methodName = methodName.trim();
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setSource(code.toCharArray());
        CompilationUnit unit = (CompilationUnit) parser.createAST(null);
        List<MethodDeclaration> declarations = getMethod(code, methodName);
        for (MethodDeclaration method : declarations) {
            String result = "";
            int startLine = unit.getLineNumber(method.getStartPosition());
            int endLine = unit.getLineNumber(method.getStartPosition()+method.getLength());
            if (startLine <= line && endLine >= line){
                List<Statement> statements = method.getBody().statements();
                for (Statement statement: statements){
                    if (unit.getLineNumber(statement.getStartPosition()) < line){
                        result += statement.toString();
                    }
                }
            }
            String returnString = "";
            if (!result.equals("")){
                String lineString = getLineFromCode(code, line).replace(" = ","=");
                while (LineUtils.isBoundaryLine(lineString)){
                    lineString = getLineFromCode(code,--line);
                }
                String lastString = getLineFromCode(code, line-1).replace(" = ","=");
                int lineBackup = line;
                while (lastString.startsWith("//")){
                    lastString = getLineFromCode(code, --lineBackup-1).replace(" = ","=");
                }
                lineBackup = line;
                String nextString = getLineFromCode(code, lineBackup+1).replace(" = ","=");
                while (nextString.startsWith("//")){
                    nextString = getLineFromCode(code, ++lineBackup+1).replace(" = ","=");
                }
                String[] resultString = result.split("\n");
                for (int i=0; i< resultString.length; i++){
                    String resultLine = resultString[i];
                    if (resultLine.replace(" ","").contains(lineString.replace(" ",""))){
                        if (resultString.length>i+1 && resultString[i+1].replace(" ","").contains(lastString.replace(" ","")) && i<resultString.length-1){
                            if (i<=1){
                                break;
                            }
                            if (resultString[i-1].replace(" ","").contains(nextString.replace(" ",""))){
                                break;
                            }
                        }
                        if (i>=1 && resultString[i-1].replace(" ","").contains(lastString.replace(" ","")) && i>0){
                            if (i==resultString.length-1){
                                break;
                            }
                            if (resultString[i-1].replace(" ","").contains(nextString.replace(" ",""))){
                                break;
                            }
                        }
                    }
                    returnString += resultLine+"\n";
                }

                return returnString;
            }
        }
        return "";
    }

    public static List<Integer> getReturnLine(String code, String methodName, int paramCount){
        List<Integer> result = new ArrayList<>();
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setSource(code.toCharArray());
        CompilationUnit unit = (CompilationUnit) parser.createAST(null);
        List<MethodDeclaration> methodDec = getMethod(code, methodName);
        for (MethodDeclaration method: methodDec){
            if (method.parameters().size() != paramCount){
                continue;
            }
            Block body =method.getBody();
            List<Statement> statements = body.statements();
            for (Statement statement: statements){
                if (statement.toString().contains("return;") || statement.toString().contains("throw new ")){
                    int startLine = unit.getLineNumber(statement.getStartPosition()) -1;
                    int lineOffset = 0;
                    for (String line: statement.toString().split("\n")){
                        lineOffset++;
                        if (line.contains("return;")|| line.contains("throw new ")){
                            result.add(startLine+lineOffset);
                        }
                    }
                }
            }

        }
        return result;
    }

    public static int getConstructorParamsCount(String functionName){
        return Integer.valueOf(functionName.substring(functionName.indexOf("(")+1,functionName.indexOf(")")));
    }

    public static int getLineNumOfLineString(String code, String lineString, int startLine){
        String[] codeLines = code.split("\n");
        for (int i= startLine; i< codeLines.length; i++){
            if (codeLines[i].replace(" ","").equals(lineString.replace(" ",""))){
                return i+1;
            }
        }
        return -1;
    }
    public static int getLineNumOfLineString(String code, String lineString) {
        return getLineNumOfLineString(code,lineString,0);
    }

    /**
     * 考虑重名函数的情况
     * @param code
     * @param methodName
     * @return
     */
    public static Map<List<String>, List<Integer>> getMethodLine(String code, String methodName){
        Map<List<String>, List<Integer>> result = new HashMap<>();
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setSource(code.toCharArray());
        CompilationUnit unit = (CompilationUnit) parser.createAST(null);
        List<MethodDeclaration> methodDec = getMethod(code, methodName);
        for (MethodDeclaration method: methodDec){
            if (method.getBody() == null){
                continue;
            }
            List<Statement> statements = method.getBody().statements();
            if (statements.size() == 0){
                continue;
            }
            Statement firstStatement = (Statement) method.getBody().statements().get(0);
            int startLine = unit.getLineNumber(firstStatement.getStartPosition()) -1;
            int endLine = unit.getLineNumber(firstStatement.getStartPosition()+method.getBody().getLength()) -2;
            int position = firstStatement.getStartPosition()+method.getBody().getLength();
            while (endLine == -3){
                endLine = unit.getLineNumber(--position)-2;
            }
            List<String> parameters = new ArrayList<>();
            List<SingleVariableDeclaration> vars = method.parameters();
            for (SingleVariableDeclaration var: vars){
                parameters.add(var.getName().getIdentifier());
            }
            result.put(parameters, Arrays.asList(startLine,endLine));
        }
        return result;
    }


    public static List<Integer> getSingleMethodLine(String code, String methodName, int errorLine){
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setSource(code.toCharArray());
        CompilationUnit unit = (CompilationUnit) parser.createAST(null);
        List<MethodDeclaration> methodDec = getMethod(code, methodName);
        for (MethodDeclaration method: methodDec){
            if (method.getBody() == null || method.getBody().statements().size() == 0){
                continue;
            }
            Statement firstStatement = (Statement) method.getBody().statements().get(0);
            int startLine = unit.getLineNumber(firstStatement.getStartPosition()) -1;
            int endLine = unit.getLineNumber(firstStatement.getStartPosition()+method.getBody().getLength()) -2;
            int position = firstStatement.getStartPosition()+method.getBody().getLength();
            while (endLine == -3){
                endLine = unit.getLineNumber(--position) -2;
            }
            if ((startLine <= errorLine && endLine >= errorLine)|| errorLine == -1){
                return Arrays.asList(startLine, endLine);
            }
        }
        return new ArrayList<>();
    }

    /**
     * 不考虑重名函数,有重名函数的话取第一个
     * @param code
     * @param methodName
     * @return
     */
    public static List<Integer> getSingleMethodLine(String code, String methodName){
        return getSingleMethodLine(code, methodName, -1);
    }

    public static boolean isConstructor(String classname, String function){
        if (function.contains("(")){
            function = function.substring(0, function.indexOf('('));
        }
        return  classname.substring(classname.lastIndexOf(".") + 1).equals(function);
    }

    public static void addImportToFile(File file, String addingImport){
        File newFile = new File(file.getAbsolutePath()+".temp");
        FileOutputStream outputStream = null;
        BufferedReader reader = null;
        try {
            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            outputStream = new FileOutputStream(newFile);
            reader = new BufferedReader(new FileReader(file));
            String lineString = null;
            while ((lineString = reader.readLine()) != null) {
                if (lineString.startsWith("package")){
                    lineString = lineString+addingImport;
                }
                outputStream.write((lineString + "\n").getBytes());
            }
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if (outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException e){
                }
            }
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e){
                }
            }
        }
        if (file.delete()){
            newFile.renameTo(file);
        }
    }

    public static void addMethodToFile(File file, String addingCode, String className){
        File newFile = new File(file.getAbsolutePath()+".temp");
        FileOutputStream outputStream = null;
        BufferedReader reader = null;
        try {
            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            outputStream = new FileOutputStream(newFile);
            reader = new BufferedReader(new FileReader(file));
            String lineString = null;
            while ((lineString = reader.readLine()) != null) {
                outputStream.write((lineString + "\n").getBytes());
                if (lineString.contains("class "+className)){
                    outputStream.write((addingCode+"\n").getBytes());
                }
            }
            outputStream.close();
            reader.close();
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if (outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException e){
                }
            }
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e){
                }
            }
        }
        if (file.delete()){
            newFile.renameTo(file);
        }
    }
    public static void addCodeToFile(File file, String addingCode, int targetLine) {
        addCodeToFile(file, addingCode, Arrays.asList(targetLine));
    }

    public static void addCodeToFile(File file, String addingCode, List<Integer> targetLine){
        File newFile = new File(file.getAbsolutePath()+".temp");
        Map<Integer, Boolean> writedMap = new HashMap<>();
        for (int line: targetLine){
            writedMap.put(line, false);
        }
        FileOutputStream outputStream = null;
        BufferedReader reader = null;
        try {
            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            outputStream = new FileOutputStream(newFile);
            reader = new BufferedReader(new FileReader(file));
            String lineString = null;
            int line = 0;
            while ((lineString = reader.readLine()) != null) {
                line++;
                if (targetLine.contains(line + 1)) {
                    if ((!lineString.contains(";") && !lineString.contains(":") && !lineString.contains("{") && !lineString.contains("}"))|| lineString.contains("return ") || lineString.contains("throw ")){
                        outputStream.write(addingCode.getBytes());
                        writedMap.put(line+1, true);

                    }
                }
                if (targetLine.contains(line) && !writedMap.get(line)) {
                    outputStream.write(addingCode.getBytes());
                    writedMap.put(line, true);
                }
                outputStream.write((lineString + "\n").getBytes());
            }
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if (outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException e){
                }
            }
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e){
                }
            }
        }
        if (file.delete()){
            newFile.renameTo(file);
        }
    }

    public static int isForLoopParam(List<String> vars){
        List<Integer> nums = new ArrayList<>();
        for (String var: vars){
            if (!StringUtils.isNumeric(var)){
                return -1;
            }
            try {
                int num = Integer.parseInt(var);
                nums.add(num);
            }catch (Exception e){
                return -1;
            }
        }
        if (nums.size() < 5){
            return -1;
        }
        Collections.sort(nums);
        int first = nums.get(0);
        int max = nums.get(0);
        for (int i=1; i< nums.size();i++){
            if (nums.get(i)> max){
                max = nums.get(i);
            }
            int second = nums.get(i);
            if (Math.abs(first-second) != 1){
                return -1;
            }
            first = second;
        }
        return max;
    }

    public static int countChar(String s,char c){
        int count= 0;
        for (int i=0; i< s.length(); i++){
            if (s.charAt(i)==c){
                count++;
            }
        }
        return count;
    }

    public static int getMethodStartLine(int lineInsideMethod, String code ,String methodName){
        for (int i= lineInsideMethod; i> 0; i--){
            String line = CodeUtils.getLineFromCode(code, i);
            if (line.contains(" "+methodName+"(")){
                return i;
            }
        }
        return -1;
    }

    public static String getMethodBodyFromMethod(String methodString){
        methodString = methodString.substring(methodString.indexOf("{")+1, methodString.lastIndexOf("}"));
        while (methodString.startsWith("\n")){
            methodString = methodString.substring(1);
        }
        while (methodString.endsWith("\n")){
            methodString = methodString.substring(0, methodString.length()-1);
        }
        while (methodString.split("\n")[0].trim().equals("") || methodString.split("\n")[0].trim().startsWith("//")){
            methodString = methodString.substring(methodString.indexOf("\n")+1);
        }
        return methodString;
    }

    public static List<String> splitCodeLines(String code){
        List<String> result = new ArrayList<>();
        int bracketCount = 0;
        String completeLine = "";
        for (String line: code.split("\n")){
            bracketCount += countChar(line, '(');
            bracketCount -= countChar(line, ')');
            completeLine += line;
            if (bracketCount == 0 && line.contains(";")){
                result.add(completeLine);
                completeLine = "";
            }
        }
        return result;
    }


    public static Set<String> getVariableInMethod(String methodSrc, Set<String> variables){
        ASTNode root = JDTUtils.createASTForSource(methodSrc, ASTParser.K_CLASS_BODY_DECLARATIONS);
        IdentifierCollectVisitor identifierCollectVisitor = new IdentifierCollectVisitor();
        root.accept(identifierCollectVisitor);
        List<String> list = identifierCollectVisitor.getIdentifierList();
        Iterator<String> it = variables.iterator();
        while(it.hasNext()){
            String var = it.next();
            if(!list.contains(var)){
                it.remove();
            }
        }
        return variables;
    }


    public static String spreadFor(String code){
        code = "public class Test { " + code + "}";
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        Map<?, ?> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
        parser.setCompilerOptions(options);
        parser.setSource(code.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        DummyVisitor dummyVisitor = new DummyVisitor();
        cu.accept(dummyVisitor);
        return dummyVisitor.spreadCode;
    }
}

class DummyVisitor extends ASTVisitor {

    public String spreadCode;

    public boolean visit(MethodDeclaration methodDeclaration) {

        Block mBody = methodDeclaration.getBody();

        List<Statement> statementList = mBody.statements();
        List<Statement> newStatements = new ArrayList<>();
        for (Statement statement : statementList) {
            if (statement instanceof ForStatement) {
                newStatements.addAll(decomposeForStatement((ForStatement) statement));
            } else {
                AST ast = AST.newAST(AST.JLS3);
                Statement s = (Statement) ASTNode.copySubtree(ast, statement);
                newStatements.add(s);
            }
        }

        mBody.statements().clear();
        AST originAST = mBody.getAST();
        for (Statement statement : newStatements) {
            Statement s = (Statement) ASTNode.copySubtree(originAST, statement);
            mBody.statements().add(s);
        }

        spreadCode = methodDeclaration.toString();

        return true;
    }

    private List<Statement> decomposeForStatement(ForStatement forStatement) {
        List<Statement> statementList = new ArrayList<>();

        Expression initializer = (Expression) forStatement.initializers().get(0);
        Expression condition = (Expression) forStatement.getExpression();
        Expression update = (Expression) forStatement.updaters().get(0);


        VariableDeclarationExpression vds = (VariableDeclarationExpression)initializer;
        VariableDeclarationFragment vdf = (VariableDeclarationFragment) vds.fragments().get(0);
        String varName = vdf.getName().toString();
        int init = Integer.parseInt(vdf.getInitializer().toString());
        int end = Integer.parseInt(((InfixExpression)condition).getRightOperand().toString());
        int step = 1;


        Block block = (Block) forStatement.getBody();
        List<Statement> sl = block.statements();
        int curvalue = init;
        for (int curValue = init; curvalue < end; curvalue += step) {
            for (Statement statement : sl) {
                statement = (Statement) ASTNode.copySubtree(statement.getAST(), statement);
                statement.accept(new ReplaceVisitor(varName, curvalue));
                statementList.add((Statement) ASTNode.copySubtree(AST.newAST(AST.JLS3), statement));
            }
        }
        return statementList;
    }
}

class ReplaceVisitor extends ASTVisitor {
    private String varName;
    private Expression updateExp;

    public ReplaceVisitor(String varName, int value) {
        this.varName = varName;
        updateExp = AST.newAST(AST.JLS3).newNumberLiteral(String.valueOf(value));
    }

    public boolean visit(InfixExpression exp){

        Expression lexp = exp.getLeftOperand();
        Expression rexp = exp.getRightOperand();

        if(lexp.toString().equals(varName)){
            exp.setLeftOperand((Expression) ASTNode.copySubtree(lexp.getAST(), updateExp));
        }
        if(rexp.toString().equals(varName)){
            exp.setRightOperand((Expression) ASTNode.copySubtree(rexp.getAST(), updateExp));
        }

        return true;
    }

    public boolean visit(MethodInvocation methodInvovation){

        List<Expression> arguments = methodInvovation.arguments();
        List<Expression> newArgs = new ArrayList<>();
        AST ast = AST.newAST(AST.JLS3);
        for(Expression exp : arguments){
            if(exp.toString().equals(varName)){
                newArgs.add((Expression) ASTNode.copySubtree(ast, updateExp));
            } else{
                newArgs.add(exp);
            }
        }

        methodInvovation.arguments().clear();
        for(Expression exp : newArgs){
            methodInvovation.arguments().add(ASTNode.copySubtree(methodInvovation.getAST(), exp));
        }

        return true;
    }
}