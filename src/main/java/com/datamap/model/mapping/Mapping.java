package com.datamap.model.mapping;

import com.datamap.model.TargetColumn;

public abstract class Mapping {
    protected TargetColumn targetColumn;
    
    public Mapping(TargetColumn targetColumn) {
        this.targetColumn = targetColumn;
    }
    
    public TargetColumn getTargetColumn() {
        return targetColumn;
    }
    
    public abstract String generateCode();
}