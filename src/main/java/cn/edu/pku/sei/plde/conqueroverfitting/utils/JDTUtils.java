package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

import java.util.Map;

/**
 * Created by yjxxtd on 2/29/16.
 */
public class JDTUtils {

    public static int[] getLineCounter(String source) {
        int[] lineCounter = new int[source.length()];
        int i = 0;
        lineCounter[0] = 1;
        int CurrentLine = 1;
        for (i = 0; i < source.length(); i++) {
            if (source.charAt(i) == '\r') {
                lineCounter[i] = CurrentLine;
                lineCounter[i + 1] = CurrentLine;
                CurrentLine++;
                i = i + 1;
                continue;
            } else {
                lineCounter[i] = CurrentLine;
            }
        }
        return lineCounter;
    }

    public static ASTNode createASTForSource(String source, int kind) {

        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setSource(source.toCharArray());
        parser.setKind(kind);
        parser.setResolveBindings(true);
        Map options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
        parser.setCompilerOptions(options);
        return parser.createAST(null);
    }
}
