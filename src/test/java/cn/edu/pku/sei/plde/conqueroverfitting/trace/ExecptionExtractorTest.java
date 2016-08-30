package cn.edu.pku.sei.plde.conqueroverfitting.trace;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanrunfa on 16/2/21.
 */
public class ExecptionExtractorTest {
    @Test
    public void testExtractWithAbandonTrueValue(){
        List<TraceResult> traceResults = new ArrayList<TraceResult>();
        TraceResult traceResultOne = new TraceResult(true);
        traceResultOne.put("A","1");
        traceResultOne.put("A","2");
        traceResultOne.put("A","3");
        traceResultOne.put("B","1");
        traceResultOne.put("B","2");
        traceResultOne.put("B","3");
        TraceResult traceResultTwo = new TraceResult(false);
        traceResultTwo.put("A","1");
        traceResultTwo.put("A","2");
        traceResultTwo.put("A","4");
        traceResultTwo.put("B","1");
        traceResultTwo.put("B","2");
        traceResultTwo.put("B","5");
        TraceResult traceResultThree = new TraceResult(true);
        traceResultThree.put("A","1");
        traceResultThree.put("A","2");
        traceResultThree.put("A","6");
        traceResultThree.put("B","1");
        traceResultThree.put("B","2");
        traceResultThree.put("B","8");
        TraceResult traceResultFour = new TraceResult(false);
        traceResultFour.put("A","1");
        traceResultFour.put("A","2");
        traceResultFour.put("A","6");
        traceResultFour.put("B","1");
        traceResultFour.put("B","2");
        traceResultFour.put("B","9");
        traceResults.add(traceResultOne);
        traceResults.add(traceResultTwo);
        traceResults.add(traceResultFour);
        traceResults.add(traceResultThree);
        //Map<String, List<String>> result = ExceptionExtractor.extractWithAbandonTrueValue(traceResults);
        //for (Map.Entry<String, List<String>> entry: result.entrySet()){
        //    System.out.print(" Key = " + entry.getKey() + ", Value = " + entry.getValue().toString());
        //}
    }
}
