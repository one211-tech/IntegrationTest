package com.one211.IntegrationTest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.net.http.HttpClient;
import java.time.Duration;

public class HttpClientProvider {
    private final HttpClient client;
    private final ObjectMapper mapper;
    private final String baseUrl;

    public HttpClientProvider() {
        Config config = ConfigFactory.load(); // automatically loads application.conf
        this.baseUrl = config.getString("api.baseUrl");
        int timeout = config.getInt("api.timeout");

        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeout))
                .build();

        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    public HttpClient getClient() { return client; }
    public ObjectMapper getMapper() { return mapper; }
    public String getBaseUrl() { return baseUrl; }
}