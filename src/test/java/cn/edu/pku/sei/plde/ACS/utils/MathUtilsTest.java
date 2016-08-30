package cn.edu.pku.sei.plde.ACS.utils;

import cn.edu.pku.sei.plde.ACS.boundary.BoundaryCollect;
import cn.edu.pku.sei.plde.ACS.boundary.model.BoundaryWithFreq;
import cn.edu.pku.sei.plde.ACS.boundary.model.Interval;
import cn.edu.pku.sei.plde.ACS.log.Log;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Created by yjxxtd on 4/23/16.
 */
public class MathUtilsTest {
    @Test
    public void testMergetDoubleInterval(){
        ArrayList<Interval> intervalList = new ArrayList<Interval>();
        List<Interval> intervalMergeList = new ArrayList<Interval>();

        intervalList.add(new Interval(1, 1, true, true));
        intervalList.add(new Interval(2, 2, true, true));
        intervalMergeList = MathUtils.mergetDoubleInterval(intervalList);
        assertTrue(intervalMergeList.size() == 2);
        assertTrue(intervalMergeList.contains(new Interval(1.0, 1.0, true, true)));
        assertTrue(intervalMergeList.contains(new Interval(2.0, 2.0, true, true)));

        intervalList.clear();
        intervalMergeList.clear();
        intervalList.add(new Interval(1, 2, true, false));
        intervalList.add(new Interval(2, 3, false, true));
        intervalMergeList = MathUtils.mergetDoubleInterval(intervalList);
        assertTrue(intervalMergeList.size() == 2);
        assertTrue(intervalMergeList.contains(new Interval(1.0, 2.0, true, false)));
        assertTrue(intervalMergeList.contains(new Interval(2.0, 3.0, false, true)));

        intervalList.clear();
        intervalMergeList.clear();
        intervalList.add(new Interval(17.0, 17.0, true, true));
        intervalList.add(new Interval(17.0, 18.0, true, true));
        intervalList.add(new Interval(18.0, 20.0, true, true));
        intervalMergeList = MathUtils.mergetDoubleInterval(intervalList);
        assertTrue(intervalMergeList.size() == 1);
        assertTrue(intervalMergeList.contains(new Interval(17.0, 20.0, true, true)));

        intervalList.clear();
        intervalMergeList.clear();
        intervalList.add(new Interval(17.0, 17.0, true, true));
        intervalList.add(new Interval(17.0, 18.0, true, true));
        intervalList.add(new Interval(18.0, 20.0, true, true));
        intervalMergeList = MathUtils.mergetDoubleInterval(intervalList);
        assertTrue(intervalMergeList.size() == 1);
        assertTrue(intervalMergeList.contains(new Interval(17.0, 20.0, true, true)));
    }

    @Test
    public void testMergetIntInterval(){
        ArrayList<Interval> intervalList = new ArrayList<Interval>();
        List<Interval> intervalMergeList = new ArrayList<Interval>();

        intervalList.add(new Interval(1, 1, true, true));
        intervalList.add(new Interval(2, 2, true, true));
        intervalMergeList = MathUtils.mergetIntInterval(intervalList);
        assertTrue(intervalMergeList.size() == 1);
        assertTrue(intervalMergeList.contains(new Interval(1.0, 2.0, true, true)));

        intervalList.clear();
        intervalMergeList.clear();
        intervalList.add(new Interval(1, 1, true, false));
        intervalList.add(new Interval(3, 3, false, true));
        intervalMergeList = MathUtils.mergetIntInterval(intervalList);
        assertTrue(intervalMergeList.size() == 2);
        assertTrue(intervalMergeList.contains(new Interval(1.0, 1.0, true, false)));
        assertTrue(intervalMergeList.contains(new Interval(3.0, 3.0, false, true)));

        intervalList.clear();
        intervalMergeList.clear();
        intervalList.add(new Interval(17.0, 17.0, true, true));
        intervalList.add(new Interval(18.0, 18.0, true, true));
        intervalList.add(new Interval(18.0, 20.0, true, true));
        intervalMergeList = MathUtils.mergetIntInterval(intervalList);
        assertTrue(intervalMergeList.size() == 1);
        assertTrue(intervalMergeList.contains(new Interval(17.0, 20.0, true, true)));
    }

