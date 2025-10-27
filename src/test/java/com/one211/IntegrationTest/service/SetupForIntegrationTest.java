package com.one211.IntegrationTest.service;

import com.one211.IntegrationTest.model.Cluster;
import com.one211.IntegrationTest.model.OrgInfo;
import com.one211.IntegrationTest.model.SignUp;
import org.apache.arrow.flight.FlightClient;
import org.apache.arrow.flight.sql.FlightSqlClient;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SetupForIntegrationTest {

    private static SignupClient signupClient;
    private static LoginClient loginClient;
    private static final String USER = "cluster101";
    private static final String PASSWORD = "123456";
    private static final String TEST_DATABASE = "memory";
    private static final String TEST_CATALOG = "test_catalog";
    private static final String TEST_SCHEMA = "test_schema";
    private static final String TEST_TABLE = "";
    private static final String TEST_FILTER = "";
    private static final String TEST_PATH = "";
    private static final String TEST_FUNCTION = "";

    private FlightSqlClient flightSqlClient;
    private FlightClient flightClient;
    private ClusterHttpClient clusterClient;
    private UserClient userClient;
    private ClusterAssignmentHttpClient clusterAssignmentHttpClient;
    private StartupScriptHttpClient startupScriptClient;

    private SignUp testUser;
    private long orgId;
    private Cluster testcluster;
    private static HttpClientProvider provider = new HttpClientProvider();

    @BeforeAll
    void init() throws Exception {
        signupClient = new SignupClient(provider);
        loginClient = new LoginClient(provider);

        //  test user
        testUser = new SignUp(
                "Demo Admin",
                "demo_admin",
                "Admin123@gmail.com",
                "Admin123",
                "Test Org one211",
                "test organization",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        try {
            signupClient.signUp(testUser);
        } catch (RuntimeException ignored) {
        }

        List<OrgInfo> orgs = loginClient.validate(testUser.email(), testUser.password());
        OrgInfo org = orgs.getFirst();
        orgId = org.orgId();
    }

    @BeforeEach
    void setup() throws Exception {
        List<OrgInfo> orgs = loginClient.validate(testUser.email(), testUser.password());
        OrgInfo org = orgs.getFirst();
        String jwtToken = loginClient.login(testUser.email(), testUser.password(), org.orgId());

        clusterClient = new ClusterHttpClient(provider.getBaseUrl(), jwtToken, provider);
        userClient = new UserClient(jwtToken, provider);
        testcluster = new Cluster(
                null,
                orgId,
                "cluster101",
                "Integration test cluster",
                true,
                LocalDateTime.now(),
                "123456"
        );
        clusterClient.addCluster(testcluster);
        startupScriptClient = new StartupScriptHttpClient("http://localhost:8080", jwtToken);
        startupScriptClient.createStartupScript(org.orgId(), "cluster101", "select 6;");

    }

}
