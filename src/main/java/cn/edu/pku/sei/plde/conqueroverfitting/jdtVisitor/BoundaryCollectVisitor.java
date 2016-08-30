package cn.edu.pku.sei.plde.conqueroverfitting.jdtVisitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.dom.*;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeInference;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;
import org.eclipse.osgi.framework.internal.core.SystemBundleActivator;

/**
 * ASTVisit that helps to generate a tree
 * 
 * @author jiewang
 */
public class BoundaryCollectVisitor extends ASTVisitor {
	private ArrayList<BoundaryInfo> boundaryInfoList;
	
	public BoundaryCollectVisitor() {
		boundaryInfoList = new ArrayList<BoundaryInfo>();
	}

	public ArrayList<BoundaryInfo> getBoundaryInfoList() {
		return boundaryInfoList;
	}
	
	@Override
	public boolean visit(FieldDeclaration node) {
		TypeInference typeInference = new TypeInference(node.getType().toString());
		for (Object obj : node.fragments()) {
			VariableDeclarationFragment v = (VariableDeclarationFragment) obj;
			String varName = v.getName().toString();
			Set<String> info = new HashSet<String>();
			if (v.getInitializer() != null) {
				BoundaryInfo boundaryInfo = new BoundaryInfo(typeInference.type,
						true, null, varName, v.getInitializer().toString(),
						info, 1, 1);
				boundaryInfoList.add(boundaryInfo);
			}
		}

		return true;
	}


	@Override
	public boolean visit(MethodDeclaration node) {
		return true;
	}
	
	@Override
	public boolean visit(InfixExpression node) {
		//System.out.println("node.toString = " + node.toString());
		//System.out.println("operator = " + node.getOperator() + " " + node.getOperator().equals(InfixExpression.Operator.EQUALS));

		if (node.getOperator().equals(InfixExpression.Operator.EQUALS) || node.getOperator().equals(
				InfixExpression.Operator.NOT_EQUALS)){
			collectBoundary(node.getLeftOperand(), node.getRightOperand(), 1, 1);
		}
		else if(node.getOperator().equals(InfixExpression.Operator.GREATER) || node.getOperator().equals(
				InfixExpression.Operator.LESS_EQUALS)){
			collectBoundary(node.getLeftOperand(), node.getRightOperand(), 0, 1);
		}
		else if(node.getOperator().equals(
						InfixExpression.Operator.GREATER_EQUALS)
				|| node.getOperator().equals(InfixExpression.Operator.LESS)) {
            collectBoundary(node.getLeftOperand(), node.getRightOperand(), 1, 0);
		}
		return true;
	}

	private void collectBoundary(Expression leftOper, Expression rightOper, int leftClose, int rightClose) {
		BoundaryInfo boundaryInfo = null;
		if (!(leftOper instanceof Name) && !leftOper.toString().contains("."))
			return;
		if (leftOper == null || rightOper == null)
			return;
		String leftOperStr = leftOper.toString();
		String rightOperStr = rightOper.toString();

		Set<String> info = new HashSet<String>();
		if (isNumeric(rightOperStr)) {
			//System.out.println("leftClose = " + leftClose + " rightClose = " + rightClose);
			boundaryInfo = new BoundaryInfo(TypeEnum.INT, true, null,
					leftOperStr, rightOperStr, info, leftClose, rightClose);
		} else if (rightOper instanceof StringLiteral) {
			boundaryInfo = new BoundaryInfo(TypeEnum.STRING, true,
					null, leftOperStr, rightOperStr, info, leftClose, rightClose);
		} else if (rightOper instanceof NullLiteral) {
			boundaryInfo = new BoundaryInfo(TypeEnum.NULL, true,
					null, leftOperStr, rightOperStr, info, leftClose, rightClose);
		} else if (rightOper instanceof CharacterLiteral) {
			boundaryInfo = new BoundaryInfo(TypeEnum.CHARACTER, true,
					null, leftOperStr, rightOperStr, info, leftClose, rightClose);
		} else if (rightOper instanceof BooleanLiteral) {
			boundaryInfo = new BoundaryInfo(TypeEnum.BOOLEAN, true,
					null, leftOperStr, rightOperStr, info, leftClose, rightClose);
		} else {
			collectConstOfSimpleType(leftOperStr, rightOperStr, leftClose, rightClose);
			return;
		}

		boundaryInfoList.add(boundaryInfo);
	}

	public boolean isNumeric(String str){
		if(str.charAt(0) == '-' || str.charAt(0) == '+'){
			str = str.substring(1);
		}
		for (int i = str.length();--i>=0;){
			if (!Character.isDigit(str.charAt(i))){
				return false;
			}
		}
		return true;
	}

	private void collectConstOfSimpleType(String leftOperStr,
			String rightOperStr, int leftClose, int rightClose) {
		int index = rightOperStr.lastIndexOf(".");
		if (index != -1) {

			BoundaryInfo boundaryInfo = null;
			Set<String> info = new HashSet<String>();
			info.add(leftOperStr);

			String prefix = rightOperStr.substring(0, index);
			if (prefix.equals("Integer")) {
				boundaryInfo = new BoundaryInfo(TypeEnum.INT, true,
						null, leftOperStr, rightOperStr, info, leftClose, rightClose);
				boundaryInfoList.add(boundaryInfo);
			} else if (prefix.endsWith("Long")) {
				boundaryInfo = new BoundaryInfo(TypeEnum.LONG, true,
						null, leftOperStr, rightOperStr, info, leftClose, rightClose);
				boundaryInfoList.add(boundaryInfo);
			}
		}
	}

