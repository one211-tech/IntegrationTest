package com.one211.restapi.service;

import com.one211.restapi.model.SignUp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SignupClient {
    private static final Logger log = LoggerFactory.getLogger(SignupClient.class);
    private final HttpClientProvider provider;

    public SignupClient(HttpClientProvider provider) {
        this.provider = provider;
    }
    public void signUp(SignUp user) throws Exception {
        String jsonBody = provider.getMapper().writeValueAsString(user);
        log.debug("Sending signup request: {}", jsonBody);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(provider.getBaseUrl() + "/signup"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = provider.getClient().send(request, HttpResponse.BodyHandlers.ofString());
        log.debug("Signup response: {} {}", response.statusCode(), response.body());
        if (response.statusCode() == 200 || response.statusCode() == 201) {
            log.info("Signup successful for email={}", user.email());
        } else {
            throw new RuntimeException("Signup failed: " + response.statusCode() + " | " + response.body());
        }
    }
}
