package cn.edu.pku.sei.plde.conqueroverfitting.gatherer;

/**
 * Created by yjxxtd on 4/16/16.
 */


import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ThreadPoolHttpClientGithub {

    public ThreadPoolHttpClientGithub() {
    }

    public void fetch(String project, String packageName, List<String> urlList) {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();

        cm.setMaxTotal(10);
        CloseableHttpClient httpclient = HttpClients.custom()
                .setConnectionManager(cm)
                .build();
        try {
            GetThread[] getThreads = new GetThread[urlList.size()];
            for (int i = 0; i < urlList.size(); i++) {
                HttpGet get = new HttpGet(urlList.get(i));
                getThreads[i] = new GetThread(httpclient, get, project,packageName, i + 1);
            }

            for (GetThread gt : getThreads) {
                gt.start();
            }

            for (GetThread gt : getThreads) {
                gt.join();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class GetThread extends Thread {

        private final CloseableHttpClient httpClient;
        private final HttpContext context;
        private final HttpGet httpget;
        private final String project;
        private final String packageName;
        private final int id;

        public GetThread(CloseableHttpClient httpClient, HttpGet httpget, String project,String packageName, int id) {
            this.httpClient = httpClient;
            this.context = new BasicHttpContext();
            this.httpget = httpget;
            this.project = project;
            this.packageName = packageName;
            this.id = id;
        }

        /**
         * Executes the GetMethod and prints some status information.
         */
        @Override
        public void run() {
            try {
                //System.out.println(id + " - about to get something from " + httpget.getURI());
                CloseableHttpResponse response = httpClient.execute(httpget, context);
                try {
                    // get the response body as an array of bytes
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        writeFile("experiment//searchcode//" + packageName + "//" + id + ".java", EntityUtils.toString(entity));
                    }
                } finally {
                    response.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeFile(String fileName, String content) {
        try {
            File outputFile = new File(fileName);
            if (!outputFile.getParentFile().exists())
                outputFile.getParentFile().mkdirs();
            if (outputFile.exists())
                outputFile.delete();
            outputFile.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
            bw.write(content + "\r\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}