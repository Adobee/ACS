package cn.edu.pku.sei.plde.conqueroverfitting.type;

public enum TypeEnum {
	BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, CHARACTER, BOOLEAN, STRING, NULL;

	@Override
	public String toString() {
		return super.toString();
	}

	public static TypeEnum getType(String type){
		for (TypeEnum typeEnum: TypeEnum.values()){
			if (typeEnum.toString().equalsIgnoreCase(type)){
				return typeEnum;
			}
		}
		return null;
	}
}
