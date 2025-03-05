package com.datamap.model;

public class TargetColumn extends Column {
    private TargetTable targetTable;
    
    public TargetColumn(TargetTable targetTable, String name) {
        super(targetTable.getTable(), name);
        this.targetTable = targetTable;
    }
    
    public TargetTable getTargetTable() {
        return targetTable;
    }
}