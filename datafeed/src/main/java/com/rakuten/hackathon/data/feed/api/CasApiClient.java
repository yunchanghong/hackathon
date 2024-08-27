package com.rakuten.hackathon.data.feed.api;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.ConnectionPool;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.java.net.cookiejar.JavaNetCookieJar;

@Component
public class CasApiClient {
    
    public static final String ICSPACE_ENDPOINT = "https://icspace.dlufl.edu.cn/ic-web/auth/address?finalAddress=https://icspace.dlufl.edu.cn&errPageUrl=https://icspace.dlufl.edu.cn/#/error&manager=false&consoleType=16";
    public static final String CAS_ENDPOINT = "https://cas.dlufl.edu.cn/cas/login";
    public static final String CAS_TICKET_ENDPOINT = "https://cas.dlufl.edu.cn/cas/login?service=https://i.dlufl.edu.cn/dcp/";
    public static final String ACCESS_ENDPOINT = "https://i.dlufl.edu.cn/dcp/";
    /**
     * MediaType: JSON
     */
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    public static final Map<String, String> headerMap;

    static {
        // Static block for initialization
        headerMap = new HashMap<>();
        headerMap.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36 " + new Date().getTime());
        headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        headerMap.put("Accept-Encoding", "gzip, deflate, br, zstd");
        headerMap.put("Accept-Language", "zh-CN,zh;q=0.9");
        headerMap.put("Cache-Control", "no-cache");
        headerMap.put("Connection", "keep-alive");
        headerMap.put("Content-Type", "application/x-www-form-urlencoded");
        headerMap.put("Pragma", "no-cache");
        
        headerMap.put("Sec-Fetch-Dest", "document");
        headerMap.put("Sec-Fetch-Mode", "navigate");
        headerMap.put("Sec-Fetch-Site", "none");
        headerMap.put("Sec-Fetch-User", "?1");
        headerMap.put("Upgrade-Insecure-Requests", "1");
        headerMap.put("sec-ch-ua", "\"Not/A)Brand\";v=\"8\", \"Chromium\";v=\"126\", \"Google Chrome\";v=\"126\"");
        headerMap.put("sec-ch-ua-mobile", "?0");
        headerMap.put("sec-ch-ua-platform", "\"macOS\"");
        headerMap.put("X-Forwarded-For", generateRandomIPAddress(new Random()));
    }
    
    private static String generateRandomIPAddress(Random random) {
        // Create an array of length 4
        int[] octets = new int[4];

        // Fill the array with random values between 0 and 255
        for (int i = 0; i < octets.length; i++) {
            octets[i] = random.nextInt(256);
        }

        // Build the IP address string
        StringBuilder ipAddress = new StringBuilder();
        for (int i = 0; i < octets.length; i++) {
            ipAddress.append(octets[i]);
            if (i < octets.length - 1) {
                ipAddress.append('.');
            }
        }

        return ipAddress.toString();
    }
    
    private static final ObjectMapper mapper = new ObjectMapper();
    
    
    
    
    private CasApiClient() {
        // private constructor to disable the class creation
    }
    
    public static String getLoginCallbackUrl(OkHttpClient client) throws IOException {
        Request request = new Request.Builder()
                .url(ICSPACE_ENDPOINT)
                .build();
        ResponseBody responseBody = client.newCall(request).execute().body();
        
        JsonNode node = mapper.readTree(responseBody.string());
        String callbakUrl = node.get("data").asText();
        return URLDecoder.decode(callbakUrl);
    }
    
    public static String extractServiceUrl(String loginUrl) throws MalformedURLException, IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(loginUrl).openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.setConnectTimeout(5000);
        String location = conn.getHeaderField("Location");
        System.out.println(location);
        String serviceUrl = location.split("=")[1];
        return URLDecoder.decode(serviceUrl);
    }
    
    public static Response extractExecutionAndTicket(OkHttpClient client, String serviceUrl) throws IOException {
        long curTime = new Date().getTime();
        String requestUrl = CAS_ENDPOINT + "?service=" + URLDecoder.decode(serviceUrl) + "&renew=true&_=" + curTime;
                
        // Create a request
        Request request = new Request.Builder()
                .url(requestUrl)
                .build();
        
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        
        
        return response;
    }
    
    public static String getTicketId(OkHttpClient client, Map<String, String> formMap, String cookie) throws IOException {
        Request.Builder builder = new Request.Builder();
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        formMap.forEach(formBodyBuilder::add);
        RequestBody formBody = formBodyBuilder.build();
        headerMap.forEach(builder::addHeader);
        builder.addHeader("Cookie", cookie);
        Request requestPost = builder
                .url(CAS_TICKET_ENDPOINT)
                .post(formBody)
                .build();
        Response responsePost = client.newCall(requestPost).execute();
        String ticketId = responsePost.header("Location").split("=")[1];
        System.out.println("ticketId=>" + ticketId);
        return ticketId;
    }
    
    public static void triggerAccess(String ticketId, OkHttpClient client) throws IOException {
        String accessUrl = ACCESS_ENDPOINT + "?ticket=" + ticketId;
        
        // Create a request
        Request requestAccess = new Request.Builder()
                .url(accessUrl)
                .build();
        Response responseAccess = client.newCall(requestAccess).execute();
        
        System.out.println("A========================================A");
        responseAccess.headers().forEach(item -> {
            System.out.println(item.getFirst() + "-" + item.getSecond());
        });
    }
    
    public static void triggerMainPage(OkHttpClient client) throws IOException {
        // Create a request
        Request requestMain = new Request.Builder()
                .url(ACCESS_ENDPOINT)
                .build();
        Response responseMain = client.newCall(requestMain).execute();
        
        System.out.println("@========================================@");
        System.out.println(responseMain.body().string());
    }

}
