package com.datamap.model.mapping;

import com.datamap.model.SourceColumn;

public class LeftJoin {
    private SourceColumn leftColumn;
    private SourceColumn rightColumn;
    
    public LeftJoin(SourceColumn leftColumn, SourceColumn rightColumn) {
        this.leftColumn = leftColumn;
        this.rightColumn = rightColumn;
    }
    
    public SourceColumn getLeftColumn() {
        return leftColumn;
    }
    
    public SourceColumn getRightColumn() {
        return rightColumn;
    }
    
    @Override
    public String toString() {
        return leftColumn.getTable().getName() + "." + leftColumn.getName() + 
               " = " + 
               rightColumn.getTable().getName() + "." + rightColumn.getName();
    }
}