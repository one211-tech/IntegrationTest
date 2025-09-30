package com.one211.restapi.model;

public record AccessRequest(String clusterName, String sourceType, String sourceName, String tableType) {}
