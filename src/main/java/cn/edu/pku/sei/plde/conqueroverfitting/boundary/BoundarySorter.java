package cn.edu.pku.sei.plde.conqueroverfitting.boundary;

import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.CodeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.InfoUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import org.apache.commons.lang.StringUtils;
import org.omg.CORBA.Object;

import java.util.*;

/**
 * Created by yanrunfa on 16/3/24.
 */
public class BoundarySorter {
    private final String _classSrc;
    private final String _className;
    private final String _code;
    private final String[] _methodCode;
    private Suspicious _suspicious;
    private Map<VariableInfo, String> _paramVarBoundary = new HashMap<>();
    private Map<VariableInfo, String> _fieldVarBoundary = new HashMap<>();
    private Map<VariableInfo, String> _localVarBoundary = new HashMap<>();
    private LinkedHashMap<VariableInfo, String> _sortedBoundary = new LinkedHashMap<>();
    private List<List<String>> _boundaryCombinations = new ArrayList<>();

    public BoundarySorter(Suspicious suspicious, String classSrc){
        _suspicious = suspicious;
        _classSrc = classSrc;
        _className = suspicious.classname();
        _code = FileUtils.getCodeFromFile(classSrc,_className);
        _methodCode = CodeUtils.getMethodString(_code, suspicious.functionnameWithoutParam(), suspicious.getDefaultErrorLine()).split("\n");
    }

    public List<String> sortList(Map<VariableInfo, List<String>> boundary){
        List<String> result = new ArrayList<>();
        for (Map.Entry<VariableInfo, List<String>> entry: boundary.entrySet()){
            Map<VariableInfo, String> map = new HashMap<>();
            for (String value: entry.getValue()){
                map.put(entry.getKey(), value);
                result.addAll(sort(map));
            }
        }
        return result;
    }

    public List<String> sort(Map<VariableInfo, String> boundary){
        if (boundary.size() == 1){
            return Arrays.asList("if("+boundary.values().toArray()[0]+")");
        }
        if (boundary.size() == 2){
            List<VariableInfo> infos = new ArrayList<>();
            for (VariableInfo info: boundary.keySet()){
                infos.add(info);
            }
            VariableInfo info1 = infos.get(0);
            VariableInfo info2 = infos.get(1);
            if (TypeUtils.isArrayFromName(info1.variableName) &&
                    TypeUtils.isArrayFromName(info2.variableName) &&
                    info1.getStringType().equals(info2.getStringType())){
                if (info1.isParameter && info2.isParameter){
                    return Arrays.asList(getIfStringFromBoundary(new ArrayList<>(boundary.values())));
                }
            }

        }
        for (Map.Entry<VariableInfo, String> entry: boundary.entrySet()){
            if (entry.getKey().isParameter){
                _paramVarBoundary.put(entry.getKey(), entry.getValue());
            }
            if (entry.getKey().isLocalVariable){
                _localVarBoundary.put(entry.getKey(), entry.getValue());
            }
            if (entry.getKey().isFieldVariable){
                _fieldVarBoundary.put(entry.getKey(), entry.getValue());
            }
        }
        sortBoundary();
        boundaryCombination();
        return getIfList();
    }


    private List<String> getIfList(){
        List<String> ifStrings = new ArrayList<>();
        for (List<String> boundarys: _boundaryCombinations){
            String ifString = getIfStringFromBoundary(boundarys);
            ifStrings.add(ifString);
        }
        return ifStrings;
    }

    public List<String> getIfStringFromBoundarys(Collection<List<String>> boundarys){
        List<String> result = new ArrayList<>();
        for (List<String> list: boundarys){
            for (String boundary: list){
                result.add(getIfStringFromBoundary(Arrays.asList(boundary)));
            }

        }
        return result;
    }

    public String getIfStringFromBoundary(List<String> boundarys){
        return "if (("+ StringUtils.join(boundarys, ")||(") +"))";
    }

    public String getIfStringFromBoundary(Collection<String> boundarys){
        return "if (("+ StringUtils.join(boundarys, ")||(") +"))";
    }

    private void boundaryCombination(){
        _boundaryCombinations = subsets(new ArrayList<String>(_sortedBoundary.values()));
        Collections.sort(_boundaryCombinations, new Comparator<List<String>>() {
            @Override
            public int compare(List<String> o1, List<String> o2) {
                return Integer.valueOf(o1.size()).compareTo(o2.size());
            }
        });
    }

    private List<List<String>> subsets(List<String> nums) {
        List<List<String>> res = new ArrayList<>();
        List<String> each = new ArrayList<>();
        helper(res, each, 0, nums);
        return res;
    }
    private void helper(List<List<String>> res, List<String> each, int pos, List<String> n) {
        if (pos <= n.size() && each.size()> 0) {
            res.add(each);
        }
        for (int i = pos; i < n.size(); i++) {
            each.add(n.get(i));
            helper(res, new ArrayList<>(each), i + 1, n);
            each.remove(each.size() - 1);
        }
        return;
    }

    private void sortBoundary(){
        for (Map.Entry<VariableInfo, String> entry: _paramVarBoundary.entrySet()){
            _sortedBoundary.put(entry.getKey(), entry.getValue());
        }
        TreeMap<Integer,Map.Entry<VariableInfo, String>> boundaryLevel = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
        for (Map.Entry<VariableInfo, String> entry: _localVarBoundary.entrySet()){
            int lastAssignLine = getLastAssignLine(entry.getKey());
            while (boundaryLevel.containsKey(lastAssignLine)){
                lastAssignLine++;
            }
            boundaryLevel.put(lastAssignLine,entry);
        }
        for (Map.Entry<VariableInfo, String> entry: boundaryLevel.values()){
            _sortedBoundary.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<VariableInfo, String> entry: _fieldVarBoundary.entrySet()){
            _sortedBoundary.put(entry.getKey(), entry.getValue());
        }
    }

    private int getLastAssignLine(VariableInfo info){
        for (int i = 0; i < _methodCode.length; i++){
            if (_methodCode[i].trim().matches(info.variableName+"\\s*=.*")){
                return i;
            }
        }
        return 0;
    }
}
