package cn.edu.pku.sei.plde.ACS.agent;
import sun.misc.BASE64Decoder;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yanrunfa on 2016/2/20.
 */
public class RunTestAgent {

    public static void premain(String agentArgs, Instrumentation inst) throws IOException{
        System.out.println("------Agent Run Success------");
        if (agentArgs == null || agentArgs.length() <= 2){
            throw new IOException("Wrong Agent Args");
        }
        if (agentArgs.startsWith("\"")){
            agentArgs = agentArgs.substring(1);
        }
        if (agentArgs.endsWith("\"")){
            agentArgs = agentArgs.substring(0,agentArgs.length()-2);
        }
        BASE64Decoder decoder = new BASE64Decoder();
        String decodedAgentArgs = new String(decoder.decodeBuffer(agentArgs), "utf-8");
        System.out.println("Agent Args: "+decodedAgentArgs);
        String[] args = decodedAgentArgs.split(",,");
        if (args.length < 5 || args.length > 10){
            throw new IOException("Wrong Number Args");
        }
        String targetClassName = "";
        String targetClassFunc = "";
        List<Integer> targetLineNum = new ArrayList<>();
        String[] targetVariables = {};
        String srcPath = "";
        String classPath = "";
        String testSrcPath = "";
        String testClassName = "";
        String patch = null;
        int assertLine = -1;
        for (String arg: args) {
            int colonPos = arg.indexOf(":");
            String key = colonPos == -1?arg:arg.substring(0, colonPos);
            String value = colonPos == -1?null:arg.substring(colonPos + 1);
            if (value == null){
                continue;
            }
            if (key.equalsIgnoreCase("class")){
                targetClassName = value.replace(".","/");
            }
            else if (key.equalsIgnoreCase("func")){
                targetClassFunc = value;
            }
            else if (key.equalsIgnoreCase("line")){
                for (String num: value.split("-")){
                    targetLineNum.add(Integer.valueOf(num));
                }
            }
            else if (key.equalsIgnoreCase("var")){
                if (targetVariables.length == 0){
                    targetVariables = value.split("//");
                }
                else {
                    targetVariables = concat(targetVariables, value.split("//"));
                }
            }
            else if (key.equalsIgnoreCase("src")){
                srcPath = value;
            }
            else if (key.equalsIgnoreCase("cp")){
                classPath = value;
            }
            else if (key.equalsIgnoreCase("method")){
                String[] methods = value.split("//");
                List<String> vars = new ArrayList<String>();
                for (String method: methods){
                    if (method.contains("??")){
                        String methodType = method.split("\\?\\?")[1];
                        String methodName = method.split("\\?\\?")[0];
                        vars.add(methodName+"()??"+methodType);
                    }
                    else {
                        vars.add(method+"()");
                    }
                }
                if (targetVariables.length == 0){
                    targetVariables = new String[vars.size()];
                    vars.toArray(targetVariables);
                }
                else {
                    String[] varsArray = new String[vars.size()];
                    vars.toArray(varsArray);
                    targetVariables = concat(targetVariables, varsArray);
                }
            }
            else if (key.equalsIgnoreCase("patch")) {
                decoder = new BASE64Decoder();
                patch = new String(decoder.decodeBuffer(value), "utf-8");
            }
            else if (key.equalsIgnoreCase("assert")){
                assertLine = Integer.valueOf(value);
            }
            else if (key.equalsIgnoreCase("test")){
                testClassName = value;
            }
            else if (key.equalsIgnoreCase("testsrc")){
                testSrcPath = value;
            }
        }
        if (!testClassName.equals("")  && !testSrcPath.equals("")){
            inst.addTransformer(new CommentAssertTransformer(testSrcPath, testClassName));
        }
        if (patch!= null){
            inst.addTransformer(new AddFixTransformer(targetClassName, targetLineNum, patch, srcPath, classPath));
        }
        else if (targetVariables.length != 0){
            inst.addTransformer(new AddPrintTransformer(targetClassName, targetClassFunc,targetLineNum.get(0), targetVariables, srcPath, classPath));
        }
        else {
            System.out.println("Wrong Agent Args");
        }
    }

    public static <T> T[] concat(T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

}
