package com.one211.restapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.one211.restapi.model.User;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class UserClient {

    private final HttpClient httpClient;
    private final String baseUrl;
    private final String jwtToken;
    private final ObjectMapper objectMapper;

    public UserClient(String baseUrl, String jwtToken) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(40))
                .build();
        this.baseUrl = baseUrl;
        this.jwtToken = jwtToken;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    public User addUser(long orgId, User user) throws Exception {
        String body = objectMapper.writeValueAsString(user);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/orgs/" + orgId + "/users"))
                .header("Content-Type", "application/json")
                .header("Authorization", jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken)
                .timeout(Duration.ofSeconds(40))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 401) {
            throw new SecurityException("Unauthorized: JWT token may be missing, expired, or invalid");
        }
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Failed to add user: " + response.statusCode() + " | " + response.body());
        }

        return objectMapper.readValue(response.body(), User.class);
    }
    public boolean deleteUser(long orgId, String userName) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/orgs/" + orgId + "/users/" + userName))
                .header("Authorization", jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken)
                .timeout(Duration.ofSeconds(40))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return true;
    }

}
