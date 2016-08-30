package cn.edu.pku.sei.plde.conqueroverfitting.gatherer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.Span;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;

public class GathererJavaGithub {
	private static final int API_PAGE_NUM = 9;

	private static final String API_SEARCH_CODE_BASE_URL = "https://github.com/search?l=java&";
	private static final String API_SEARCH_CODE_POST_URL = "&ref=searchresults&type=Code&utf8=✓";
	private static final String CODE_BASE_URL = "https://raw.githubusercontent.com";


	private static final int MAX_URL_NUM = 200;

	private HttpClient httpClient;

	private ArrayList<String> keyWords;
	private String project;
	private String packageName;

	public GathererJavaGithub(ArrayList<String> keyWords, String packageName,
			String project) {
		httpClient = new HttpClient();
		httpClient.getHttpConnectionManager().getParams()
				.setConnectionTimeout(50000);

		this.keyWords = keyWords;
		this.project = project;
		this.packageName = packageName;
	}

	public void searchCode() {
		String question = keyWords.get(0);
		for (int i = 1; i < keyWords.size(); i++) {
			question = question + "+" + keyWords.get(i);
		}

		ArrayList<String> codeUrlList = new ArrayList<String>();
		boolean flag = false;
		Random ra =new Random();
		ArrayList<String> pageUrlList = new ArrayList<String>();
		for (int i = 1; i <= API_PAGE_NUM; i++) {
			String url = API_SEARCH_CODE_BASE_URL + "p=" + i + "&q=" + question
					+ API_SEARCH_CODE_POST_URL;
			pageUrlList.add(url);
			System.out.println("search : " + url);
            ArrayList<String> urls = getCodeUrlList(url);
            if(urls.size() == 0){
				break;
            }


			try {
				int sleep = 7500;
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			codeUrlList.addAll(urls);
		}

//		try {
//			int sleep = 40000;
//			Thread.sleep(sleep);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}

		int size = codeUrlList.size();
//		for (int i = MAX_URL_NUM; i < size; i++) {
//			codeUrlList.remove(codeUrlList.size() - 1);
//		}
		//new ThreadPoolHttpClientGithubUrlList().fetch(pageUrlList);
		new ThreadPoolHttpClientGithub().fetch(project, packageName, codeUrlList);
	}

	public String getHtml(String url) {
		String html = null;
		GetMethod getMethod = new GetMethod(url);
		getMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 50000);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler());
		try {
			int statusCode = httpClient.executeMethod(getMethod);
			if (statusCode != HttpStatus.SC_OK)
				System.err.println("Method failed: "
						+ getMethod.getStatusLine());
			InputStream bodyIs = getMethod.getResponseBodyAsStream();
			BufferedReader br = new BufferedReader(
					new InputStreamReader(bodyIs));
			StringBuffer sb = new StringBuffer();
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			html = sb.toString();
			return html;
		} catch (HttpException e) {
			System.out.println("Please check your http address!");
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			getMethod.releaseConnection();
		}
	}

	private NodeList getNodeList(String _html, NodeFilter _filter) {
		Parser parser = Parser.createParser(_html, "utf8");
		NodeList node_list = null;
		try {
			node_list = parser.extractAllNodesThatMatch(_filter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return node_list;
	}

	public ArrayList<String> getCodeUrlList(String url) {
		String html = getHtml(url);
		ArrayList<String> codeUrlList = new ArrayList<String>();
		try {
			NodeFilter filter = new AndFilter(new TagNameFilter("p"),
					new HasAttributeFilter("class", "title"));
			NodeList nodeListCode = getNodeList(html, filter);
			NodeIterator iterator = nodeListCode.elements();
			while (iterator.hasMoreNodes()) {
				String str = iterator.nextNode().toHtml();
				filter = new NodeClassFilter(LinkTag.class);
				NodeList nodeListUrl = getNodeList(str, filter);
				for (Node node : nodeListUrl.toNodeArray()) {
					if (node instanceof LinkTag) {
						String link = ((LinkTag) node).getLink();
					    link = link.replace("blob/", "");
						if(link.contains(project)){
							continue;
						}
						if (link != null && link.contains(".java")) {
							codeUrlList.add(CODE_BASE_URL + link.toString());
						}
					}
				}
			}
		}

		catch (Exception e) {
			e.printStackTrace();
		}
		return codeUrlList;
	}
}
