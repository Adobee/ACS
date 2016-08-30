package cn.edu.pku.sei.plde.conqueroverfitting.visible.model;

import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;

import java.io.Serializable;

public class VariableInfo implements Comparable<VariableInfo>,Serializable{
	public String variableName;
	public TypeEnum variableSimpleType;
	public boolean isSimpleType;
	public String otherType;
    public boolean isPublic;
	public boolean isStatic;
    public int methodStartPos;
    public int methodEndPos;
    public int variablePos;
	public boolean interval = false;
	public boolean isParameter = false;
	public boolean isLocalVariable = false;
	public boolean isFieldVariable = false;
	public boolean isFinal = false;
	public boolean isAddon = false;
	public int priority = 1;
	public boolean isExpression = false;
	public String expressMethod = "";

	/**
	 *
	 * @param variableName
	 * @param variableSimpleType
	 * @param isSimpleType
	 * @param otherType
	 * @param methodStartPos
	 * @param methodEndPos
     * @param variablePos
     */
	public VariableInfo(String variableName,
			TypeEnum variableSimpleType, boolean isSimpleType,
			String otherType, int methodStartPos, int methodEndPos, int variablePos) {
		this.variableName = variableName;
		this.variableSimpleType = variableSimpleType;
		this.isSimpleType = isSimpleType;
		this.otherType = otherType;
		this.methodStartPos = methodStartPos;
		this.methodEndPos = methodEndPos;
		this.variablePos = variablePos;
	}

	public static VariableInfo copy(VariableInfo info){
		VariableInfo newInfo =  new VariableInfo(info.variableName, info.variableSimpleType, info.isSimpleType, info.otherType, info.methodStartPos, info.methodEndPos, info.variablePos);
		newInfo.isStatic = info.isStatic;
		newInfo.isPublic = info.isPublic;
		newInfo.isAddon = info.isAddon;
		newInfo.isExpression = info.isExpression;
		return newInfo;
	}

	/**
	 *
	 * @param variableName
	 * @param variableSimpleType
	 * @param isSimpleType
	 * @param otherType
	 * @param methodStartPos
     * @param methodEndPos
     */
	public VariableInfo(String variableName,
			TypeEnum variableSimpleType, boolean isSimpleType,
			String otherType, int methodStartPos, int methodEndPos) {
		this.variableName = variableName;
		this.variableSimpleType = variableSimpleType;
		this.isSimpleType = isSimpleType;
		this.otherType = otherType;
		this.methodStartPos = methodStartPos;
		this.methodEndPos = methodEndPos;
	}

	/**
	 *
	 * @param variableName
	 * @param variableSimpleType
	 * @param isSimpleType
	 * @param otherType
     * @param isPublic
     */
	public VariableInfo(String variableName,
			TypeEnum variableSimpleType, boolean isSimpleType,
			String otherType, boolean isPublic, boolean isStatic) {
		this.variableName = variableName;
		this.variableSimpleType = variableSimpleType;
		this.isSimpleType = isSimpleType;
		this.otherType = otherType;
		this.isPublic = isPublic;
		this.isStatic = isStatic;
	}
	public VariableInfo(String variableName,
						TypeEnum variableSimpleType, boolean isSimpleType,
						String otherType, boolean isPublic, boolean isStatic, boolean isFinal) {
		this.variableName = variableName;
		this.variableSimpleType = variableSimpleType;
		this.isSimpleType = isSimpleType;
		this.otherType = otherType;
		this.isPublic = isPublic;
		this.isStatic = isStatic;
		this.isFinal = isFinal;
	}
	/**
	 *
	 * @param variableName
	 * @param variableSimpleType
	 * @param isSimpleType
	 * @param otherType
     */
	public VariableInfo(String variableName,
			TypeEnum variableSimpleType, boolean isSimpleType,
			String otherType) {
		this.variableName = variableName;
		this.variableSimpleType = variableSimpleType;
		this.isSimpleType = isSimpleType;
		this.otherType = otherType;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof VariableInfo))
			return false;
		VariableInfo other = (VariableInfo) obj;
		if (isSimpleType != other.isSimpleType)
			return false;
		if (isSimpleType) {
			return variableName.equals(other.variableName)
					&& variableSimpleType.equals(other.variableSimpleType)
					&& isPublic == other.isPublic
					&& isStatic == other.isStatic;
		} else {
			return variableName.equals(other.variableName)
					&& otherType.equals(other.otherType)
					&& isPublic == other.isPublic
					&& isStatic == other.isStatic;
		}
	}

	@Override
	public int hashCode(){
		int result = 0;
		String code = variableName+getStringType();
		for (Character ch: code.toCharArray()){
			result += ch;
		}
		return result;
	}

	public int compareTo(VariableInfo variableInfo) {
		return methodStartPos - variableInfo.methodStartPos;
	}

	public String getStringType(){
		return isSimpleType?variableSimpleType.toString():otherType;
	}


}
