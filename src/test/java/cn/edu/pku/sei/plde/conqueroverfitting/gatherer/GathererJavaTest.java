package cn.edu.pku.sei.plde.conqueroverfitting.gatherer;

import java.util.ArrayList;

import junit.framework.TestCase;
import cn.edu.pku.sei.plde.conqueroverfitting.log.Log;
import org.junit.Test;

public class GathererJavaTest extends TestCase {


	@Test
	public void testGathererJavaGithubRepositories() {
		GatherJavaGithubRepositories gatherJavaGithubRepositories = new GatherJavaGithubRepositories();
		gatherJavaGithubRepositories.searchCode(100);
	}

    @Test
	public void testGathererJavaGithub(){
		long timeStart = System.currentTimeMillis();
		ArrayList<String> keyWords = new ArrayList<String>();
		keyWords.add("if");
		keyWords.add("double");
		keyWords.add("factorial");
		//keyWords.add("Complex");
		GathererJavaGithubCodeSnippet gathererJava = new GathererJavaGithubCodeSnippet(keyWords, "if-double-factorial", "commons-math");
		//GathererJavaGithubCodeSnippet gathererJava = new GathererJavaGithubCodeSnippet(keyWords, "if-Complex", "commons-math");
		//new GathererJavaGithub(keyWords, "commons-math");Math
		//new GathererJavaGithub(keyWords, "commons-lang");Lang
		//new GathererJavaGithub(keyWords, "cGosure-compiler");Closure
		//new GathererJavaGithub(keyWords, "jfreechart");Chart
		//new GathererJavaGithub(keyWords, "joda-time");Time
		//GathererJavaGithub gathererJavaGithub = new GathererJavaGithub(keyWords, "if-Complex","math-complex");
		gathererJava.searchCode();
		long timeEnd = System.currentTimeMillis();
		//103s
		System.out.println("time = " + (timeEnd - timeStart)/1000 );
	}
}
