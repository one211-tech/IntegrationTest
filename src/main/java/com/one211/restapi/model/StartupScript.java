package com.one211.restapi.model;


import java.time.LocalDateTime;

public record StartupScript(Integer orgId, String clusterName, String version, String content, LocalDateTime createdAt, LocalDateTime updatedAt) {}