package cn.edu.pku.sei.plde.ACS.localization.gzoltar;

import cn.edu.pku.sei.plde.ACS.localizationInConstructor.LocalizationInConstructor;
import cn.edu.pku.sei.plde.ACS.localizationInConstructor.model.ConstructorDeclarationInfo;
import cn.edu.pku.sei.plde.ACS.utils.CodeUtils;
import cn.edu.pku.sei.plde.ACS.utils.FileUtils;
import cn.edu.pku.sei.plde.ACS.utils.PathUtils;
import com.gzoltar.core.GZoltar;
import com.gzoltar.core.components.Clazz;
import com.gzoltar.core.components.Method;
import com.gzoltar.core.components.Statement;
import com.gzoltar.core.instr.testing.TestResult;
import cn.edu.pku.sei.plde.ACS.localization.metric.Metric;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * GZoltar wrapper, this wrapper supports different cn.edu.pku.sei.plde.ACS.localization.metric
 */
public class WGzoltar extends GZoltar {
    private Metric metric;
    private String testSrcPath;
    private String srcPath;
    private List<String> libPath;


    public WGzoltar(String wD, Metric metric, String testSrcPath, String srcPath, List<String> libPath) throws IOException {
        super(wD);
        this.metric = metric;
        this.testSrcPath = testSrcPath;
        this.srcPath = srcPath;
        this.libPath = libPath;
    }

    @Override
    public List<Statement> getSuspiciousStatements() {
        List<Statement> statements = new ArrayList<Statement>();
        for (StatementExt statementExt: getSuspiciousStatements(metric)){
            statements.add(statementExt);
        }
        return statements;
    }

    public List<StatementExt> getSuspiciousStatementExts() {
        return getSuspiciousStatements(metric);
    }

    private List<StatementExt> getSuspiciousStatements(Metric metric) {
        List<Statement> suspiciousStatements = super.getSuspiciousStatements();
        List<StatementExt> result = new ArrayList<StatementExt>(suspiciousStatements.size());
        List<Statement> constructors = new ArrayList<>();
        int successfulTests;
        int nbFailingTest = 0;
        for (int i = this.getTestResults().size() - 1 ; i >= 0; i--) {
            TestResult testResult = this.getTestResults().get(i);
            if(!testResult.wasSuccessful()) {
                System.out.println("Error test: "+testResult.getTrace().split("\n")[0]);
                if (
                        testResult.getTrace().split("\n")[0].contains("java.lang.NoClassDefFoundError:") ||
                        testResult.getTrace().split("\n")[0].contains("java.lang.ExceptionInInitializerError") ||
                        testResult.getTrace().split("\n")[0].contains("java.lang.IllegalAccessError")
                        ){
                    testResult.setSuccessful(true);
                }
                else {
                    nbFailingTest++;
                    suspiciousStatements.addAll(statementFromTestResult(testResult,i, constructors));
                }
            }
        }

        successfulTests = this.getTestResults().size() - nbFailingTest;
        for (int i = suspiciousStatements.size() - 1 ; i >= 0; i--) {
            Statement statement = suspiciousStatements.get(i);
            BitSet coverage = statement.getCoverage();
            int executedAndPassedCount = 0;
            int executedAndFailedCount = 0;
            int nextTest = coverage.nextSetBit(0);
            StatementExt s = new StatementExt(statement, metric);

            while(nextTest != -1) {
                TestResult testResult = this.getTestResults().get(nextTest);
                if(testResult.wasSuccessful()) {
                    executedAndPassedCount++;
                } else {
                    executedAndFailedCount++;
                }
                s.addTest(testResult.getName());
                if (!testResult.wasSuccessful()){
                    s.addFailTest(testResult.getName());
                }
                nextTest = coverage.nextSetBit(nextTest + 1);
            }
            s.setEf(executedAndFailedCount);
            s.setEp(executedAndPassedCount);
            s.setNp(successfulTests - executedAndPassedCount);
            s.setNf(nbFailingTest - executedAndFailedCount);
            //recordTestResults(s,outputStream);
            result.add(s);
        }
        Collections.sort(result, new Comparator<Statement>() {

            public int compare(final Statement o1, final Statement o2) {
                // reversed parameters because we want a descending order list
                if (o2.getSuspiciousness() == o1.getSuspiciousness()){
                    return Integer.compare(o2.getLineNumber(),o1.getLineNumber());
                }
                return Double.compare(o2.getSuspiciousness(), o1.getSuspiciousness());
            }
        });
            //outputStream.close();
        //}catch (IOException e){
        //    System.out.println("ERROR");
        //}
        return result;
    }

    private List<Statement> statementFromTestResult(TestResult testResult, int i, List<Statement> histories){
        List<Statement> result = new ArrayList<>();
        String classname = testResult.getName().split("#")[0];
        if (classname.contains("$")){
            classname = classname.substring(0, classname.lastIndexOf("$"));
        }
        String functionName = testResult.getName().split("#")[1];
        if (!new File(FileUtils.getFileAddressOfJava(testSrcPath, classname)).exists()){
            return result;
        }
        LocalizationInConstructor constructor = new LocalizationInConstructor(srcPath, FileUtils.getFileAddressOfJava(testSrcPath, classname), functionName);
        HashMap<String, ArrayList<ConstructorDeclarationInfo>> constructMap = constructor.getConstructorMap();
        if (constructMap.size() == 0){
            String code = FileUtils.getCodeFromFile(testSrcPath, classname);
            String functionCode = FileUtils.getTestFunctionCodeFromCode(code, functionName);
            if (functionCode.equals("")){
                return result;
            }
            for (String line: functionCode.split("\n")){
                if (!line.contains("(") || !line.contains(")") || line.contains("=")){
                    continue;
                }
                String callMethod = line.substring(0, line.indexOf("(")).trim();
                if (code.contains("void "+callMethod+"(")){
                    constructor = new LocalizationInConstructor(srcPath, FileUtils.getFileAddressOfJava(testSrcPath, classname), callMethod);
                    constructMap = constructor.getConstructorMap();
                    break;
                }
            }
        }
        for (String key: constructMap.keySet()){
            String packageName = PathUtils.getPackageNameFromPath(key);
            int paramCount;
            try {
                paramCount = CodeUtils.countParamsOfConstructorInTest(FileUtils.getFileAddressOfJava(testSrcPath, classname), functionName, packageName.substring(packageName.lastIndexOf(".")+1));
            } catch (Exception e){
                paramCount = -1;
            }
            ArrayList<ConstructorDeclarationInfo> constructors = constructMap.get(key);
            if (constructors.size() == 0){
                continue;
            }
            for (int s=0; s< constructors.size(); s++){
                ConstructorDeclarationInfo info = constructors.get(s);
                Clazz clazz = new Clazz(packageName);
                clazz.setSource(key.substring(key.lastIndexOf(PathUtils.getFileSeparator())+1));
                Method method = new Method(clazz, info.methodName+"("+info.parameterNum+")");
                Statement statement = new Statement(method,info.endPos);
                statement.setCount(i,1);
                statement.setCoverage(i);
                int flag = 0;
                for (Statement history: histories){
                    if (history.getLabel().equals(statement.getLabel())){
                        history.setCoverage(i);
                        history.setCount(i,1);
                        flag = 1;
                    }
                }
                if (flag == 1){
                    continue;
                }
                result.add(statement);
            }

        }
        histories.addAll(result);
        return result;
    }




}
