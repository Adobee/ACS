package cn.edu.pku.sei.plde.conqueroverfitting.boundary.model;

import java.util.Set;

import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.MethodInfo;

public class BoundaryInfo {
	public String fileName;
	public TypeEnum variableSimpleType;
	public boolean isSimpleType = false;
	public String otherType;
	public String name;
	public String value;
	public Set<String> info;
	public int leftClose;
	public int rightClose;

	public BoundaryInfo(TypeEnum variableSimpleType, boolean isSimpleType,
			String otherType, String name, String value, Set<String> info, int leftClose, int rightClose) {
		this.variableSimpleType = variableSimpleType;
		this.isSimpleType = isSimpleType;
		this.otherType = otherType;
		this.name = name;
		if(value.contains(".0")){
			value = value.replace(".0", "");
		}
		this.value = value;

		this.info = info;
		this.leftClose = leftClose;//[k
		this.rightClose = rightClose;//k]
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof BoundaryInfo))
			return false;
		BoundaryInfo other = (BoundaryInfo) obj;
		if (isSimpleType != other.isSimpleType)
			return false;
		if (isSimpleType) {
			return variableSimpleType.equals(other.variableSimpleType)
					&& name.equals(other.name) && value.equals(other.value);
		} else {
			return otherType.equals(other.otherType) && name.equals(other.name)
					&& value.equals(other.value);
		}
	}
	public String getStringType(){
		if (isSimpleType && variableSimpleType==null){
			return "";
		}
		if (isSimpleType && otherType == null){
			return "";
		}
		return isSimpleType?variableSimpleType.toString():otherType;
	}

}
