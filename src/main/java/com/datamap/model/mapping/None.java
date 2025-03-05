package com.datamap.model.mapping;

import com.datamap.model.SourceColumn;
import com.datamap.model.TargetColumn;

public class None extends Mapping {
    private SourceColumn sourceColumn;
    
    public None(TargetColumn targetColumn, SourceColumn sourceColumn) {
        super(targetColumn);
        this.sourceColumn = sourceColumn;
    }
    
    public SourceColumn getSourceColumn() {
        return sourceColumn;
    }
    
    @Override
    public String generateCode() {
        // Format properly with underscore notation
        String targetVarName = targetColumn.getTable().getName() + "_" + targetColumn.getName();
        String sourceVarName = sourceColumn.getTable().getName() + "_" + sourceColumn.getName();
        return "set.add(new None(" + targetVarName + "," + sourceVarName + "));";
    }
}