package com.datamap.model.mapping;

import com.datamap.model.SourceColumn;
import com.datamap.model.TargetColumn;

public class ExternalConnection extends Mapping {
    private SourceColumn externalSourceColumn;
    private SourceColumn externalIdColumn;
    private SourceColumn sourceIdColumn;
    private String sqlSession;
    
    public ExternalConnection(TargetColumn targetColumn, SourceColumn externalSourceColumn, 
                             SourceColumn externalIdColumn, SourceColumn sourceIdColumn, String sqlSession) {
        super(targetColumn);
        this.externalSourceColumn = externalSourceColumn;
        this.externalIdColumn = externalIdColumn;
        this.sourceIdColumn = sourceIdColumn;
        this.sqlSession = sqlSession;
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
    
    public String getSqlSession() {
        return sqlSession;
    }
    
    @Override
    public String generateCode() {
        // Format properly with underscore notation
        String targetVarName = targetColumn.getTable().getName() + "_" + targetColumn.getName();
        String externalSourceVarName = externalSourceColumn.getTable().getName() + "_" + externalSourceColumn.getName();
        String externalIdVarName = externalIdColumn.getTable().getName() + "_" + externalIdColumn.getName();
        String sourceIdVarName = sourceIdColumn.getTable().getName() + "_" + sourceIdColumn.getName();
        
        return "set.add(new ExternalConnection(" + targetVarName + "," + externalSourceVarName + "," + 
               externalIdVarName + "," + sourceIdVarName + "," + sqlSession + "));";
    }
}