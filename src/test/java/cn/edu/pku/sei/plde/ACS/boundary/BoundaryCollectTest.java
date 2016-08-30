package cn.edu.pku.sei.plde.ACS.boundary;

import cn.edu.pku.sei.plde.ACS.boundary.model.Interval;
import cn.edu.pku.sei.plde.ACS.trace.filter.SearchBoundaryFilter;
import cn.edu.pku.sei.plde.ACS.type.TypeUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;


import static org.junit.Assert.assertNotNull;

public class BoundaryCollectTest {

    private static final ArrayList<String> KEYWORDS = new ArrayList<>(Arrays.asList("if", "int", "len"));
    private static final String PROJECT_NAME = "common-math";//for filter the clone repo
    private static final String CLASS_NAME = "int";//the class name of variable in if statement

    @Test
    public void testGetBoundaryInterval() {
        String path = "experiment//searchcode//"+ StringUtils.join(KEYWORDS, "-");
        if (!new File(path).exists()){
            SearchBoundaryFilter.searchCode(KEYWORDS, PROJECT_NAME);
        }
        BoundaryCollect boundaryCollect = new BoundaryCollect(path, TypeUtils.isSimpleType(CLASS_NAME), CLASS_NAME);
        ArrayList<Interval> intervals = boundaryCollect.getBoundaryInterval();
        assertNotNull(intervals);
        System.out.println(intervals);
    }

}
