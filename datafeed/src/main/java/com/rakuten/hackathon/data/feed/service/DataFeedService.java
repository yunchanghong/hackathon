package com.rakuten.hackathon.data.feed.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rakuten.hackathon.data.feed.api.CasApiClient;
import com.rakuten.hackathon.data.feed.api.DataFeedApiClient;
import com.rakuten.hackathon.data.feed.dto.DES;
import com.rakuten.hackathon.data.feed.dto.User;
import com.rakuten.hackathon.data.feed.repository.UserRepository;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.java.net.cookiejar.JavaNetCookieJar;


@Service
public class DataFeedService {

    @Autowired
    UserRepository userRepository;

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 获取重定向地址
     * 
     * @param path
     * @return
     * @throws Exception
     */
    private static String getRedirectUrl(String path) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(path).openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.setConnectTimeout(5000);
        return conn.getHeaderField("Location");
    }
    
    public void execute() throws Exception {
        List<User> userList = userRepository.findAll();
        
        userList.forEach(item -> {
            System.out.println("Item=>" + item.getUserid());
        });
        try {
            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
            
            OkHttpClient client = new OkHttpClient.Builder()
                    .cookieJar(new JavaNetCookieJar(cookieManager))
                    .build();
            
            OkHttpClient clientRedirect = new OkHttpClient.Builder()
                    .cookieJar(new JavaNetCookieJar(cookieManager))
                    .followRedirects(false)
                    .followSslRedirects(false)
                    .build();
        
        
        //1. Retrieve Login CallBack URL
        String loginUrl = CasApiClient.getLoginCallbackUrl(client);
        
        //2. Retrieve Service URL from Header location
        String serviceUrl = CasApiClient.extractServiceUrl(loginUrl);
        
        //3. Extract Execution and Ticket
        Response response = CasApiClient.extractExecutionAndTicket(client, serviceUrl);
        String htmlDoc = response.body().string();
        Document serviceDoc = Jsoup.parse(htmlDoc);
        Elements eles = serviceDoc.select("input");
        String execution = null;
        String ticket = null;
        for (Element input : eles) {
            if ("execution".equals(input.attr("name"))) {
                execution = input.val();
            } 
            
            if ("lt".equals(input.attr("name"))) {
                ticket = input.val();
            }
            System.out.println(input.attr("name") + "-" + input.val());
        }
        
        String studentId = "xxxx";
        String password = "yyyyy";
        System.out.println(studentId+password+ticket);
        String rsa = DES.strEnc(studentId+password+ticket, "1", "2", "3");
        System.out.println(rsa);
        
        HashMap<String, String> map = new HashMap<>();
        map.put("rsa", rsa);
        map.put("ul", studentId.length() + "");
        map.put("pl", password.length() + "");
        map.put("lt", ticket);
        map.put("execution", execution);
        map.put("_eventId", "submit");
        
        //4. Retrieve ticket ID
        String ticketId = CasApiClient.getTicketId(clientRedirect, map, response.header("Set-Cookie"));
        
        //5. Trigger Access
        CasApiClient.triggerAccess(ticketId, clientRedirect);
        
        //6. Trigger Main
        CasApiClient.triggerMainPage(client);
        
        DataFeedApiClient.initializeClient(5, 10, 20, 80, true);
        //Test APIs
          String homePage = "https://i.dlufl.edu.cn/dcp/view?m=up";
          Request requestHomePage = new Request.Builder()
                  .url(homePage)
                  .build();
          Response responseHome = client.newCall(requestHomePage).execute();
          System.out.println("========================================");
          System.out.println(responseHome.body().string());

          
          String newActivityUrl = "https://i.dlufl.edu.cn/dcp/up/subgroup/hotActivity";
          Request newActivityPost = new Request.Builder()
                  .url(newActivityUrl)
                  .post(RequestBody.create("{\"eventsType\":\"e\"}", DataFeedApiClient.JSON))
                  .build();
          Response responseNewActivity = client.newCall(newActivityPost).execute();
          System.out.println(responseNewActivity.body().string());
          
          
          String cardMoneyUrl = "https://i.dlufl.edu.cn/dcp/up/subgroup/getCardMoney";
          Request cardMoneyUrlPost = new Request.Builder()
                  .url(cardMoneyUrl)
                  .post(RequestBody.create("{}", DataFeedApiClient.JSON))
                  .build();
          Response cardMoneyUrlResponse = client.newCall(cardMoneyUrlPost).execute();
          
          String data = cardMoneyUrlResponse.body().string();
          System.out.println(data);
          String prompt = "This is the data of the campus card, flag is the status of the campus card, success means it can be used normally, cardbal is the balance value of the campus card, the unit is RMB, card is the university card, each time the latest cardbal is queried, it is displayed as the balance of the card \n";
          long curTime = new Date().getTime();
          String fileName = "MyInfo_" + curTime
                  + ".txt";
          try {
              FileWriter writer = new FileWriter(fileName);
              writer.write(prompt + data);
              writer.close();
              System.out.println("文件创建成功！");
          } catch (IOException e) {
              System.out.println("创建文件时出现错误：" + e.getMessage());
          }
          String jsonConfig = "{\"configurable\":{\"assistant_id\":\"a1ffffda-5f5b-4c15-bf9d-f9550cad14be\"}}";
          Map<String, Object> formBodyMap = new HashMap<>();
          formBodyMap.put("files", new File(fileName));
          formBodyMap.put("config", jsonConfig);
          Map<String, String> headerMap = new HashMap<>();
          DataFeedApiClient.postMultipart("http://localhost:8100/ingest", formBodyMap, headerMap,
                  MediaType.get("text/plain"));
        
        } catch (Exception e) {
            e.printStackTrace();
        }

        Document doc = Jsoup.connect("https://news.dlufl.edu.cn/").get();
        System.out.println(doc.title());
        List<String> newsList = new ArrayList<>();
        Elements newsHeadlines = doc.select(".index-news .list2 .tab-box li");
        for (Element headline : newsHeadlines) {
            Element subA = headline.child(1).child(0);
            Element subDiv = headline.child(0);
            String newDateStr = subDiv.child(1).html() + "." + subDiv.child(0).html();
            String newsHref = subA.attr("href");
            if (!newsHref.contains("http")) {
                newsHref = "https://news.dlufl.edu.cn/" + newsHref;
            }

            if (!newsHref.contains("dlufl")) {
                continue;
            }

            Date newsDate = sdf.parse(newDateStr.replace(".", "-"));

            if (newsDate.after(DateUtils.addDays(new Date(), -4))) {
                newsList.add(newsHref);
            }
            System.out.println(newsHref);
            System.out.println(newsDate.toLocaleString());
        }

        for (String currentNews : newsList) {
            Document currentDoc = Jsoup.connect(currentNews).get();
            if (currentNews.contains("news")) {
                Element title = currentDoc.select(".nry_bt .bt").first();
                System.out.println(title.html());
                String fileName = currentNews.substring(currentNews.lastIndexOf("/") + 1, currentNews.length())
                        + ".txt";
                try {

                    FileWriter writer = new FileWriter(fileName);
                    writer.write(title.html());
                    writer.close();
                    System.out.println("文件创建成功！");
                } catch (IOException e) {
                    System.out.println("创建文件时出现错误：" + e.getMessage());
                }

                String jsonConfig = "{\"configurable\":{\"assistant_id\":\"a1ffffda-5f5b-4c15-bf9d-f9550cad14be\"}}";

                Map<String, Object> formBodyMap = new HashMap<>();
                formBodyMap.put("files", new File(fileName));
                formBodyMap.put("config", jsonConfig);
                Map<String, String> headerMap = new HashMap<>();
//               , Map<String, String> headerMap, MediaType fileMediaType
//               formBodyMap.put("item", fileMediaType)
                DataFeedApiClient.postMultipart("http://localhost:8100/ingest", formBodyMap, headerMap,
                        MediaType.get("text/plain"));

            } else {
                Element title = currentDoc.select(".news-title").first();
                System.out.println(title.child(0).html());
                String fileName = currentNews.substring(currentNews.lastIndexOf("/") + 1, currentNews.length())
                        + ".txt";
                try {
                    FileWriter writer = new FileWriter(fileName);
                    writer.write(title.html());
                    writer.close();
                    System.out.println("文件创建成功！");
                } catch (IOException e) {
                    System.out.println("创建文件时出现错误：" + e.getMessage());
                }
                String jsonConfig = "{\"configurable\":{\"assistant_id\":\"a1ffffda-5f5b-4c15-bf9d-f9550cad14be\"}}";
                Map<String, Object> formBodyMap = new HashMap<>();
                formBodyMap.put("files", new File(fileName));
                formBodyMap.put("config", jsonConfig);
                Map<String, String> headerMap = new HashMap<>();
                DataFeedApiClient.postMultipart("http://localhost:8100/ingest", formBodyMap, headerMap,
                        MediaType.get("text/plain"));
            }

        }

    }
}
