package com.one211.restapi.service;

import com.one211.restapi.model.OrgInfo;
import com.one211.restapi.model.SignUp;
import com.one211.restapi.model.User;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SignupClientIntegrationTest {

    private static SignupClient signupClient;
    private static LoginClient loginClient;

    private static ClusterHttpClient clusterClient;

    private static SignUp testUser;
    private static String jwtToken;

    @BeforeAll
    static void setup() throws Exception {
        HttpClientProvider provider = new HttpClientProvider();
        signupClient = new SignupClient(provider);
        loginClient = new LoginClient(provider);
        testUser = new SignUp(
                "Demo Admin",
                "demo.integration@example.com",
                "Admin123",
                "IntegrationOrg",
                "Integration Test Org",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        try {
            signupClient.signUp(testUser);
        } catch (RuntimeException ignored) {}

        List<OrgInfo> orgs = loginClient.validate(testUser.email(), testUser.password());
        OrgInfo org = orgs.getFirst();
        jwtToken = loginClient.login(testUser.email(), testUser.password(), org.orgId());
        clusterClient = new ClusterHttpClient(provider.getBaseUrl(), jwtToken);
    }

    @Test
    @Order(1)
    void testSignupAndLoginFlow() throws Exception {
        // Signup (ignore if already exists)
        try {
            signupClient.signUp(testUser);
        } catch (RuntimeException ignored) {}

        List<OrgInfo> orgs = loginClient.validate(testUser.email(), testUser.password());
        assertFalse(orgs.isEmpty(), "No orgs found for " + testUser.email());
        OrgInfo org = orgs.getFirst();
        assertNotNull(org.orgId(), "Org ID should not be null");
        String jwt = loginClient.login(testUser.email(), testUser.password(), org.orgId());
        assertNotNull(jwt, "JWT token should not be null");
        assertTrue(jwt.length() > 10, "JWT should look valid");
    }

    @Test
    @Order(2)
    void testSignupFailsForDuplicateUser() {
        SignUp duplicateUser = new SignUp(
                "Demo Admin",
                testUser.email(), // same email → duplicate
                "Admin123",
                "IntegrationOrg",
                "Integration Test Org",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> signupClient.signUp(duplicateUser));
        assertTrue(ex.getMessage().contains("Signup failed"), "Should fail with signup error");
    }

    @Test
    @Order(4)
    void testAddClusterToOrganization() throws Exception {
        List<OrgInfo> orgs = loginClient.validate(testUser.email(), testUser.password());
        OrgInfo org = orgs.getFirst();
        String freshJwt = loginClient.login(testUser.email(), testUser.password(), org.orgId());
        ClusterHttpClient clusterClient = new ClusterHttpClient("http://localhost:8080", freshJwt);
        String response = clusterClient.addCluster(
                org.orgId(),
                "cluster-integration",
                "Integration test cluster",
                true
        );
        assertNotNull(response, "Response should not be null");
        assertTrue(response.contains("cluster-integration"), "Response should contain cluster name");
    }
    @Test
    @Order(5)
    void testAddUserToOrganization() throws Exception {
        List<OrgInfo> orgs = loginClient.validate(testUser.email(), testUser.password());
        OrgInfo org = orgs.getFirst();
        UserClient userClient = new UserClient("http://localhost:8080", jwtToken);
        User newUser = new User(
                null,  // let backend assign ID
                "Integration User",
                "intuser@example.com",
                "UserPass123",
                "USER",
                "Added by integration test",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        User createdUser = userClient.addUser(org.orgId(), newUser);
        assertNotNull(createdUser);
        assertEquals("Integration User", createdUser.name());
        assertEquals("intuser@example.com", createdUser.email());
    }

}
