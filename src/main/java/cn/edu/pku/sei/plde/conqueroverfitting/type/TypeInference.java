package cn.edu.pku.sei.plde.conqueroverfitting.type;

import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;

public class TypeInference {
	public TypeEnum type;
	public String otherType;
	public boolean isSimpleType = true;

	public TypeInference(String typeStr) {
		if (typeStr.equals("byte")) {
			type = TypeEnum.BYTE;
		} else if (typeStr.equals("short")) {
			type = TypeEnum.SHORT;
		} else if (typeStr.equals("int")) {
			type = TypeEnum.INT;
		} else if (typeStr.equals("long")) {
			type = TypeEnum.LONG;
		} else if (typeStr.equals("float")) {
			type = TypeEnum.FLOAT;
		} else if (typeStr.equals("double")) {
			type = TypeEnum.DOUBLE;
		} else if (typeStr.equals("char")) {
			type = TypeEnum.CHARACTER;
		} else if (typeStr.equals("boolean")) {
			type = TypeEnum.BOOLEAN;
		} else if (typeStr.equals("Integer")) {
			type = TypeEnum.INT;
		} else if (typeStr.equals("String")) {
			type = TypeEnum.STRING;
		} else {
			otherType = typeStr;
			isSimpleType = false;
		}
	}
}