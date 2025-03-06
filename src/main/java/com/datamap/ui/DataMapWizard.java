package com.datamap.ui;

import com.datamap.model.*;
import com.datamap.model.mapping.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataMapWizard extends JFrame {
    private CardLayout cardLayout;
    private JPanel wizardPanel;
    private JButton prevButton;
    private JButton nextButton;

    // Data model
    private Map<String, SourceTable> sourceTables = new HashMap<>();
    private Map<String, TargetTable> targetTables = new HashMap<>();
    private Map<String, SourceColumn> sourceColumns = new HashMap<>();
    private Map<String, TargetColumn> targetColumns = new HashMap<>();
    private List<Mapping> mappings = new ArrayList<>();

    // Wizard panels
    private AddTablesPanel addTablesPanel;
    private AddColumnsPanel addColumnsPanel;
    private NoneMappingPanel noneMappingPanel;
    private DictMappingPanel dictMappingPanel;
    private ConstantMappingPanel constantMappingPanel;
    private ExternalConnectionPanel externalConnectionPanel;
    private GenerateCodePanel generateCodePanel;

    // Current panel tracking
    private String currentPanel = "addTables";

    public DataMapWizard() {
        setTitle("Data Mapping Wizard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        wizardPanel = new JPanel(cardLayout);

        initPanels();
        initNavigationPanel();

        add(wizardPanel, BorderLayout.CENTER);
    }

    private void initPanels() {
        addTablesPanel = new AddTablesPanel(this);
        addColumnsPanel = new AddColumnsPanel(this);
        noneMappingPanel = new NoneMappingPanel(this);
        dictMappingPanel = new DictMappingPanel(this);
        constantMappingPanel = new ConstantMappingPanel(this);
        externalConnectionPanel = new ExternalConnectionPanel(this);
        generateCodePanel = new GenerateCodePanel(this);

        wizardPanel.add(addTablesPanel, "addTables");
        wizardPanel.add(addColumnsPanel, "addColumns");
        wizardPanel.add(noneMappingPanel, "noneMapping");
        wizardPanel.add(dictMappingPanel, "dictMapping");
        wizardPanel.add(constantMappingPanel, "constantMapping");
        wizardPanel.add(externalConnectionPanel, "externalConnection");
        wizardPanel.add(generateCodePanel, "generateCode");

        cardLayout.show(wizardPanel, "addTables");
    }

    private void initNavigationPanel() {
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        prevButton = new JButton("Previous");
        nextButton = new JButton("Next");

        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigatePrevious();
            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigateNext();
            }
        });

        navPanel.add(prevButton);
        navPanel.add(nextButton);

        add(navPanel, BorderLayout.SOUTH);

        // Initially disable previous button on first panel
        prevButton.setEnabled(false);
    }

    public void navigateNext() {
        String nextPanel = null;

        switch (currentPanel) {
            case "addTables":
                nextPanel = "addColumns";
                addColumnsPanel.updateComponents();
                break;
            case "addColumns":
                nextPanel = "noneMapping";
                noneMappingPanel.updateComponents();
                break;
            case "noneMapping":
                nextPanel = "dictMapping";
                dictMappingPanel.updateComponents();
                break;
            case "dictMapping":
                nextPanel = "constantMapping";
                constantMappingPanel.updateComponents();
                break;
            case "constantMapping":
                nextPanel = "externalConnection";
                externalConnectionPanel.updateComponents();
                break;
            case "externalConnection":
                nextPanel = "generateCode";
                generateCodePanel.updateCode();
                break;
        }

        if (nextPanel != null) {
            cardLayout.show(wizardPanel, nextPanel);
            currentPanel = nextPanel;
            updateButtonState();
        }
    }

    public void navigatePrevious() {
        String prevPanel = null;

        switch (currentPanel) {
            case "addColumns":
                prevPanel = "addTables";
                break;
            case "noneMapping":
                prevPanel = "addColumns";
                addColumnsPanel.updateComponents();
                break;
            case "dictMapping":
                prevPanel = "noneMapping";
                noneMappingPanel.updateComponents();
                break;
            case "constantMapping":
                prevPanel = "dictMapping";
                dictMappingPanel.updateComponents();
                break;
            case "externalConnection":
                prevPanel = "constantMapping";
                constantMappingPanel.updateComponents();
                break;
            case "generateCode":
                prevPanel = "externalConnection";
                externalConnectionPanel.updateComponents();
                break;
        }

        if (prevPanel != null) {
            cardLayout.show(wizardPanel, prevPanel);
            currentPanel = prevPanel;
            updateButtonState();
        }
    }

    private void updateButtonState() {
        // Disable previous button on first panel
        prevButton.setEnabled(!currentPanel.equals("addTables"));

        // Change Next button text to "Generate" on last mapping panel
        if (currentPanel.equals("externalConnection")) {
            nextButton.setText("Generate Code");
        } else if (currentPanel.equals("generateCode")) {
            nextButton.setText("Finish");
        } else {
            nextButton.setText("Next");
        }
    }

    // Data model management methods
    public void addSourceTable(String name, String... columns) {
        Table table = new Table(name, columns);
        SourceTable sourceTable = new SourceTable(table);
        sourceTables.put(name, sourceTable);
    }

    public void removeSourceTable(String tableName) {
        SourceTable sourceTable = sourceTables.remove(tableName);
        if (sourceTable != null) {
            // Remove associated columns
            List<String> keysToRemove = new ArrayList<>();
            for (String key : sourceColumns.keySet()) {
                if (key.startsWith(tableName + ".")) {
                    keysToRemove.add(key);
                }
            }
            for (String key : keysToRemove) {
                sourceColumns.remove(key);
            }

            // Remove associated target tables
            List<String> targetKeysToRemove = new ArrayList<>();
            for (Map.Entry<String, TargetTable> entry : targetTables.entrySet()) {
                if (entry.getValue().getSourceTable().getTable().getName().equals(tableName)) {
                    targetKeysToRemove.add(entry.getKey());
                }
            }
            for (String key : targetKeysToRemove) {
                removeTargetTable(key);
            }

            // Remove associated mappings
            removeMappingsWithSource(tableName);
        }
    }

    public void addTargetTable(String sourceName, String targetName, String... columns) {
        SourceTable sourceTable = sourceTables.get(sourceName);
        if (sourceTable != null) {
            Table table = new Table(targetName, columns);
            TargetTable targetTable = new TargetTable(sourceTable, table);
            targetTables.put(targetName, targetTable);
        }
    }

    public void removeTargetTable(String tableName) {
        TargetTable targetTable = targetTables.remove(tableName);
        if (targetTable != null) {
            // Remove associated columns
            List<String> keysToRemove = new ArrayList<>();
            for (String key : targetColumns.keySet()) {
                if (key.startsWith(tableName + ".")) {
                    keysToRemove.add(key);
                }
            }
            for (String key : keysToRemove) {
                targetColumns.remove(key);
            }

            // Remove associated mappings
            removeMappingsWithTarget(tableName);
        }
    }

    public void addSourceColumn(String tableName, String columnName) {
        SourceTable sourceTable = sourceTables.get(tableName);
        if (sourceTable != null) {
            sourceTable.getTable().addColumn(columnName);
            SourceColumn column = new SourceColumn(sourceTable, columnName);
            sourceColumns.put(tableName + "." + columnName, column);
        }
    }

    public void removeSourceColumn(String tableName, String columnName) {
        String key = tableName + "." + columnName;
        sourceColumns.remove(key);

        // Also remove from table's column list
        SourceTable sourceTable = sourceTables.get(tableName);
        if (sourceTable != null) {
            sourceTable.getTable().removeColumn(columnName);
        }

        // Remove associated mappings
        removeMappingsWithSourceColumn(tableName, columnName);
    }

    public void addTargetColumn(String tableName, String columnName) {
        TargetTable targetTable = targetTables.get(tableName);
        if (targetTable != null) {
            targetTable.getTable().addColumn(columnName);
            TargetColumn column = new TargetColumn(targetTable, columnName);
            targetColumns.put(tableName + "." + columnName, column);
        }
    }

    public void removeTargetColumn(String tableName, String columnName) {
        String key = tableName + "." + columnName;
        targetColumns.remove(key);

        // Also remove from table's column list
        TargetTable targetTable = targetTables.get(tableName);
        if (targetTable != null) {
            targetTable.getTable().removeColumn(columnName);
        }

        // Remove associated mappings
        removeMappingsWithTargetColumn(tableName, columnName);
    }

    public void addNoneMapping(String targetTableName, String targetColumnName,
                               String sourceTableName, String sourceColumnName) {
        TargetColumn targetColumn = targetColumns.get(targetTableName + "." + targetColumnName);
        SourceColumn sourceColumn = sourceColumns.get(sourceTableName + "." + sourceColumnName);

        if (targetColumn != null && sourceColumn != null) {
            None mapping = new None(targetColumn, sourceColumn);
            mappings.add(mapping);
        }
    }

    public void addDictMapping(String targetTableName, String targetColumnName,
                               String dictType, String sourceTableName, String sourceColumnName) {
        TargetColumn targetColumn = targetColumns.get(targetTableName + "." + targetColumnName);
        SourceColumn sourceColumn = sourceColumns.get(sourceTableName + "." + sourceColumnName);

        if (targetColumn != null && sourceColumn != null) {
            Dict mapping = new Dict(targetColumn, dictType, sourceColumn, "OdsDaoKit.getSqlSession()");
            mappings.add(mapping);
        }
    }

    public void addConstantMapping(String targetTableName, String targetColumnName, String constantValue) {
        TargetColumn targetColumn = targetColumns.get(targetTableName + "." + targetColumnName);

        if (targetColumn != null) {
            Constant mapping = new Constant(targetColumn, constantValue);
            mappings.add(mapping);
        }
    }

    public void addExternalConnectionMapping(String targetTableName, String targetColumnName,
                                             String externalTableName, String externalColumnName,
                                             String externalIdColumnName, String sourceTableName, String sourceIdColumnName) {
        TargetColumn targetColumn = targetColumns.get(targetTableName + "." + targetColumnName);
        SourceColumn externalSourceColumn = sourceColumns.get(externalTableName + "." + externalColumnName);
        SourceColumn externalIdColumn = sourceColumns.get(externalTableName + "." + externalIdColumnName);
        SourceColumn sourceIdColumn = sourceColumns.get(sourceTableName + "." + sourceIdColumnName);

        if (targetColumn != null && externalSourceColumn != null &&
                externalIdColumn != null && sourceIdColumn != null) {
            ExternalConnection mapping = new ExternalConnection(targetColumn, externalSourceColumn,
                    externalIdColumn, sourceIdColumn,
                    "OdsDaoKit.getSqlSession()");
            mappings.add(mapping);
        }
    }

    public void removeMapping(int index, String type) {
        if (index >= 0 && index < mappings.size()) {
            mappings.remove(index);
        }
    }

    private void removeMappingsWithSource(String tableName) {
        List<Mapping> mappingsToRemove = new ArrayList<>();
        for (Mapping mapping : mappings) {
            if (mapping instanceof None) {
                SourceColumn sourceColumn = ((None)mapping).getSourceColumn();
                if (sourceColumn.getTable().getName().equals(tableName)) {
                    mappingsToRemove.add(mapping);
                }
            } else if (mapping instanceof Dict) {
                SourceColumn sourceColumn = ((Dict)mapping).getSourceColumn();
                if (sourceColumn.getTable().getName().equals(tableName)) {
                    mappingsToRemove.add(mapping);
                }
            } else if (mapping instanceof ExternalConnection) {
                ExternalConnection ec = (ExternalConnection)mapping;
                if (ec.getSourceIdColumn().getTable().getName().equals(tableName) ||
                        ec.getExternalSourceColumn().getTable().getName().equals(tableName) ||
                        ec.getExternalIdColumn().getTable().getName().equals(tableName)) {
                    mappingsToRemove.add(mapping);
                }
            }
        }
        mappings.removeAll(mappingsToRemove);
    }

    private void removeMappingsWithTarget(String tableName) {
        List<Mapping> mappingsToRemove = new ArrayList<>();
        for (Mapping mapping : mappings) {
            if (mapping.getTargetColumn().getTable().getName().equals(tableName)) {
                mappingsToRemove.add(mapping);
            }
        }
        mappings.removeAll(mappingsToRemove);
    }

    private void removeMappingsWithSourceColumn(String tableName, String columnName) {
        List<Mapping> mappingsToRemove = new ArrayList<>();
        for (Mapping mapping : mappings) {
            if (mapping instanceof None) {
                SourceColumn sourceColumn = ((None)mapping).getSourceColumn();
                if (sourceColumn.getTable().getName().equals(tableName) &&
                        sourceColumn.getName().equals(columnName)) {
                    mappingsToRemove.add(mapping);
                }
            } else if (mapping instanceof Dict) {
                SourceColumn sourceColumn = ((Dict)mapping).getSourceColumn();
                if (sourceColumn.getTable().getName().equals(tableName) &&
                        sourceColumn.getName().equals(columnName)) {
                    mappingsToRemove.add(mapping);
                }
            } else if (mapping instanceof ExternalConnection) {
                ExternalConnection ec = (ExternalConnection)mapping;
                if ((ec.getSourceIdColumn().getTable().getName().equals(tableName) &&
                        ec.getSourceIdColumn().getName().equals(columnName)) ||
                        (ec.getExternalSourceColumn().getTable().getName().equals(tableName) &&
                                ec.getExternalSourceColumn().getName().equals(columnName)) ||
                        (ec.getExternalIdColumn().getTable().getName().equals(tableName) &&
                                ec.getExternalIdColumn().getName().equals(columnName))) {
                    mappingsToRemove.add(mapping);
                }
            }
        }
        mappings.removeAll(mappingsToRemove);
    }

    private void removeMappingsWithTargetColumn(String tableName, String columnName) {
        List<Mapping> mappingsToRemove = new ArrayList<>();
        for (Mapping mapping : mappings) {
            if (mapping.getTargetColumn().getTable().getName().equals(tableName) &&
                    mapping.getTargetColumn().getName().equals(columnName)) {
                mappingsToRemove.add(mapping);
            }
        }
        mappings.removeAll(mappingsToRemove);
    }

    public Map<String, SourceTable> getSourceTables() {
        return sourceTables;
    }

    public Map<String, TargetTable> getTargetTables() {
        return targetTables;
    }

    public Map<String, SourceColumn> getSourceColumns() {
        return sourceColumns;
    }

    public Map<String, TargetColumn> getTargetColumns() {
        return targetColumns;
    }

    public List<Mapping> getMappings() {
        return mappings;
    }

    public String generateCode() {
        StringBuilder code = new StringBuilder();

        // Define set
        code.append("Set<Mapping> set = new HashSet<>();\n\n");

        // Define source tables
        for (SourceTable sourceTable : sourceTables.values()) {
            code.append("SourceTable ").append(sourceTable.getTable().getName()).append(" = new SourceTable(new Table(\"")
                    .append(sourceTable.getTable().getName()).append("\",");

            List<String> columns = sourceTable.getTable().getColumns();
            for (int i = 0; i < columns.size(); i++) {
                code.append("\"").append(columns.get(i)).append("\"");
                if (i < columns.size() - 1) {
                    code.append(",");
                }
            }
            code.append("));\n");
        }

        // Define target tables
        for (TargetTable targetTable : targetTables.values()) {
            code.append("TargetTable ").append(targetTable.getTable().getName()).append(" = new TargetTable(")
                    .append(targetTable.getSourceTable().getTable().getName()).append(",new Table(\"")
                    .append(targetTable.getTable().getName()).append("\",");

            List<String> columns = targetTable.getTable().getColumns();
            for (int i = 0; i < columns.size(); i++) {
                code.append("\"").append(columns.get(i)).append("\"");
                if (i < columns.size() - 1) {
                    code.append(",");
                }
            }
            code.append("));\n");
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
                code.append("try {\n");
                code.append("    //查询[").append(externalMapping.getExternalSourceColumn().getTable().getName())
                        .append("].{").append(externalMapping.getExternalIdColumn().getName()).append("}为[")
                        .append(externalMapping.getSourceIdColumn().getTable().getName()).append("].{")
                        .append(externalMapping.getSourceIdColumn().getName()).append("}的[")
                        .append(externalMapping.getExternalSourceColumn().getTable().getName()).append("].{")
                        .append(externalMapping.getExternalSourceColumn().getName()).append("}-->")
                        .append(externalMapping.getTargetColumn().getTable().getName()).append("的")
                        .append(externalMapping.getTargetColumn().getName()).append("\n");
                code.append("    ").append(mapping.generateCode()).append("\n");
                code.append("} catch (SQLException e) {\n");
                code.append("    e.printStackTrace();\n");
                code.append("}\n");
            }
        }

        return code.toString();
    }

    /**
     * Resets all data structures for loading a new configuration
     */
    public void resetData() {
        sourceTables.clear();
        targetTables.clear();
        sourceColumns.clear();
        targetColumns.clear();
        mappings.clear();
    }

    /**
     * Refreshes all panel UIs to reflect the current data model
     * Called after loading configuration to ensure UI displays loaded data
     */
    public void refreshAllPanelUIs() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Refresh AddTablesPanel
                refreshAddTablesPanel();

                // Refresh AddColumnsPanel
                addColumnsPanel.updateComponents();
                refreshAddColumnsPanel();

                // Refresh mapping panels
                refreshNoneMappingPanel();
                refreshDictMappingPanel();
                refreshConstantMappingPanel();
                refreshExternalConnectionPanel();

                // Update code panel
                generateCodePanel.updateCode();
            }
        });
    }

    /**
     * Refreshes the AddTablesPanel UI
     */
    private void refreshAddTablesPanel() {
        // Get the field for sourceTablesModel using reflection
        try {
            java.lang.reflect.Field sourceTablesModelField = AddTablesPanel.class.getDeclaredField("sourceTablesModel");
            sourceTablesModelField.setAccessible(true);
            DefaultListModel<String> sourceTablesModel = (DefaultListModel<String>)sourceTablesModelField.get(addTablesPanel);

            java.lang.reflect.Field targetTablesModelField = AddTablesPanel.class.getDeclaredField("targetTablesModel");
            targetTablesModelField.setAccessible(true);
            DefaultListModel<String> targetTablesModel = (DefaultListModel<String>)targetTablesModelField.get(addTablesPanel);

            java.lang.reflect.Field sourceTableComboField = AddTablesPanel.class.getDeclaredField("sourceTableCombo");
            sourceTableComboField.setAccessible(true);
            JComboBox<String> sourceTableCombo = (JComboBox<String>)sourceTableComboField.get(addTablesPanel);

            // Clear current values
            sourceTablesModel.clear();
            targetTablesModel.clear();
            sourceTableCombo.removeAllItems();

            // Add source tables to model and combo
            for (String tableName : sourceTables.keySet()) {
                sourceTablesModel.addElement(tableName);
                sourceTableCombo.addItem(tableName);
            }

            // Add target tables to model
            for (String tableName : targetTables.keySet()) {
                targetTablesModel.addElement(tableName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Refreshes the AddColumnsPanel UI
     */
    private void refreshAddColumnsPanel() {
        try {
            java.lang.reflect.Field sourceColumnsModelField = AddColumnsPanel.class.getDeclaredField("sourceColumnsModel");
            sourceColumnsModelField.setAccessible(true);
            DefaultListModel<String> sourceColumnsModel = (DefaultListModel<String>)sourceColumnsModelField.get(addColumnsPanel);

            java.lang.reflect.Field targetColumnsModelField = AddColumnsPanel.class.getDeclaredField("targetColumnsModel");
            targetColumnsModelField.setAccessible(true);
            DefaultListModel<String> targetColumnsModel = (DefaultListModel<String>)targetColumnsModelField.get(addColumnsPanel);

            // Clear current values
            sourceColumnsModel.clear();
            targetColumnsModel.clear();

            // Add source columns to model
            for (String columnKey : sourceColumns.keySet()) {
                sourceColumnsModel.addElement(columnKey);
            }

            // Add target columns to model
            for (String columnKey : targetColumns.keySet()) {
                targetColumnsModel.addElement(columnKey);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Refreshes the NoneMappingPanel UI
     */
    private void refreshNoneMappingPanel() {
        try {
            // Get access to the mappingsModel field
            java.lang.reflect.Field mappingsModelField = NoneMappingPanel.class.getDeclaredField("mappingsModel");
            mappingsModelField.setAccessible(true);
            DefaultListModel<String> mappingsModel = (DefaultListModel<String>)mappingsModelField.get(noneMappingPanel);

            // Clear current mappings
            mappingsModel.clear();

            // Add mappings of type None
            for (Mapping mapping : mappings) {
                if (mapping instanceof None) {
                    None noneMapping = (None) mapping;
                    String targetKey = noneMapping.getTargetColumn().getTable().getName() + "." +
                            noneMapping.getTargetColumn().getName();
                    String sourceKey = noneMapping.getSourceColumn().getTable().getName() + "." +
                            noneMapping.getSourceColumn().getName();
                    mappingsModel.addElement(targetKey + " <- " + sourceKey);
                }
            }

            // Update comboboxes
            noneMappingPanel.updateComponents();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Refreshes the DictMappingPanel UI
     */
    private void refreshDictMappingPanel() {
        try {
            // Get access to the mappingsModel field
            java.lang.reflect.Field mappingsModelField = DictMappingPanel.class.getDeclaredField("mappingsModel");
            mappingsModelField.setAccessible(true);
            DefaultListModel<String> mappingsModel = (DefaultListModel<String>)mappingsModelField.get(dictMappingPanel);

            // Clear current mappings
            mappingsModel.clear();

            // Add mappings of type Dict
            for (Mapping mapping : mappings) {
                if (mapping instanceof Dict) {
                    Dict dictMapping = (Dict) mapping;
                    String targetKey = dictMapping.getTargetColumn().getTable().getName() + "." +
                            dictMapping.getTargetColumn().getName();
                    String sourceKey = dictMapping.getSourceColumn().getTable().getName() + "." +
                            dictMapping.getSourceColumn().getName();
                    mappingsModel.addElement(targetKey + " <- Dict(" + dictMapping.getDictType() + ") <- " + sourceKey);
                }
            }

            // Update comboboxes
            dictMappingPanel.updateComponents();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Refreshes the ConstantMappingPanel UI
     */
    private void refreshConstantMappingPanel() {
        try {
            // Get access to the mappingsModel field
            java.lang.reflect.Field mappingsModelField = ConstantMappingPanel.class.getDeclaredField("mappingsModel");
            mappingsModelField.setAccessible(true);
            DefaultListModel<String> mappingsModel = (DefaultListModel<String>)mappingsModelField.get(constantMappingPanel);

            // Clear current mappings
            mappingsModel.clear();

            // Add mappings of type Constant
            for (Mapping mapping : mappings) {
                if (mapping instanceof Constant) {
                    Constant constantMapping = (Constant) mapping;
                    String targetKey = constantMapping.getTargetColumn().getTable().getName() + "." +
                            constantMapping.getTargetColumn().getName();
                    mappingsModel.addElement(targetKey + " <- Constant(\"" + constantMapping.getConstantValue() + "\")");
                }
            }

            // Update combobox
            constantMappingPanel.updateComponents();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Refreshes the ExternalConnectionPanel UI
     */
    private void refreshExternalConnectionPanel() {
        try {
            // Get access to the mappingsModel field
            java.lang.reflect.Field mappingsModelField = ExternalConnectionPanel.class.getDeclaredField("mappingsModel");
            mappingsModelField.setAccessible(true);
            DefaultListModel<String> mappingsModel = (DefaultListModel<String>)mappingsModelField.get(externalConnectionPanel);

            // Clear current mappings
            mappingsModel.clear();

            // Add mappings of type ExternalConnection
            for (Mapping mapping : mappings) {
                if (mapping instanceof ExternalConnection) {
                    ExternalConnection ecMapping = (ExternalConnection) mapping;
                    String targetKey = ecMapping.getTargetColumn().getTable().getName() + "." +
                            ecMapping.getTargetColumn().getName();
                    String externalKey = ecMapping.getExternalSourceColumn().getTable().getName() + "." +
                            ecMapping.getExternalSourceColumn().getName();
                    String sourceIdKey = ecMapping.getSourceIdColumn().getTable().getName() + "." +
                            ecMapping.getSourceIdColumn().getName();

                    mappingsModel.addElement(targetKey + " <- " + externalKey + " (Join on " + sourceIdKey + ")");
                }
            }

            // Update comboboxes
            externalConnectionPanel.updateComponents();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // Set system look and feel
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                new DataMapWizard().setVisible(true);
            }
        });
    }
}