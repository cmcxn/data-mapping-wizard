package com.datamap.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Table {
    private String name;
    private List<String> columns = new ArrayList<>();

    public Table(String name, String... columns) {
        this.name = name;
        this.columns.addAll(Arrays.asList(columns));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getColumns() {
        return columns;
    }
    
    public void addColumn(String column) {
        if (!columns.contains(column)) {
            columns.add(column);
        }
    }
    
    public void removeColumn(String column) {
        columns.remove(column);
    }
    
    @Override
    public String toString() {
        return name;
    }
}