package com.one211.restapi.service;


import com.one211.restapi.model.Cluster;
import com.one211.restapi.model.OrgInfo;
import com.one211.restapi.model.SignUp;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.LocalDateTime;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IntegrationTest {
    private static SignupClient signupClient;
    private static LoginClient loginClient;

    private static ClusterHttpClient clusterClient;
    private static UserClient userClient;
    private static ClusterAssignmentHttpClient clusterAssignmentHttpClient;
    private static StartupScriptHttpClient startupScriptClient;
    private static SignUp testUser;
    private static long orgId;
    private static Cluster testcluster;
    static HttpClientProvider provider = new HttpClientProvider();
    @BeforeAll
    static void init() throws Exception {

        signupClient = new SignupClient(provider);
        loginClient = new LoginClient(provider);

        // Prepare test user
        testUser = new SignUp(
                "Demo Admin",
                "demo_exam",
                "Admin123@gmail.com",
                "one211",
                "Test Org one211",
                "test organization",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        try {
            signupClient.signUp(testUser);
        } catch (RuntimeException ignored) {}
        List<OrgInfo> orgs = loginClient.validate(testUser.email(), testUser.password());
        OrgInfo org = orgs.getFirst();
        orgId = org.orgId();
    }

    @BeforeEach
    void setup() throws Exception {
        List<OrgInfo> orgs = loginClient.validate(testUser.email(), testUser.password());
        OrgInfo org = orgs.getFirst();
        String jwtToken = loginClient.login(testUser.email(), testUser.password(), org.orgId());

        // Build clients with new token
        clusterClient = new ClusterHttpClient(provider.getBaseUrl(), jwtToken, provider);
        userClient = new UserClient( jwtToken, provider);
        clusterAssignmentHttpClient = new ClusterAssignmentHttpClient("http://localhost:8080", jwtToken);
    }
}
