package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanrunfa on 8/8/16.
 */
public class RequestUtils {

    public static String getHtml(String url, HttpClient httpClient) {
        String html = null;
        GetMethod getMethod = new GetMethod(url);
        getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler());
        InputStream bodyIs = null;
        BufferedReader br = null;
        try {
            int statusCode = httpClient.executeMethod(getMethod);
            if (statusCode != HttpStatus.SC_OK){
                System.err.println("Method failed: " + getMethod.getStatusLine());
                while (statusCode == 429){
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e){
                        getMethod.releaseConnection();
                        e.printStackTrace();
                        break;
                    }
                    statusCode = httpClient.executeMethod(getMethod);
                }
            }

            bodyIs = getMethod.getResponseBodyAsStream();
            br = new BufferedReader(
                    new InputStreamReader(bodyIs));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            html = sb.toString();
            bodyIs.close();
            br.close();
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
            if (bodyIs != null){
                try {
                    bodyIs.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
            if (br != null){
                try {
                    br.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static File download(String url, String filePath, HttpClient httpClient){
        GetMethod getMethod = new GetMethod(url);
        getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler());
        InputStream bodyIs = null;
        BufferedReader br = null;
        try {
            int statusCode = httpClient.executeMethod(getMethod);

            if (statusCode != HttpStatus.SC_OK){
                System.err.println("Method failed: " + getMethod.getStatusLine());
                while (statusCode == 429){
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e){
                        getMethod.releaseConnection();
                        e.printStackTrace();
                        break;
                    }
                    statusCode = httpClient.executeMethod(getMethod);
                }
            }
            bodyIs = getMethod.getResponseBodyAsStream();
            FileOutputStream out = new FileOutputStream(new File(filePath));
            br = new BufferedReader(
                    new InputStreamReader(bodyIs));
            byte[] b = new byte[1024];
            int len = 0;
            while((len=bodyIs.read(b))!= -1){
                out.write(b,0,len);
            }
            bodyIs.close();
            out.close();
            return new File(filePath);
        } catch (HttpException e) {
            System.out.println("Please check your http address!");
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            getMethod.releaseConnection();
            if (bodyIs != null){
                try {
                    bodyIs.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
            if (br != null){
                try {
                    br.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }


    public static List<String> xpath(String html, String xpathString){
        List<String> result = new ArrayList<>();
        try {

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            XPathExpression expression = xpath.compile(xpathString);

            W3CDom helper = new W3CDom();
            Document document = helper.fromJsoup(Jsoup.parse(html));
            NodeList list = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
            for (int i=0; i< list.getLength(); i++){
                result.add(list.item(i).getNodeValue());
            }
        } catch (XPathExpressionException e){
            e.printStackTrace();
        }
        return result;
    }
}
