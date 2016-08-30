package cn.edu.pku.sei.plde.conqueroverfitting.fix;

import cn.edu.pku.sei.plde.conqueroverfitting.assertCollect.Asserts;
import cn.edu.pku.sei.plde.conqueroverfitting.junit.JunitRunner;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.common.support.Factory;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.*;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.StreamHandler;

/**
 * Created by yanrunfa on 5/3/16.
 */
public class AssertComment {
    public Asserts asserts;
    public int line;
    private List<Integer> commented;
    private List<Integer> errorAsserts;
    private String testClassPath;
    private String testSrcPath;
    private String testClassName;
    private String code;
    private File targetFile;
    private File javaBackup;
    private File classBackup;
    private boolean isCommented = false;

    public AssertComment(Asserts asserts, int line){
        this.asserts = asserts;
        this.errorAsserts = asserts._errorAssertLines;
        this.testClassPath = asserts._testClasspath;
        this.testSrcPath = asserts._testSrcPath;
        this.testClassName = asserts._testClassname;
        this.targetFile = new File(FileUtils.getFileAddressOfJava(testSrcPath, testClassName));
        this.code = FileUtils.getCodeFromFile(testSrcPath, testClassName);
        this.line = line;
        this.commented = new ArrayList<>();
    }


    public void comment(){
        if (!errorAsserts.contains(line)){
            return;
        }
        if (errorAsserts.size() <=1){
            return;
        }
        if (line == -1){
            return;
        }
        backup();
        for (int commentLine: errorAsserts){
            if (commentLine == line){
                continue;
            }
            commented.add(commentLine);
            SourceUtils.commentCodeInSourceFile(targetFile, commentLine);
            if (LineUtils.isLineInFailBlock(code, commentLine)){
                int i = commentLine -1;
                String lineString = CodeUtils.getLineFromCode(code,i);
                while (!lineString.contains("try {")){
                    SourceUtils.commentCodeInSourceFile(targetFile, i);
                    i--;
                    lineString = CodeUtils.getLineFromCode(code, i);
                }
            }
        }
        isCommented = true;
        make();
    }


    public void uncomment(){
        if (isCommented){
            FileUtils.copyFile(classBackup.getAbsolutePath(), FileUtils.getFileAddressOfClass(testClassPath, testClassName));
            FileUtils.copyFile(javaBackup.getAbsolutePath(), FileUtils.getFileAddressOfJava(testSrcPath, testClassName));
        }
    }

    private void backup(){
        classBackup = new File(FileUtils.tempClassPath(testClassName,"AssertComment"));
        FileUtils.copyFile(FileUtils.getFileAddressOfClass(testClassPath, testClassName),classBackup.getAbsolutePath());

        javaBackup = new File(FileUtils.tempJavaPath(testClassName,"AssertComment"));
        FileUtils.copyFile(FileUtils.getFileAddressOfJava(testSrcPath, testClassName),javaBackup.getAbsolutePath());
    }

    private void make(){
        try {
            System.out.println(ShellUtils.shellRun(Arrays.asList("javac -Xlint:unchecked -source 1.6 -target 1.6 -cp "+ buildClasspath(Arrays.asList(PathUtils.getJunitPath())) +" -d "+ testClassPath+" "+ targetFile.getAbsolutePath())));
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private String buildClasspath(List<String> additionalPath){
        if (asserts._libPath.size()!=0){
            additionalPath = new ArrayList<>(additionalPath);
            additionalPath.addAll(asserts._libPath);
        }
        String path = "\"";
        path += asserts._classpath;
        path += System.getProperty("path.separator");
        path += asserts._testClasspath;
        path += System.getProperty("path.separator");
        path += StringUtils.join(additionalPath,System.getProperty("path.separator"));
        path += "\"";
        return path;
    }

}
