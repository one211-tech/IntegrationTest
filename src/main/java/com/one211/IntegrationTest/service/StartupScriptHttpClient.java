package com.one211.IntegrationTest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class StartupScriptHttpClient {

    private final HttpClient httpClient;
    private final String baseUrl;
    private final String jwtToken;
    private final ObjectMapper objectMapper;

    public StartupScriptHttpClient(String baseUrl, String jwtToken) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(40))
                .build();
        this.baseUrl = baseUrl;
        this.jwtToken = jwtToken;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    // ---------------- CREATE ----------------
    public String createStartupScript(long orgId, String clusterName, String script) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(new ContentPayload(script));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/orgs/" + orgId + "/clusters/" + clusterName + "/startup-scripts"))
                .header("Content-Type", "application/json")
                .header("Authorization",  jwtToken)
                .timeout(Duration.ofSeconds(40))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        return sendRequest(request, jsonBody);
    }

    // ---------------- UPDATE ----------------
    public String updateStartupScript(long orgId, String clusterName, String script) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(new ContentPayload(script));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/orgs/" + orgId + "/clusters/" + clusterName + "/startup-scripts"))
                .header("Content-Type", "application/json")
                .header("Authorization", jwtToken)
                .timeout(Duration.ofSeconds(40))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        return sendRequest(request, jsonBody);
    }

    // ---------------- GET ----------------
    public String getStartupScripts(long orgId, String clusterName) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/orgs/" + orgId + "/clusters/" + clusterName + "/startup-scripts"))
                .header("Authorization", jwtToken)
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        return sendRequest(request, null);
    }

    // ---------------- TEST ----------------
    public String testStartupScript(long orgId, String clusterName, String script) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(new ScriptPayload(script));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/orgs/" + orgId + "/clusters/" + clusterName + "/startup-scripts/test"))
                .header("Content-Type", "application/json")
                .header("Authorization", jwtToken)
                .timeout(Duration.ofSeconds(40))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        return sendRequest(request, jsonBody);
    }

    // ---------------- HELPER ----------------
    private String sendRequest(HttpRequest request, String requestBody) throws Exception {

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int code = response.statusCode();
        if (code >= 200 && code < 300) {
            return response.body();
        } else {
            throw new IllegalStateException("HTTP " + code + " -> " + response.body());
        }
    }
    record ContentPayload(String content) {}
    record ScriptPayload(String script) {}
}
