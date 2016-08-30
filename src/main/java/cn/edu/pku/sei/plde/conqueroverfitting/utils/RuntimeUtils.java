package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Arrays;

/**
 * Created by yanrunfa on 5/12/16.
 */
public class RuntimeUtils {
    public static int getPid() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        String name = runtime.getName(); // format: "pid@hostname"
        try {
            return Integer.parseInt(name.substring(0, name.indexOf('@')));
        } catch (Exception e) {
            return -1;
        }
    }

    public static void killProcess(){
        try {
            String[] args = {"python","kill_process.py",String.valueOf(RuntimeUtils.getPid())};
            System.out.println(ShellUtils.shellRun(Arrays.asList(StringUtils.join(args," "))));
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
