package com.one211.IntegrationTest.service;

import com.one211.IntegrationTest.model.Cluster;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
public class ClusterHttpClient {
    private final HttpClient httpClient;
    private final String baseUrl;
    private final String jwtToken;
    private final HttpClientProvider provider;
    public ClusterHttpClient(String baseUrl, String jwtToken, HttpClientProvider provider) {
        this.provider = provider;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.baseUrl = baseUrl;
        this.jwtToken = jwtToken;
    }
    public String addCluster(Cluster cluster) throws Exception {

        String jsonBody = provider.getMapper().writeValueAsString(cluster);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/orgs/" + cluster.orgId() + "/clusters"))
                .header("Content-Type", "application/json")
                .header("Authorization", jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken)
                .timeout(Duration.ofSeconds(40))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 401) {
            throw new SecurityException("Unauthorized: JWT token may be missing, expired, or invalid");
        }
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Failed to add cluster: " + response.statusCode() + " | " + response.body());
        }
        return response.body();
    }
    public String deleteCluster(long orgId, String clusterName) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/orgs/" + orgId + "/clusters/" + clusterName))
                .header("Authorization", jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken)
                .timeout(Duration.ofSeconds(40))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return "Cluster deleted successfully.";
    }

}
