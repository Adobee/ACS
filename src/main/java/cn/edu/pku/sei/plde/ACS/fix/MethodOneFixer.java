package cn.edu.pku.sei.plde.ACS.fix;

import cn.edu.pku.sei.plde.ACS.assertCollect.Asserts;
import cn.edu.pku.sei.plde.ACS.junit.JunitRunner;
import cn.edu.pku.sei.plde.ACS.localization.Suspicious;
import cn.edu.pku.sei.plde.ACS.type.TypeUtils;
import cn.edu.pku.sei.plde.ACS.utils.*;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.internal.compiler.ast.OR_OR_Expression;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by yanrunfa on 16/2/24.
 */
public class MethodOneFixer {
    private final String _classpath;
    private final String _testClassPath;
    private final String _classSrcPath;
    private final String _testSrcPath;
    private Suspicious _suspicious;
    private int _errorTestNum;
    private String _project;
    public List<Patch> _patches = new ArrayList<>();
    public List<String> triedPatch = new ArrayList<>();

    public MethodOneFixer(Suspicious suspicious, String project){
        _suspicious = suspicious;
        _classpath = suspicious._classpath;
        _testClassPath = suspicious._testClasspath;
        _classSrcPath = suspicious._srcPath;
        _testSrcPath = suspicious._testSrcPath;
        _project = project;
        _errorTestNum = SuspiciousFixer.FAILED_TEST_NUM;
    }

    public boolean addPatch(Patch patch){
        File targetJavaFile = new File(FileUtils.getFileAddressOfJava(_classSrcPath, patch._className));
        File targetClassFile = new File(FileUtils.getFileAddressOfClass(_classpath, patch._className));
        File javaBackup = FileUtils.copyFile(targetJavaFile.getAbsolutePath(), FileUtils.tempJavaPath(patch._className,"MethodOneFixer"));
        File classBackup = FileUtils.copyFile(targetClassFile.getAbsolutePath(), FileUtils.tempClassPath(patch._className,"MethodOneFixer"));

        int minErrorTest = _errorTestNum;
        String truePatchString = "";
        int truePatchLine = 0;
        String code = FileUtils.getCodeFromFile(javaBackup);
        patch._patchString = TypeUtils.arrayDup(patch._patchString);
        boolean fixedFlag = false;
        for (String patchString: patch._patchString){
            if (patchString.equals("")){
                continue;
            }
            if (fixedFlag){
                break;
            }
            for (int patchLine: patch._patchLines){
                if (CodeUtils.getLineFromCode(code,patchLine).contains("else")){
                    while (CodeUtils.getLineFromCode(code, --patchLine).contains("if"));
                }
                FileUtils.copyFile(javaBackup, targetJavaFile);
                CodeUtils.addCodeToFile(targetJavaFile, patchString, patchLine);
                triedPatch.add(patchString);
                System.out.print("Method 1 try patch: "+patchString);
                if (!patch._addonFunction.equals("")){
                    CodeUtils.addMethodToFile(targetJavaFile, patch._addonFunction, patch._className.substring(patch._className.lastIndexOf(".")+1));
                }
                if (!patch._addonImport.equals("")){
                    CodeUtils.addImportToFile(targetJavaFile, patch._addonImport);
                }
                try {
                    targetClassFile.delete();
                    System.out.println(ShellUtils.shellRun(Arrays.asList("javac -Xlint:unchecked -source 1.6 -target 1.6 -cp "+ buildClasspath(Arrays.asList(PathUtils.getJunitPath())) +" -d "+_classpath+" "+ targetJavaFile.getAbsolutePath())));
                }
                catch (IOException e){
                    System.out.println("fix fail");
                    continue;
                }
                if (!targetClassFile.exists()){ //编译不成功
                    System.out.println("MethodOneFixer: fix fail because of compile fail");
                    continue;
                }
                Asserts asserts = new Asserts(_classpath,_classSrcPath, _testClassPath, _testSrcPath, patch._testClassName, patch._testMethodName, _project);
                int errAssertNumAfterFix = asserts.errorNum();
                int errAssertBeforeFix = _suspicious._assertsMap.get(patch._testClassName+"#"+patch._testMethodName).errorNum();
                if (errAssertNumAfterFix < errAssertBeforeFix){
                    int errorTestAterFix = TestUtils.getFailTestNumInProject(_project);
                    if (errorTestAterFix < minErrorTest){
                        minErrorTest = errorTestAterFix;
                        truePatchLine = patchLine;
                        truePatchString = patchString;
                        RecordUtils.recordIfReturn(code,patchString, patchLine, _project);
                        FileUtils.copyFile(classBackup, targetClassFile);
                        FileUtils.copyFile(javaBackup, targetJavaFile);
                        //if (errorTestAterFix == 0){
                            fixedFlag = true;
                            break;
                        //}
                    }
                }
                FileUtils.copyFile(classBackup, targetClassFile);
                FileUtils.copyFile(javaBackup, targetJavaFile);
            }
        }
        if (minErrorTest < _errorTestNum){
            patch._patchString.clear();
            patch._patchString.add(truePatchString);
            patch._patchLines.clear();
            patch._patchLines.add(truePatchLine);
            _patches.add(patch);
            System.out.println("MethodOneFixer: fix success");
            SuspiciousFixer.FAILED_TEST_NUM = minErrorTest;
            return true;
        }else {
            System.out.println("MethodOneFixer: fix fail");
        }

        FileUtils.copyFile(classBackup, targetClassFile);
        FileUtils.copyFile(javaBackup, targetJavaFile);
        return false;
    }



