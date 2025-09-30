package com.one211.restapi.model;
public record OrgInfo(
        String name,
        String email,
        String password,
        String role,
        Integer orgId,
        String orgName
) {}