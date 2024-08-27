package com.rakuten.hackathon.data.feed.api;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.ConnectionPool;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

@Component
public class DataFeedApiClient {

    private DataFeedApiClient() {
        // private constructor to disable the class creation
    }

    private static final Log LOGGER = LogFactory.getLog(DataFeedApiClient.class);

    /**
     * MediaType: JSON
     */
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * client singleton instance
     */
    private static OkHttpClient client;

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Get client instance, if null a new instance will be created
     *
     * @return OkHttpClient
     */
    public static OkHttpClient getOkHttpClient() throws IOException {
        if (client == null) {
            throw new IOException("Http client is not initialized yet.");
        } else {
            return client;
        }
    }
    
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
    
    /**
     * new initializeClient This method should be called manually before using it
     *
     * @param keepAliveDuration  keepAliveDuration
     * @param connectionTimeout  connectionTimeout, time unit is second
     * @param readTimeout        readTimeout, time unit is second
     */
    public static void initializeClient(int maxIdleConnections, int keepAliveDuration, int connectionTimeout,
                                        int readTimeout, boolean reInit) {
        // when we start using the https request, we need more configuration
        initializeClient(maxIdleConnections, keepAliveDuration, connectionTimeout, readTimeout, reInit, null, null, null, false);
    }
    
    public static void initializeClient(int maxIdleConnections, int keepAliveDuration, int connectionTimeout,
            int readTimeout, boolean reInit, boolean redirect) {
        // when we start using the https request, we need more configuration
        initializeClient(maxIdleConnections, keepAliveDuration, connectionTimeout, readTimeout, reInit, null, null, null, redirect);
    }

    /**
     * initializeClient this method use retry interceptor.
     *
     * @param maxIdleConnections     max idle connections
     * @param keepAliveDuration      keep alive duration
     * @param connectionTimeout      connection timeout
     * @param readTimeout            read timeout
     * @param reInit                 force re-init flag
     * @param customisedInterceptors Interceptors
     * @param proxy                  Proxy
     */
    public static void initializeClient(int maxIdleConnections, int keepAliveDuration, int connectionTimeout,
                                        int readTimeout, boolean reInit, List<Interceptor> customisedInterceptors, Proxy proxy) {
        initializeClient(maxIdleConnections, keepAliveDuration, connectionTimeout,readTimeout, reInit, customisedInterceptors, proxy,null, false);
    }

    /**
     * initializeClient.
     *
     * @param maxIdleConnections     max idle connections
     * @param keepAliveDuration      keep alive duration
     * @param connectionTimeout      connection timeout
     * @param readTimeout            read timeout
     * @param reInit                 force re-init flag
     * @param customisedInterceptors Interceptors
     */
    public static void initializeClient(int maxIdleConnections, int keepAliveDuration, int connectionTimeout,
                                        int readTimeout, boolean reInit, List<Interceptor> customisedInterceptors) {
        initializeClient(maxIdleConnections, keepAliveDuration, connectionTimeout,readTimeout, reInit, customisedInterceptors, null,null, false);
    }

    /**
     * initializeClient this method use retry interceptor.
     *
     * @param maxIdleConnections     max idle connections
     * @param keepAliveDuration      keep alive duration
     * @param connectionTimeout      connection timeout
     * @param readTimeout            read timeout
     * @param reInit                 force re-init flag
     * @param customisedInterceptors Interceptors
     * @param proxy                  Proxy
     * @param networkInterceptors    Network Interceptors
     */
    public static void initializeClient(int maxIdleConnections, int keepAliveDuration, int connectionTimeout,
                                        int readTimeout, boolean reInit, List<Interceptor> customisedInterceptors, Proxy proxy, List<Interceptor> networkInterceptors, boolean redirect) {

        // when we start using the https request, we need more configuration
        if (reInit || client == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                    .readTimeout(readTimeout, TimeUnit.SECONDS)
                    .hostnameVerifier((hostname, session) -> true)
                    .connectionPool(new ConnectionPool(maxIdleConnections, keepAliveDuration, TimeUnit.MINUTES));
            Optional.ofNullable(customisedInterceptors).ifPresent(interceptors -> interceptors.forEach(builder::addInterceptor));
            Optional.ofNullable(networkInterceptors).ifPresent(interceptors -> interceptors.forEach(builder::addNetworkInterceptor));
            Optional.ofNullable(proxy).ifPresent(builder::proxy);
            if (redirect) {
                builder.followRedirects(false);
                builder.followSslRedirects(false);
            }
            
            DataFeedApiClient.client = builder.build();
        }
    }

