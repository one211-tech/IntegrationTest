package com.one211.IntegrationTest.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class ClusterAssignmentHttpClient {
    private final HttpClient httpClient;
    private final String baseUrl;
    private final String jwtToken;

    public ClusterAssignmentHttpClient(String baseUrl, String jwtToken) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.baseUrl = baseUrl;
        this.jwtToken = jwtToken;
    }

    public boolean toggleAssignment(long orgId, String sourceType, String sourceName,
                                    String clusterName, String action) throws Exception {

        String body = String.format("""
            {
              "sourceType": "%s",
              "sourceName": "%s",
              "name": "%s",
              "action": "%s"
            }
            """, sourceType, sourceName, clusterName, action);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/orgs/" + orgId + "/cluster-assignments"))
                .header("Content-Type", "application/json")
                .header("Authorization", jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken)
                .timeout(Duration.ofSeconds(40))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return Boolean.parseBoolean(response.body());
    }
}
