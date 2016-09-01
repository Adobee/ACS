package cn.edu.pku.sei.plde.ACS.utils;

import cn.edu.pku.sei.plde.ACS.boundary.BoundaryGenerator;
import cn.edu.pku.sei.plde.ACS.boundary.model.Interval;
import cn.edu.pku.sei.plde.ACS.fix.SuspiciousFixer;
import cn.edu.pku.sei.plde.ACS.localization.Suspicious;
import cn.edu.pku.sei.plde.ACS.main.Config;
import cn.edu.pku.sei.plde.ACS.main.MainProcess;
import cn.edu.pku.sei.plde.ACS.main.TimeLine;
import cn.edu.pku.sei.plde.ACS.trace.ExceptionVariable;
import cn.edu.pku.sei.plde.ACS.visible.model.VariableInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yanrunfa on 16/4/23.
 */
public class RecordUtils {
    public String project;
    public String type;
    private FileWriter writer;



    public RecordUtils(String type){
        this.project = MainProcess.PROJECT_NAME;
        this.type = type;
        File recordPackage = new File(Config.RESULT_PATH+"/"+type+"/");
        recordPackage.mkdirs();
        File recordFile = new File(recordPackage.getAbsolutePath()+"/"+project);
        try {
            if (!recordFile.exists()){
                recordFile.createNewFile();
            }
            this.writer = new FileWriter(recordFile, true);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }


    public void write(String message){
        try {
            this.writer.write(message);
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    public void close(){
        try {
            writer.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }


    public static void record(String message, String identity){
        File recordFile = new File(Config.RESULT_PATH+"/"+identity+".log");
        try {
            if (!recordFile.exists()){
                recordFile.createNewFile();
            }
            FileWriter writer = new FileWriter(recordFile, true);
            writer.write(message+"\n");
            writer.close();
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    public static void recordIfReturn(String code, String patch, int line, String project){
        File patchSourcePackage = new File(Config.PATCH_SOURCE_PATH);
        if (!patchSourcePackage.exists()){
            patchSourcePackage.mkdirs();
        }
        try {
            File patchSource = new File(patchSourcePackage.getAbsolutePath()+"/"+project+"_if_return_" +".java");
            if (!patchSource.exists()){
                patchSource.createNewFile();
            }
            FileWriter writer = new FileWriter(patchSource, false);
            writer.write(code);
            writer.close();
            patch = "***********************************************************************patch begin*********************************************************************\n"
                    + patch+ "\n" +
                    "************************************************************************patch end****************************************************************\n";
            CodeUtils.addCodeToFile(patchSource,patch,line);
        } catch (IOException e){
            e.printStackTrace();
        }

    }


    public static void recordIf(String code, String ifString, int startLine, int endLine, boolean replace,String project){
        File patchSourcePackage = new File(Config.PATCH_SOURCE_PATH);
        if (!patchSourcePackage.exists()){
            patchSourcePackage.mkdirs();
        }
        try {
            File patchSource = new File(patchSourcePackage.getAbsolutePath()+"/"+project+"_if_"+".java");
            if (!patchSource.exists()){
                patchSource.createNewFile();
            }
            FileWriter writer = new FileWriter(patchSource, false);
            writer.write(code);
            writer.close();
            ifString ="***********************************************************************patch begin*****************************************************************\n"+ ifString+"\n";
            SourceUtils.insertIfStatementToSourceFile(patchSource, ifString, startLine,endLine, replace);
            if (replace){
                int braceCount = 0;
                for (int i=startLine; i< code.split("\n").length; i++){
                    String lineString = CodeUtils.getLineFromCode(code, i);
                    braceCount += CodeUtils.countChar(lineString,'{');
                    braceCount -= CodeUtils.countChar(lineString,'}');
                    if (braceCount <= 0){
                        endLine = i+1;
                        break;
                    }
                }
                CodeUtils.addCodeToFile(patchSource,
                       "**************************************************************patch end****************************************************************\n",endLine+1);
            }else {
                CodeUtils.addCodeToFile(patchSource,
                        "*****************************************************************patch end***************************************************************\n",endLine+3);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }


    public static void printBoundaryCollectMessage(List<Map.Entry<Interval, Integer>> intervalsMap){
        RecordUtils writer = new RecordUtils("intervalMessage");
        writer.write("---------------------------------------------------\n");
        writer.write("variable: "+ BoundaryGenerator.GENERATING_VARIABLE.name+"\n");
        writer.write("value   : "+ BoundaryGenerator.GENERATING_VARIABLE.values+"\n");
        writer.write("count: "+ intervalsMap.size()+"\n\n");//
        int count = 1;
        for (Map.Entry<Interval, Integer> entry: intervalsMap){
            writer.write(count+" : " +entry.getKey().toString()+"\t"+entry.getValue()+"\n");
        }
        writer.write("---------------------------------------------------\n\n\n");
        writer.close();
    }

    public static void printRuntimeMessage(Suspicious suspicious, String project, List<ExceptionVariable> exceptionVariables, List<List<ExceptionVariable>> echelons, long searchTime){
        RecordUtils writer = new RecordUtils("runtimeMessage");
        writer.write("---------------------------------------------------\n");
        writer.write("suspicious variable of suspicious before sort: "+suspicious.classname()+"#"+suspicious.functionnameWithoutParam()+"#"+suspicious.getDefaultErrorLine()+"\n");
        for (ExceptionVariable variable: exceptionVariables){
            writer.write(variable.name+" = "+variable.values.toString()+"\n");
        }
        writer.write("---------------------------------------------------\n");
        writer.write("variable echelon of suspicious before search: "+suspicious.classname()+"#"+suspicious.functionnameWithoutParam()+"#"+suspicious.getDefaultErrorLine()+"\n");
        int echelonsNum = 0;
        for (List<ExceptionVariable> echelon: echelons){
            writer.write("////////////////Echelon "+ ++echelonsNum+"////////////\n");
            for (ExceptionVariable variable: echelon){
                writer.write(variable.name+" = "+variable.values.toString()+"\n");
            }
        }
        writer.write("---------------------------------------------------\n");
        writer.write("variable candidate of : "+suspicious.classname()+"#"+suspicious.functionnameWithoutParam()+"#"+suspicious.getDefaultErrorLine()+"\n");
        for (VariableInfo variable: suspicious.getAllInfo()){
            if (variable.isParameter || variable.isLocalVariable){
                writer.write(variable.variableName+" [ "+variable.getStringType()+" ] \n");
            }

        }
        writer.write("---------------------------------------------------\n");
        writer.write("====================================================\n\n");
        writer.write("Search Boundary Cost Time: "+searchTime/1000+"\n");
        writer.close();
    }



    public static void printHistoryBoundary(
            Map<String, List<String>> boundary,
            String ifStatement,
            Suspicious suspicious,
            List<String> methodOneHistory,
            List<String> methodTwoHistory,
            List<String> bannedHistory){
        RecordUtils patchWriter = new RecordUtils("patch");
        patchWriter.write("====================================================\n");
        patchWriter.write("boundary of suspicious: "+suspicious.classname()+"#"+suspicious.functionnameWithoutParam()+"#"+suspicious.getDefaultErrorLine()+"\n");
        patchWriter.write(MathUtils.replaceSpecialNumber(ifStatement)+"\n");
        patchWriter.close();

        RecordUtils writer = new RecordUtils("patch");
        for (Map.Entry<String, List<String>> entry: boundary.entrySet()){
            String assertString = entry.getKey();
            writer.write("====================================================\n");
            writer.write("Tried ifStrings With AssertMessage:"+assertString+"\n");
            for (String ifString: methodOneHistory){
                writer.write(ifString+"\n");
            }
            for (String ifString: methodTwoHistory){
                writer.write(ifString+"\n");
            }
            writer.write("====================================================\n");
            writer.write("Banned ifStrings With AssertMessage:"+assertString+"\n");
            for (String ifString: bannedHistory){
                writer.write(ifString+"\n");
            }
            writer.write("====================================================\n\n");
        }
        writer.close();
    }

    public static void printCollectingMessage(Suspicious suspicious, TimeLine timeLine){
        RecordUtils writer = new RecordUtils("runtimeMessage");

        writer.write("Whole Cost Time: "+timeLine.time() +"\n");
        writer.write("True Test Num: "+suspicious.trueTestNums()+"\n");
        writer.write("True Assert Num: "+suspicious.trueAssertNums()+"\n");
        writer.write("====================================================================\n\n");
        writer.close();
    }

}
