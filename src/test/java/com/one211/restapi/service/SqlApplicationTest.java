package com.one211.restapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.one211.restapi.model.OrgInfo;
import io.dazzleduck.sql.flight.server.auth2.AuthUtils;
import org.apache.arrow.flight.FlightClient;
import org.apache.arrow.flight.Location;
import org.apache.arrow.flight.sql.FlightSqlClient;
import org.apache.arrow.memory.RootAllocator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.one211.restapi.model.SignUp;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class SqlApplicationTest {

    private static FlightSqlClient client;
    private static RootAllocator clientAllocator;
    private static final String HOST = "0.0.0.0";
    private static final int PORT = 59307;
    private static SignupClient signupClient;
    private static LoginClient loginClient;

    private static SignUp testUser;
    private static long orgId;

    @BeforeAll
    public static void setup() throws Exception {
        HttpClientProvider provider = new HttpClientProvider();
        SignupClient signupClient = new SignupClient(provider);
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

        // Try signup (ignore if already exists)
        try {
            signupClient.signUp(testUser);
        } catch (RuntimeException ignored) {}

        // Validate org
        List<OrgInfo> orgs = loginClient.validate(testUser.email(), testUser.password());
        OrgInfo org = orgs.getFirst();
        orgId = org.orgId();
    }

    private static FlightSqlClient getClient() {
        clientAllocator = new RootAllocator();
        Location location =Location.forGrpcTls(HOST, PORT);

        FlightClient flightClient = FlightClient.builder()
                .location(location)
                .allocator(clientAllocator)
                .intercept(AuthUtils.createClientMiddlewareFactory(
                        testUser.email(),
                        testUser.password(),
                        Map.of("database", "memory",
                                "schema", "main",
                                "cluster", "cc1",
                                "login.url", "http://localhost:8080/api/login"
                )))
                .verifyServer(false)
                .build();

        return new FlightSqlClient(flightClient);
    }

    @Test
    void loginTest() throws Exception {
        var result =getClient().execute("select 1");
        assertNotNull(result);


    }

    @AfterAll
    public static void cleanup() throws Exception {
        if (client != null) client.close();
        if (clientAllocator != null) clientAllocator.close();
    }
}
