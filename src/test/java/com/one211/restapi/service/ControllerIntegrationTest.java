package com.one211.restapi.service;

import io.dazzleduck.sql.flight.server.auth2.AuthUtils;
import org.apache.arrow.flight.*;
import org.apache.arrow.flight.sql.FlightSqlClient;
import org.apache.arrow.memory.RootAllocator;
import org.junit.jupiter.api.*;

import java.util.Map;

public class ControllerIntegrationTest {

    private static final String USER = "cluster101";
    private static final String PASSWORD = "123456";
    private static final String TEST_DATABASE = "memory";
    private static final String TEST_CATALOG = "test_catalog";
    private static final String TEST_SCHEMA = "test_schema";
    private static final String TEST_TABLE = "";
    private static final String TEST_FILTER = "";
    private static final String TEST_PATH = "";
    private static final String TEST_FUNCTION = "";
    private static FlightSqlClient flightSqlClient;

    @BeforeAll
    public static void setup() throws Exception {


        Location controllerLocation = Location.forGrpcTls("localhost", 59307);
        Map<String, String> claims = Map.of(
                "cluster","cluster101",
                "database", TEST_DATABASE,
                "catalog", TEST_CATALOG,
                "schema", TEST_SCHEMA,
                "table", TEST_TABLE,
                "filter", TEST_FILTER,
                "path", TEST_PATH,
                "function", TEST_FUNCTION
        );

        FlightClient flightClient = FlightClient.builder()
                .allocator(new RootAllocator())
                .location(controllerLocation)
                .intercept(AuthUtils.createClientMiddlewareFactory(USER, PASSWORD, claims))
                .verifyServer(false)
                .build();

        flightSqlClient = new FlightSqlClient(flightClient);

        System.out.println("connected " + controllerLocation.getUri());
    }

    @Test
    public void executeGetFlightInfo() {

        String query = "SELECT 1";
        FlightInfo flightInfo = flightSqlClient.execute(query);
        Assertions.assertNotNull(flightInfo);
        System.out.println(" FlightInfo successfully: " + flightInfo);

    }
}
