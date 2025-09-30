package com.one211.restapi.service;

import com.one211.restapi.model.Group;
import com.one211.restapi.model.OrgInfo;
import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GroupHttpClientIntegrationTest {

    private static LoginClient loginClient;
    private static GroupHttpClient groupClient;
    private static long orgId;
    private static String jwtToken;

    private static final TestUser testUser = new TestUser(
            "demo.integration@example.com",
            "Admin123"
    );

    @BeforeAll
    static void setup() throws Exception {
        HttpClientProvider provider = new HttpClientProvider();
        loginClient = new LoginClient(provider);
        List<OrgInfo> orgs = loginClient.validate(testUser.email(), testUser.password());
        OrgInfo org = orgs.getFirst();
        jwtToken = loginClient.login(testUser.email(), testUser.password(), org.orgId());
        groupClient = new GroupHttpClient("http://localhost:8080", jwtToken);
        orgId = org.orgId();
    }

    @Test
    @Order(1)
    void testCreateGroup() throws Exception {
        Group group = groupClient.createGroup(orgId, "demo-group", "test group");
        assertNotNull(group, "Group should not be null");
        assertEquals("demo-group", group.name());
        assertEquals("test group", group.description());
    }
    @Test
    @Order(2)
    void testHandleUserInGroup() throws Exception {
        String response = groupClient.handleUserInGroup(orgId, testUser.email(),
                "demo-group", "add");
        assertNotNull(response, "Response should not be null");

    }
    @Test
    @Order(3)
    void testDeleteGroup() throws Exception {
        boolean deleted = groupClient.deleteGroup(orgId, "demo-group");
        assertTrue(deleted, "Group deleted successfully");

    }


    private static class TestUser {
        private final String email;
        private final String password;
        public TestUser(String email, String password) {
            this.email = email;
            this.password = password;
        }
        public String email() {
            return email;
        }
        public String password() {
            return password;
        }
    }
}
