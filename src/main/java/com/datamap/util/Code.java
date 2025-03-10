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
        String classname = null;
        for (TargetTable targetTable : targetTables.values()) {
            classname = targetTable.getTable().getName();
            break;
        }

        String before ="package Util.impl;\n" +
                "\n" +
                "import Util.ComputationalLogicSet;\n" +
                "import core.mapper.*;\n" +
                "import core.metadata.SourceColumn;\n" +
                "import core.metadata.SourceTable;\n" +
                "import core.metadata.TargetColumn;\n" +
                "import core.metadata.TargetTable;\n" +
                "\n" +
                "import java.util.LinkedHashSet;\n" +
                "import java.util.Set;\n" +
                "\n" +
                "import static Util.PgSink.setSource;\n" +
                "import static Util.PgSink.setTarget;\n" +
                "\n" +
                "public class "+classname+" implements ComputationalLogicSet {\n" +
                "    @Override\n" +
                "    public Set<ComputationalLogic> getComputationalLogicSet() {\n" ;

        code.append(before);
        // Define set
        code.append("Set<Object> set = new LinkedHashSet<>();\n\n");

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

                StringBuilder commentBuilder = new StringBuilder();
                commentBuilder.append("//查询[").append(externalMapping.getFinalSelectColumn().getTable().getName())
                        .append("].{").append(externalMapping.getFinalSelectColumn().getName()).append("}，条件是[")
                        .append(externalMapping.getWhereSelectTable().getTable().getName()).append("].{")
                        .append(externalMapping.getWhereIdColumn().getName()).append("}=[")
                        .append(externalMapping.getSourceIdColumn().getTable().getName()).append("].{")
                        .append(externalMapping.getSourceIdColumn().getName()).append("}-->");
                
                // Add LEFT JOIN comments if any
                List<LeftJoin> joins = externalMapping.getJoins();
                if (!joins.isEmpty()) {
                    commentBuilder.append(" 通过");
                    for (int i = 0; i < joins.size(); i++) {
                        LeftJoin join = joins.get(i);
                        if (i > 0) {
                            commentBuilder.append(" 和");
                        }
                        commentBuilder.append(" LEFT JOIN ")
                                    .append(join.getLeftColumn().getTable().getName())
                                    .append(".")
                                    .append(join.getLeftColumn().getName())
                                    .append(" = ")
                                    .append(join.getRightColumn().getTable().getName())
                                    .append(".")
                                    .append(join.getRightColumn().getName());
                    }
                }
                
                commentBuilder.append(externalMapping.getTargetColumn().getTable().getName())
                            .append("的")
                            .append(externalMapping.getTargetColumn().getName());
                
                code.append(commentBuilder.toString()).append("\n");
                code.append("    ").append(externalMapping.generateCode()).append("\n");
            }
        }

        String end = "        return set;\n" +
                "    }\n" +
                "}\n";
        code.append(end);
        return code.toString();
    }
}