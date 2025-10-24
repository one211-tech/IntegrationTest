package com.one211.restapi.service;

import com.one211.restapi.model.Cluster;
import com.one211.restapi.model.OrgInfo;
import com.one211.restapi.model.SignUp;
import io.dazzleduck.sql.flight.server.auth2.AuthUtils;
import org.apache.arrow.flight.FlightClient;
import org.apache.arrow.flight.FlightInfo;
import org.apache.arrow.flight.Location;
import org.apache.arrow.flight.sql.FlightSqlClient;
import org.apache.arrow.memory.RootAllocator;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IntegrationTest {

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

        // Setup Flight SQL client
        Location controllerLocation = Location.forGrpcTls("localhost", 59307);
        Map<String, String> claims = Map.of(
                "cluster", "cluster101",
                "database", TEST_DATABASE,
                "catalog", TEST_CATALOG,
                "schema", TEST_SCHEMA,
                "table", TEST_TABLE,
                "filter", TEST_FILTER,
                "path", TEST_PATH,
                "function", TEST_FUNCTION
        );

        flightClient = FlightClient.builder()
                .allocator(new RootAllocator())
                .location(controllerLocation)
                .intercept(AuthUtils.createClientMiddlewareFactory(USER, PASSWORD, claims))
                .verifyServer(false)
                .build();

        flightSqlClient = new FlightSqlClient(flightClient);
        System.out.println("Connected to " + controllerLocation.getUri());
    }

    @Test
    @Order(1)
    void executeGetFlightInfo() throws InterruptedException {
        Thread.sleep(10_000);
        String query = "SELECT 1";
        FlightInfo flightInfo = flightSqlClient.execute(query);
        assertNotNull(flightInfo);
        System.out.println("FlightInfo successfully retrieved: " + flightInfo);
    }

}
