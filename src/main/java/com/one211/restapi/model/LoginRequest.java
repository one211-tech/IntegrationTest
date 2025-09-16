package com.one211.restapi.model;


import java.util.Map;

public record LoginRequest(
        String email,
        String password,
        Map<String, String> claims
) {}