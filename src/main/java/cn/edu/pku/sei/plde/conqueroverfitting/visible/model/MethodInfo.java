package cn.edu.pku.sei.plde.conqueroverfitting.visible.model;

import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;

import java.io.Serializable;

public class MethodInfo implements Serializable {
	public String methodName;
	public TypeEnum variableSimpleType;
	public boolean isSimpleType;
	public String otherType;
	public boolean isPublic;
	public boolean isStatic;
	public boolean hasParameter;

	public MethodInfo(String methodName, TypeEnum variableSimpleType,
			boolean isSimpleType, String otherType, boolean isPublic, boolean isStatic, boolean hasParameter) {
		this.methodName = methodName;
		this.variableSimpleType = variableSimpleType;
		this.isSimpleType = isSimpleType;
		this.otherType = otherType;
		this.isPublic = isPublic;
		this.isStatic = isStatic;
		this.hasParameter = hasParameter;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MethodInfo))
			return false;
		MethodInfo other = (MethodInfo) obj;
		if (isSimpleType != other.isSimpleType)
			return false;
		if (isSimpleType) {
			return methodName.equals(other.methodName)
					&& variableSimpleType.equals(other.variableSimpleType)
					&& isPublic == other.isPublic
					&& isStatic == other.isStatic
					&& hasParameter == other.hasParameter;
		} else {
			return methodName.equals(other.methodName)
					&& otherType.equals(other.otherType)
					&& isPublic == other.isPublic
					&& isStatic == other.isStatic
					&& hasParameter == hasParameter;
		}
	}

	public String getStringType(){
		return isSimpleType?variableSimpleType.toString():otherType;
	}

}
