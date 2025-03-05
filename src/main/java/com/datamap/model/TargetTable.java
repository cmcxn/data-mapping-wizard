package com.datamap.model;

public class TargetTable {
    private SourceTable sourceTable;
    private Table table;
    
    public TargetTable(SourceTable sourceTable, Table table) {
        this.sourceTable = sourceTable;
        this.table = table;
    }
    
    public SourceTable getSourceTable() {
        return sourceTable;
    }
    
    public Table getTable() {
        return table;
    }
    
    @Override
    public String toString() {
        return "TargetTable(" + table.getName() + ")";
    }
}