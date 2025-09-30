package com.one211.restapi.model;

import java.sql.Timestamp;
import java.util.List;

public record AccessRow(
        String clusterName,
        String sourceType, // USER OR GROUP
        String sourceName, // userName or groupName
        String database,
        String schema,
        String tableOrPath, // tableName or path
        String tableType, // BASE_TABLE OR FUNCTION_TABLE
        List<String> columns,
        String filter,
        String functionName,
        String expiration,
        Timestamp accessTime
) {}
