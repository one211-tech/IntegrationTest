package com.one211.IntegrationTest.model;

import java.time.LocalDateTime;

public record Cluster(Long id, Long orgId, String name, String description, Boolean status, LocalDateTime createdAt, String password) {}
