package com.datamap.model.mapping;

import com.datamap.model.TargetColumn;

public class Constant extends Mapping {
    private String constantValue;
    
    public Constant(TargetColumn targetColumn, String constantValue) {
        super(targetColumn);
        this.constantValue = constantValue;
    }
    
    public String getConstantValue() {
        return constantValue;
    }
    
    @Override
    public String generateCode() {
        // Format properly with underscore notation
        String targetVarName = targetColumn.getTable().getName() + "_" + targetColumn.getName();
        return "set.add(new Constant(" + targetVarName + ",\"" + constantValue + "\"));";
    }
}