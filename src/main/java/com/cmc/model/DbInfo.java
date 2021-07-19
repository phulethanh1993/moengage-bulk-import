package com.cmc.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DbInfo {
    private String dbName;
    private String schema;
    private String tableName;
}
