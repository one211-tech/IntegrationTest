package com.one211.restapi.service;

import com.one211.restapi.model.AccessRow;
import com.one211.restapi.model.OrgInfo;
import com.one211.restapi.model.SignUp;
import com.one211.restapi.model.User;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccessControlHttpClientTest {

    private static LoginClient loginClient;
    private static SignUp testUser;
    private static long orgId;

    private ClusterHttpClient clusterClient;
    private UserClient userClient;
    private ClusterAssignmentHttpClient clusterAssignmentHttpClient;
    private AccessControlHttpClient accessControlClient;

    @BeforeAll
    static void init() throws Exception {
        HttpClientProvider provider = new HttpClientProvider();
        SignupClient signupClient = new SignupClient(provider);
        loginClient = new LoginClient(provider);

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
        } catch (RuntimeException ignored) {
        }

        List<OrgInfo> orgs = loginClient.validate(testUser.email(), testUser.password());
        orgId = orgs.getFirst().orgId();
    }

    @BeforeEach
    void setup() throws Exception {
        List<OrgInfo> orgs = loginClient.validate(testUser.email(), testUser.password());
        OrgInfo org = orgs.getFirst();
        String jwtToken = loginClient.login(testUser.email(), testUser.password(), org.orgId());

        clusterClient = new ClusterHttpClient("http://localhost:8080", jwtToken);
        userClient = new UserClient("http://localhost:8080", jwtToken);
        clusterAssignmentHttpClient = new ClusterAssignmentHttpClient("http://localhost:8080", jwtToken);
        accessControlClient = new AccessControlHttpClient("http://localhost:8080", jwtToken);
    }

    @Test
    @Order(1)
    void testSignupAndLoginFlow() throws Exception {
        List<OrgInfo> orgs = loginClient.validate(testUser.email(), testUser.password());
        assertFalse(orgs.isEmpty());

        OrgInfo org = orgs.get(0);
        assertNotNull(org.orgId());

        String jwt = loginClient.login(testUser.email(), testUser.password(), org.orgId());
        assertNotNull(jwt);
        assertTrue(jwt.length() > 10);
    }

    @Test
    @Order(2)
    void testAddClusterToOrganization() throws Exception {
        String response = clusterClient.addCluster(orgId, "cluster-integration3", "Integration test cluster", true);
        assertNotNull(response);
        assertTrue(response.contains("cluster-integration3"));
    }

    @Test
    @Order(3)
    void testAddUserToOrganization() throws Exception {
        User newUser = new User(null, "Integration User.1", "intuser@example123com",
                "UserPass123", "USER", "Added by integration test", LocalDateTime.now(), LocalDateTime.now());
        User createdUser = userClient.addUser(orgId, newUser);
        assertNotNull(createdUser);
        assertEquals("Integration User.1", createdUser.name());
    }

    @Test
    @Order(4)
    void testAssignClusterToUser() throws Exception {
        boolean result = clusterAssignmentHttpClient.toggleAssignment(
                orgId, "USER", "intuser@example123com", "cluster-integration3", "assign");
        assertTrue(result);
    }

    @Test
    @Order(5)
    void testCreateAccessControl() throws Exception {
        AccessRow accessRow = new AccessRow(
                "cluster-integration3",
                "USER",
                "intuser@example123com",
                "dbname",
                "main",
                "table_name",
                "BASE_TABLE",
                List.of("col1", "col2"),
                "col1 > 5",
                null,
                "1999-12-20",
                null
        );

        String response = accessControlClient.createAccess(orgId, "cluster-integration3", accessRow);
        assertNotNull(response);
        assertTrue(response.contains("successfully"));
    }

    @Test
    @Order(6)
    void testGetAccessControlByCluster() throws Exception {
        String results = accessControlClient.getByCluster(orgId, "cluster-integration3");
        assertNotNull(results);
        assertTrue(results.contains("cluster-integration3"));
    }

    @Test
    @Order(7)
    void testGetAccessControlBySource() throws Exception {
        String results = accessControlClient.getBySource(orgId, "USER", "intuser@example123com");
        assertNotNull(results);
        assertTrue(results.contains("intuser@example123com"));
    }

    @Test
    @Order(8)
    void testUpdateAccessControl() throws Exception {
        AccessRow updatedRow = new AccessRow(
                "cluster-integration3",
                "USER",
                "intuser@example123com",
                "dbname",
                "main",
                "table_name",
                "BASE_TABLE",
                List.of("c1", "c2"),
                "c1 < 100",
                null,
                LocalDate.now().plusDays(14).toString(),
                null
        );

        String result = accessControlClient.updateAccess(orgId, "cluster-integration3", updatedRow);
        assertNotNull(result);
        assertTrue(result.contains("successfully"));
    }

    @Test
    @Order(9)
    void testDeleteAccessControl() throws Exception {
        String result = accessControlClient.deleteAccess(orgId,
                "cluster-integration3",
                "USER",
                "intuser@example123com",
                "BASE_TABLE");
        assertNotNull(result);
        assertTrue(result.contains("deleted"));
    }
}
