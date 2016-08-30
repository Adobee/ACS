package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import cn.edu.pku.sei.plde.conqueroverfitting.agent.RunTestAgent;
import org.junit.runner.JUnitCore;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by yanrunfa on 16/2/24.
 */
public class PathUtils {

    public static String getFileSeparator(){
        //"/"
        return System.getProperty("file.separator");
    }
    public static String getPathSeparator(){
        //":"
        return System.getProperty("path.separator");
    }

    public static String getLineSeparator(){
        //"\n"
        return System.getProperty("line.separator");
    }

    public static String getAgentPath(){
        return System.getProperty("user.dir")+"/lib/RunTestAgent.jar";
    }


    public static String getJunitPath(){
        return System.getProperty("user.dir")+"/lib/junit-4.10.jar";
    }

    public static String getPackageNameFromPath(String path){
        try {
            String className = path.substring(path.lastIndexOf(getFileSeparator())+1,path.lastIndexOf("."));
            String code = FileUtils.getCodeFromFile(path);
            for (String line: code.split("\n")){
                if (line.startsWith("package")){
                    return line.substring(line.indexOf(' '),line.indexOf(";"))+"."+className;
                }
            }
        } catch (Exception e){
            if (path.contains("/java/")){
                return path.split("/java/")[1].replace(getFileSeparator(),".");
            }
        }
        return path.replace(getFileSeparator(),".");
    }

    public static ArrayList<String> getSrcPath(String bugProject){
        ArrayList<String> path = new ArrayList<String>();
        String[] words = bugProject.split("_");
        String projectName = words[0];
        int bugId = Integer.parseInt(words[1]);
        if(projectName.equals("Math")){
            if(bugId < 85){
                path.add("/target/classes/");
                path.add("/target/test-classes/");
                path.add("/src/main/java/");
                path.add("/src/test/java/");
            }else{
                path.add("/target/classes/");
                path.add("/target/test-classes/");
                path.add("/src/java/");
                path.add("/src/test/");
            }
        }else if(projectName.equals("Time")){
            if(bugId < 12){
                path.add("/target/classes/");
                path.add("/target/test-classes/");
                path.add("/src/main/java/");
                path.add("/src/test/java/");
            }else{
                path.add("/build/classes/");
                path.add("/build/tests/");
                path.add("/src/main/java/");
                path.add("/src/test/java/");
            }

        }else if(projectName.equals("Lang")){
            if(bugId <= 20){
                path.add("/target/classes/");
                path.add("/target/tests/");
                path.add("/src/main/java/");
                path.add("/src/test/java/");
            }else if(bugId >= 21 && bugId <= 35){
                path.add("/target/classes/");
                path.add("/target/test-classes/");
                path.add("/src/main/java/");
                path.add("/src/test/java/");
            }else if(bugId >= 36 && bugId <= 41){
                path.add("/target/classes/");
                path.add("/target/test-classes/");
                path.add("/src/java/");
                path.add("/src/test/");
            }else if(bugId >= 42 && bugId <= 65){
                path.add("/target/classes/");
                path.add("/target/tests/");
                path.add("/src/java/");
                path.add("/src/test/");
            }


        }else if(projectName.equals("Chart")){
            path.add("/build/");
            path.add("/build-tests/");
            path.add("/source/");
            path.add("/tests/");

        }else if(projectName.equals("Closure")){
            path.add("/build/classes/");
            path.add("/build/test/");
            path.add("/src/");
            path.add("/test/");
        }
        return path;
    }

}
