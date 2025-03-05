package com.datamap.model;

public class SourceColumn extends Column {
    private SourceTable sourceTable;
    
    public SourceColumn(SourceTable sourceTable, String name) {
        super(sourceTable.getTable(), name);
        this.sourceTable = sourceTable;
    }
    
    public SourceTable getSourceTable() {
        return sourceTable;
    }
}