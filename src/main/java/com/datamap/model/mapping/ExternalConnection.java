package com.datamap.model.mapping;

import com.datamap.model.SourceColumn;
import com.datamap.model.TargetColumn;

import java.util.ArrayList;
import java.util.List;

public class ExternalConnection extends Mapping {
    private SourceColumn finalSelectColumn;
    private SourceColumn finalIdColumn;
    private SourceColumn sourceIdColumn;
    private List<LeftJoin> joins;
    
    public ExternalConnection(TargetColumn targetColumn, SourceColumn finalSelectColumn, 
                             SourceColumn finalIdColumn, SourceColumn sourceIdColumn) {
        super(targetColumn);
        this.finalSelectColumn = finalSelectColumn;
        this.finalIdColumn = finalIdColumn;
        this.sourceIdColumn = sourceIdColumn;
        this.joins = new ArrayList<>();
    }
    
    public SourceColumn getFinalSelectColumn() {
        return finalSelectColumn;
    }
    
    public SourceColumn getFinalIdColumn() {
        return finalIdColumn;
    }
    
    public SourceColumn getSourceIdColumn() {
        return sourceIdColumn;
    }
    
    public List<LeftJoin> getJoins() {
        return joins;
    }
    
    public ExternalConnection addJoin(LeftJoin join) {
        this.joins.add(join);
        return this;
    }
    
    public void removeJoin(int index) {
        if (index >= 0 && index < joins.size()) {
            joins.remove(index);
        }
    }
    
    @Override
    public String generateCode() {
        // Format properly with underscore notation
        String targetVarName = targetColumn.getTable().getName() + "_" + targetColumn.getName();
        String finalSelectVarName = finalSelectColumn.getTable().getName() + "_" + finalSelectColumn.getName();
        String finalIdVarName = finalIdColumn.getTable().getName() + "_" + finalIdColumn.getName();
        String sourceIdVarName = sourceIdColumn.getTable().getName() + "_" + sourceIdColumn.getName();
        
        StringBuilder codeBuilder = new StringBuilder();
        codeBuilder.append("set.add(new ExternalConnection(")
                 .append(targetVarName).append(",")
                 .append(finalSelectVarName).append(",")
                 .append(finalIdVarName).append(",")
                 .append(sourceIdVarName).append(")");
        
        // Add joins if any
        if (!joins.isEmpty()) {
            for (LeftJoin join : joins) {
                String leftVarName = join.getLeftColumn().getTable().getName() + "_" + join.getLeftColumn().getName();
                String rightVarName = join.getRightColumn().getTable().getName() + "_" + join.getRightColumn().getName();
                codeBuilder.append("\n        .addJoin(new LeftJoin(")
                         .append(leftVarName).append(",")
                         .append(rightVarName).append("))");
            }
        }
        
        codeBuilder.append(");");
        
        return codeBuilder.toString();
    }
}