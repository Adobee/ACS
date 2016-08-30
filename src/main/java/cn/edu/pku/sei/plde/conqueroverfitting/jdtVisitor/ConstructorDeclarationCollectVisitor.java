package cn.edu.pku.sei.plde.conqueroverfitting.jdtVisitor;

import cn.edu.pku.sei.plde.conqueroverfitting.localizationInConstructor.model.ConstructorDeclarationInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeInference;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.MethodInfo;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yjxxtd on 3/1/16.
 */
public class ConstructorDeclarationCollectVisitor extends ASTVisitor {
    private int[] lineCounter;
    private ArrayList<ConstructorDeclarationInfo> constructorDeclarationList;

    public ConstructorDeclarationCollectVisitor(int[] lineCounter) {
        constructorDeclarationList = new ArrayList<ConstructorDeclarationInfo>();
        this.lineCounter = lineCounter;
    }

    public ArrayList<ConstructorDeclarationInfo> getConstructorDeclarationList() {
        return constructorDeclarationList;
    }

    @Override
    public boolean visit(FieldDeclaration node) {
        return true;
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        if (node.getReturnType2() == null) {
            String constructorName = node.getName().toString();
            Block body = node.getBody();
            int parametersNum = node.parameters().size();
            int startPos = lineCounter[body.getStartPosition()];
            int endPos = lineCounter[body.getStartPosition() + body.getLength()];
            ConstructorDeclarationInfo constructorDeclarationInfo = new ConstructorDeclarationInfo(constructorName, parametersNum, startPos, endPos);
            constructorDeclarationList.add(constructorDeclarationInfo);
            return true;
        }

        return true;
    }


    @Override
    public boolean visit(VariableDeclarationStatement node) {
        return true;
    }

    @Override
    public boolean visit(InfixExpression node) {
        return true;
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
