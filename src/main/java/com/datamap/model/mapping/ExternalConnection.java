package com.datamap.model.mapping;

import com.datamap.model.SourceColumn;
import com.datamap.model.TargetColumn;

public class ExternalConnection extends Mapping {
    private SourceColumn externalSourceColumn;
    private SourceColumn externalIdColumn;
    private SourceColumn sourceIdColumn;

    
    public ExternalConnection(TargetColumn targetColumn, SourceColumn externalSourceColumn, 
                             SourceColumn externalIdColumn, SourceColumn sourceIdColumn ) {
        super(targetColumn);
        this.externalSourceColumn = externalSourceColumn;
        this.externalIdColumn = externalIdColumn;
        this.sourceIdColumn = sourceIdColumn;

    }
    
    public SourceColumn getExternalSourceColumn() {
        return externalSourceColumn;
    }
    
    public SourceColumn getExternalIdColumn() {
        return externalIdColumn;
    }
    
    public SourceColumn getSourceIdColumn() {
        return sourceIdColumn;
    }

    
    @Override
    public String generateCode() {
        // Format properly with underscore notation
        String targetVarName = targetColumn.getTable().getName() + "_" + targetColumn.getName();
        String externalSourceVarName = externalSourceColumn.getTable().getName() + "_" + externalSourceColumn.getName();
        String externalIdVarName = externalIdColumn.getTable().getName() + "_" + externalIdColumn.getName();
        String sourceIdVarName = sourceIdColumn.getTable().getName() + "_" + sourceIdColumn.getName();
        
        return "set.add(new ExternalConnection(" + targetVarName + "," + externalSourceVarName + "," + 
               externalIdVarName + "," + sourceIdVarName +   "));";
    }
}