package com.one211.IntegrationTest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.one211.IntegrationTest.model.Group;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class GroupHttpClient {

    private final HttpClient httpClient;
    private final String baseUrl;
    private final String jwtToken;
    private final ObjectMapper objectMapper;

    public GroupHttpClient(String baseUrl, String jwtToken) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(40))
                .build();
        this.baseUrl = baseUrl;
        this.jwtToken = jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    public Group createGroup(long orgId, String name, String description) throws Exception {
        String body = String.format("""
                {
                  "name": "%s",
                  "description": "%s"
                }
                """, name, description);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/orgs/" + orgId + "/groups"))
                .header("Content-Type", "application/json")
                .header("Authorization", jwtToken)
                .timeout(Duration.ofSeconds(40))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), Group.class);
    }

    public String handleUserInGroup(long orgId, String userName, String groupName, String action) throws Exception {
        if (!("add".equalsIgnoreCase(action) || "remove".equalsIgnoreCase(action))) {
            throw new IllegalArgumentException("Action must be 'add' or 'remove'");
        }

        String body = String.format("""
                {
                  "name": "%s",
                  "action": "%s"
                }
                """, groupName, action.toLowerCase());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/orgs/" + orgId + "/users/" + userName))
                .header("Content-Type", "application/json")
                .header("Authorization", jwtToken)
                .timeout(Duration.ofSeconds(40))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public boolean deleteGroup(long orgId, String groupName) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/orgs/" + orgId + "/groups/" + groupName))
                .header("Authorization", jwtToken)
                .timeout(Duration.ofSeconds(40))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return Boolean.parseBoolean(response.body().trim());
    }
}