    @Test
    public void testGenerateInterval() {
        ArrayList<BoundaryWithFreq> boundaryWithFreqs = new ArrayList<BoundaryWithFreq>();

//        String path = "experiment//searchcode//if-double-elitismRate";
//        BoundaryCollect boundaryCollect = new BoundaryCollect(path);
//        boundaryWithFreqs = boundaryCollect.getBoundaryWithFreqList();
//
//        Log log = new Log("log//if-double-elitismRate.log");
//        for(BoundaryWithFreq boundaryInfo : boundaryWithFreqs){
//            log.logSignLine("begin");
//            //log.logStr("name: " + boundaryInfo.name);
//            log.logStr("value: " + boundaryInfo.value);
//            log.logStr("type: " + boundaryInfo.variableSimpleType);
//            log.logSignLine("end");
//        }
//
//        ArrayList<BoundaryWithFreq> boundaryWithFreqArrayList = MathUtils.generateInterval(boundaryWithFreqs, -0.25);
//
//        System.out.println("17xx " + boundaryWithFreqArrayList.get(0).value + boundaryWithFreqArrayList.get(0).leftClose);
//        System.out.println("17xx " + boundaryWithFreqArrayList.get(1).value + boundaryWithFreqArrayList.get(1).rightClose);


//        String path = "experiment//searchcode//if-Complex";
//        BoundaryCollect boundaryCollect = new BoundaryCollect(path, true, "Complex");
//        boundaryWithFreqs = boundaryCollect.getBoundaryWithFreqList();
//
//        Log log = new Log("log//if-Complex.log");
//        for(BoundaryWithFreq boundaryInfo : boundaryWithFreqs){
//            log.logSignLine("begin");
//            //log.logStr("name: " + boundaryInfo.name);
//            log.logStr("value: " + boundaryInfo.value);
//            log.logStr("type: " + boundaryInfo.variableSimpleType);
//            log.logStr("freq " + boundaryInfo.freq);
//            log.logStr("isSimpleType " + boundaryInfo.isSimpleType);
//            log.logStr("otherType " + boundaryInfo.otherType);
//            log.logStr("variableSimpleType " + boundaryInfo.variableSimpleType);
//            log.logSignLine("end");
//        }
//
        String path = "experiment//searchcode//if-double-factorial";
        BoundaryCollect boundaryCollect = new BoundaryCollect(path, false , null);
        boundaryWithFreqs = boundaryCollect.getBoundaryWithFreqList();

        Log log = new Log("log//if-double-factorial.log");
        for(BoundaryWithFreq boundaryInfo : boundaryWithFreqs){
            log.logSignLine("begin");
            //log.logStr("name: " + boundaryInfo.name);
            log.logStr("value: " + boundaryInfo.value);
            log.logStr("type: " + boundaryInfo.variableSimpleType);
            log.logStr("freq " + boundaryInfo.freq);
            log.logStr("isSimpleType " + boundaryInfo.isSimpleType);
            log.logStr("otherType " + boundaryInfo.otherType);
            log.logStr("variableSimpleType " + boundaryInfo.variableSimpleType);
            log.logStr("leftClose " + boundaryInfo.leftClose);
            log.logStr("rightClose " + boundaryInfo.rightClose);
            log.logSignLine("end");
        }

//        String path = "experiment//searchcode//if-INT-months";
//        BoundaryCollect boundaryCollect = new BoundaryCollect(path);
//        boundaryWithFreqs = boundaryCollect.getBoundaryWithFreqList();
//
//        Log log = new Log("log//if-INT-months.log");
//        for(BoundaryWithFreq boundaryInfo : boundaryWithFreqs){
//            log.logSignLine("begin");
//            log.logStr("size: " + boundaryInfo.value.length());
//            log.logStr("value: " + boundaryInfo.value);
//            log.logStr("type: " + boundaryInfo.variableSimpleType);
//            log.logStr("freq " + boundaryInfo.freq);
//            log.logStr("isSimpleType " + boundaryInfo.isSimpleType);
//            log.logStr("otherType " + boundaryInfo.otherType);
//            log.logStr("variableSimpleType " + boundaryInfo.variableSimpleType);
//            log.logSignLine("end");
//        }

        ArrayList<BoundaryWithFreq> boundaryWithFreqArrayList = MathUtils.generateInterval(boundaryWithFreqs, 1,"double");
//
//        System.out.println("17xx " + boundaryWithFreqArrayList.get(0).value + boundaryWithFreqArrayList.get(0).leftClose);
//        System.out.println("17xx " + boundaryWithFreqArrayList.get(1).value + boundaryWithFreqArrayList.get(1).rightClose);


        boundaryWithFreqArrayList = MathUtils.generateInterval(boundaryWithFreqs, 18.0,"double");

        System.out.println("18xx " + boundaryWithFreqArrayList.get(0).value + boundaryWithFreqArrayList.get(0).leftClose);
        System.out.println("18xx " + boundaryWithFreqArrayList.get(1).value + boundaryWithFreqArrayList.get(1).rightClose);

//        boundaryWithFreqArrayList = MathUtils.generateInterval(boundaryWithFreqs, 19.0);

        //System.out.println("19xx " + boundaryWithFreqArrayList.get(0).value + boundaryWithFreqArrayList.get(0).leftClose);
        //System.out.println("19xx " + boundaryWithFreqArrayList.get(1).value + boundaryWithFreqArrayList.get(1).rightClose);

//        boundaryWithFreqs.add(new BoundaryWithFreq(null, false, "xx", "1.0", 1, 1, 1));
//        boundaryWithFreqs.add(new BoundaryWithFreq(TypeEnum.DOUBLE, true, null, "1.0", 1, 1, 1));
//        ArrayList<BoundaryWithFreq> boundaryWithFreqArrayList = MathUtils.generateInterval(boundaryWithFreqs, 1.0);
//        assertTrue(boundaryWithFreqArrayList.get(0).value.equals("1.0"));
//        assertTrue(boundaryWithFreqArrayList.get(1).value.equals("1.0"));
//
//        boundaryWithFreqs.add(new BoundaryWithFreq(null, false, "xx", "1.0", 1, 1, 1));
//        boundaryWithFreqs.add(new BoundaryWithFreq(TypeEnum.DOUBLE, true, null, "1.0", 1, 1, 1));
//        boundaryWithFreqs.add(new BoundaryWithFreq(TypeEnum.DOUBLE, true, null, "3.0", 1, 1, 1));
//        boundaryWithFreqArrayList = MathUtils.generateInterval(boundaryWithFreqs, 2.0);
//        assertTrue(boundaryWithFreqArrayList.get(0).value.equals("1.0"));
//        assertTrue(boundaryWithFreqArrayList.get(1).value.equals("3.0"));
//
//        boundaryWithFreqs.clear();
//        boundaryWithFreqs.add(new BoundaryWithFreq(null, false, "xx", "1.0", 1, 1, 1));
//        boundaryWithFreqs.add(new BoundaryWithFreq(TypeEnum.DOUBLE, true, null, "1.0", 2, 1, 1));
//        boundaryWithFreqs.add(new BoundaryWithFreq(TypeEnum.DOUBLE, true, null, "3.0", 4, 3, 1));
//        boundaryWithFreqArrayList = MathUtils.generateInterval(boundaryWithFreqs, 2.0);
//        assertTrue(boundaryWithFreqArrayList.get(0).value.equals("1.0"));
//        assertTrue(boundaryWithFreqArrayList.get(1).value.equals("3.0"));

    }
}
