package com.one211.restapi.model;

import java.time.LocalDateTime;

public record SignUp(
        String name,
        String email,
        String password,
        String orgName,
        String orgDescription,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
