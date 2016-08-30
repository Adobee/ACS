package cn.edu.pku.sei.plde.conqueroverfitting.localizationInConstructor.model;

import java.io.Serializable;

/**
 * Created by yjxxtd on 2/29/16.
 */
public class ConstructorDeclarationInfo implements Comparable<ConstructorDeclarationInfo>, Serializable {
    public String methodName;
    public int parameterNum;
    public int startPos;
    public int endPos;

    public ConstructorDeclarationInfo(String methodName, int parameterNum, int startPos, int endPos) {
        this.methodName = methodName;
        this.parameterNum = parameterNum;
        this.startPos = startPos;
        this.endPos = endPos;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ConstructorDeclarationInfo))
            return false;
        ConstructorDeclarationInfo other = (ConstructorDeclarationInfo) obj;
        return methodName.equals(other.methodName) &&
                parameterNum == other.parameterNum &&
                startPos == other.startPos &&
                endPos == other.endPos;
    }

    public int compareTo(ConstructorDeclarationInfo constructorDeclarationInfo) {
        return constructorDeclarationInfo.parameterNum - this.parameterNum;
    }
}
