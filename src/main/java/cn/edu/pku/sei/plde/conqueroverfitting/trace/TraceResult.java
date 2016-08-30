package cn.edu.pku.sei.plde.conqueroverfitting.trace;

import com.gzoltar.core.instr.testing.TestResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yanrunfa on 16/2/21.
 */
public class TraceResult implements Serializable {
    private Map<String, List<String>> result = new HashMap<String, List<String>>();
    private final boolean _testResult;
    public int _assertLine;
    public int _traceLine;
    public String _testClass;
    public String _testMethod;
    /**
     *
     * @param testResult if the test is success or fail
     */
    public TraceResult(boolean testResult){
        _testResult = testResult;
    }

    public boolean getTestResult(){
        return _testResult;
    }

    public Map<String, List<String>> getResultMap(){
        return result;
    }

    /**
     *
     * @param key the key of map
     * @param value the value of map
     */
    public void put(String key, String value){
        List<String> values = result.get(key);
        if (values == null){
            values = new ArrayList<String>();
        }
        if (!values.contains(value)){
            values.add(value);
            result.put(key, values);
        }
    }

    public static TraceResult copy(TraceResult traceResult){
        TraceResult newTraceResult = new TraceResult(traceResult._testResult);
        for (Map.Entry<String, List<String>> entry: traceResult.result.entrySet()){
            for (String value: entry.getValue()){
                newTraceResult.put(entry.getKey(), value);
            }
        }
        newTraceResult._assertLine = traceResult._assertLine;
        newTraceResult._testClass = traceResult._testClass;
        newTraceResult._testMethod = traceResult._testMethod;
        return newTraceResult;
    }


    public static List<TraceResult> copy(List<TraceResult> traceResults){
        List<TraceResult> result = new ArrayList<>();
        for (TraceResult traceResult: traceResults){
            result.add(copy(traceResult));
        }
        return result;
    }
    /**
     *
     * @param key get value from key
     * @return the value
     */
    public List<String> get(String key){
        if (result.containsKey(key)){
            return result.get(key);
        }
        return null;
    }
}
