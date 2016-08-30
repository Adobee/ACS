package cn.edu.pku.sei.plde.ACS.visible;

import cn.edu.pku.sei.plde.ACS.file.ReadFile;
import cn.edu.pku.sei.plde.ACS.jdtVisitor.MethodCollectVisitor;
import cn.edu.pku.sei.plde.ACS.utils.FileUtils;
import cn.edu.pku.sei.plde.ACS.utils.JDTUtils;
import cn.edu.pku.sei.plde.ACS.visible.model.MethodInfo;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class MethodCollect {
    private static MethodCollect instance;
    private static LinkedHashMap<String, ArrayList<MethodInfo>> methodsInClassMap;
    public String projectPath;

    public MethodCollect(String projectPath) {
        getAllVisibleMethod(projectPath);
    }

    public static MethodCollect GetInstance(String projectPath) {
        if (instance == null) {
            synchronized (MethodCollect.class) {
                instance = new MethodCollect(projectPath);
            }
        }
        else if (!projectPath.equals(instance.projectPath)) {
            synchronized (VariableCollect.class) {
                instance = new MethodCollect(projectPath);
            }
        }
        instance.projectPath = projectPath;
        return instance;
    }


    public static void getAllVisibleMethod(String projectPath) {
        methodsInClassMap = new LinkedHashMap<String, ArrayList<MethodInfo>>();
        ArrayList<String> filesPath = FileUtils.getJavaFilesInProj(projectPath);
        for (String filePath : filesPath) {
//            JDTParse jdtParse = new JDTParse(new ReadFile(filePath).getSource(), ASTParser.K_COMPILATION_UNIT);
//            methodsInClassMap.put(filePath, jdtParse.getMethodInClassList());
            ASTNode root = JDTUtils.createASTForSource(new ReadFile(filePath).getSource(), ASTParser.K_COMPILATION_UNIT);
            MethodCollectVisitor methodCollectVisitor = new MethodCollectVisitor();
            root.accept(methodCollectVisitor);
            methodsInClassMap.put(filePath, methodCollectVisitor.getMethodsInClassList());

        }
    }

    public static boolean checkIsStaticMethod(String sourcePath, String methodName) {
        if (methodsInClassMap == null){
            MethodCollect methodCollect = MethodCollect.GetInstance(sourcePath);
            methodCollect.getVisibleMethodWithoutParametersInAllClassMap(sourcePath);
        }
        if (!methodsInClassMap.containsKey(sourcePath)) {
            return false;
        }
        ArrayList<MethodInfo> methodInfos = methodsInClassMap.get(sourcePath);
        for (MethodInfo methodInfo : methodInfos) {
            if (methodInfo.methodName.equals(methodName)) {
                return methodInfo.isStatic;
            }
        }
        return false;
    }

    public LinkedHashMap<String, ArrayList<MethodInfo>> getVisibleMethodWithoutParametersInAllClassMap(
            String sourcePath) {
        LinkedHashMap<String, ArrayList<MethodInfo>> methodsInClassMapRet = deepClone(methodsInClassMap);
        for (Map.Entry<String, ArrayList<MethodInfo>> entry : methodsInClassMapRet
                .entrySet()) {
            String filePath = entry.getKey();
            ArrayList<MethodInfo> methodsInClassList = entry.getValue();
            for (Iterator<MethodInfo> it = methodsInClassList.iterator(); it
                    .hasNext(); ) {
                MethodInfo methodInfo = it.next();
                if (methodInfo.hasParameter || (methodInfo.otherType != null && methodInfo.otherType.equals("void"))) {
                    it.remove();
                } else {
                    if (!filePath.equals(sourcePath)) {
                        if (!methodInfo.isPublic) {
                            it.remove();
                        }
                    }
                }
            }
        }
        return methodsInClassMapRet;
    }

    public LinkedHashMap<String, ArrayList<MethodInfo>> deepClone(LinkedHashMap<String, ArrayList<MethodInfo>> map) {
        LinkedHashMap<String, ArrayList<MethodInfo>> mapRet = new LinkedHashMap<String, ArrayList<MethodInfo>>();
        for (Map.Entry<String, ArrayList<MethodInfo>> entry : map
                .entrySet()) {
            mapRet.put(entry.getKey(), new ArrayList<MethodInfo>(entry.getValue()));

        }
        return mapRet;
    }
}
