package com.one211.restapi.service;

import com.one211.restapi.model.OrgInfo;
import com.one211.restapi.model.SignUp;
import com.one211.restapi.model.User;
import org.junit.jupiter.api.*;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IntegrationFlowTest {

    private static SignupClient signupClient;
    private static LoginClient loginClient;

    private static ClusterHttpClient clusterClient;
    private static UserClient userClient;
    private static ClusterAssignmentHttpClient clusterAssignmentHttpClient;
    private static StartupScriptHttpClient startupScriptClient;
    private static SignUp testUser;
    private static long orgId;

    @BeforeEach
     void init() throws Exception {
        HttpClientProvider provider = new HttpClientProvider();
        signupClient = new SignupClient(provider);
        loginClient = new LoginClient(provider);

        // Prepare test user
        testUser = new SignUp(
                "Demo Admin",
                "demo@example.com",
                "Admin123",
                "one211",
                "Test Org one211",
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
        clusterClient = new ClusterHttpClient("http://localhost:8080", jwtToken);
        userClient = new UserClient("http://localhost:8080", jwtToken);
        clusterAssignmentHttpClient = new ClusterAssignmentHttpClient("http://localhost:8080", jwtToken);
    }

    @Test
    @Order(1)
    void testSignupAndLoginFlow() throws Exception {
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
                testUser.email(), // duplicate email
                "Admin123",
                "demo123Org",
                "demo Test Org",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> signupClient.signUp(duplicateUser));
        assertTrue(ex.getMessage().contains("Signup failed"), "Should fail with signup error");
    }

    @Test
    @Order(3)
    void testAddClusterToOrganization() throws Exception {
        String response = clusterClient.addCluster(
                orgId,
                "cluster-integration3",
                "Integration test cluster",
                true
        );
        assertNotNull(response, "Response should not be null");
        assertTrue(response.contains("cluster-integration"), "Response should contain cluster name");
    }

    @Test
    @Order(4)
    void testAddUserToOrganization() throws Exception {
        User newUser = new User(
                null,
                "Integration User.1",
                "intuser@example123com",
                "UserPass123",
                "USER",
                "Added by integration test",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        User createdUser = userClient.addUser(orgId, newUser);
        assertNotNull(createdUser);
        assertEquals("Integration User.1", createdUser.name());
        assertEquals("intuser@example123com", createdUser.email());
    }

    @Test
    @Order(5)
    void testAssignClusterToUser() throws Exception {
        boolean result = clusterAssignmentHttpClient.toggleAssignment(
                orgId,
                "USER",
                "intuser@example123com",
                "cluster-integration3",
                "assign"
        );
        assertTrue(result, "Cluster should be assigned successfully to user");
    }
    @Test
    @Order(6)
    void testUnassignClusterFromUser() throws Exception {
        boolean result = clusterAssignmentHttpClient.toggleAssignment(
                orgId,
                "USER",
                "intuser@example123com",
                "cluster-integration3",
                "unassign"
        );
        assertTrue(result, "Cluster should be unassigned successfully from user");
    }

    @Test
    @Order(7)
    void testCreateStartupScript() throws Exception {
        List<OrgInfo> orgs = loginClient.validate(testUser.email(), testUser.password());
        OrgInfo org = orgs.getFirst(); // Use get(0) instead of getFirst()
        String newJwtToken = loginClient.login(testUser.email(), testUser.password(), org.orgId());
        startupScriptClient = new StartupScriptHttpClient("http://localhost:8080", newJwtToken);
        String created = startupScriptClient.createStartupScript(org.orgId(), "cluster-integration3", "select 1;");
        assertNotNull(created);
        assertTrue(created.contains("select 1;"));
    }


//    @Test
//    @Order(8)
//    void testGetStartupScript() throws Exception {
//        // Fetch the startup script
//        var fetched = startupScriptClient.getStartupScripts(orgId, "cluster-integration3");
//
//        // Assertions
//        assertNotNull(fetched, "Startup script should be fetched");
//        assertTrue(fetched.contains("SELECT 1"), "Fetched script content should match created content");
//    }

    @Test
    @Order(9)
    void testUpdateStartupScript() throws Exception {

        List<OrgInfo> orgs = loginClient.validate(testUser.email(), testUser.password());
        OrgInfo org = orgs.getFirst(); // Use get(0) instead of getFirst()
        String newJwtToken = loginClient.login(testUser.email(), testUser.password(), org.orgId());
        startupScriptClient = new StartupScriptHttpClient("http://localhost:8080", newJwtToken);
        var updated = startupScriptClient.updateStartupScript(org.orgId(), "cluster-integration3", "select 5;");
        assertNotNull(updated);
        assertTrue(updated.contains("select 5;"));
    }

    @Test
    @Order(10)
    void testDeleteUserFromOrganization() throws Exception {
        boolean deleted = userClient.deleteUser(orgId, "intuser@example123com");
        assertTrue(deleted, "User should be deleted successfully");
    }

    @Test
    @Order(11)
    void testDeleteClusterFromOrganization() throws Exception {
        String result = clusterClient.deleteCluster(orgId, "cluster-integration3");
        assertNotNull(result, "Delete result should not be null");
        assertEquals("Cluster deleted successfully.", result, "Cluster should be deleted");
    }
}
