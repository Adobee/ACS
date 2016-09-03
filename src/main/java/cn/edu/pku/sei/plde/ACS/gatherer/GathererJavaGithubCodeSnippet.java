package cn.edu.pku.sei.plde.ACS.gatherer;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

import cn.edu.pku.sei.plde.ACS.file.WriteFile;
import cn.edu.pku.sei.plde.ACS.utils.FileUtils;
import cn.edu.pku.sei.plde.ACS.utils.RequestUtils;
import org.apache.commons.httpclient.*;
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

public class GathererJavaGithubCodeSnippet {
    private static final int API_PAGE_NUM = 8;

    private static final String API_SEARCH_CODE_BASE_URL = "https://github.com/search?l=java&";
    private static final String API_SEARCH_CODE_POST_URL = "&ref=searchresults&type=Code&utf8=✓";
    private static final String CODE_BASE_URL = "https://raw.githubusercontent.com";


    private static final int MAX_URL_NUM = 200;

    private HttpClient httpClient;

    private ArrayList<String> keyWords;
    private String project;
    private String packageName;

    private ArrayList<String> codeSnippets;

    public GathererJavaGithubCodeSnippet(ArrayList<String> keyWords, String packageName,
                                         String project) {
        httpClient = new HttpClient();
        httpClient.getHttpConnectionManager().getParams()
                .setConnectionTimeout(50000);

        this.keyWords = keyWords;
        this.project = project;
        this.packageName = packageName;

        codeSnippets = new ArrayList<String>();
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
            getCodeSnippets(url);


//            try {
//                int sleep = 7500;
//                Thread.sleep(sleep);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            //codeUrlList.addAll(urls);
        }

        try {
            // get the response body as an array of bytes
            for(int i = 0; i < codeSnippets.size(); i ++) {
                //System.out.println("i = " + i);
                System.out.println(codeSnippets.get(i));
                WriteFile.writeFile("experiment//searchcode//" + packageName + "//" + i + ".code", codeSnippets.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        httpClient.getHttpConnectionManager().closeIdleConnections(0);
        File searchResult = new File("experiment//searchcode//" + packageName);
        if (searchResult.exists() && searchResult.list().length == 0){
            searchResult.delete();
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

    public void getCodeSnippets(String url) {
        String html = RequestUtils.getHtml(url, httpClient);
        try {
            NodeFilter filter = new AndFilter(new TagNameFilter("div"),
                    new HasAttributeFilter("class", "file-box blob-wrapper"));
            NodeList nodeListCode = getNodeList(html, filter);
            NodeIterator iterator = nodeListCode.elements();
            while (iterator.hasMoreNodes()) {
                String str = iterator.nextNode().toHtml();
                filter = new AndFilter(new TagNameFilter("td"),
                        new HasAttributeFilter("class", "blob-code blob-code-inner"));
                NodeList nodeListLine = getNodeList(str, filter);
                NodeIterator iterator2 = nodeListLine.elements();
                StringBuilder code = new StringBuilder();
                while(iterator2.hasMoreNodes()){
                    String str2 = iterator2.nextNode().toPlainTextString().trim();
                    while(str2.contains("&gt;")){
                        str2 = str2.replace("&gt;", ">");
                    }
                    while(str2.contains("&lt;")){
                        str2 = str2.replace("&lt;", "<");
                    }
                    code = code.append(str2 + "\n");
                }
                boolean flag = false;
                filter = new NodeClassFilter(LinkTag.class);
                NodeList nodeListUrl = getNodeList(str, filter);
                for (Node node : nodeListUrl.toNodeArray()) {
                    if (node instanceof LinkTag) {
                        String link = ((LinkTag) node).getLink();
                        link = link.replace("blob/", "");
                        if(link.contains(project)){
                            //continue;
                            flag = true;
                            break;
                        }
                    }
                }
                if(!flag){
                    codeSnippets.add(code.toString());
                }
                else{
                    System.out.println("code " + code);
                }
            }
        }

        catch (Exception e) {
            e.printStackTrace();
        }
        // return codeUrlList;
    }
}