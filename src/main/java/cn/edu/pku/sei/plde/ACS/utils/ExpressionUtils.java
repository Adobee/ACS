package cn.edu.pku.sei.plde.ACS.utils;

import cn.edu.pku.sei.plde.ACS.jdtVisitor.ExpressionCollectVisitor;
import org.eclipse.jdt.core.dom.*;

import java.util.*;

/**
 * Created by yjxxtd on 4/24/16.
 */
public class ExpressionUtils {

    public static Set<String> getExpressionsInMethod(String source){
        ASTNode root = JDTUtils.createASTForSource(source, ASTParser.K_CLASS_BODY_DECLARATIONS);
        ExpressionCollectVisitor expressionCollectVisitor = new ExpressionCollectVisitor();
        root.accept(expressionCollectVisitor);
        Set<String> expressionSet = expressionCollectVisitor.getExpressionSet();
        Iterator<String> it = expressionSet.iterator();
        Set<String> removeSet = new HashSet<String>();

        while(it.hasNext()){
            String expr = it.next();
            if(expr.contains(">") || expr.contains("==") || expr.contains("<")){
                removeSet.add(expr);
            }
        }

        expressionSet.removeAll(removeSet);
        removeSet.clear();;

        it = expressionSet.iterator();
        while(it.hasNext()){
            String expr = it.next();
            Iterator<String> it2 = expressionSet.iterator();
            while(it2.hasNext()){
                String expr2 = it2.next();
                if(!expr.equals(expr2) && expr2.contains(expr)){
                    removeSet.add(expr);
                }
            }
        }

        expressionSet.removeAll(removeSet);
        removeSet.clear();

        it = expressionSet.iterator();
        while(it.hasNext()){
            String expr = it.next();
            if(expr.contains("=")){
                removeSet.add(expr);
            }
        }

        expressionSet.removeAll(removeSet);
        removeSet.clear();;


        return expressionCollectVisitor.getExpressionSet();
    }


    public static List<String> variablesInInfixExpression(Expression expression){
        if (expression instanceof InfixExpression){
            InfixExpression infixExpression = (InfixExpression) expression;
            List<String> lefts = variablesInInfixExpression(infixExpression.getLeftOperand());
            List<String> rights = variablesInInfixExpression(infixExpression.getRightOperand());
            if (((InfixExpression) expression).getOperator().toString().equals("||") ||
                    ((InfixExpression) expression).getOperator().toString().equals("&&")){
                //List<String> result = new ArrayList<>(lefts);
                //result.addAll(rights);
                //return result;
                return new ArrayList<>();
            }
            else {
                if (lefts.size()!=0 && rights.size() == 0){
                    return lefts;
                }
                if (rights.size()!=0 && lefts.size() == 0){
                    return rights;
                }
                return new ArrayList<>();
            }
        }
        if (expression instanceof MethodInvocation){
            MethodInvocation invocation = (MethodInvocation) expression;
            if (invocation.getExpression() != null){
                return Arrays.asList(invocation.getExpression().toString());
            }
        }
        if (expression instanceof SimpleName){
            return Arrays.asList(((SimpleName) expression).getIdentifier());
        }
        return new ArrayList<>();
    }
}
