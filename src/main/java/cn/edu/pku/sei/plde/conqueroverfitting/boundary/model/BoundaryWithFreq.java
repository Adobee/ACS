package cn.edu.pku.sei.plde.conqueroverfitting.boundary.model;

import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;

import java.util.Set;

/**
 * Created by yjxxtd on 4/23/16.
 */
public class BoundaryWithFreq {
    public TypeEnum variableSimpleType;
    public boolean isSimpleType = false;
    public String otherType;
    public String value;
    public double dvalue;
    public int leftClose;
    public int rightClose;
    public int freq;

    public static BoundaryWithFreq copyOf(BoundaryWithFreq boundaryWithFreq){
        BoundaryWithFreq newBoundary = new BoundaryWithFreq(
                boundaryWithFreq.variableSimpleType,
                boundaryWithFreq.isSimpleType,
                boundaryWithFreq.otherType,
                boundaryWithFreq.value,
                boundaryWithFreq.leftClose,
                boundaryWithFreq.rightClose,
                boundaryWithFreq.freq);
        newBoundary.dvalue = boundaryWithFreq.dvalue;
        return newBoundary;
    }

    public BoundaryWithFreq(TypeEnum variableSimpleType, boolean isSimpleType,
                        String otherType, String value, int leftClose, int rightClose, int freq) {
        this.variableSimpleType = variableSimpleType;
        this.isSimpleType = isSimpleType;
        this.otherType = otherType;
        this.value = value;
        try{

            if(isSimpleType) {
                if(!value.equals("null")) {
                    int valueSize = value.length();
                    if (value.endsWith("L") || value.endsWith("l") || value.endsWith("D") || value.endsWith("d") || value.endsWith("F") || value.endsWith("f")) {
                        this.value = value.substring(0, valueSize - 1);
                    }
                    dvalue = Double.parseDouble(value);
                }
            }
        }catch (Exception e){
            if(value.equals("Integer.MIN_VALUE") || value.equals("Integer.MIN_VAUE")){
                this.value = "Integer.MIN_VALUE";
                dvalue = Integer.MIN_VALUE;
            }
            if(value.equals("Integer.MAX_VALUE") || value.equals("Integer.MAX_VAUE")){
                this.value = "Integer.MAX_VALUE";
                dvalue = Integer.MAX_VALUE;
            }
            if(value.equals("Double.MIN_VALUE") || value.equals("Double.MIN_VAUE")){
                this.value = "Double.MIN_VALUE";
                dvalue = Double.MIN_VALUE;
            }
            if(value.equals("Double.MAX_VALUE") || value.equals("Double.MAX_VAUE")){
                this.value = "Double.MAX_VALUE";
                dvalue = Double.MAX_VALUE;
            }
            if(value.equals("Long.MIN_VALUE") || value.equals("Long.MIN_VAUE")){
                this.value = "Long.MIN_VALUE";
                dvalue = Long.MIN_VALUE;
            }
            if(value.equals("Long.MAX_VALUE") || value.equals("Long.MAX_VAUE")){
                this.value = "Long.MAX_VALUE";
                dvalue = Long.MAX_VALUE;
            }

        }
        this.leftClose = leftClose;//[k
        this.rightClose = rightClose;//k]
        this.freq = freq;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BoundaryWithFreq))
            return false;
        BoundaryWithFreq other = (BoundaryWithFreq) obj;
        if (isSimpleType != other.isSimpleType)
            return false;
        if (isSimpleType) {
            return variableSimpleType.equals(other.variableSimpleType)
                    && value.equals(other.value);
        } else {
            return otherType.equals(other.otherType)
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
