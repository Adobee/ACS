package cn.edu.pku.sei.plde.ACS.utils;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by yanrunfa on 16/6/3.
 */
public class AnnotationUtils {


    public static String getExceptionAnnotation(String code, String methodName, int methodLine, String exceptionName){
        Javadoc annotationDoc = CodeUtils.getMethodAnnotation(code, methodName, methodLine);
        if (annotationDoc == null){
            return "";
        }
        List<TagElement> annotationList = annotationDoc.tags();
        for (TagElement annotation: annotationList){
            if (annotation.getTagName()!=null && annotation.getTagName().equals("@throws")){
                if (annotation.fragments().get(0).toString().equals(exceptionName)){
                    String result = "";
                    List<ASTNode> fragements = annotation.fragments();
                    for (ASTNode node: fragements){
                        if (node instanceof TextElement){
                            result += node.toString() +" ";
                        }
                    }
                    return result.trim();
                }
            }
        }
        return "";
    }

    public static String getReturnAnnotation(String code, String methodName, int methodLine){
        Javadoc annotationDoc = CodeUtils.getMethodAnnotation(code, methodName, methodLine);
        if (annotationDoc == null){
            return "";
        }
        List<TagElement> annotationList = annotationDoc.tags();
        for (TagElement annotation: annotationList){
            if (annotation.getTagName()!=null && annotation.getTagName().equals("@return")){
                String result = "";
                List<ASTNode> fragements = annotation.fragments();
                for (ASTNode node: fragements){
                    if (node instanceof TextElement){
                        result += node.toString() +" ";
                    }
                }
                return result.trim();
            }
        }
        return "";
    }

    public static List<String> Parse(String text){
        List<String> keyword = new ArrayList<>();
        Parse[] parses = parse(text);
        Stack<Parse> parseStack= new Stack<>();
        for (Parse parse: parses){
            parseStack.push(parse);
        }
        while (!parseStack.isEmpty()){
            Parse parse = parseStack.pop();
            for (Parse subParse: parse.getChildren()){
                parseStack.push(subParse);
            }
            if (parse.getChildCount() >= 2){
                Parse one = parse.getChildren()[0];
                Parse two = parse.getChildren()[1];
                if (one.getType().equals("NP")
                        && (two.getType().equals("VP"))){
                    keyword.add(parse.getChildren()[0].toString());
                }
            }
        }
        return keyword;
    }

    private static Parse[] parse(String text){
        InputStream modelIn = null;
        try {
            modelIn = new FileInputStream(System.getProperty("user.dir")+"/NLPModel/en-parser-chunking.bin");
            ParserModel model = new ParserModel(modelIn);
            Parser parser = ParserFactory.create(model);
            Parse[] parses =  ParserTool.parseLine(text, parser, 1);
            return parses;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                }
                catch (IOException e) {
                }
            }
        }
        return null;
    }

    private static String[] tokenizer(String text){
        InputStream modelIn = null;
        try {
            modelIn = new FileInputStream(System.getProperty("user.dir")+"/NLPModel/en-token.bin");
            TokenizerModel model = new TokenizerModel(modelIn);
            Tokenizer tokenizer = new TokenizerME(model);
            return tokenizer.tokenize(text);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                }
                catch (IOException e) {
                }
            }
        }
        return null;
    }


    private static String[] posTagger(String[] sent){
        InputStream modelIn = null;
        try {
            modelIn = new FileInputStream(System.getProperty("user.dir")+"/NLPModel/en-pos-maxent.bin");
            POSModel model = new POSModel(modelIn);
            POSTaggerME tagger = new POSTaggerME(model);
            return tagger.tag(sent);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                }
                catch (IOException e) {
                }
            }
        }
        return null;
    }


    private static String[] chunking(String[] sent, String[] tag){
        InputStream modelIn = null;
        try {
            modelIn = new FileInputStream(System.getProperty("user.dir")+"/NLPModel/en-chunker.bin");
            ChunkerModel model = new ChunkerModel(modelIn);
            ChunkerME chunker = new ChunkerME(model);
            return chunker.chunk(sent, tag);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                }
                catch (IOException e) {
                }
            }
        }
        return null;
    }
}