    public static void initializeClient(int maxIdleConnections, int keepAliveDuration, int connectionTimeout,
                                        int readTimeout, boolean reInit, List<Interceptor> customisedInterceptors, List<Interceptor> networkInterceptors) {

        // when we start using the https request, we need more configuration
        if (reInit || client == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                    .readTimeout(readTimeout, TimeUnit.SECONDS)
                    .hostnameVerifier((hostname, session) -> true)
                    .connectionPool(new ConnectionPool(maxIdleConnections, keepAliveDuration, TimeUnit.MINUTES));
            Optional.ofNullable(customisedInterceptors).ifPresent(interceptors -> interceptors.forEach(builder::addInterceptor));
            Optional.ofNullable(networkInterceptors).ifPresent(interceptors -> interceptors.forEach(builder::addNetworkInterceptor));
            DataFeedApiClient.client = builder.build();
        }
    }


    /**
     * get
     *
     * @param url request url, should contains all params
     * @return Response
     * @throws IOException Exception
     */
    public static Response get(String url) throws IOException {
        return get(url, null);
    }

    /**
     * get
     *
     * @param url request url, set header parameter
     * @return Response
     * @throws IOException Exception
     */
    public static Response get(String url, Map<String, String> httpHeaderMap) throws IOException {
        Request.Builder builder = new Request.Builder();
        if (httpHeaderMap != null) {
            httpHeaderMap.forEach(builder::addHeader);
        }
        Request request = builder
                .url(url)
                .build();
        Response response = getOkHttpClient().newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new OkHttpResponseException(response);
        }
        Headers responseHeaders = response.headers();
        for (int i = 0; i < responseHeaders.size(); i++) {
            System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
            LOGGER.debug(responseHeaders.name(i) + ": " + responseHeaders.value(i));
        }
        
