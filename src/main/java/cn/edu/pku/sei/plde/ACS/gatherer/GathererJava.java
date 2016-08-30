package cn.edu.pku.sei.plde.ACS.gatherer;

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
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class GathererJava {
    private static final int API_PAGE_NUM = 2;
    private static final int API_PER_PAGE = 100;
    private static final int API_CODE_LANGUAGE = 23;// java

    private static final String API_SEARCH_CODE_BASE_URL = "https://searchcode.com/api/";
    private static final String API_CODE_SEARCH = "codesearch_I";
    private static final String API_CODE_RESULT = "result";

    private static final int MAX_URL_NUM = 200;

    private HttpClient httpClient;

    private ArrayList<String> keyWords;
    private String project;
    private String packageName;

    public GathererJava(ArrayList<String> keyWords, String packageName, String project) {
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
        for (int i = 0; i < API_PAGE_NUM; i++) {
            String url = API_SEARCH_CODE_BASE_URL + API_CODE_SEARCH + "/?q="
                    + question + "&p=" + i + "&per_page=" + API_PER_PAGE
                    + "&lan=" + API_CODE_LANGUAGE;
            System.out.println("search : " + url);

            try {
                codeUrlList.addAll(getCodeUrlList(url));
            } catch (Exception e){
                e.printStackTrace();
            }

        }
//        int size = codeUrlList.size();
//        for(int i = MAX_URL_NUM; i < size; i ++){
//            codeUrlList.remove(codeUrlList.size() - 1);
//        }
        new ThreadPoolHttpClient().fetch(project, packageName, codeUrlList);
    }

    public String getHtml(String url) {
        String html = null;
        GetMethod getMethod = new GetMethod(url);
        getMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 50000);
        getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler());
        InputStream bodyIs = null;
        BufferedReader br = null;
        try {
            int statusCode = httpClient.executeMethod(getMethod);
            if (statusCode != HttpStatus.SC_OK)
                System.err.println("Method failed: " + getMethod.getStatusLine());
            bodyIs = getMethod.getResponseBodyAsStream();
            br = new BufferedReader(
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
        }
        finally {
            getMethod.releaseConnection();
            if (bodyIs != null){
                try {
                    bodyIs.close();
                } catch (IOException e){
                }
            }
            if (br != null){
                try {
                    br.close();
                } catch (IOException e){
                }
            }

        }
    }

    public ArrayList<String> getCodeUrlList(String url) {
        String html = getHtml(url);
        ArrayList<String> codeUrlList = new ArrayList<String>();
        try {
            JSONObject jsonObj = JSONObject.fromObject(html);
            JSONArray jsonArray = jsonObj.getJSONArray("results");
            for (int i = 0; i < jsonArray.size(); i++) {
                String repo = jsonArray.getJSONObject(i).getString("repo");
                System.out.println("repo " + repo);
                if(repo.contains(project)){
                    continue;
                }
                String id = jsonArray.getJSONObject(i).getString("id");
                String codeUrl = API_SEARCH_CODE_BASE_URL + API_CODE_RESULT + "/" + id + "/";
                System.out.println("result : " + codeUrl);
                codeUrlList.add(codeUrl);
            }
        } catch (JSONException e){
        }
        return codeUrlList;
    }
}
