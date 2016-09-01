package cn.edu.pku.sei.plde.ACS.gatherer;

import cn.edu.pku.sei.plde.ACS.main.Config;
import cn.edu.pku.sei.plde.ACS.utils.FileUtils;
import cn.edu.pku.sei.plde.ACS.utils.RequestUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpResponse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanrunfa on 8/8/16.
 */
public class GatherJavaGithubRepositories {

    private static final String searchURL = "https://github.com/search?o=desc&q=language%3AJava&ref=searchresults&s=stars&type=Repositories&utf8=✓";

    private HttpClient httpClient;

    public GatherJavaGithubRepositories(){
        this.httpClient = new HttpClient();
        httpClient.getHttpConnectionManager().getParams()
                .setConnectionTimeout(1000000);
    }

    public void searchCode(int topNum){
        List<String> repositoriesURL = getRepositoriesURL(topNum);
        for (String url: repositoriesURL){
            System.out.println("Downloading Repository: "+url);
            downloadRepository(url);
        }
    }
    private String getRepositoryDownloadURL(String url){
        if (!url.startsWith("https://github.com")){
            url = "https://github.com"+url;
        }
        String xpath = "//a[contains(text(),\"Download ZIP\")]/@href";
        String repositoryHTML = RequestUtils.getHtml(url, httpClient);
        if (repositoryHTML == null){
            return null;
        }
        List<String> urls = RequestUtils.xpath(repositoryHTML, xpath);
        if (urls.size() < 1){
            return null;
        }
        return "https://github.com"+urls.get(0);
    }


    private List<String> getRepositoriesURL(int topNum){
        List<String> result = new ArrayList<>();
        int page = 1;
        while (true){
            String searchHTML = RequestUtils.getHtml(getSearchURL(page), httpClient);
            if (searchHTML == null){
                continue;
            }
            List<String> urls = RequestUtils.xpath(searchHTML, "//h3[@class=\"repo-list-name\"]/a/@href");
            for (String url: urls){
                result.add(url);
                System.out.println("Searched URL: https://github.com"+url);
                if (result.size() == topNum){
                    break;
                }
            }
            if (result.size() == topNum){
                break;
            }
            page++;
        }
        return result;
    }

    private void downloadRepository(String url){
        String downloadURL = getRepositoryDownloadURL(url);
        String repositoryName = url.substring(1).replace("/","-");
        String repositoryPath = System.getProperty("user.dir")+"/experiment/searchRepository/"+repositoryName+"/";
        if (new File(repositoryPath).exists() && new File(repositoryPath).list().length != 0){
            System.out.println("Existed Repository: "+repositoryName);
            return;
        }
        else {
            new File(repositoryPath).mkdirs();
        }
        File zipPackage = RequestUtils.download(downloadURL, Config.TEMP_FILES_PATH+repositoryName+".zip", httpClient);
        if (zipPackage == null){
            System.out.println("Failed to Download Repository: "+ repositoryName);
            new File(repositoryPath).delete();
            return;
        }
        try {
            FileUtils.unzip(zipPackage.getAbsolutePath(), repositoryPath);
            System.out.println("Unzip Repository Success: "+ repositoryPath);
        } catch (IOException e){
            System.out.println("Unzip Repository Fail: "+ repositoryPath);
            e.printStackTrace();
        }
    }

    private String getSearchURL(int page){
        return "https://github.com/search?l=&p="+page+"&q=language%3AJava&ref=advsearch&type=Repositories&utf8=%E2%9C%93";
    }
}
