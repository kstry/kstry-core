package cn.kstry.flux.demo.service.http;

import cn.kstry.flux.demo.facade.http.ResultProperty;
import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskParam;
import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.core.bus.ScopeDataOperator;
import cn.kstry.framework.core.component.conversion.TypeConverterProcessor;
import cn.kstry.framework.core.exception.BusinessException;
import cn.kstry.framework.core.util.ElementParserUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
@Slf4j
@TaskComponent
public class HttpActionService {

    private CloseableHttpClient httpClient;

    private CloseableHttpAsyncClient asyncHttpClient;

    @Resource
    private TypeConverterProcessor typeConverterProcessor;

    @PostConstruct
    public void init() {
        // init http client
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(1000);
        connectionManager.setDefaultMaxPerRoute(500);
        httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();

        // init async http client
        PoolingAsyncClientConnectionManager asyncConnectionManager = new PoolingAsyncClientConnectionManager();
        asyncConnectionManager.setMaxTotal(1000);
        asyncConnectionManager.setDefaultMaxPerRoute(500);
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(10))
                .setIoThreadCount(10)
                .build();
        asyncHttpClient = HttpAsyncClients.custom()
                .setIOReactorConfig(ioReactorConfig)
                .setConnectionManager(asyncConnectionManager)
                .build();
        asyncHttpClient.start();
    }

    @TaskService(name = "http-post")
    public void httpPostAction(ScopeDataOperator dataOperator,
                               @TaskParam("url") String url,
                               @TaskParam("result") ResultProperty result,
                               @TaskParam("data") Map<String, Object> data,
                               @TaskParam("header") Map<String, String> header) {
        if (StringUtils.isBlank(url)) {
            return;
        }
        try {
            HttpPost httpPost = new HttpPost(url);
            if (data == null) {
                data = Maps.newHashMap();
            }
            httpPost.setEntity(new StringEntity(JSON.toJSONString(data), ContentType.APPLICATION_JSON));
            if (MapUtils.isNotEmpty(header)) {
                header.forEach((k, v) -> {
                    if (StringUtils.isAnyBlank(k, v)) {
                        return;
                    }
                    httpPost.setHeader(k, v);
                });
            }
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String r = EntityUtils.toString(response.getEntity());
            log.info("HttpActionService httpPostAction success. url: {}, header: {}, data: {}, response: {}, result: {}", url, header, data, response, r);
            if (StringUtils.isBlank(r)) {
                return;
            }
            if (result == null) {
                return;
            }
            noticeResult(dataOperator, result, r);
        } catch (Exception e) {
            log.error("HttpActionService httpPostAction error. url: {}, header: {}, data: {}", url, header, data, e);
            throw new BusinessException("-100", e.getMessage(), e);
        }
    }

    private void noticeResult(ScopeDataOperator dataOperator, ResultProperty resultProperty, String result) {
        if (StringUtils.isBlank(resultProperty.getTarget()) || !ElementParserUtil.isValidDataExpression(resultProperty.getTarget())) {
            return;
        }
        JSONObject jsonObject = JSON.parseObject(result);
        Object resData = jsonObject.get("data");
        if (resData != null) {
            jsonObject.put("data", typeConverterProcessor.convert(resultProperty.getConverter(), resData, Optional.ofNullable(resultProperty.getType())
                            .filter(StringUtils::isNotBlank).map(className -> {
                                try {
                                    return Class.forName(className);
                                } catch (Exception e) {
                                    log.error("HttpActionService convert. type invalid. type: {}", className, e);
                                }
                                return null;
                            }).orElse(null)
                    ).getValue()
            );
        }
        dataOperator.setData(resultProperty.getTarget(), jsonObject);
    }

    @TaskService(name = "async-http-post")
    public Mono<Void> asyncHttpPostAction(ScopeDataOperator dataOperator,
                                          @TaskParam("url") String url,
                                          @TaskParam("result") ResultProperty result,
                                          @TaskParam("data") Map<String, Object> data,
                                          @TaskParam("header") Map<String, String> header) {
        if (StringUtils.isBlank(url)) {
            return Mono.empty();
        }
        try {
            SimpleRequestBuilder requestBuilder = SimpleRequestBuilder.post(url);
            if (MapUtils.isNotEmpty(header)) {
                header.forEach((k, v) -> {
                    if (StringUtils.isAnyBlank(k, v)) {
                        return;
                    }
                    requestBuilder.setHeader(k, v);
                });
            }
            if (data == null) {
                data = Maps.newHashMap();
            }
            requestBuilder.setBody(JSON.toJSONString(data), ContentType.APPLICATION_JSON);
            SimpleHttpRequest request = requestBuilder.build();

            Pair<CompletableFuture<SimpleHttpResponse>, FutureCallback<SimpleHttpResponse>> futureCallbackPair = getFutureCallbackPair();
            asyncHttpClient.execute(request, futureCallbackPair.getValue());
            return Mono.fromFuture(futureCallbackPair.getKey()).mapNotNull(response -> {
                String r = null;
                try {
                    r = response.getBodyText();
                    log.info("HttpActionService async httpPostAction success. url: {}, header: {}, data: {}, response: {}, result: {}", url, header, request.getBody().getBodyText(), response, r);
                    if (StringUtils.isBlank(r)) {
                        return null;
                    }
                    noticeResult(dataOperator, result, r);
                    return null;
                } catch (Exception e) {
                    log.error("HttpActionService async httpPostAction error. url: {}, header: {}, data: {}, response: {}, result: {}", url, header, request.getBody().getBodyText(), response, r);
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            log.error("HttpActionService async httpPostAction error. url: {}, header: {}, data: {}", url, header, data, e);
            throw new BusinessException("-100", e.getMessage(), e);
        }
    }

    private Pair<CompletableFuture<SimpleHttpResponse>, FutureCallback<SimpleHttpResponse>> getFutureCallbackPair() {
        CompletableFuture<SimpleHttpResponse> completableFuture = new CompletableFuture<>();
        return ImmutablePair.of(completableFuture, new FutureCallback<>() {

            @Override
            public void completed(SimpleHttpResponse response) {
                completableFuture.complete(response);
            }

            @Override
            public void failed(Exception ex) {
                completableFuture.completeExceptionally(ex);
            }

            @Override
            public void cancelled() {
                completableFuture.cancel(true);
            }
        });
    }
}