        return response;
    }

    /**
     * post with json
     *
     * @param url         request url
     * @param requestJson json string request data
     * @return String
     * @throws IOException Exception
     */
    public static String post(String url, String requestJson) throws IOException {
        RequestBody body = RequestBody.create(requestJson, JSON); // new
        // RequestBody body = RequestBody.create(JSON, json); // old
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = getOkHttpClient().newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new OkHttpResponseException(response);
        }
        return Objects.requireNonNull(response.body()).string();
    }

    /**
     * post with json
     *
     * @param url         request url
     * @param requestJson json string request data
     * @return Response
     * @throws IOException Exception
     */
    public static Response postReturnResponse(String url, String requestJson) throws IOException {
        RequestBody body = RequestBody.create(requestJson, JSON);
        return post(url, body, null);
    }

    /**
     * post with form data
     *
     * @param url          request url
     * @param formBodyMap  json string request data
     * @return Response
     * @throws IOException Exception
     */
    public static Response post(String url, Map<String, String> formBodyMap) throws IOException {
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        formBodyMap.forEach(formBodyBuilder::add);
        RequestBody formBody = formBodyBuilder.build();
        return post(url, formBody, null);
    }


    /**
     * post
     *
     * @param url          request url
     * @param formBody     RequestBody
     * @return Response
     * @throws IOException Exception
     */
    public static Response post(String url, RequestBody formBody, Map<String, String> httpHeaderMap) throws IOException {
        Request.Builder builder = new Request.Builder();
        // add header
        if (httpHeaderMap != null) {
            httpHeaderMap.forEach(builder::addHeader);
        }
        Request request = builder
                .url(url)
                .post(formBody)
                .build();
        Response response = getOkHttpClient().newCall(request).execute();
        System.out.println(response.body().string());

        if (!response.isSuccessful()) {
            throw new OkHttpResponseException(response);
        }
        return response;
    }
    
    /**
     * post
     *
     * @param url          request url
     * @param formBody     RequestBody
     * @return Response
     * @throws IOException Exception
     */
    public static Response post3(String url, RequestBody formBody, Map<String, String> httpHeaderMap) throws IOException {
        Request.Builder builder = new Request.Builder();
        // add header
        if (httpHeaderMap != null) {
            httpHeaderMap.forEach(builder::addHeader);
        }
        Request request = builder
                .url(url)
                .post(formBody)
                .build();
        Response response = getOkHttpClient().newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new OkHttpResponseException(response);
        }
        return response;
    }

    /**
     * post2
     * Same as post but returns the native Response object from the okhttp library instead of checking isSuccessful()
     * @param url      String
     * @param formBody RequestBody
     * @return Response
     * @throws IOException Exception
     */
    public static Response post2(String url, RequestBody formBody, Map<String, String> httpHeaderMap) throws IOException {
        Request.Builder builder = new Request.Builder();
        // add header
        if (httpHeaderMap != null) {
            httpHeaderMap.forEach(builder::addHeader);
        }
        Request request = builder
                .url(url)
                .post(formBody)
                .build();

        return getOkHttpClient().newCall(request).execute();
    }

    /**
     * put
     *
     * @param url          request url
     * @param requestJson  json string request data
     * @return Response
     * @throws IOException Exception
     */
    public static Response put(String url, String requestJson, Map<String, String> httpHeaderMap) throws IOException {
        Request.Builder builder = new Request.Builder();
        if (httpHeaderMap != null) {
            httpHeaderMap.forEach(builder::addHeader);
        }
        RequestBody body = RequestBody.create(requestJson, JSON);
        Request request = builder
                .url(url)
                .put(body)
                .build();
        Response response = getOkHttpClient().newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new OkHttpResponseException(response);
        }
        return response;
    }

    /**
     * getFile
     *
     * @param url              request url
     * @param headerMap        for access token or some other things that need to put into header
     * @param outputFolderPath output folder path
     * @return String          output file path
     * @throws IOException     Exception
     */
    public static String getFile(String url, Map<String, String> headerMap, String outputFolderPath) throws IOException {
        AtomicReference<String> outputFilePath = new AtomicReference<>();
        // send request
        Response response = get(url, headerMap);
        Optional.ofNullable(getFileNameFromResponse(response)).ifPresent(fileName -> {
            BufferedSink sink = null;
            try {
                if (!Files.exists(Paths.get(outputFolderPath))) {
                    Files.createDirectories(Paths.get(outputFolderPath));
                }
                String filePath = outputFolderPath + File.separator + fileName;
                File downloadedFile = new File(filePath);
                sink = Okio.buffer(Okio.sink(downloadedFile));
                sink.writeAll(Objects.requireNonNull(response.body()).source());
                outputFilePath.set(filePath);
            } catch (Exception e) {
                LOGGER.error("Error occurs while downloading zip file.", e);
            } finally {
                try {
                    if (sink != null) {
                        sink.close();
                    }
                } catch (IOException e) {
                    LOGGER.error("Error occurs while trying to close the buffer stream.", e);
                }
            }
        });
        return outputFilePath.get();
    }

    /**
     * Get File name from response
     *
     * @param response Response
     * @return file name
     */
    private static String getFileNameFromResponse(Response response) {
        String fileName = null;
        Pattern pattern = Pattern.compile("filename=['\"]?([^'\"\\s]+)['\"]?");
        String contentDisposition = response.header("Content-Disposition");
        if (!StringUtils.isEmpty(contentDisposition)) {
            // Get filename from the Content-Disposition header.
            Matcher matcher = pattern.matcher(contentDisposition);
            if (matcher.find()) {
                fileName = matcher.group(1).replaceAll(".*[/\\\\]", "");
            }
        }
        return fileName;
    }

    /**
     * post multipart
     *
     * @param url           request url
     * @param formBodyMap   request data
     *                      For file object, please add it to the map like
     *                      {
     *                      file_keyword: File("file_path")
     *                      }
     * @param headerMap     for access token or some other things that need to put into header
     * @param fileMediaType MediaType of upload file
     * @return Response
     * @throws IOException Exception
     */
    public static Response postMultipart(String url, Map<String, Object> formBodyMap, Map<String, String> headerMap, MediaType fileMediaType) throws IOException {
        MultipartBody.Builder formBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for (Map.Entry<String, Object> entry : formBodyMap.entrySet()) {
            Object val = entry.getValue();
            String key = entry.getKey();
            if (val instanceof File) {
                File file = (File) val;
                formBodyBuilder.addFormDataPart(key, file.getName(), RequestBody.create(file, fileMediaType));
            } else if (val instanceof String) {
                formBodyBuilder.addFormDataPart(key, String.valueOf(val));
            } else {
                throw new IOException(String.format("Parameter type not support. Key is [%s], type is [%s].", key, val.getClass()));
            }
        }

        RequestBody formBody = formBodyBuilder.build();
        return post(url, formBody, headerMap);
    }

    /**
     * delete
     * @param url       request url
     * @param formBody  nullable
     * @param headerMap for access token or some other things that need to put into header
     * @return Response
     * @throws IOException IOException
     */
    public static Response delete(String url, RequestBody formBody, Map<String, String> headerMap) throws IOException {
        Request.Builder builder = new Request.Builder();
        // add header
        if (headerMap != null) {
            headerMap.forEach(builder::addHeader);
        }
        Request request = builder
                .url(url)
                .delete(formBody)
                .build();
        return getOkHttpClient().newCall(request).execute();
    }

    public static <T> T convertResponse(Response response, Class<T> clazz) throws IOException {
        JsonNode root = mapper.readTree(Objects.requireNonNull(response.body()).byteStream());
        return mapper.readValue(root.toString(), clazz);
    }
}
