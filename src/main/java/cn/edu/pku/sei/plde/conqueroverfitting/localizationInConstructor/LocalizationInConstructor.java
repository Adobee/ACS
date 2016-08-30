package cn.edu.pku.sei.plde.conqueroverfitting.localizationInConstructor;

import cn.edu.pku.sei.plde.conqueroverfitting.file.ReadFile;
import cn.edu.pku.sei.plde.conqueroverfitting.jdtVisitor.ConstructorDeclarationCollectVisitor;
import cn.edu.pku.sei.plde.conqueroverfitting.jdtVisitor.ConstructorInvocationInTestCollectVisitor;
import cn.edu.pku.sei.plde.conqueroverfitting.jdtVisitor.MethodCollectVisitor;
import cn.edu.pku.sei.plde.conqueroverfitting.localizationInConstructor.model.ConstructorDeclarationInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.JDTUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

import java.io.File;
import java.util.*;

/**
 * Created by yjxxtd on 2/29/16.
 */
public class LocalizationInConstructor {
    private String projectPath;
    private String testFilePath;
    private String testMethodName;

    private HashMap<String, ArrayList<ConstructorDeclarationInfo>> constructorMap;

    public LocalizationInConstructor(String projectPath, String testFilePath, String testMethodName) {
        this.projectPath = projectPath;
        this.testFilePath = testFilePath;
        this.testMethodName = testMethodName;

        constructorMap = new HashMap<String, ArrayList<ConstructorDeclarationInfo>>();

        locateConstructor();
    }

    public HashMap<String, ArrayList<ConstructorDeclarationInfo>> getConstructorMap(){
        return constructorMap;
    }

    private void locateConstructor(){
        ArrayList<String> constructorInvocationList = getConstructorInvocationListInTest();
        for(String constructorInvocation : constructorInvocationList){
            String classPath = getClassPath(constructorInvocation);
            if(classPath == null){
                continue;
            }
            constructorMap.put(classPath, getConstructorDeclarationList(classPath));
        }
    }

    private ArrayList<String> getConstructorInvocationListInTest(){
        ASTNode root = JDTUtils.createASTForSource(new ReadFile(testFilePath).getSource(), ASTParser.K_COMPILATION_UNIT);
        ConstructorInvocationInTestCollectVisitor constructorInvocationInTestCollectVisitor = new ConstructorInvocationInTestCollectVisitor(testMethodName);
        root.accept(constructorInvocationInTestCollectVisitor);
        ArrayList<String> constructorInvocationList = constructorInvocationInTestCollectVisitor.getConstructorInvocationList();
        return constructorInvocationList;
    }

    private ArrayList<ConstructorDeclarationInfo> getConstructorDeclarationList(String classPath){
        String source = new ReadFile(classPath).getSource();
        ASTNode root = JDTUtils.createASTForSource(source, ASTParser.K_COMPILATION_UNIT);
        int[] lineCounter = JDTUtils.getLineCounter(source);
        ConstructorDeclarationCollectVisitor constructorDeclarationCollectVisitor = new ConstructorDeclarationCollectVisitor(lineCounter);
        root.accept(constructorDeclarationCollectVisitor);
        ArrayList<ConstructorDeclarationInfo> constructorDeclarationList = constructorDeclarationCollectVisitor.getConstructorDeclarationList();
        Collections.sort(constructorDeclarationList);
        return constructorDeclarationList;
    }

    private String getClassPath(String className){
        File file = new File(this.projectPath);
        LinkedList<File> fileList = new LinkedList<File>();
        fileList.add(file);
        while (!fileList.isEmpty()){
            File firstFile = fileList.removeFirst();
            File [] subFiles = firstFile.listFiles();
            for (File subFile : subFiles){
                String name = subFile.getName();
                if (subFile.isDirectory()) {
                    fileList.add(subFile);
                }
                else if (subFile.getName().equals(className + ".java")){
                    return subFile.getAbsolutePath();
                }
            }
        }
        return null;
    }
}
