package com.one211.IntegrationTest.model;


import java.util.HashMap;
import java.util.Map;

public record LoginRequest(String emailOrUserName, String password, Map<String, String> claims) {
    public LoginRequest(String emailOrUserName, String password) {
        this(emailOrUserName, password, new HashMap<>());
    }
}
