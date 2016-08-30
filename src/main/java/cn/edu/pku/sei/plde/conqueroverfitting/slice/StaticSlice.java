package cn.edu.pku.sei.plde.conqueroverfitting.slice;

import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.sei.plde.conqueroverfitting.jdtVisitor.IdentifierCollectVisitor;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.CodeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.JDTUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

/**
 * Created by jiewang on 2016/2/23.
 */
public class StaticSlice {

	private String statements;
	private String expression;
	private String sliceStatements;
	
	public StaticSlice(String statements, String expression){
		this.statements = statements;
		this.expression = expression;
		
		slice();
	}
	
	private void slice(){
		sliceStatements = "";
		//List<String> identifierInExpression = new JDTParse(expression, ASTParser.K_EXPRESSION).getIdentifierList();
		List<String> identifierInExpression = getIdentifierList(expression, ASTParser.K_EXPRESSION);

		String[] statementsArray = statements.split("\n");
		for(String statement : statementsArray){
			//List<String> identifierInStatement = new JDTParse(statement, ASTParser.K_STATEMENTS).getIdentifierList();
			List<String> identifierInStatement = getIdentifierList(statement, ASTParser.K_STATEMENTS);
			identifierInStatement.retainAll(identifierInExpression);
			if(identifierInStatement.size() > 0){
				sliceStatements += statement;
				sliceStatements += "\n";
			}
		}
		
	}

	private ArrayList<String> getIdentifierList(String source, int kind){
		ASTNode root = JDTUtils.createASTForSource(source, kind);
		IdentifierCollectVisitor identifierCollectVisitor = new IdentifierCollectVisitor();
		root.accept(identifierCollectVisitor);
		return identifierCollectVisitor.getIdentifierList();
	}

	public String getSliceStatements(){
		String addon = "";
		if (statements.replace(" ","").contains(expression+"={")){
			int bracketCount = 0;
			boolean uncompleteLine = false;
			for (String line: statements.split("\n")){
				if (line.replace(" ","").contains(expression+"=")||uncompleteLine){
					bracketCount += CodeUtils.countChar(line, '(');
					bracketCount -= CodeUtils.countChar(line, ')');
					addon += line+"\n";
					uncompleteLine = true;
					if (bracketCount == 0 && line.contains(";")){
						break;
					}
				}
			}
		}
		return addon + sliceStatements.toString();
	}
}
