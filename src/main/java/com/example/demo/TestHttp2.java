package com.example.demo;

import io.netty.handler.codec.http.HttpHeaderNames;
import reactor.core.publisher.Flux;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

public class TestHttp2 {

    public static void main(String[] args) throws InterruptedException {
        singleRequest(true);
    }

    private static void singleRequest(boolean http2) {
        HttpClient httpClient = buildHttpClient(http2);
        String data = sendRequest(httpClient, getOneKString());
        System.err.println(data);
    }
    private static HttpClient buildHttpClient(boolean http2) {
        HttpClient httpClient = HttpClient.create()
                .headers(entries -> entries.add("content-type", "application/json"))
                .http2Settings(builder -> {
                    builder.initialWindowSize(8388608);
                    builder.maxFrameSize(8388608);
                });
        if (http2) {
            httpClient = httpClient.protocol(HttpProtocol.H2C);
        }
        httpClient.warmup().block();
        return httpClient;
    }

    private static String sendRequest(HttpClient httpClient, String body) {
        return httpClient
                .headers(h -> h.set(HttpHeaderNames.CONTENT_LENGTH, body.length()))
                .post()
                .uri("http://localhost:50051/com.example.demo.dubbo.api.DemoService/sayHello")
                .send(ByteBufFlux.fromString(Flux.just(body)))
                .responseContent()
                .aggregate()
                .asString()
                .block(Duration.ofSeconds(10));
    }

    public static String getOneKString() {
        String oneK =
                "123232121213saudasladklawdhkalshdlhalhwidashifhsadhwiaoshdakwld23eadiq12847321247832938491272198328941hajshdwiabskb324325dc";
        oneK = oneK + oneK + oneK + oneK;
        oneK = "{\"a\":\"" + oneK + "\"}";
        return oneK;
    }
}
