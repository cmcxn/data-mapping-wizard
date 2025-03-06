package com.datamap.model;

/**
 * Represents a target table in the data mapping
 */
public class TargetTable {
    private SourceTable sourceTable;
    private Table table;
    private String dataSourceName; // Name of associated data source

    public TargetTable(SourceTable sourceTable, Table table) {
        this.sourceTable = sourceTable;
        this.table = table;
    }

    public TargetTable(SourceTable sourceTable, Table table, String dataSourceName) {
        this.sourceTable = sourceTable;
        this.table = table;
        this.dataSourceName = dataSourceName;
    }

    public SourceTable getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(SourceTable sourceTable) {
        this.sourceTable = sourceTable;
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