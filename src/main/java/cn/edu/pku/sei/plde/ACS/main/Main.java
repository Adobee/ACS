package cn.edu.pku.sei.plde.ACS.main;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by yanrunfa on 16/2/21.
 */
public class Main {

    public static void main(String[] args){
        if (args.length == 0){
            System.out.println("Hello world");
            System.exit(0);
        }
        new File(Config.TEMP_FILES_PATH).mkdirs();
        new File(Config.LOCALIZATION_RESULT_CACHE).mkdirs();
        String path = args[0];
        File file = new File(path);
        File [] sub_files = file.listFiles();
        if (sub_files == null){
            System.out.println("No file in path");
            System.exit(0);
        }
        List<String> bannedList = new ArrayList<>();
        if (args.length == 2){
            if (args[1].startsWith("ban:")){
                String banned = args[1].substring(args[1].indexOf(":")+1);
                bannedList.addAll(Arrays.asList(banned.split(":")));
            }
            else if (args[1].contains(":")){
                for (String name: args[1].split(":")){
                    System.out.println("Main: fixing project "+name);
                    try {
                        fixProject(name, path);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
                System.exit(0);
            }
            else {
                String projectName = args[1];
                try {
                    fixProject(projectName, path);
                } catch (Exception e){
                    e.printStackTrace();
                }
                System.exit(0);
            }
        }
        for (File sub_file : sub_files){
            if (sub_file.isDirectory()){
                System.out.println("Main: fixing project "+sub_file.getName());
                try {
                    if (bannedList.contains(sub_file.getName())){
                        System.out.println("Main: jumped project "+sub_file.getName());
                        continue;
                    }
                    fixProject(sub_file.getName(), path);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        System.exit(0);
    }
    private static void fixProject(String project, String path) throws Exception{
        if (!project.contains("_")){
            System.out.println("Main: cannot recognize project name \""+project+"\"");
            return;
        }
        if (!StringUtils.isNumeric(project.split("_")[1])){
            System.out.println("Main: cannot recognize project name \""+project+"\"");
            return;
        }
        TimeLine timeLine = new TimeLine(Config.TOTAL_RUN_TIMEOUT);
        String projectType = project.split("_")[0];
        int projectNumber = Integer.valueOf(project.split("_")[1]);
        MainProcess process = new MainProcess(path);
        boolean result;
        File main = new File(Config.FIX_RESULT_FILE_PATH);
        if (!main.exists()){
            if (!new File(Config.RESULT_PATH).exists()){
                new File(Config.RESULT_PATH).mkdirs();
            }
            main.createNewFile();
        }
        try {
            FileWriter writer = new FileWriter(main, true);
            Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            writer.write("project "+project+" begin Time:"+format.format(new Date())+"\n");
            writer.close();
            cleanPrevResult(project);
            result = process.mainProcess(projectType, projectNumber, timeLine);
            writer = new FileWriter(main, true);
            writer.write("project "+project+" "+(result?"Success":"Fail")+" Time:"+format.format(new Date())+"\n");
            writer.close();
        } catch (Exception e){
            result = false;
        }
        if (!result){
           processFail(project, timeLine);
        }
    }

    private static void cleanPrevResult(String projectName){
        cleanMessage(Config.PATCH_PATH, projectName);
        cleanMessage(Config.LOCALIZATION_PATH, projectName);
        cleanMessage(Config.PATCH_SOURCE_PATH, projectName);
        cleanMessage(Config.RUNTIMEMESSAGE_PATH, projectName);
        cleanMessage(Config.PREDICATE_MESSAGE_PATH, projectName);
    }

    private static void cleanMessage(String forder, String projectName){
        if (new File(forder).exists() && new File(forder).isDirectory()){
            for (File file: new File(forder).listFiles()){
                if (file.getName().startsWith(projectName)){
                    file.delete();
                }
            }
        }
    }

    private static void processFail(String project, TimeLine timeLine){
        File[] patchFiles = {
                new File(Config.PATCH_PATH),
                new File(Config.PATCH_SOURCE_PATH)
        };
        for (File patchFile: patchFiles){
            if (patchFile.exists()){
                patchFile.delete();
            }
        }
        if (timeLine.isTimeout()){
            try {
                File main = new File(Config.FIX_RESULT_FILE_PATH);
                if (!main.exists()){
                    main.createNewFile();
                }
                FileWriter writer = new FileWriter(main, true);
                Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                writer.write("project "+project+" Timeout At :"+format.format(new Date())+"\n");
                writer.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }


}
