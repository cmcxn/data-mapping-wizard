package com.datamap.model.mapping;

import com.datamap.model.SourceColumn;
import com.datamap.model.TargetColumn;

public class Dict extends Mapping {
    private String dictType;
    private SourceColumn sourceColumn;

    public Dict(TargetColumn targetColumn, String dictType, SourceColumn sourceColumn ) {
        super(targetColumn);
        this.dictType = dictType;
        this.sourceColumn = sourceColumn;
    }
    
    public String getDictType() {
        return dictType;
    }
    
    public SourceColumn getSourceColumn() {
        return sourceColumn;
    }
    

    @Override
    public String generateCode() {
        // Format properly with underscore notation
        String targetVarName = targetColumn.getTable().getName() + "_" + targetColumn.getName();
        String sourceVarName = sourceColumn.getTable().getName() + "_" + sourceColumn.getName();
        return "set.add(new Dict(" + targetVarName + ",\"" + dictType + "\"," + sourceVarName +  "));";
    }
}