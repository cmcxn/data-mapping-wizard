package com.datamap.ui;

import com.datamap.model.DataSource;
import com.datamap.model.SourceTable;
import com.datamap.model.TargetTable;
import com.datamap.util.DatabaseConnectionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class AddColumnsPanel extends JPanel {
    private DataMapWizard wizard;
    private JComboBox<String> sourceTableCombo;
    private JComboBox<String> targetTableCombo;
    private JComboBox<String> sourceColumnCombo;
    private JComboBox<String> targetColumnCombo;
    private DefaultListModel<String> sourceColumnsModel;
    private DefaultListModel<String> targetColumnsModel;
    private JList<String> sourceColumnsList;
    private JList<String> targetColumnsList;
    
    public AddColumnsPanel(DataMapWizard wizard) {
        this.wizard = wizard;
        setLayout(new BorderLayout());
        
        sourceColumnsModel = new DefaultListModel<>();
        targetColumnsModel = new DefaultListModel<>();
        
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
        sourceColumnCombo = new JComboBox<>();
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
        JScrollPane sourceScrollPane = new JScrollPane(sourceColumnsList);
        
        JPanel sourceActionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sourceActionsPanel.add(addSourceColumnButton);
        sourceActionsPanel.add(deleteSourceColumnButton);
        
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
        targetColumnCombo = new JComboBox<>();
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
        JScrollPane targetScrollPane = new JScrollPane(targetColumnsList);
        
        JPanel targetActionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        targetActionsPanel.add(addTargetColumnButton);
        targetActionsPanel.add(deleteTargetColumnButton);
        
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
        
        sourceColumnCombo.removeAllItems();
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
            
            for (String column : columns) {
                sourceColumnCombo.addItem(column);
            }
            
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
        
        targetColumnCombo.removeAllItems();
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
            
            for (String column : columns) {
                targetColumnCombo.addItem(column);
            }
            
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
        String columnName = (String) sourceColumnCombo.getSelectedItem();
        
        if (tableName != null && columnName != null) {
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
        }
    }
    
    private void deleteSourceColumn() {
        int selectedIndex = sourceColumnsList.getSelectedIndex();
        if (selectedIndex >= 0) {
            String selectedColumn = sourceColumnsModel.getElementAt(selectedIndex);
            sourceColumnsModel.remove(selectedIndex);
            
            // Delete from the data model
            String[] parts = selectedColumn.split("\\.");
            if (parts.length == 2) {
                wizard.removeSourceColumn(parts[0], parts[1]);
            }
        }
    }
    
    private void addTargetColumn() {
        String tableName = (String) targetTableCombo.getSelectedItem();
        String columnName = (String) targetColumnCombo.getSelectedItem();
        
        if (tableName != null && columnName != null) {
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
        }
    }
    
    private void deleteTargetColumn() {
        int selectedIndex = targetColumnsList.getSelectedIndex();
        if (selectedIndex >= 0) {
            String selectedColumn = targetColumnsModel.getElementAt(selectedIndex);
            targetColumnsModel.remove(selectedIndex);
            
            // Delete from the data model
            String[] parts = selectedColumn.split("\\.");
            if (parts.length == 2) {
                wizard.removeTargetColumn(parts[0], parts[1]);
            }
        }
    }
    
    public void updateComponents() {
        sourceTableCombo.removeAllItems();
        targetTableCombo.removeAllItems();
        
        Map<String, SourceTable> sourceTables = wizard.getSourceTables();
        Map<String, TargetTable> targetTables = wizard.getTargetTables();
        
        for (String tableName : sourceTables.keySet()) {
            sourceTableCombo.addItem(tableName);
        }
        
        for (String tableName : targetTables.keySet()) {
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