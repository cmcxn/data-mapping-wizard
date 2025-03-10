package com.datamap.ui;

import com.datamap.model.*;
import com.datamap.model.mapping.*;
import com.datamap.util.Code;
import com.datamap.util.DatabaseConnectionManager;
import com.datamap.util.JsonConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
    private List<DataSource> dataSources = new ArrayList<>();

    // Wizard panels
    private DatabaseConfigPanel databaseConfigPanel;
    private AddTablesPanel addTablesPanel;
    private AddColumnsPanel addColumnsPanel;
    private NoneMappingPanel noneMappingPanel;
    private DictMappingPanel dictMappingPanel;
    private ConstantMappingPanel constantMappingPanel;
    private ExternalConnectionPanel externalConnectionPanel;
    private GenerateCodePanel generateCodePanel;

    // Current panel tracking
    private String currentPanel = "databaseConfig";

    public DataMapWizard() {
        setTitle("Data Mapping Wizard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700); // Increased size for better UI experience
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        wizardPanel = new JPanel(cardLayout);

        initPanels();
        initNavigationPanel();

        add(wizardPanel, BorderLayout.CENTER);
    }

    private void initPanels() {
        databaseConfigPanel = new DatabaseConfigPanel(this);
        addTablesPanel = new AddTablesPanel(this);
        addColumnsPanel = new AddColumnsPanel(this);
        noneMappingPanel = new NoneMappingPanel(this);
        dictMappingPanel = new DictMappingPanel(this);
        constantMappingPanel = new ConstantMappingPanel(this);
        externalConnectionPanel = new ExternalConnectionPanel(this);
        generateCodePanel = new GenerateCodePanel(this);

        wizardPanel.add(databaseConfigPanel, "databaseConfig");
        wizardPanel.add(addTablesPanel, "addTables");
        wizardPanel.add(addColumnsPanel, "addColumns");
        wizardPanel.add(noneMappingPanel, "noneMapping");
        wizardPanel.add(dictMappingPanel, "dictMapping");
        wizardPanel.add(constantMappingPanel, "constantMapping");
        wizardPanel.add(externalConnectionPanel, "externalConnection");
        wizardPanel.add(generateCodePanel, "generateCode");

        cardLayout.show(wizardPanel, "databaseConfig");
    }

    private void initNavigationPanel() {
        JPanel navPanel = new JPanel(new BorderLayout());

        // Left-aligned panel for configuration buttons
        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton saveConfigButton = new JButton("Save Configuration");
        JButton loadConfigButton = new JButton("Load Configuration");

        saveConfigButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (generateCodePanel != null) {
                    generateCodePanel.saveConfiguration();
                }
            }
        });

        loadConfigButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (generateCodePanel != null) {
                    generateCodePanel.loadConfiguration();
                }
            }
        });

        leftButtonPanel.add(saveConfigButton);
        leftButtonPanel.add(loadConfigButton);

        // Right-aligned panel for navigation buttons
        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
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

        rightButtonPanel.add(prevButton);
        rightButtonPanel.add(nextButton);

        navPanel.add(leftButtonPanel, BorderLayout.WEST);
        navPanel.add(rightButtonPanel, BorderLayout.EAST);

        add(navPanel, BorderLayout.SOUTH);

        // Initially disable previous button on first panel
        prevButton.setEnabled(false);
    }

    public void navigateNext() {
        String nextPanel = null;

        switch (currentPanel) {
            case "databaseConfig":
                nextPanel = "addTables";
                dataSources = databaseConfigPanel.getDataSources();
                // Refresh the tables panel with new data sources
                addTablesPanel.refreshDataSources();
                break;
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
            case "generateCode":
                // Final panel - here you could implement exiting or restarting the wizard
                JOptionPane.showMessageDialog(this,
                        "Wizard completed! Your code has been generated.",
                        "Wizard Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
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
            case "addTables":
                prevPanel = "databaseConfig";
                break;
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
        prevButton.setEnabled(!currentPanel.equals("databaseConfig"));

        // Change Next button text to "Generate" on last mapping panel
        if (currentPanel.equals("externalConnection")) {
            nextButton.setText("Generate Code");
        } else if (currentPanel.equals("generateCode")) {
            nextButton.setText("Finish");
        } else {
            nextButton.setText("Next");
        }
    }

    // Data model management methods with data source support
    public void addSourceTable(String name, DataSource dataSource, String... columns) {
        Table table = new Table(name, columns);
        SourceTable sourceTable = new SourceTable(table, dataSource.getName());
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

    public void addTargetTable(String sourceName, String targetName, DataSource dataSource, String... columns) {
        SourceTable sourceTable = sourceTables.get(sourceName);
        if (sourceTable != null) {
            Table table = new Table(targetName, columns);
            TargetTable targetTable = new TargetTable(sourceTable, table, dataSource.getName());
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
            // First check if there's already a mapping for this target column
            removeMappingsWithTargetColumn(targetTableName, targetColumnName);

            // Then add the new mapping
            None mapping = new None(targetColumn, sourceColumn);
            mappings.add(mapping);
        }
    }

    public void addDictMapping(String targetTableName, String targetColumnName,
                               String dictType, String sourceTableName, String sourceColumnName) {
        TargetColumn targetColumn = targetColumns.get(targetTableName + "." + targetColumnName);
        SourceColumn sourceColumn = sourceColumns.get(sourceTableName + "." + sourceColumnName);

        if (targetColumn != null && sourceColumn != null) {
            // First check if there's already a mapping for this target column
            removeMappingsWithTargetColumn(targetTableName, targetColumnName);

            // Then add the new mapping
            Dict mapping = new Dict(targetColumn, dictType, sourceColumn);
            mappings.add(mapping);
        }
    }

    public void addConstantMapping(String targetTableName, String targetColumnName, String constantValue) {
        TargetColumn targetColumn = targetColumns.get(targetTableName + "." + targetColumnName);

        if (targetColumn != null) {
            // First check if there's already a mapping for this target column
            removeMappingsWithTargetColumn(targetTableName, targetColumnName);

            // Then add the new mapping
            Constant mapping = new Constant(targetColumn, constantValue);
            mappings.add(mapping);
        }
    }

    // Updated to match the new method names in ExternalConnection
    public int addExternalConnectionMapping(String targetTableName, String targetColumnName,
                                            String finalSelectTableName, String finalSelectColumnName,
                                            String whereSelectTableName, String whereIdColumnName,
                                            String sourceTableName, String sourceIdColumnName) {
        TargetColumn targetColumn = targetColumns.get(targetTableName + "." + targetColumnName);
        SourceColumn finalSelectColumn = sourceColumns.get(finalSelectTableName + "." + finalSelectColumnName);
        SourceColumn whereIdColumn = sourceColumns.get(whereSelectTableName + "." + whereIdColumnName);
        SourceColumn whereSelectTable = sourceColumns.get(whereSelectTableName + "." + whereIdColumnName); // Using the same column but it's the table we need
        SourceColumn sourceIdColumn = sourceColumns.get(sourceTableName + "." + sourceIdColumnName);

        if (targetColumn != null && finalSelectColumn != null && whereIdColumn != null && whereSelectTable != null && sourceIdColumn != null) {
            // First check if there's already a mapping for this target column
            removeMappingsWithTargetColumn(targetTableName, targetColumnName);

            // Then add the new mapping
            ExternalConnection mapping = new ExternalConnection(targetColumn, finalSelectColumn, whereIdColumn, whereSelectTable, sourceIdColumn);
            mappings.add(mapping);
            return mappings.size() - 1;
        }
        return -1;
    }

    // New method to add LEFT JOIN to external connection mapping
    public void addLeftJoinToExternalConnection(int mappingIndex, String leftTableName, String leftColumnName,
                                                String rightTableName, String rightColumnName) {
        if (mappingIndex >= 0 && mappingIndex < mappings.size() && mappings.get(mappingIndex) instanceof ExternalConnection) {
            ExternalConnection externalConnection = (ExternalConnection) mappings.get(mappingIndex);
            SourceColumn leftColumn = sourceColumns.get(leftTableName + "." + leftColumnName);
            SourceColumn rightColumn = sourceColumns.get(rightTableName + "." + rightColumnName);

            if (leftColumn != null && rightColumn != null) {
                LeftJoin join = new LeftJoin(leftColumn, rightColumn);
                externalConnection.addJoin(join);
            }
        }
    }

    /**
     * Improved method to remove a mapping by index and type
     * This ensures the mapping is completely removed from the data model
     */
    public void removeMapping(int index, String type) {
        if (index >= 0 && index < mappings.size()) {
            // Get the mapping before removing it to check its type
            Mapping mappingToRemove = mappings.get(index);

            // Verify the mapping type matches the expected type
            boolean typesMatch =
                    (type.equals("None") && mappingToRemove instanceof None) ||
                            (type.equals("Dict") && mappingToRemove instanceof Dict) ||
                            (type.equals("Constant") && mappingToRemove instanceof Constant) ||
                            (type.equals("ExternalConnection") && mappingToRemove instanceof ExternalConnection);

            if (typesMatch) {
                // Remove the mapping
                mappings.remove(index);

                System.out.println("Mapping removed successfully. Remaining mappings: " + mappings.size());
            } else {
                System.out.println("Type mismatch when removing mapping. Expected: " + type +
                        ", Actual: " + mappingToRemove.getClass().getSimpleName());
            }
        } else {
            System.out.println("Invalid mapping index: " + index + ", total mappings: " + mappings.size());
        }
    }

    /**
     * Improved method to remove all mappings with a specific source table
     */
    private void removeMappingsWithSource(String tableName) {
        Iterator<Mapping> iterator = mappings.iterator();
        while (iterator.hasNext()) {
            Mapping mapping = iterator.next();

            if (mapping instanceof None) {
                SourceColumn sourceColumn = ((None)mapping).getSourceColumn();
                if (sourceColumn.getTable().getName().equals(tableName)) {
                    iterator.remove();
                }
            } else if (mapping instanceof Dict) {
                SourceColumn sourceColumn = ((Dict)mapping).getSourceColumn();
                if (sourceColumn.getTable().getName().equals(tableName)) {
                    iterator.remove();
                }
            } else if (mapping instanceof ExternalConnection) {
                ExternalConnection ec = (ExternalConnection)mapping;
                if (ec.getSourceIdColumn().getTable().getName().equals(tableName) ||
                        ec.getFinalSelectColumn().getTable().getName().equals(tableName) ||
                        ec.getWhereIdColumn().getTable().getName().equals(tableName) ||
                        ec.getWhereSelectTable().getTable().getName().equals(tableName)) {
                    iterator.remove();
                    continue;
                }

                // Also check if any join columns reference the table being removed
                boolean joinReferencesTable = false;
                for (LeftJoin join : ec.getJoins()) {
                    if (join.getLeftColumn().getTable().getName().equals(tableName) ||
                            join.getRightColumn().getTable().getName().equals(tableName)) {
                        joinReferencesTable = true;
                        break;
                    }
                }

                if (joinReferencesTable) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Improved method to remove all mappings with a specific target table
     */
    private void removeMappingsWithTarget(String tableName) {
        Iterator<Mapping> iterator = mappings.iterator();
        while (iterator.hasNext()) {
            Mapping mapping = iterator.next();
            if (mapping.getTargetColumn().getTable().getName().equals(tableName)) {
                iterator.remove();
            }
        }
    }

    /**
     * Improved method to remove all mappings with a specific source column
     */
    private void removeMappingsWithSourceColumn(String tableName, String columnName) {
        Iterator<Mapping> iterator = mappings.iterator();
        while (iterator.hasNext()) {
            Mapping mapping = iterator.next();

            if (mapping instanceof None) {
                SourceColumn sourceColumn = ((None)mapping).getSourceColumn();
                if (sourceColumn.getTable().getName().equals(tableName) &&
                        sourceColumn.getName().equals(columnName)) {
                    iterator.remove();
                }
            } else if (mapping instanceof Dict) {
                SourceColumn sourceColumn = ((Dict)mapping).getSourceColumn();
                if (sourceColumn.getTable().getName().equals(tableName) &&
                        sourceColumn.getName().equals(columnName)) {
                    iterator.remove();
                }
            } else if (mapping instanceof ExternalConnection) {
                ExternalConnection ec = (ExternalConnection)mapping;
                if ((ec.getSourceIdColumn().getTable().getName().equals(tableName) &&
                        ec.getSourceIdColumn().getName().equals(columnName)) ||
                        (ec.getFinalSelectColumn().getTable().getName().equals(tableName) &&
                                ec.getFinalSelectColumn().getName().equals(columnName)) ||
                        (ec.getWhereIdColumn().getTable().getName().equals(tableName) &&
                                ec.getWhereIdColumn().getName().equals(columnName)) ||
                        (ec.getWhereSelectTable().getTable().getName().equals(tableName) &&
                                ec.getWhereSelectTable().getName().equals(columnName))) {
                    iterator.remove();
                    continue;
                }

                // Also check for columns in LEFT JOINs
                boolean joinReferencesColumn = false;
                for (LeftJoin join : ec.getJoins()) {
                    if ((join.getLeftColumn().getTable().getName().equals(tableName) &&
                            join.getLeftColumn().getName().equals(columnName)) ||
                            (join.getRightColumn().getTable().getName().equals(tableName) &&
                                    join.getRightColumn().getName().equals(columnName))) {
                        joinReferencesColumn = true;
                        break;
                    }
                }

                if (joinReferencesColumn) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Improved method to remove all mappings with a specific target column
     */
    private void removeMappingsWithTargetColumn(String tableName, String columnName) {
        Iterator<Mapping> iterator = mappings.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Mapping mapping = iterator.next();
            if (mapping.getTargetColumn().getTable().getName().equals(tableName) &&
                    mapping.getTargetColumn().getName().equals(columnName)) {
                iterator.remove();
                System.out.println("Removed mapping for target column " + tableName + "." + columnName +
                        " at position " + i);
            }
            i++;
        }
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

    public List<DataSource> getDataSources() {
        return dataSources;
    }

    public String generateCode(){
        return Code.generateCode(sourceTables, targetTables, sourceColumns, targetColumns, mappings);
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
                noneMappingPanel.updateComponents();
                dictMappingPanel.updateComponents();
                constantMappingPanel.updateComponents();
                externalConnectionPanel.updateComponents();

                // Update code panel
                generateCodePanel.updateCode();
            }
        });
    }

    /**
     * Refreshes the AddTablesPanel UI
     */
    private void refreshAddTablesPanel() {
        try {
            // Update data sources on AddTablesPanel
            java.lang.reflect.Method refreshMethod = AddTablesPanel.class.getDeclaredMethod("refreshDataSources");
            refreshMethod.setAccessible(true);
            refreshMethod.invoke(addTablesPanel);

            // Get the field for sourceTablesModel using reflection
            java.lang.reflect.Field sourceTablesModelField = AddTablesPanel.class.getDeclaredField("sourceTablesModel");
            sourceTablesModelField.setAccessible(true);
            DefaultListModel<String> sourceTablesModel = (DefaultListModel<String>)sourceTablesModelField.get(addTablesPanel);

            java.lang.reflect.Field targetTablesModelField = AddTablesPanel.class.getDeclaredField("targetTablesModel");
            targetTablesModelField.setAccessible(true);
            DefaultListModel<String> targetTablesModel = (DefaultListModel<String>)targetTablesModelField.get(addTablesPanel);

            java.lang.reflect.Field sourceTableForTargetComboField = AddTablesPanel.class.getDeclaredField("sourceTableForTargetCombo");
            sourceTableForTargetComboField.setAccessible(true);
            AutoCompleteComboBox sourceTableForTargetCombo = (AutoCompleteComboBox)sourceTableForTargetComboField.get(addTablesPanel);

            // Clear current values
            sourceTablesModel.clear();
            targetTablesModel.clear();

            // Add source tables to model 
            List<String> sourceTables = new ArrayList<>(this.sourceTables.keySet());
            for (String tableName : sourceTables) {
                sourceTablesModel.addElement(tableName);
            }

            // Update the combo box with source tables
            sourceTableForTargetCombo.setAutoCompleteItems(sourceTables);

            // Add target tables to model
            for (String tableName : this.targetTables.keySet()) {
                targetTablesModel.addElement(tableName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    // Following methods from original code - kept for compatibility with existing panels

    /**
     * Get a data source by name from the list of configured data sources
     *
     * @param name The name of the data source
     * @return The data source or null if not found
     */
    public DataSource getDataSourceByName(String name) {
        for (DataSource ds : dataSources) {
            if (ds.getName().equals(name)) {
                return ds;
            }
        }
        return null;
    }

    /**
     * Updates the database connections for all connected components
     * This should be called whenever data sources are added/modified/removed
     */
    public void updateDatabaseConnections() {
        // First, update the add tables panel
        try {
            if (addTablesPanel != null) {
                java.lang.reflect.Method refreshMethod = AddTablesPanel.class.getDeclaredMethod("refreshDataSources");
                refreshMethod.setAccessible(true);
                refreshMethod.invoke(addTablesPanel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Then, update columns if tables are selected
        if (addColumnsPanel != null) {
            addColumnsPanel.updateComponents();
        }
    }

    /**
     * Get the table list from a data source
     *
     * @param dataSource The data source to query
     * @return A list of table names or an empty list if an error occurred
     */
    public List<String> getTablesFromDataSource(DataSource dataSource) {
        List<String> tables = new ArrayList<>();

        try {
            Connection conn = DatabaseConnectionManager.getConnection(dataSource);
            tables = DatabaseConnectionManager.getTables(conn);
            conn.close();
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this,
                    "Error retrieving tables: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        return tables;
    }

    /**
     * Get the column list from a specific table in a data source
     *
     * @param dataSource The data source to query
     * @param tableName The name of the table
     * @return A list of column names or an empty list if an error occurred
     */
    public List<String> getColumnsFromTable(DataSource dataSource, String tableName) {
        List<String> columns = new ArrayList<>();

        try {
            Connection conn = DatabaseConnectionManager.getConnection(dataSource);
            columns = DatabaseConnectionManager.getColumns(conn, tableName);
            conn.close();
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this,
                    "Error retrieving columns: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        return columns;
    }

    /**
     * Updates the data source for an existing source table
     *
     * @param tableName The name of the table to update
     * @param dataSource The data source to associate with the table
     */
    public void updateSourceTableDataSource(String tableName, DataSource dataSource) {
        SourceTable sourceTable = sourceTables.get(tableName);
        if (sourceTable != null) {
            sourceTable.setDataSourceName(dataSource.getName());
        }
    }

    /**
     * Updates the data source for an existing target table
     *
     * @param tableName The name of the table to update
     * @param dataSource The data source to associate with the table
     */
    public void updateTargetTableDataSource(String tableName, DataSource dataSource) {
        TargetTable targetTable = targetTables.get(tableName);
        if (targetTable != null) {
            targetTable.setDataSourceName(dataSource.getName());
        }
    }

    /**
     * Returns whether a target column already has a mapping
     *
     * @param targetTableName The target table name
     * @param targetColumnName The target column name
     * @return true if the target column already has a mapping
     */
    public boolean hasTargetColumnMapping(String targetTableName, String targetColumnName) {
        for (Mapping mapping : mappings) {
            TargetColumn targetColumn = mapping.getTargetColumn();
            if (targetColumn.getTable().getName().equals(targetTableName) &&
                    targetColumn.getName().equals(targetColumnName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the mapping type for a specific target column
     *
     * @param targetTableName The target table name
     * @param targetColumnName The target column name
     * @return The mapping type as a string: "None", "Dict", "Constant", "ExternalConnection", or null if no mapping exists
     */
    public String getMappingTypeForTargetColumn(String targetTableName, String targetColumnName) {
        for (Mapping mapping : mappings) {
            TargetColumn targetColumn = mapping.getTargetColumn();
            if (targetColumn.getTable().getName().equals(targetTableName) &&
                    targetColumn.getName().equals(targetColumnName)) {

                if (mapping instanceof None) {
                    return "None";
                } else if (mapping instanceof Dict) {
                    return "Dict";
                } else if (mapping instanceof Constant) {
                    return "Constant";
                } else if (mapping instanceof ExternalConnection) {
                    return "ExternalConnection";
                }

                return mapping.getClass().getSimpleName();
            }
        }
        return null;
    }

    /**
     * Returns a list of mappings for a specific target table
     *
     * @param targetTableName The target table name
     * @return List of mappings for the specified target table
     */
    public List<Mapping> getMappingsForTargetTable(String targetTableName) {
        List<Mapping> result = new ArrayList<>();
        for (Mapping mapping : mappings) {
            TargetColumn targetColumn = mapping.getTargetColumn();
            if (targetColumn.getTable().getName().equals(targetTableName)) {
                result.add(mapping);
            }
        }
        return result;
    }

    /**
     * Log detailed information about the current mapping state
     * Useful for debugging purposes
     */
    public void logMappingsState() {
        System.out.println("\n==== CURRENT MAPPINGS STATE ====");
        System.out.println("Total mappings: " + mappings.size());

        for (int i = 0; i < mappings.size(); i++) {
            Mapping mapping = mappings.get(i);
            String targetTable = mapping.getTargetColumn().getTable().getName();
            String targetColumn = mapping.getTargetColumn().getName();
            String type = mapping.getClass().getSimpleName();

            System.out.println(i + ". " + type + ": " + targetTable + "." + targetColumn);

            if (mapping instanceof None) {
                None none = (None) mapping;
                String sourceTable = none.getSourceColumn().getTable().getName();
                String sourceColumn = none.getSourceColumn().getName();
                System.out.println("   Source: " + sourceTable + "." + sourceColumn);
            } else if (mapping instanceof Dict) {
                Dict dict = (Dict) mapping;
                String sourceTable = dict.getSourceColumn().getTable().getName();
                String sourceColumn = dict.getSourceColumn().getName();
                String dictType = dict.getDictType();
                System.out.println("   Source: " + sourceTable + "." + sourceColumn);
                System.out.println("   Dict Type: " + dictType);
            } else if (mapping instanceof Constant) {
                Constant constant = (Constant) mapping;
                System.out.println("   Value: " + constant.getConstantValue());
            } else if (mapping instanceof ExternalConnection) {
                ExternalConnection ec = (ExternalConnection) mapping;

                String finalSelectTable = ec.getFinalSelectColumn().getTable().getName();
                String finalSelectColumn = ec.getFinalSelectColumn().getName();
                String whereIdTable = ec.getWhereIdColumn().getTable().getName();
                String whereIdColumn = ec.getWhereIdColumn().getName();
                String sourceIdTable = ec.getSourceIdColumn().getTable().getName();
                String sourceIdColumn = ec.getSourceIdColumn().getName();

                System.out.println("   Final Select: " + finalSelectTable + "." + finalSelectColumn);
                System.out.println("   Where Id: " + whereIdTable + "." + whereIdColumn);
                System.out.println("   Source Id: " + sourceIdTable + "." + sourceIdColumn);

                List<LeftJoin> joins = ec.getJoins();
                if (!joins.isEmpty()) {
                    System.out.println("   Left Joins:");
                    for (int j = 0; j < joins.size(); j++) {
                        LeftJoin join = joins.get(j);
                        String leftTable = join.getLeftColumn().getTable().getName();
                        String leftColumn = join.getLeftColumn().getName();
                        String rightTable = join.getRightColumn().getTable().getName();
                        String rightColumn = join.getRightColumn().getName();

                        System.out.println("     " + j + ". " + leftTable + "." + leftColumn +
                                " = " + rightTable + "." + rightColumn);
                    }
                }
            }
        }
        System.out.println("===============================\n");
    }

    /**
     * Main method to launch the application
     */
    public static void main(String[] args) {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DataMapWizard wizard = new DataMapWizard();
                wizard.setVisible(true);
            }
        });
    }
}