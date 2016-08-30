package cn.edu.pku.sei.plde.conqueroverfitting.utils;


import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanrunfa on 16/3/11.
 */
public class SourceUtils {



    public static void insertIfStatementToSourceFile(File file, String ifStatement, int startLine, int endLine, boolean replace){
        int i=0;
        while (new File(System.getProperty("user.dir")+"/temp/source"+i+".temp").exists()){
            i++;
        }
        File tempFile = new File(System.getProperty("user.dir")+"/temp/source"+i+".temp");
        FileOutputStream outputStream = null;
        BufferedReader reader = null;
        try {
            outputStream = new FileOutputStream(tempFile);
            reader = new BufferedReader(new FileReader(file));
            String lineString = null;
            int lineNum = 0;
            while ((lineString = reader.readLine()) != null) {
                lineNum++;
                if (lineNum == startLine){
                    outputStream.write((ifStatement).getBytes());
                }
                if (lineNum == endLine && !replace){
                    outputStream.write("}".getBytes());
                }
                if (lineNum != startLine || !replace){
                    outputStream.write((lineString+"\n").getBytes());
                }
            }

        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if (outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException e){
                }
            }
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e){
                }
            }

        }
        FileUtils.copyFile(tempFile.getAbsolutePath(),file.getAbsolutePath());
        tempFile.delete();
    }


    public static void commentCodeInSourceFile(File file, int line){
        int i=0;
        while (new File(System.getProperty("user.dir")+"/temp/source"+i+".temp").exists()){
            i++;
        }
        File tempFile = new File(System.getProperty("user.dir")+"/temp/source"+i+".temp");
        FileOutputStream outputStream = null;
        BufferedReader reader = null;
        try {
            outputStream = new FileOutputStream(tempFile);
            reader = new BufferedReader(new FileReader(file));
            String lineString = null;
            int lineNum = 0;
            int brackets = 0;
            while ((lineString = reader.readLine()) != null) {
                lineNum++;
                if (lineNum == line-1 && (!lineString.contains(";") && !lineString.contains(":") && !lineString.contains("{") && !lineString.contains("}"))){
                    lineString = "//"+lineString;
                    brackets += CodeUtils.countChar(lineString, '(');
                    brackets -= CodeUtils.countChar(lineString, ')');
                }
                if (lineNum == line || brackets > 0) {
                    lineString = "//"+lineString;
                    brackets += CodeUtils.countChar(lineString, '(');
                    brackets -= CodeUtils.countChar(lineString, ')');
                }
                outputStream.write((lineString+"\n").getBytes());
            }
            outputStream.close();
            reader.close();
        }  catch (IOException e){
            e.printStackTrace();
        }    finally {
            if (outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException e){
                }
            }
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e){
                }
            }

        }
        FileUtils.copyFile(tempFile.getAbsolutePath(),file.getAbsolutePath());
        tempFile.delete();
    }

    public static File backupSource(String srcpath, String classname){
        int i=0;
        while (new File(FileUtils.tempJavaPath(classname, "sourceUBK"+i)).exists()){
            i++;
        }
        File backup = new File(FileUtils.tempJavaPath(classname, "sourceUBK"+i));
        FileUtils.copyFile(FileUtils.getFileAddressOfJava(srcpath, classname),backup.getAbsolutePath());
        return backup;
    }

    public static File backupClass(String classpath, String classname){
        int i=0;
        while (new File(FileUtils.tempClassPath(classname, "sourceUBK"+i)).exists()){
            i++;
        }
        File backup = new File(FileUtils.tempClassPath(classname, "sourceUBK"+i));
        FileUtils.copyFile(FileUtils.getFileAddressOfClass(classpath, classname),backup.getAbsolutePath());
        return backup;
    }

}