    public int fix(){
        if (_patches.size() == 0){
            return -1;
        }
        Map<File, File> backups = new HashMap<>();
        List<File> tobeCompile = new ArrayList<>();
        for (Patch patch: _patches){
            File targetJavaFile = new File(FileUtils.getFileAddressOfJava(_classSrcPath, patch._className));
            File targetClassFile = new File(FileUtils.getFileAddressOfClass(_classpath, patch._className));
            File javaBackup = FileUtils.copyFile(targetJavaFile.getAbsolutePath(), FileUtils.tempJavaPath(patch._className,"MethodOneFixer"));
            File classBackup = FileUtils.copyFile(targetClassFile.getAbsolutePath(), FileUtils.tempClassPath(patch._className,"MethodOneFixer"));
            if (!backups.containsKey(targetJavaFile)){
                backups.put(targetJavaFile, javaBackup);
            }
            if (!backups.containsKey(targetClassFile)){
                backups.put(targetClassFile, classBackup);
            }
            CodeUtils.addCodeToFile(targetJavaFile, patch._patchString.get(0), patch._patchLines);
            if (!patch._addonFunction.equals("")){
                CodeUtils.addMethodToFile(targetJavaFile, patch._addonFunction, patch._className.substring(patch._className.lastIndexOf(".")+1));
            }
            if (!patch._addonImport.equals("")){
                CodeUtils.addImportToFile(targetJavaFile, patch._addonImport);
            }
            tobeCompile.add(targetJavaFile);
        }
        for (File javaFile: tobeCompile){
            try {
                System.out.println(ShellUtils.shellRun(Arrays.asList("javac -Xlint:unchecked -source 1.6 -target 1.6 -cp "+ buildClasspath(Arrays.asList(PathUtils.getJunitPath())) +" -d "+_classpath+" "+ javaFile.getAbsolutePath())));
            }
            catch (IOException e){
                return -1;
            }
        }
        return 0;
    }


    private Map<String, String> getTestsOfPatch(){
        Map<String, String> result = new HashMap<>();
        for (Patch patch: _patches){
            if (!result.containsKey(patch._testMethodName)){
                result.put(patch._testMethodName, patch._testClassName);
            }
        }
        return result;
    }


    private String buildClasspath(List<String> additionalPath){
        String path = "\"";
        path += _classpath;
        path += System.getProperty("path.separator");
        path += _testClassPath;
        path += System.getProperty("path.separator");
        path += JunitRunner.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        path += System.getProperty("path.separator");
        path += StringUtils.join(additionalPath,System.getProperty("path.separator"));
        path += "\"";
        return path;
    }

}
