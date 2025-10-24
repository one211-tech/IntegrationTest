package com.one211.restapi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.one211.restapi.model.OrgInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class LoginClient {
    private static final Logger log = LoggerFactory.getLogger(LoginClient.class);
    private final HttpClientProvider provider;

    public LoginClient(HttpClientProvider provider) {
        this.provider = provider;
    }

    public List<OrgInfo> validate(String email, String password) throws Exception {
        String body = """
            { "emailOrUserName": "%s", "password": "%s" }
            """.formatted(email, password);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(provider.getBaseUrl() + "/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = provider.getClient().send(request, HttpResponse.BodyHandlers.ofString());
        log.debug("Validation response: {}", response.body());
        return provider.getMapper().readValue(response.body(), new TypeReference<>() {});
    }

    // --- NEW flexible login ---
    public String login(String email, String password, Object claimValue) throws Exception {
        String claimsJson;

        if (claimValue instanceof Number) {
            claimsJson = "\"orgId\": " + claimValue;
        } else {
            // e.g. cluster name or other string claims
            claimsJson = "\"cluster\": \"" + claimValue + "\"";
        }

        String body = """
            {
              "emailOrUserName": "%s",
              "password": "%s",
              "claims": { %s }
            }
            """.formatted(email, password, claimsJson);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(provider.getBaseUrl() + "/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = provider.getClient().send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, String> tokenResponse = provider.getMapper().readValue(response.body(), new TypeReference<>() {});
        return tokenResponse.get("token");
    }
}
