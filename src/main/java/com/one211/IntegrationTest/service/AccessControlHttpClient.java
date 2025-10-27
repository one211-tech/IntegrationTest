package com.one211.IntegrationTest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.one211.IntegrationTest.model.AccessRow;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class AccessControlHttpClient {

    private final HttpClient httpClient;
    private final String baseUrl;
    private final String jwtToken;
    private final ObjectMapper objectMapper;

    public AccessControlHttpClient(String baseUrl, String jwtToken) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.baseUrl = baseUrl;
        this.jwtToken = jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken;
        this.objectMapper = new ObjectMapper();
    }

    // ---------------- CREATE ----------------
    public String createAccess(long orgId, String clusterName, AccessRow accessRow) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(accessRow);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/orgs/" + orgId + "/access-control/clusters/" + clusterName))
                .header("Content-Type", "application/json")
                .header("Authorization", jwtToken)
                .timeout(Duration.ofSeconds(40))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        return sendRequest(request);
    }

    // ---------------- GET BY CLUSTER ----------------
    public String getByCluster(long orgId, String clusterName) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/orgs/" + orgId + "/access-control/clusters/" + clusterName))
                .header("Authorization", jwtToken)
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        return sendRequest(request);
    }

    // ---------------- GET BY SOURCE ----------------
    public String getBySource(long orgId, String sourceType, String sourceName) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/orgs/" + orgId + "/access-control/sources/" + sourceType + "/" + sourceName))
                .header("Authorization", jwtToken)
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        return sendRequest(request);
    }

    // ---------------- UPDATE ----------------
    public String updateAccess(long orgId, String clusterName, AccessRow accessRow) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(accessRow);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/orgs/" + orgId + "/access-control/clusters/" + clusterName))
                .header("Content-Type", "application/json")
                .header("Authorization", jwtToken)
                .timeout(Duration.ofSeconds(40))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        return sendRequest(request);
    }

    // ---------------- DELETE ----------------
    public String deleteAccess(long orgId, String clusterName, String sourceType, String sourceName, String tableType) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/orgs/" + orgId +
                        "/access-control/clusters/" + clusterName +
                        "/" + sourceType +
                        "/" + sourceName +
                        "/" + tableType))
                .header("Authorization", jwtToken)
                .timeout(Duration.ofSeconds(40))
                .DELETE()
                .build();

        return sendRequest(request);
    }

    // ---------------- HELPER ----------------
    private String sendRequest(HttpRequest request) throws Exception {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int code = response.statusCode();
        if (code >= 200 && code < 300) {
            return response.body();
        } else {
            throw new IllegalStateException("HTTP " + code + " -> " + response.body());
        }
    }
}