	@Override
	public boolean visit(SimpleName node) {
		return true;
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		return true;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		return true;
	}

	@Override
	public boolean visit(TryStatement node) {
		return true;
	}

	@Override
	public boolean visit(NumberLiteral node) {
		return true;
	}

	@Override
	public boolean visit(StringLiteral node) {
		return true;
	}

	@Override
	public boolean visit(NullLiteral node) {
		return true;
	}

	@Override
	public boolean visit(CharacterLiteral node) {
		return true;
	}

	@Override
	public boolean visit(BooleanLiteral node) {
		return true;
	}

	@Override
	public boolean visit(TypeLiteral node) {
		return true;
	}

	@Override
	public boolean visit(ReturnStatement node) {
		return true;
	}

	@Override
	public boolean visit(PrefixExpression node) {
		return true;
	}

	@Override
	public boolean visit(ThisExpression node) {
		return true;
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		return true;
	}

	@Override
	public boolean visit(CastExpression node) {
		return true;
	}

	@Override
	public boolean visit(Modifier node) {
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		return true;
	}

	@Override
	public boolean visit(CompilationUnit node) {
		return true;
	}

	@Override
	public boolean visit(PrimitiveType node) {
		return true;
	}

	@Override
	public boolean visit(ArrayType node) {
		return true;
	}

	@Override
	public boolean visit(ArrayCreation node) {
		return true;
	}

	@Override
	public boolean visit(QualifiedName node) {
		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		return true;
	}

	@Override
	public boolean visit(Assignment node) {
		return true;
	}

	@Override
	public boolean visit(SimpleType node) {
		return true;
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		return true;
	}

	@Override
	public boolean visit(CatchClause node) {
		return true;
	}

	@Override
	public boolean visit(Block node) {
		return true;
	}

	@Override
	public boolean visit(ExpressionStatement node) {
		return true;
	}

	@Override
	public boolean visit(IfStatement node) {
		return true;
	}

	@Override
	public boolean visit(UnionType node) {
		return true;
	}

	@Override
	public boolean visit(TypeDeclarationStatement node) {
		return true;
	}

	@Override
	public boolean visit(TextElement node) {
		return false;
	}

	@Override
	public boolean visit(TagElement node) {
		return false;
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		return true;
	}

	@Override
	public boolean visit(QualifiedType node) {
		return true;
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		return true;
	}

	@Override
	public boolean visit(MethodRefParameter node) {
		return false;
	}

	@Override
	public boolean visit(MethodRef node) {
		return false;
	}

	@Override
	public boolean visit(MemberValuePair node) {
		return true;
	}

	@Override
	public boolean visit(MemberRef node) {
		return false;
	}

	@Override
	public boolean visit(LabeledStatement node) {
		return true;
	}

	@Override
	public boolean visit(LineComment node) {
		return false;
	}

	@Override
	public boolean visit(Javadoc node) {
		return false;
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		return true;
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		return true;
	}

	@Override
	public boolean visit(BlockComment node) {
		return false;
	}

	@Override
	public boolean visit(AssertStatement node) {
		return true;
	}

	@Override
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		return true;
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		return true;
	}

	@Override
	public boolean visit(ConstructorInvocation node) {
		return true;
	}

	@Override
	public boolean visit(ContinueStatement node) {
		return true;
	}

	@Override
	public boolean visit(WhileStatement node) {
		return true;
	}

	@Override
	public boolean visit(SynchronizedStatement node) {
		return true;
	}

	@Override
	public boolean visit(EmptyStatement node) {
		return true;
	}

	@Override
	public boolean visit(TypeParameter node) {
		return true;
	}

	@Override
	public boolean visit(ThrowStatement node) {
		return true;
	}

	@Override
	public boolean visit(WildcardType node) {
		return true;
	}

	@Override
	public boolean visit(ArrayAccess node) {
		return true;
	}

	@Override
	public boolean visit(DoStatement node) {
		return true;
	}

	@Override
	public boolean visit(Initializer node) {
		return true;
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		return true;
	}

	@Override
	public boolean visit(InstanceofExpression node) {
		return true;
	}

	@Override
	public boolean visit(PostfixExpression node) {
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationExpression node) {
		return true;
	}

	@Override
	public boolean visit(ForStatement node) {
		return true;
	}

	@Override
	public boolean visit(BreakStatement node) {
		return true;
	}

	@Override
	public boolean visit(SwitchCase node) {
		return true;
	}

	@Override
	public boolean visit(SwitchStatement node) {
		return true;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		return true;
	}

	@Override
	public boolean visit(SuperConstructorInvocation node) {
		return true;
	}

	@Override
	public boolean visit(ParameterizedType node) {
		return true;
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		return true;
	}

	@Override
	public boolean visit(ParenthesizedExpression node) {
		return true;
	}

	@Override
	public boolean visit(ConditionalExpression node) {
		return true;
	}

	@Override
	public boolean visit(FieldAccess node) {
		return true;
	}

	@Override
	public boolean visit(ArrayInitializer node) {
		return true;
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		return true;
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		return true;
	}
}
