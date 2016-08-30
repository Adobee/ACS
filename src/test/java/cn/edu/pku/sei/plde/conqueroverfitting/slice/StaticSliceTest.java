package cn.edu.pku.sei.plde.conqueroverfitting.slice;

import static org.junit.Assert.*;

import cn.edu.pku.sei.plde.conqueroverfitting.jdtVisitor.IdentifierCollectVisitor;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.JDTUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.junit.Test;

import java.util.List;

/**
 * Created by jiewang on 2016/2/23.
 */
public class StaticSliceTest {

	@Test
	public void testStaticSlice() {
		String statements = "final double[] a = {1.23456789};\nfinal double[] b = {98765432.1};\nfinal double[] c = {0.0};\n";
		String expression = "a[0] * b[0]";
		StaticSlice staticSlice = new StaticSlice(statements, expression);
		String sliceStatements = staticSlice.getSliceStatements();
		String expectedSliceStatements = "final double[] a = {1.23456789};\nfinal double[] b = {98765432.1};\n";
		assertTrue(sliceStatements.equals(expectedSliceStatements));
	}

	@Test
	public void testVariableCollect() {


	}
}
