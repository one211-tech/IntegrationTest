//package com.one211.restapi.service;
//
//import com.one211.restapi.model.SignUp;
//import com.one211.restapi.model.OrgInfo;
//
//import org.junit.jupiter.api.*;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//public class AddUserHttpClientIntegrationTest {
//
//    private static LoginClient loginClient;
//    private static UserClient addUserClient;
//
//    private static final String BASE_URL = "http://localhost:8080/api"; // Adjust if needed
//    private static final SignUp testUser = new SignUp(
//            "Demo Admin",
//            "demoadmin@example.com",
//            "Admin123",
//            "MyOrg",
//            "Demo Organization",
//            null,
//            null
//    );
//
//    private static String jwtToken;
//    private static Long orgId;
//
//    @BeforeAll
//    static void setup() throws Exception {
//        HttpClientProvider provider = new HttpClientProvider();
//        loginClient = new LoginClient(provider);
//
//        // Ensure test user exists
//        try {
//            new com.one211.restapi.service.SignupClient(provider).signUp(testUser);
//        } catch (Exception ignored) {}
//
//        // Login and get JWT
//        List<OrgInfo> orgs = loginClient.validate(testUser.email(), testUser.password());
//        orgId = Long.valueOf(orgs.getFirst().orgId());
//        jwtToken = loginClient.login(testUser.email(), testUser.password(), Math.toIntExact(orgId));
//
//        addUserClient = new UserClient(BASE_URL, jwtToken);
//    }
//
//    @Test
//    @Order(1)
//    void addUser_shouldSucceed() throws Exception {
//        String email = "integrationuser@example.com";
//
//        String response = addUserClient.addUser(
//                orgId,
//                "Integration User",
//                email,
//                "User123",
//                "USER",
//                "Integration test user"
//        );
//
//        assertNotNull(response, "Response should not be null");
//        assertTrue(response.contains(email), "Response should contain the new user's email");
//    }
//
//    @Test
//    @Order(2)
//    void addUser_duplicate_shouldFail() {
//        String email = "integrationuser@example.com"; // same as previous test
//
//        Exception ex = assertThrows(IllegalStateException.class, () -> {
//            addUserClient.addUser(
//                    orgId,
//                    "Integration User Duplicate",
//                    email,
//                    "User123",
//                    "USER",
//                    "Duplicate test"
//            );
//        });
//
//        assertTrue(ex.getMessage().contains("Failed to add user"));
//    }
//}
