package com.datamap.util;

import com.datamap.model.*;
import com.datamap.model.mapping.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonConfig {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Configuration {
        private List<TableConfig> sourceTables = new ArrayList<>();
        private List<TableConfig> targetTables = new ArrayList<>();
        private List<MappingConfig> mappings = new ArrayList<>();

        public List<TableConfig> getSourceTables() {
            return sourceTables;
        }

        public void setSourceTables(List<TableConfig> sourceTables) {
            this.sourceTables = sourceTables;
        }

        public List<TableConfig> getTargetTables() {
            return targetTables;
        }

        public void setTargetTables(List<TableConfig> targetTables) {
            this.targetTables = targetTables;
        }

        public List<MappingConfig> getMappings() {
            return mappings;
        }

        public void setMappings(List<MappingConfig> mappings) {
            this.mappings = mappings;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TableConfig {
        private String name;
        private List<String> columns = new ArrayList<>();
        private String sourceTableName; // Only for target tables

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getColumns() {
            return columns;
        }

        public void setColumns(List<String> columns) {
            this.columns = columns;
        }

        public String getSourceTableName() {
            return sourceTableName;
        }

        public void setSourceTableName(String sourceTableName) {
            this.sourceTableName = sourceTableName;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LeftJoinConfig {
        private String leftTable;
        private String leftColumn;
        private String rightTable;
        private String rightColumn;

        public String getLeftTable() {
            return leftTable;
        }

        public void setLeftTable(String leftTable) {
            this.leftTable = leftTable;
        }

        public String getLeftColumn() {
            return leftColumn;
        }

        public void setLeftColumn(String leftColumn) {
            this.leftColumn = leftColumn;
        }

        public String getRightTable() {
            return rightTable;
        }

        public void setRightTable(String rightTable) {
            this.rightTable = rightTable;
        }

        public String getRightColumn() {
            return rightColumn;
        }

        public void setRightColumn(String rightColumn) {
            this.rightColumn = rightColumn;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MappingConfig {
        private String type; // "None", "Dict", "Constant", "ExternalConnection"
        private String targetTable;
        private String targetColumn;
        private String sourceTable; // For None, Dict
        private String sourceColumn; // For None, Dict
        private String dictType; // For Dict
        private String constantValue; // For Constant
        // For ExternalConnection
        private String finalSelectTable;  // Renamed from externalTable
        private String finalSelectColumn; // Renamed from externalColumn
        private String finalIdColumn;     // Renamed from externalIdColumn
        private String sourceIdTable;
        private String sourceIdColumn;
        // For LEFT JOINs in ExternalConnection
        private List<LeftJoinConfig> leftJoins = new ArrayList<>();

        // Added for backward compatibility
        private String externalTable;
        private String externalColumn;
        private String externalIdColumn;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTargetTable() {
            return targetTable;
        }

        public void setTargetTable(String targetTable) {
            this.targetTable = targetTable;
        }

        public String getTargetColumn() {
            return targetColumn;
        }

        public void setTargetColumn(String targetColumn) {
            this.targetColumn = targetColumn;
        }

        public String getSourceTable() {
            return sourceTable;
        }

        public void setSourceTable(String sourceTable) {
            this.sourceTable = sourceTable;
        }

        public String getSourceColumn() {
            return sourceColumn;
        }

        public void setSourceColumn(String sourceColumn) {
            this.sourceColumn = sourceColumn;
        }

        public String getDictType() {
            return dictType;
        }

        public void setDictType(String dictType) {
            this.dictType = dictType;
        }

        public String getConstantValue() {
            return constantValue;
        }

        public void setConstantValue(String constantValue) {
            this.constantValue = constantValue;
        }

        public String getFinalSelectTable() {
            return finalSelectTable != null ? finalSelectTable : externalTable; // Handle backward compatibility
        }

        public void setFinalSelectTable(String finalSelectTable) {
            this.finalSelectTable = finalSelectTable;
        }

        public String getFinalSelectColumn() {
            return finalSelectColumn != null ? finalSelectColumn : externalColumn; // Handle backward compatibility
        }

        public void setFinalSelectColumn(String finalSelectColumn) {
            this.finalSelectColumn = finalSelectColumn;
        }

        public String getFinalIdColumn() {
            return finalIdColumn != null ? finalIdColumn : externalIdColumn; // Handle backward compatibility
        }

        public void setFinalIdColumn(String finalIdColumn) {
            this.finalIdColumn = finalIdColumn;
        }

        public String getSourceIdTable() {
            return sourceIdTable;
        }

        public void setSourceIdTable(String sourceIdTable) {
            this.sourceIdTable = sourceIdTable;
        }

        public String getSourceIdColumn() {
            return sourceIdColumn;
        }

        public void setSourceIdColumn(String sourceIdColumn) {
            this.sourceIdColumn = sourceIdColumn;
        }

        public List<LeftJoinConfig> getLeftJoins() {
            return leftJoins;
        }

        public void setLeftJoins(List<LeftJoinConfig> leftJoins) {
            this.leftJoins = leftJoins;
        }

        // For backward compatibility
        public String getExternalTable() {
            return externalTable;
        }

        public void setExternalTable(String externalTable) {
            this.externalTable = externalTable;
        }

        public String getExternalColumn() {
            return externalColumn;
        }

        public void setExternalColumn(String externalColumn) {
            this.externalColumn = externalColumn;
        }

        public String getExternalIdColumn() {
            return externalIdColumn;
        }

        public void setExternalIdColumn(String externalIdColumn) {
            this.externalIdColumn = externalIdColumn;
        }
    }

    public static void saveToFile(File file, Map<String, SourceTable> sourceTables,
                                  Map<String, TargetTable> targetTables, List<Mapping> mappings) throws IOException {
        Configuration config = new Configuration();

        // Convert source tables
        for (Map.Entry<String, SourceTable> entry : sourceTables.entrySet()) {
            TableConfig tableConfig = new TableConfig();
            tableConfig.setName(entry.getKey());
            tableConfig.setColumns(entry.getValue().getTable().getColumns());
            config.getSourceTables().add(tableConfig);
        }

        // Convert target tables
        for (Map.Entry<String, TargetTable> entry : targetTables.entrySet()) {
            TableConfig tableConfig = new TableConfig();
            tableConfig.setName(entry.getKey());
            tableConfig.setColumns(entry.getValue().getTable().getColumns());
            tableConfig.setSourceTableName(entry.getValue().getSourceTable().getTable().getName());
            config.getTargetTables().add(tableConfig);
        }

        // Convert mappings
        for (Mapping mapping : mappings) {
            MappingConfig mappingConfig = new MappingConfig();

            if (mapping instanceof None) {
                None noneMapping = (None) mapping;
                mappingConfig.setType("None");
                mappingConfig.setTargetTable(noneMapping.getTargetColumn().getTable().getName());
                mappingConfig.setTargetColumn(noneMapping.getTargetColumn().getName());
                mappingConfig.setSourceTable(noneMapping.getSourceColumn().getTable().getName());
                mappingConfig.setSourceColumn(noneMapping.getSourceColumn().getName());
            } else if (mapping instanceof Dict) {
                Dict dictMapping = (Dict) mapping;
                mappingConfig.setType("Dict");
                mappingConfig.setTargetTable(dictMapping.getTargetColumn().getTable().getName());
                mappingConfig.setTargetColumn(dictMapping.getTargetColumn().getName());
                mappingConfig.setSourceTable(dictMapping.getSourceColumn().getTable().getName());
                mappingConfig.setSourceColumn(dictMapping.getSourceColumn().getName());
                mappingConfig.setDictType(dictMapping.getDictType());
            } else if (mapping instanceof Constant) {
                Constant constantMapping = (Constant) mapping;
                mappingConfig.setType("Constant");
                mappingConfig.setTargetTable(constantMapping.getTargetColumn().getTable().getName());
                mappingConfig.setTargetColumn(constantMapping.getTargetColumn().getName());
                mappingConfig.setConstantValue(constantMapping.getConstantValue());
            } else if (mapping instanceof ExternalConnection) {
                ExternalConnection ecMapping = (ExternalConnection) mapping;
                mappingConfig.setType("ExternalConnection");
                mappingConfig.setTargetTable(ecMapping.getTargetColumn().getTable().getName());
                mappingConfig.setTargetColumn(ecMapping.getTargetColumn().getName());
                mappingConfig.setFinalSelectTable(ecMapping.getFinalSelectColumn().getTable().getName());
                mappingConfig.setFinalSelectColumn(ecMapping.getFinalSelectColumn().getName());
                mappingConfig.setFinalIdColumn(ecMapping.getFinalIdColumn().getName());
                mappingConfig.setSourceIdTable(ecMapping.getSourceIdColumn().getTable().getName());
                mappingConfig.setSourceIdColumn(ecMapping.getSourceIdColumn().getName());

                // Handle LEFT JOINs
                for (LeftJoin join : ecMapping.getJoins()) {
                    LeftJoinConfig joinConfig = new LeftJoinConfig();
                    joinConfig.setLeftTable(join.getLeftColumn().getTable().getName());
                    joinConfig.setLeftColumn(join.getLeftColumn().getName());
                    joinConfig.setRightTable(join.getRightColumn().getTable().getName());
                    joinConfig.setRightColumn(join.getRightColumn().getName());
                    mappingConfig.getLeftJoins().add(joinConfig);
                }
            }

            config.getMappings().add(mappingConfig);
        }

        // Write to file
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, config);
    }

    public static Configuration loadFromFile(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(file, Configuration.class);
    }
}