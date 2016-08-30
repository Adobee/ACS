package cn.edu.pku.sei.plde.ACS.agent;


import java.io.*;
import java.util.*;

/**
 * Created by yanrunfa on 2016/2/20.
 */
public class Utils {

    /**
     *
     * @param tempJavaName
     * @param tempClassName
     * @param classPath
     * @param srcPath
     * @param className
     * @param targetLine
     * @param addingCode
     * @return
     * @throws IOException
     */

    public static byte[] AddCodeToSource(String tempJavaName, String tempClassName, String classPath, String srcPath, String className,String methodName, int targetLine, String addingCode) throws IOException {
        return AddCodeToSource(tempJavaName, tempClassName, classPath, srcPath, className, methodName, Arrays.asList(targetLine), addingCode);
    }


    public static byte[] AddCodeToSource(String tempJavaName, String tempClassName, String classPath, String srcPath, String className, List<Integer> targetLines, String addingCode) throws IOException {
        return AddCodeToSource(tempJavaName, tempClassName, classPath, srcPath, className, "", targetLines, addingCode);
    }

    public static byte[] AddCodeToSource(String tempJavaName, String tempClassName, String classPath, String srcPath, String className, String methodName, List<Integer> targetLine, String addingCode) throws IOException{
        Map<Integer, Boolean> writedMap = new HashMap<>();
        for (int line: targetLine){
            writedMap.put(line, false);
        }
        int methodStartLine = 0;
        if (!methodName.equals("")){
            methodStartLine = getMethodStartLine(getCodeFromFile(srcPath+"/"+className.replace(".","/")+".java"), targetLine.get(0), methodName);
        }

        File tempJavaFile = new File(tempJavaName);
        FileOutputStream outputStream = null;
        BufferedReader reader = null;
        try {
            outputStream = new FileOutputStream(tempJavaFile);
            reader = new BufferedReader(new FileReader(srcPath+"/"+className.replace(".","/")+".java"));
            String lineString = null;
            int line = 0;
            while ((lineString = reader.readLine()) != null) {
                line++;
                if (targetLine.contains(line+1)){
                    if ((!lineString.contains(";") && !lineString.contains(":") && !lineString.contains("{") && !lineString.contains("}"))|| lineString.contains("return ") || lineString.contains("throw ")){
                        outputStream.write(addingCode.getBytes());
                        writedMap.put(line+1, true);
                    }
                }

                if (line == methodStartLine+1 && methodStartLine != 0){
                    outputStream.write(("System.out.println(\"|into_method|\");").getBytes());
                }
                if (targetLine.contains(line) && !writedMap.get(line)){
                    outputStream.write(addingCode.getBytes());
                    writedMap.put(line, true);
                }
                outputStream.write((lineString+"\n").getBytes());
                if (lineString.startsWith("package")){
                    outputStream.write("import java.util.*;\n".getBytes());
                    outputStream.write("import java.lang.System;\n".getBytes());
                    outputStream.write("import java.lang.Thread;\n".getBytes());
                }
            }

            String result = Utils.shellRun(Arrays.asList("javac -Xlint:unchecked -cp "+ classPath+" "+ tempJavaName));
            if (result.contains("找不到文件") || result.contains("not found")){
                throw new FileNotFoundException();
            }
            if (!result.trim().equals("")){
                System.out.println(result);
            }
        } catch (FileNotFoundException e){
            System.out.println("AddCodeToSource: TempJavaName: "+tempJavaName +" No Found");
            throw new FileNotFoundException();
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if (outputStream!=null){
                outputStream.close();
            }
            if (reader != null){
                reader.close();
            }

        }

        return getBytesFromFile(tempClassName);
    }



    private static int getMethodStartLine(String code, int errorLine, String methodName){
        String[] codeLines = code.split("\n");
        for (int i= errorLine; i > 0; i--){
            if (codeLines[i].contains(" "+methodName+"(") &&(codeLines[i].contains("public ")||codeLines[i].contains("private ")||codeLines[i].contains("protected "))){
                while (!codeLines[i].contains("{")){
                    i++;
                }
                if (codeLines[i+1].contains("super(")){
                    i++;
                    while (!codeLines[i].contains(";")){
                        i++;
                    }
                }
                return i+1;
            }
        }
        return 0;
    }

    /**
     *
     * @param fileName
     * @return
     */
    public static byte[] getBytesFromFile(String fileName) {
        try {
            FileInputStream in=new FileInputStream(fileName);
            ByteArrayOutputStream out=new ByteArrayOutputStream(1024);
            byte[] temp=new byte[1024];
            int size=0;
            while((size=in.read(temp))!=-1)
            {
                out.write(temp,0,size);
            }
            in.close();
            byte[] bytes=out.toByteArray();
            return bytes;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     *
     * @param p the process
     * @return the shell out
     * @throws IOException
     */
    public static String getShellOut(Process p) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedInputStream in = null;
        BufferedReader br = null;
        BufferedInputStream errIn = null;
        BufferedReader errBr = null;
        try {
            String s;
            errIn = new BufferedInputStream(p.getErrorStream());
            errBr = new BufferedReader(new InputStreamReader(errIn));
            while ((s = errBr.readLine()) != null) {
                sb.append(System.getProperty("line.separator"));
                sb.append(s);
            }

            in = new BufferedInputStream(p.getInputStream());
            br = new BufferedReader(new InputStreamReader(in));
            while ((s = br.readLine()) != null) {
                sb.append(System.getProperty("line.separator"));
                sb.append(s);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw e;
        } finally {
            if (br != null){
                br.close();
            }
            if (in != null){
                in.close();
            }
            if (errBr != null){
                errBr.close();
            }
            if (errIn !=null){
                errIn.close();
            }
        }
        p.destroy();
        return sb.toString();
    }

    /**
     *
     * @param args the args to be run in the shell
     * @return the result print of the run
     * @throws IOException
     */
    public static String shellRun(List<String> args) throws IOException{
        String fileName;
        String cmd;
        if (System.getProperty("os.name").toLowerCase().startsWith("win")){
            fileName = System.getProperty("user.dir")+"/temp"+"/args.bat";
            cmd = System.getProperty("user.dir")+"/temp"+"/args.bat";
        }
        else {
            fileName = System.getProperty("user.dir")+"/temp"+"/args.sh";
            cmd = "bash " + System.getProperty("user.dir")+"/temp"+"/args.sh";
        }
        File batFile = new File(fileName);
        if (!batFile.exists()){
            boolean result = batFile.createNewFile();
            if (!result){
                throw new IOException("Cannot Create bat file:" + fileName);
            }
        }
        FileOutputStream outputStream = new FileOutputStream(batFile);
        for (String arg: args){
            outputStream.write(arg.getBytes());
        }
        outputStream.close();
        batFile.deleteOnExit();
        Process process= Runtime.getRuntime().exec(cmd);
        return Utils.getShellOut(process);
    };

    public static String getCodeFromFile(String fileaddress){
        try {
            FileInputStream stream = new FileInputStream(new File(fileaddress));
            byte[] b=new byte[stream.available()];
            int len = stream.read(b);
            if (len <= 0){
                throw new IOException("Source code file "+fileaddress+" read fail!");
            }
            stream.close();
            return new String(b);
        } catch (Exception e){
            System.out.println(e.getMessage());
            return "";
        }
    }

}

