package com.one211.IntegrationTest.model;

import java.time.LocalDateTime;

public record User (Long id, String fullName, String userName, String email, String password, String role, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {}
