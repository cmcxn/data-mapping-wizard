package com.datamap.model;

/**
 * Represents a source table in the data mapping
 */
public class SourceTable {
    private Table table;
    private String dataSourceName; // Name of associated data source

    public SourceTable(Table table) {
        this.table = table;
    }

    public SourceTable(Table table, String dataSourceName) {
        this.table = table;
        this.dataSourceName = dataSourceName;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }
    
    public String getName() {
        return table.getName();
    }
}