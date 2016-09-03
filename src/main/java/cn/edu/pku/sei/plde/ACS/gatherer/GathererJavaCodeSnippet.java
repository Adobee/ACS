package cn.edu.pku.sei.plde.ACS.gatherer;

/**
 * Created by wangjie on 2016/5/21.
 */

import cn.edu.pku.sei.plde.ACS.file.WriteFile;
import cn.edu.pku.sei.plde.ACS.utils.FileUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class GathererJavaCodeSnippet {
    private static final int API_PAGE_NUM = 1;
    private static final int API_PER_PAGE = 100;
    private static final int API_CODE_LANGUAGE = 23;// java

    private static final String API_SEARCH_CODE_BASE_URL = "https://searchcode.com/api/";
    private static final String API_CODE_SEARCH = "codesearch_I";
    private static final String API_CODE_RESULT = "result";

    private static final int MAX_URL_NUM = 200;

    private HttpClient httpClient;

    private ArrayList<ArrayList<String>> keyWordsList = new ArrayList<>();

    private String project;
    private String packageName;

    private ArrayList<String> codeSnippets;

    public GathererJavaCodeSnippet(ArrayList<ArrayList<String>> keyWordsList, String packageName, String project) {
        httpClient = new HttpClient();
        httpClient.getHttpConnectionManager().getParams()
                .setConnectionTimeout(50000);

        this.keyWordsList = keyWordsList;
        this.project = project;
        this.packageName = packageName;

        codeSnippets = new ArrayList<String>();
    }


    public void searchCode() {
        for (List<String> keyWords: keyWordsList){
            String question = keyWords.get(0);
            for (int i = 1; i < keyWords.size(); i++) {
                question = question + "+" + keyWords.get(i);
            }

            for (int i = 0; i < API_PAGE_NUM; i++) {
                String url = API_SEARCH_CODE_BASE_URL + API_CODE_SEARCH + "/?q="
                        + question + "&p=" + i + "&per_page=" + API_PER_PAGE
                        + "&lan=" + API_CODE_LANGUAGE;
                System.out.println("search : " + url);

                try {
                    getCodeSnippets(url);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        try {
            // get the response body as an array of bytes
            for(int i = 0; i < codeSnippets.size(); i ++) {
                System.out.println("i = " + i);
                System.out.println(codeSnippets.get(i));
                WriteFile.writeFile("experiment//searchcode//" + packageName + "//" + i + ".java", codeSnippets.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //new ThreadPoolHttpClient().fetch(project, packageName, codeUrlList);
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
                System.err.println("Method failed: " + getMethod.getStatusLine());
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

    private void getCodeSnippets(String url) {
        String html = getHtml(url);

        try {
            JSONObject jsonObj = JSONObject.fromObject(html);
            JSONArray jsonArray = jsonObj.getJSONArray("results");
            for (int i = 0; i < jsonArray.size(); i++) {
                String repo = jsonArray.getJSONObject(i).getString("repo");
                System.out.println("repo " + repo);
                if (repo.contains(project)) {
                    continue;
                }
                String code = processCodeSnippet(jsonArray.getJSONObject(i).getString("lines"));
                if(code != null) {
                    codeSnippets.add(code);
                }
            }
        } catch (JSONException e) {
        }
    }

    private String processCodeSnippet(String codeSnippet){
        if(codeSnippet == null){
            return null;
        }
        //System.out.println("codeSnippet1 : " + codeSnippet);
        while(codeSnippet.contains("\\t")){
            codeSnippet = codeSnippet.replace("\\t", "");
        }
        //System.out.println("codeSnippet2 : " + codeSnippet);
        String code = "";
        String[] lines = codeSnippet.split(",\"");
        for(int i = 0; i < lines.length; i ++){
            String line = lines[i];
            int index1 = line.indexOf(":\"");
            int index2 = line.lastIndexOf("\"");
            //System.out.println("line : " + line);
            //System.out.println("index1 : " + index1);
            //System.out.println("index2 : " + index2);
            if(index1 + 2 < index2) {
                code = code + line.substring(index1 + 2, index2) + "\n";
            }
        }
       // System.out.println("code : " + code);
        return code;
    }
}
