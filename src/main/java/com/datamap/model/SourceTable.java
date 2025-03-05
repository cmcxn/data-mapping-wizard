package com.datamap.model;

public class SourceTable {
    private Table table;
    
    public SourceTable(Table table) {
        this.table = table;
    }
    
    public Table getTable() {
        return table;
    }
    
    @Override
    public String toString() {
        return "SourceTable(" + table.getName() + ")";
    }
}