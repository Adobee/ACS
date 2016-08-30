package cn.edu.pku.sei.plde.conqueroverfitting.boundary;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.MathUtils;

import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanrunfa on 16/2/22.
 */
public class BoundaryFilter {

    /**
     *
     * @param boundaryInfos The boundary info list
     * @param name the specific name
     * @return the boundary info with specific name.
     */
    public static List<BoundaryInfo> getBoundaryWithName(List<BoundaryInfo> boundaryInfos, String name){
        List<BoundaryInfo> result = new ArrayList<BoundaryInfo>();
        for (BoundaryInfo info: boundaryInfos){
            if (info.name.equals(name)){
                result.add(info);
            }
            else if (name.startsWith("is") && name.endsWith("()")){
                if (info.name.equals(name.substring(0, name.lastIndexOf("(")))){
                    result.add(info);
                }
            }
            else if (name.contains("[") && name.contains("]") && !name.contains("[]")){
                if (info.name.equals(name.substring(0, name.indexOf("[")))){
                    result.add(info);
                }
            }
        }
        return result;
    }


    public static List<BoundaryInfo> getBoundaryWithType(List<BoundaryInfo> boundaryInfos, String type){
        List<BoundaryInfo> result = new ArrayList<BoundaryInfo>();
        if (TypeUtils.isArray(type)){
            return result;
        }
        if (TypeUtils.isContainer(type)){
            return result;
        }
        for (BoundaryInfo info: boundaryInfos){
            if (info.getStringType().equals(type)){
                result.add(info);
            }
        }
        return result;
    }

    /**
     *
     * @param boundaryInfos
     * @param name
     * @param type
     * @return
     */
    public static List<BoundaryInfo> getBoundaryWithNameAndType(List<BoundaryInfo> boundaryInfos, String name, String type){
        List<BoundaryInfo> result = new ArrayList<BoundaryInfo>();
        for (BoundaryInfo info: boundaryInfos){
            if (info.isSimpleType && info.variableSimpleType==null){
                continue;
            }
            if (!info.isSimpleType && info.otherType == null){
                continue;
            }
            String infoType = info.getStringType();
            if (info.name.equals(name) && (infoType.equals(type) || (MathUtils.isNumberType(infoType) && MathUtils.isNumberType(type)))){
                result.add(info);
            }
        }
        return result;
    }


}
