package cn.edu.pku.sei.plde.conqueroverfitting.visible;

import java.io.File;
import java.util.*;

import cn.edu.pku.sei.plde.conqueroverfitting.jdtVisitor.VariableCollectVisitor;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.JDTUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

import cn.edu.pku.sei.plde.conqueroverfitting.file.ReadFile;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;

public class VariableCollect {
	private static VariableCollect instance;

	public String projectPath;
	private static LinkedHashMap<String, ArrayList<VariableInfo>> filedsInClassMap;
	private static LinkedHashMap<String, ArrayList<VariableInfo>> parameterInMethodMap;
	private static LinkedHashMap<String, ArrayList<VariableInfo>> localInMethodMap;
  
	public VariableCollect(String projectPath) {
		getAllVisibleVariable(projectPath);
	}

	public static VariableCollect GetInstance(String projectPath) {
		if (instance == null ) {
			synchronized (VariableCollect.class) {
				instance = new VariableCollect(projectPath);
			}
		}
		else if (!projectPath.equals(instance.projectPath)){
			synchronized (VariableCollect.class) {
				instance = new VariableCollect(projectPath);
			}
		}
		instance.projectPath = projectPath;
		return instance;
	}

	public static void getAllVisibleVariable(String projectPath) {
		filedsInClassMap = new LinkedHashMap<String, ArrayList<VariableInfo>>();
		parameterInMethodMap = new LinkedHashMap<String, ArrayList<VariableInfo>>();
		localInMethodMap = new LinkedHashMap<String, ArrayList<VariableInfo>>();
		ArrayList<String> filesPath = FileUtils.getJavaFilesInProj(projectPath);
		for (String filePath : filesPath) {
			String source = new ReadFile(filePath).getSource();
			ASTNode root = JDTUtils.createASTForSource(source, ASTParser.K_COMPILATION_UNIT);
			int[] lineCounter = JDTUtils.getLineCounter(source);
			VariableCollectVisitor variableCollectVisitor = new VariableCollectVisitor(lineCounter, new File(filePath).getName().replace(".java", ""));
			root.accept(variableCollectVisitor);

			ArrayList<VariableInfo> filedInClassList = variableCollectVisitor.getFiledsInClassList();
			ArrayList<VariableInfo> parametersInMethodList = variableCollectVisitor.getParametersInMethodList();
			ArrayList<VariableInfo> localsInMethodList = variableCollectVisitor.getLocalInMethodList();
			Collections.sort(parametersInMethodList);
			Collections.sort(localsInMethodList);

//			JDTParse jdtParse = new JDTParse(new ReadFile(filePath).getSource(), ASTParser.K_COMPILATION_UNIT);
			filedsInClassMap.put(filePath, filedInClassList);
			parameterInMethodMap.put(filePath, parametersInMethodList);
			localInMethodMap.put(filePath, localsInMethodList);
		}
	}

	public LinkedHashMap<String, ArrayList<VariableInfo>> getVisibleFieldInAllClassMap(
			String sourcePath) {
		LinkedHashMap<String, ArrayList<VariableInfo>> fieldsInClassMapRet = new LinkedHashMap<String, ArrayList<VariableInfo>>(filedsInClassMap);
		for (Map.Entry<String, ArrayList<VariableInfo>> entry : fieldsInClassMapRet
				.entrySet()) {
			String filePath = entry.getKey();
			if(filePath.equals(sourcePath)){
				continue;
			}
			ArrayList<VariableInfo> fieldsInClassList = entry.getValue();
			for (Iterator<VariableInfo> it = fieldsInClassList.iterator(); it
					.hasNext();) {
				VariableInfo variableInfo = it.next();
				//if (variableInfo.isPublic && variableInfo.isFinal) {
				//	it.remove();
				//}
			}
		}
		return fieldsInClassMapRet;
	}

	public ArrayList<VariableInfo> getVisibleParametersInMethodList(String sourcePath, int suspiciousLineNum) {
		ArrayList<VariableInfo> parameters = new ArrayList<VariableInfo>();
		if (parameterInMethodMap == null || !parameterInMethodMap.containsKey(sourcePath))
			return parameters;

		ArrayList<VariableInfo> parametersInMethods = parameterInMethodMap
				.get(sourcePath);
		for (VariableInfo variableInfo : parametersInMethods) {
			if (variableInfo.methodEndPos < suspiciousLineNum)
				continue;
			if (variableInfo.methodStartPos < suspiciousLineNum
					&& variableInfo.methodEndPos >= suspiciousLineNum) {
				parameters.add(variableInfo);
			}
			if (variableInfo.methodStartPos > suspiciousLineNum)
				break;
		}

		return parameters;
	}

	public ArrayList<VariableInfo> getVisibleLocalInMethodList(
			String sourcePath, int suspiciousLineNum) {
		ArrayList<VariableInfo> locals = new ArrayList<VariableInfo>();

		if (localInMethodMap == null
				|| !localInMethodMap.containsKey(sourcePath))
			return locals;

		ArrayList<VariableInfo> localsInMethods = localInMethodMap
				.get(sourcePath);
		for (VariableInfo variableInfo : localsInMethods) {
			if (variableInfo.methodEndPos < suspiciousLineNum)
				continue;
			if (variableInfo.methodStartPos < suspiciousLineNum
					&& variableInfo.methodEndPos > suspiciousLineNum
					&& variableInfo.variablePos < suspiciousLineNum) {
				locals.add(variableInfo);
			}
			if (variableInfo.methodStartPos > suspiciousLineNum)
				break;
		}

		return locals;
	}
}
