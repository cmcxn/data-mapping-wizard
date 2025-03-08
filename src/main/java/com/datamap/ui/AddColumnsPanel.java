package com.datamap.ui;

import com.datamap.model.*;
import com.datamap.util.DatabaseConnectionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class AddColumnsPanel extends JPanel {
    private DataMapWizard wizard;
    private JComboBox<String> sourceTableCombo;
    private JComboBox<String> targetTableCombo;
    private AutoCompleteComboBox sourceColumnCombo;
    private AutoCompleteComboBox targetColumnCombo;
    private DefaultListModel<String> sourceColumnsModel;
    private DefaultListModel<String> targetColumnsModel;
    private JList<String> sourceColumnsList;
    private JList<String> targetColumnsList;

    // Current columns from database for selection operations
    private List<String> availableSourceColumns;
    private List<String> availableTargetColumns;

    public AddColumnsPanel(DataMapWizard wizard) {
        this.wizard = wizard;
        setLayout(new BorderLayout());

        sourceColumnsModel = new DefaultListModel<>();
        targetColumnsModel = new DefaultListModel<>();
        availableSourceColumns = new ArrayList<>();
        availableTargetColumns = new ArrayList<>();

        initComponents();
    }

    private void initComponents() {
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        // Source columns panel
        JPanel sourcePanel = new JPanel(new BorderLayout());
        sourcePanel.setBorder(BorderFactory.createTitledBorder("Source Columns"));

        JPanel sourceInputPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        sourceInputPanel.add(new JLabel("Source Table:"));
        sourceTableCombo = new JComboBox<>();
        sourceTableCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateSourceColumns();
                }
            }
        });
        sourceInputPanel.add(sourceTableCombo);

        sourceInputPanel.add(new JLabel("Column Name:"));
        sourceColumnCombo = new AutoCompleteComboBox();
        sourceInputPanel.add(sourceColumnCombo);

        JButton addSourceColumnButton = new JButton("Add Source Column");
        addSourceColumnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addSourceColumn();
            }
        });

        JButton deleteSourceColumnButton = new JButton("Delete Source Column");
        deleteSourceColumnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSourceColumn();
            }
        });

        sourceColumnsList = new JList<>(sourceColumnsModel);
        // 允许多选
        sourceColumnsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane sourceScrollPane = new JScrollPane(sourceColumnsList);

        JPanel sourceActionsPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        JPanel sourceMainActionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sourceMainActionsPanel.add(addSourceColumnButton);
        sourceMainActionsPanel.add(deleteSourceColumnButton);

        // Add source column selection buttons
        JPanel sourceSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton selectAllSourceButton = new JButton("Select All");
        selectAllSourceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectAllSourceColumns();
            }
        });

        JButton deselectAllSourceButton = new JButton("Deselect All");
        deselectAllSourceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deselectAllSourceColumns();
            }
        });

        JButton invertSourceSelectionButton = new JButton("Invert Selection");
        invertSourceSelectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                invertSourceColumnSelection();
            }
        });

        sourceSelectionPanel.add(selectAllSourceButton);
        sourceSelectionPanel.add(deselectAllSourceButton);
        sourceSelectionPanel.add(invertSourceSelectionButton);

        sourceActionsPanel.add(sourceMainActionsPanel);
        sourceActionsPanel.add(sourceSelectionPanel);

        sourcePanel.add(sourceInputPanel, BorderLayout.NORTH);
        sourcePanel.add(sourceScrollPane, BorderLayout.CENTER);
        sourcePanel.add(sourceActionsPanel, BorderLayout.SOUTH);

        // Target columns panel
        JPanel targetPanel = new JPanel(new BorderLayout());
        targetPanel.setBorder(BorderFactory.createTitledBorder("Target Columns"));

        JPanel targetInputPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        targetInputPanel.add(new JLabel("Target Table:"));
        targetTableCombo = new JComboBox<>();
        targetTableCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateTargetColumns();
                }
            }
        });
        targetInputPanel.add(targetTableCombo);

        targetInputPanel.add(new JLabel("Column Name:"));
        targetColumnCombo = new AutoCompleteComboBox();
        targetInputPanel.add(targetColumnCombo);

        JButton addTargetColumnButton = new JButton("Add Target Column");
        addTargetColumnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTargetColumn();
            }
        });

        JButton deleteTargetColumnButton = new JButton("Delete Target Column");
        deleteTargetColumnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteTargetColumn();
            }
        });

        targetColumnsList = new JList<>(targetColumnsModel);
        // 允许多选
        targetColumnsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane targetScrollPane = new JScrollPane(targetColumnsList);

        JPanel targetActionsPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        JPanel targetMainActionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        targetMainActionsPanel.add(addTargetColumnButton);
        targetMainActionsPanel.add(deleteTargetColumnButton);

        // Add target column selection buttons
        JPanel targetSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton selectAllTargetButton = new JButton("Select All");
        selectAllTargetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectAllTargetColumns();
            }
        });

        JButton deselectAllTargetButton = new JButton("Deselect All");
        deselectAllTargetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deselectAllTargetColumns();
            }
        });

        JButton invertTargetSelectionButton = new JButton("Invert Selection");
        invertTargetSelectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                invertTargetColumnSelection();
            }
        });

        targetSelectionPanel.add(selectAllTargetButton);
        targetSelectionPanel.add(deselectAllTargetButton);
        targetSelectionPanel.add(invertTargetSelectionButton);

        targetActionsPanel.add(targetMainActionsPanel);
        targetActionsPanel.add(targetSelectionPanel);

        targetPanel.add(targetInputPanel, BorderLayout.NORTH);
        targetPanel.add(targetScrollPane, BorderLayout.CENTER);
        targetPanel.add(targetActionsPanel, BorderLayout.SOUTH);

        contentPanel.add(sourcePanel);
        contentPanel.add(targetPanel);

        add(contentPanel, BorderLayout.CENTER);

        JLabel instructionLabel = new JLabel("Step 2: Add columns to the tables");
        instructionLabel.setFont(new Font(instructionLabel.getFont().getName(), Font.BOLD, 14));
        add(instructionLabel, BorderLayout.NORTH);
    }

    private void updateSourceColumns() {
        String tableName = (String) sourceTableCombo.getSelectedItem();
        if (tableName == null) return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            // Get the source table with its data source
            SourceTable sourceTable = wizard.getSourceTables().get(tableName);
            if (sourceTable == null) return;

            // Find the data source this table belongs to
            DataSource dataSource = findDataSourceForTable(sourceTable.getDataSourceName());
            if (dataSource == null) return;

            // Get columns from database
            Connection conn = DatabaseConnectionManager.getConnection(dataSource);
            List<String> columns = DatabaseConnectionManager.getColumns(conn, tableName);

            // Sort columns alphabetically
            Collections.sort(columns);

            // Update the autocomplete items
            sourceColumnCombo.setAutoCompleteItems(columns);

            // Store the full list of available columns for selection operations
            availableSourceColumns = new ArrayList<>(columns);

            if (conn != null) conn.close();
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this,
                    "Error retrieving columns: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void updateTargetColumns() {
        String tableName = (String) targetTableCombo.getSelectedItem();
        if (tableName == null) return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            // Get the target table with its data source
            TargetTable targetTable = wizard.getTargetTables().get(tableName);
            if (targetTable == null) return;

            // Find the data source this table belongs to
            DataSource dataSource = findDataSourceForTable(targetTable.getDataSourceName());
            if (dataSource == null) return;

            // Get columns from database
            Connection conn = DatabaseConnectionManager.getConnection(dataSource);
            List<String> columns = DatabaseConnectionManager.getColumns(conn, tableName);

            // Sort columns alphabetically
            Collections.sort(columns);

            // Update the autocomplete items
            targetColumnCombo.setAutoCompleteItems(columns);

            // Store the full list of available columns for selection operations
            availableTargetColumns = new ArrayList<>(columns);

            if (conn != null) conn.close();
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this,
                    "Error retrieving columns: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private DataSource findDataSourceForTable(String dataSourceName) {
        if (dataSourceName == null) {
            // If not set, use the first available data source
            List<DataSource> dataSources = wizard.getDataSources();
            if (!dataSources.isEmpty()) {
                return dataSources.get(0);
            }
            return null;
        }

        // Find data source by name
        for (DataSource ds : wizard.getDataSources()) {
            if (ds.getName().equals(dataSourceName)) {
                return ds;
            }
        }
        return null;
    }

    private void addSourceColumn() {
        String tableName = (String) sourceTableCombo.getSelectedItem();
        String columnName = sourceColumnCombo.getSelectedText();

        if (tableName != null && columnName != null && !columnName.trim().isEmpty()) {
            // Check if column is already added
            String columnKey = tableName + "." + columnName;
            for (int i = 0; i < sourceColumnsModel.size(); i++) {
                if (sourceColumnsModel.getElementAt(i).equals(columnKey)) {
                    JOptionPane.showMessageDialog(this,
                            "This column has already been added.",
                            "Duplicate Column", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            wizard.addSourceColumn(tableName, columnName);
            sourceColumnsModel.addElement(columnKey);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select or enter a valid column name.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteSourceColumn() {
        // 获取所有选中的索引
        int[] selectedIndices = sourceColumnsList.getSelectedIndices();
        if (selectedIndices.length == 0) {
            return;
        }

        // 创建要删除的列的列表
        List<String> columnsToRemove = new ArrayList<>();
        for (int index : selectedIndices) {
            columnsToRemove.add(sourceColumnsModel.getElementAt(index));
        }

        // 从数据模型中删除所选列
        for (String selectedColumn : columnsToRemove) {
            sourceColumnsModel.removeElement(selectedColumn);

            // 从数据模型中删除
            String[] parts = selectedColumn.split("\\.");
            if (parts.length == 2) {
                wizard.removeSourceColumn(parts[0], parts[1]);
            }
        }

        // 如果删除了多个项目，显示通知
        if (columnsToRemove.size() > 1) {
            JOptionPane.showMessageDialog(this,
                    columnsToRemove.size() + " source columns removed.",
                    "Columns Removed", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void addTargetColumn() {
        String tableName = (String) targetTableCombo.getSelectedItem();
        String columnName = targetColumnCombo.getSelectedText();

        if (tableName != null && columnName != null && !columnName.trim().isEmpty()) {
            // Check if column is already added
            String columnKey = tableName + "." + columnName;
            for (int i = 0; i < targetColumnsModel.size(); i++) {
                if (targetColumnsModel.getElementAt(i).equals(columnKey)) {
                    JOptionPane.showMessageDialog(this,
                            "This column has already been added.",
                            "Duplicate Column", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            wizard.addTargetColumn(tableName, columnName);
            targetColumnsModel.addElement(columnKey);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select or enter a valid column name.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteTargetColumn() {
        // 获取所有选中的索引
        int[] selectedIndices = targetColumnsList.getSelectedIndices();
        if (selectedIndices.length == 0) {
            return;
        }

        // 创建要删除的列的列表
        List<String> columnsToRemove = new ArrayList<>();
        for (int index : selectedIndices) {
            columnsToRemove.add(targetColumnsModel.getElementAt(index));
        }

        // 从数据模型中删除所选列
        for (String selectedColumn : columnsToRemove) {
            targetColumnsModel.removeElement(selectedColumn);

            // 从数据模型中删除
            String[] parts = selectedColumn.split("\\.");
            if (parts.length == 2) {
                wizard.removeTargetColumn(parts[0], parts[1]);
            }
        }

        // 如果删除了多个项目，显示通知
        if (columnsToRemove.size() > 1) {
            JOptionPane.showMessageDialog(this,
                    columnsToRemove.size() + " target columns removed.",
                    "Columns Removed", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // New methods for column selection operations

    private void selectAllSourceColumns() {
        String tableName = (String) sourceTableCombo.getSelectedItem();
        if (tableName == null || availableSourceColumns.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No source columns available to select.",
                    "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int addedCount = 0;
        for (String columnName : availableSourceColumns) {
            String columnKey = tableName + "." + columnName;

            // Check if column is already added
            boolean exists = false;
            for (int i = 0; i < sourceColumnsModel.size(); i++) {
                if (sourceColumnsModel.getElementAt(i).equals(columnKey)) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                wizard.addSourceColumn(tableName, columnName);
                sourceColumnsModel.addElement(columnKey);
                addedCount++;
            }
        }

        if (addedCount > 0) {
            JOptionPane.showMessageDialog(this,
                    addedCount + " source columns added successfully.",
                    "Columns Added", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "All columns are already added.",
                    "No Change", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deselectAllSourceColumns() {
        if (sourceColumnsModel.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No source columns to remove.",
                    "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tableName = (String) sourceTableCombo.getSelectedItem();
        if (tableName == null) return;

        // Create a list of columns to remove
        List<String> columnsToRemove = new ArrayList<>();
        for (int i = 0; i < sourceColumnsModel.size(); i++) {
            String columnKey = sourceColumnsModel.getElementAt(i);
            if (columnKey.startsWith(tableName + ".")) {
                columnsToRemove.add(columnKey);
            }
        }

        // Remove columns from model and wizard
        for (String columnKey : columnsToRemove) {
            sourceColumnsModel.removeElement(columnKey);
            String[] parts = columnKey.split("\\.");
            if (parts.length == 2) {
                wizard.removeSourceColumn(parts[0], parts[1]);
            }
        }

        if (!columnsToRemove.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    columnsToRemove.size() + " source columns removed.",
                    "Columns Removed", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void invertSourceColumnSelection() {
        String tableName = (String) sourceTableCombo.getSelectedItem();
        if (tableName == null || availableSourceColumns.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No source columns available to select.",
                    "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create a set of currently selected columns
        List<String> currentlySelected = new ArrayList<>();
        for (int i = 0; i < sourceColumnsModel.size(); i++) {
            String columnKey = sourceColumnsModel.getElementAt(i);
            if (columnKey.startsWith(tableName + ".")) {
                String[] parts = columnKey.split("\\.");
                if (parts.length == 2) {
                    currentlySelected.add(parts[1]);
                }
            }
        }

        // Remove currently selected columns
        for (String columnName : currentlySelected) {
            wizard.removeSourceColumn(tableName, columnName);
            sourceColumnsModel.removeElement(tableName + "." + columnName);
        }

        // Add currently unselected columns
        int addedCount = 0;
        for (String columnName : availableSourceColumns) {
            if (!currentlySelected.contains(columnName)) {
                wizard.addSourceColumn(tableName, columnName);
                sourceColumnsModel.addElement(tableName + "." + columnName);
                addedCount++;
            }
        }

        JOptionPane.showMessageDialog(this,
                currentlySelected.size() + " columns removed and " +
                        addedCount + " columns added.",
                "Selection Inverted", JOptionPane.INFORMATION_MESSAGE);
    }

    private void selectAllTargetColumns() {
        String tableName = (String) targetTableCombo.getSelectedItem();
        if (tableName == null || availableTargetColumns.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No target columns available to select.",
                    "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int addedCount = 0;
        for (String columnName : availableTargetColumns) {
            String columnKey = tableName + "." + columnName;

            // Check if column is already added
            boolean exists = false;
            for (int i = 0; i < targetColumnsModel.size(); i++) {
                if (targetColumnsModel.getElementAt(i).equals(columnKey)) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                wizard.addTargetColumn(tableName, columnName);
                targetColumnsModel.addElement(columnKey);
                addedCount++;
            }
        }

        if (addedCount > 0) {
            JOptionPane.showMessageDialog(this,
                    addedCount + " target columns added successfully.",
                    "Columns Added", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "All columns are already added.",
                    "No Change", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deselectAllTargetColumns() {
        if (targetColumnsModel.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No target columns to remove.",
                    "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tableName = (String) targetTableCombo.getSelectedItem();
        if (tableName == null) return;

        // Create a list of columns to remove
        List<String> columnsToRemove = new ArrayList<>();
        for (int i = 0; i < targetColumnsModel.size(); i++) {
            String columnKey = targetColumnsModel.getElementAt(i);
            if (columnKey.startsWith(tableName + ".")) {
                columnsToRemove.add(columnKey);
            }
        }

        // Remove columns from model and wizard
        for (String columnKey : columnsToRemove) {
            targetColumnsModel.removeElement(columnKey);
            String[] parts = columnKey.split("\\.");
            if (parts.length == 2) {
                wizard.removeTargetColumn(parts[0], parts[1]);
            }
        }

        if (!columnsToRemove.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    columnsToRemove.size() + " target columns removed.",
                    "Columns Removed", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void invertTargetColumnSelection() {
        String tableName = (String) targetTableCombo.getSelectedItem();
        if (tableName == null || availableTargetColumns.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No target columns available to select.",
                    "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create a list of currently selected columns
        List<String> currentlySelected = new ArrayList<>();
        for (int i = 0; i < targetColumnsModel.size(); i++) {
            String columnKey = targetColumnsModel.getElementAt(i);
            if (columnKey.startsWith(tableName + ".")) {
                String[] parts = columnKey.split("\\.");
                if (parts.length == 2) {
                    currentlySelected.add(parts[1]);
                }
            }
        }

        // Remove currently selected columns
        for (String columnName : currentlySelected) {
            wizard.removeTargetColumn(tableName, columnName);
            targetColumnsModel.removeElement(tableName + "." + columnName);
        }

        // Add currently unselected columns
        int addedCount = 0;
        for (String columnName : availableTargetColumns) {
            if (!currentlySelected.contains(columnName)) {
                wizard.addTargetColumn(tableName, columnName);
                targetColumnsModel.addElement(tableName + "." + columnName);
                addedCount++;
            }
        }

        JOptionPane.showMessageDialog(this,
                currentlySelected.size() + " columns removed and " +
                        addedCount + " columns added.",
                "Selection Inverted", JOptionPane.INFORMATION_MESSAGE);
    }

    public void updateComponents() {
        // Clear and update the source table combo
        sourceTableCombo.removeAllItems();
        Map<String, SourceTable> sourceTables = wizard.getSourceTables();

        // Convert to sorted array for alphabetical order
        String[] sourceTableNames = sourceTables.keySet().toArray(new String[0]);
        java.util.Arrays.sort(sourceTableNames);

        for (String tableName : sourceTableNames) {
            sourceTableCombo.addItem(tableName);
        }

        // Clear and update the target table combo
        targetTableCombo.removeAllItems();
        Map<String, TargetTable> targetTables = wizard.getTargetTables();

        // Convert to sorted array for alphabetical order
        String[] targetTableNames = targetTables.keySet().toArray(new String[0]);
        java.util.Arrays.sort(targetTableNames);

        for (String tableName : targetTableNames) {
            targetTableCombo.addItem(tableName);
        }

        // Update columns if tables are selected
        if (sourceTableCombo.getItemCount() > 0) {
            updateSourceColumns();
        }

        if (targetTableCombo.getItemCount() > 0) {
            updateTargetColumns();
        }
    }
}