package cn.edu.pku.sei.plde.conqueroverfitting.main;

import cn.edu.pku.sei.plde.conqueroverfitting.utils.RuntimeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.ShellUtils;
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
import java.util.concurrent.*;

/**
 * Created by yanrunfa on 16/2/21.
 */
public class Main {

    public static void main(String[] args){
        if (args.length == 0){
            System.out.println("Hello world");
            System.exit(0);
        }
        new File(System.getProperty("user.dir")+"/temp/").mkdirs();
        new File(System.getProperty("user.dir")+"/suspicious/").mkdirs();
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
                deleteTempFile();
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
        deleteTempFile();
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
        File main = new File(System.getProperty("user.dir")+"/"+"FixResult.log");
        try {
            FileWriter writer = new FileWriter(main, true);
            Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            writer.write("project "+project+" begin Time:"+format.format(new Date())+"\n");
            writer.close();
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


    private static void processFail(String project, TimeLine timeLine){
        File recordFile = new File(System.getProperty("user.dir")+"/patch/"+project);
        if (recordFile.exists()){
            recordFile.delete();
        }
        if (timeLine.isTimeout()){
            try {
                File main = new File(System.getProperty("user.dir")+"/"+"FixResult.log");
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


    private static void deleteTempFile(){
        backupPackage(System.getProperty("user.dir")+"/patch");
        backupPackage(System.getProperty("user.dir")+"/patch_source");
        backupPackage(System.getProperty("user.dir")+"/Localization");
        backupPackage(System.getProperty("user.dir")+"/RuntimeMessage");
        backupPackage(System.getProperty("user.dir")+"/RawLocalization");
        File log = new File(System.getProperty("user.dir")+"/FixResult.log");
        if (log.exists()){
            log.delete();
        }

    }

    private static void backupPackage(String packagePath){
        File file = new File(packagePath);
        if (!file.exists()){
            return;
        }
        if (!file.isDirectory()){
            return;
        }
        if (file.listFiles() == null){
            return;
        }
        File [] sub_files = file.listFiles();
        for (File sub_file: sub_files){
            if (sub_file.isFile()){
                sub_file.renameTo(new File(sub_file.getAbsolutePath()+".old"));
            }
        }
    }
}

/*
class RunFixProcess implements Callable<Boolean>  {
    public String path;
    public String projectType;
    public String project;
    public int projectNumber;

    public RunFixProcess(String path, String project){
        this.path = path;
        this.project = project;
        this.projectType = project.split("_")[0];
        this.projectNumber = Integer.valueOf(project.split("_")[1]);
    }

    public synchronized Boolean call() throws InterruptedException,TimeoutException{
        MainProcess process = new MainProcess(path);
        boolean result;
        File main = new File(System.getProperty("user.dir")+"/"+"FixResult.log");

        try {
            FileWriter writer = new FileWriter(main, true);
            Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            writer.write("project "+project+" begin Time:"+format.format(new Date())+"\n");
            writer.close();
            result = process.mainProcess(projectType, projectNumber);
            if (Thread.interrupted()){
                return false;
            }
            writer = new FileWriter(main, true);
            writer.write("project "+project+" "+(result?"Success":"Fail")+" Time:"+format.format(new Date())+"\n");
            writer.close();
        } catch (Exception e){
            result = false;
        }
        if (!result){
            File recordFile = new File(System.getProperty("user.dir")+"/patch/"+project);
            if (recordFile.exists()){
                recordFile.delete();
            }
        }


        return true;
    }
}*/
