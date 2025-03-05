package com.datamap.model;

public abstract class Column {
    private Table table;
    private String name;
    
    public Column(Table table, String name) {
        this.table = table;
        this.name = name;
    }
    
    public Table getTable() {
        return table;
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return table.getName() + "." + name;
    }
}