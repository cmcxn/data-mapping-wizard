package com.datamap.util;

import com.datamap.model.SourceColumn;
import com.datamap.model.SourceTable;
import com.datamap.model.TargetColumn;
import com.datamap.model.TargetTable;
import com.datamap.model.mapping.*;

import java.util.List;
import java.util.Map;

public class Code {
    public static String generateCode(Map<String, SourceTable> sourceTables,
                                      Map<String, TargetTable> targetTables,
                                      Map<String, SourceColumn> sourceColumns,
                                      Map<String, TargetColumn> targetColumns,
                                      List<Mapping> mappings
    ) {
        StringBuilder code = new StringBuilder();

        // Define set
        code.append("Set<Mapping> set = new HashSet<>();\n\n");

        // Define source tables
        for (SourceTable sourceTable : sourceTables.values()) {
            code.append("SourceTable ").append(sourceTable.getTable().getName()).append(" = setSource(\"")
                    .append(sourceTable.getTable().getName()).append("\" ");


            code.append(");\n");
        }

        // Define target tables
        for (TargetTable targetTable : targetTables.values()) {
            code.append("TargetTable ").append(targetTable.getTable().getName()).append(" = setTarget(")
                    .append(targetTable.getSourceTable().getTable().getName()).append(", \"")
                    .append(targetTable.getTable().getName()).append("\" ");

            code.append(" );\n");
        }

        code.append("\n");

        // Define source columns
        for (SourceColumn sourceColumn : sourceColumns.values()) {
            code.append("SourceColumn ").append(sourceColumn.getTable().getName()).append("_")
                    .append(sourceColumn.getName()).append(" = new SourceColumn(")
                    .append(sourceColumn.getTable().getName()).append(",\"")
                    .append(sourceColumn.getName()).append("\");\n");
        }

        code.append("\n");

        // Define target columns
        for (TargetColumn targetColumn : targetColumns.values()) {
            code.append("TargetColumn ").append(targetColumn.getTable().getName()).append("_")
                    .append(targetColumn.getName()).append(" = new TargetColumn(")
                    .append(targetColumn.getTable().getName()).append(",\"")
                    .append(targetColumn.getName()).append("\");\n");
        }

        code.append("\n");

        // Add mappings with comments
        for (Mapping mapping : mappings) {
            if (mapping instanceof None) {
                None noneMapping = (None) mapping;
                code.append("//").append(noneMapping.getTargetColumn().getTable().getName()).append("的")
                        .append(noneMapping.getTargetColumn().getName()).append("-->")
                        .append(noneMapping.getSourceColumn().getTable().getName()).append("的")
                        .append(noneMapping.getSourceColumn().getName()).append("\n");
                code.append(mapping.generateCode()).append("\n");
            } else if (mapping instanceof Dict) {
                Dict dictMapping = (Dict) mapping;
                code.append("//根据").append(dictMapping.getSourceColumn().getTable().getName()).append("的")
                        .append(dictMapping.getSourceColumn().getName()).append("与给定的字典类型（")
                        .append(dictMapping.getDictType()).append("）查询字典表的name-->")
                        .append(dictMapping.getTargetColumn().getTable().getName()).append("的")
                        .append(dictMapping.getTargetColumn().getName()).append("\n");
                code.append(mapping.generateCode()).append("\n");
            } else if (mapping instanceof Constant) {
                Constant constantMapping = (Constant) mapping;
                code.append("//固定字符串-->").append(constantMapping.getTargetColumn().getTable().getName())
                        .append("的").append(constantMapping.getTargetColumn().getName()).append("\n");
                code.append(mapping.generateCode()).append("\n");
            } else if (mapping instanceof ExternalConnection) {
                ExternalConnection externalMapping = (ExternalConnection) mapping;

                code.append("//查询[").append(externalMapping.getExternalSourceColumn().getTable().getName())
                        .append("].{").append(externalMapping.getExternalIdColumn().getName()).append("}为[")
                        .append(externalMapping.getSourceIdColumn().getTable().getName()).append("].{")
                        .append(externalMapping.getSourceIdColumn().getName()).append("}的[")
                        .append(externalMapping.getExternalSourceColumn().getTable().getName()).append("].{")
                        .append(externalMapping.getExternalSourceColumn().getName()).append("}-->")
                        .append(externalMapping.getTargetColumn().getTable().getName()).append("的")
                        .append(externalMapping.getTargetColumn().getName()).append("\n");
                code.append("    ").append(mapping.generateCode()).append("\n");

            }
        }

        return code.toString();
    }

}
