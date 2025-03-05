package com.datamap.model.mapping;

import com.datamap.model.SourceColumn;
import com.datamap.model.TargetColumn;

public class Dict extends Mapping {
    private String dictType;
    private SourceColumn sourceColumn;
    private String sqlSession;
    
    public Dict(TargetColumn targetColumn, String dictType, SourceColumn sourceColumn, String sqlSession) {
        super(targetColumn);
        this.dictType = dictType;
        this.sourceColumn = sourceColumn;
        this.sqlSession = sqlSession;
    }
    
    public String getDictType() {
        return dictType;
    }
    
    public SourceColumn getSourceColumn() {
        return sourceColumn;
    }
    
    public String getSqlSession() {
        return sqlSession;
    }
    
    @Override
    public String generateCode() {
        // Format properly with underscore notation
        String targetVarName = targetColumn.getTable().getName() + "_" + targetColumn.getName();
        String sourceVarName = sourceColumn.getTable().getName() + "_" + sourceColumn.getName();
        return "set.add(new Dict(" + targetVarName + ",\"" + dictType + "\"," + sourceVarName + "," + sqlSession + "));";
    }
}