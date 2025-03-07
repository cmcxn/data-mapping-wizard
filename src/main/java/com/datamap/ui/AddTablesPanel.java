package com.datamap.ui;

import com.datamap.model.DataSource;
import com.datamap.util.DatabaseConnectionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddTablesPanel extends JPanel {
    private DataMapWizard wizard;
    private DefaultListModel<String> sourceTablesModel;
    private DefaultListModel<String> targetTablesModel;
    
    private JComboBox<DataSource> sourceDataSourceCombo;
    private AutoCompleteComboBox sourceTableComboBox;
    private JComboBox<DataSource> targetDataSourceCombo;
    private AutoCompleteComboBox targetTableComboBox;
    private AutoCompleteComboBox sourceTableForTargetCombo;
    private JList<String> sourceTablesList;
    private JList<String> targetTablesList;
    
    public AddTablesPanel(DataMapWizard wizard) {
        this.wizard = wizard;
        setLayout(new BorderLayout());
        
        sourceTablesModel = new DefaultListModel<>();
        targetTablesModel = new DefaultListModel<>();
        
        initComponents();
    }
    
    private void initComponents() {
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        
        // Source tables panel
        JPanel sourcePanel = new JPanel(new BorderLayout());
        sourcePanel.setBorder(BorderFactory.createTitledBorder("Source Tables"));
        
        JPanel sourceInputPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        sourceInputPanel.add(new JLabel("Data Source:"));
        sourceDataSourceCombo = new JComboBox<>();
        sourceDataSourceCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DataSource) {
                    setText(((DataSource) value).getName());
                }
                return this;
            }
        });
        sourceDataSourceCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateSourceTables();
                }
            }
        });
        sourceInputPanel.add(sourceDataSourceCombo);
        
        sourceInputPanel.add(new JLabel("Source Table:"));
        sourceTableComboBox = new AutoCompleteComboBox();
        sourceInputPanel.add(sourceTableComboBox);
        
        JButton addSourceButton = new JButton("Add Source Table");
        addSourceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addSourceTable();
            }
        });
        
        JButton deleteSourceButton = new JButton("Delete Source Table");
        deleteSourceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSourceTable();
            }
        });
        
        sourceTablesList = new JList<>(sourceTablesModel);
        JScrollPane sourceScrollPane = new JScrollPane(sourceTablesList);
        
        JPanel sourceActionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sourceActionsPanel.add(addSourceButton);
        sourceActionsPanel.add(deleteSourceButton);
        
        sourcePanel.add(sourceInputPanel, BorderLayout.NORTH);
        sourcePanel.add(sourceScrollPane, BorderLayout.CENTER);
        sourcePanel.add(sourceActionsPanel, BorderLayout.SOUTH);
        
        // Target tables panel
        JPanel targetPanel = new JPanel(new BorderLayout());
        targetPanel.setBorder(BorderFactory.createTitledBorder("Target Tables"));
        
        JPanel targetInputPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        targetInputPanel.add(new JLabel("Based on Source Table:"));
        sourceTableForTargetCombo = new AutoCompleteComboBox();
        sourceTableForTargetCombo.setName("sourceTableForTargetCombo"); // for refreshing
        targetInputPanel.add(sourceTableForTargetCombo);
        
        targetInputPanel.add(new JLabel("Target Data Source:"));
        targetDataSourceCombo = new JComboBox<>();
        targetDataSourceCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DataSource) {
                    setText(((DataSource) value).getName());
                }
                return this;
            }
        });
        targetDataSourceCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateTargetTables();
                }
            }
        });
        targetInputPanel.add(targetDataSourceCombo);
        
        targetInputPanel.add(new JLabel("Target Table:"));
        targetTableComboBox = new AutoCompleteComboBox();
        targetInputPanel.add(targetTableComboBox);
        
        JButton addTargetButton = new JButton("Add Target Table");
        addTargetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTargetTable();
            }
        });
        
        JButton deleteTargetButton = new JButton("Delete Target Table");
        deleteTargetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteTargetTable();
            }
        });
        
        targetTablesList = new JList<>(targetTablesModel);
        JScrollPane targetScrollPane = new JScrollPane(targetTablesList);
        
        JPanel targetActionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        targetActionsPanel.add(addTargetButton);
        targetActionsPanel.add(deleteTargetButton);
        
        targetPanel.add(targetInputPanel, BorderLayout.NORTH);
        targetPanel.add(targetScrollPane, BorderLayout.CENTER);
        targetPanel.add(targetActionsPanel, BorderLayout.SOUTH);
        
        contentPanel.add(sourcePanel);
        contentPanel.add(targetPanel);
        
        add(contentPanel, BorderLayout.CENTER);
        
        JLabel instructionLabel = new JLabel("Step 1: Add source and target tables");
        instructionLabel.setFont(new Font(instructionLabel.getFont().getName(), Font.BOLD, 14));
        add(instructionLabel, BorderLayout.NORTH);
        
        // Initialize with available data sources
        populateDataSources();
    }
    
    private void populateDataSources() {
        List<DataSource> dataSources = wizard.getDataSources();
        sourceDataSourceCombo.removeAllItems();
        targetDataSourceCombo.removeAllItems();
        
        for (DataSource ds : dataSources) {
            sourceDataSourceCombo.addItem(ds);
            targetDataSourceCombo.addItem(ds);
        }
        
        // Update tables if data sources are available
        if (sourceDataSourceCombo.getItemCount() > 0) {
            updateSourceTables();
        }
        if (targetDataSourceCombo.getItemCount() > 0) {
            updateTargetTables();
        }
    }
    
    private void updateSourceTables() {
        DataSource selectedDS = (DataSource) sourceDataSourceCombo.getSelectedItem();
        if (selectedDS == null) return;
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        try {
            Connection conn = DatabaseConnectionManager.getConnection(selectedDS);
            List<String> tables = DatabaseConnectionManager.getTables(conn);
            
            // Sort tables alphabetically
            Collections.sort(tables);
            
            // Update the autocomplete items
            sourceTableComboBox.setAutoCompleteItems(tables);
            
            if (conn != null) conn.close();
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, 
                "Error retrieving tables: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    private void updateTargetTables() {
        DataSource selectedDS = (DataSource) targetDataSourceCombo.getSelectedItem();
        if (selectedDS == null) return;
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        try {
            Connection conn = DatabaseConnectionManager.getConnection(selectedDS);
            List<String> tables = DatabaseConnectionManager.getTables(conn);
            
            // Sort tables alphabetically
            Collections.sort(tables);
            
            // Update the autocomplete items
            targetTableComboBox.setAutoCompleteItems(tables);
            
            if (conn != null) conn.close();
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, 
                "Error retrieving tables: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void addSourceTable() {
        String tableName = sourceTableComboBox.getSelectedText();
        DataSource dataSource = (DataSource) sourceDataSourceCombo.getSelectedItem();

        if (tableName != null && !tableName.trim().isEmpty() && dataSource != null) {
            // Check if the table already exists in the list
            for (int i = 0; i < sourceTablesModel.size(); i++) {
                if (sourceTablesModel.getElementAt(i).equals(tableName)) {
                    JOptionPane.showMessageDialog(this,
                            "This table has already been added.",
                            "Duplicate Table", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            // Add the source table with data source info
            wizard.addSourceTable(tableName, dataSource);
            sourceTablesModel.addElement(tableName);

            // Update the source table for target combo
            List<String> sourceTables = new ArrayList<>();
            for (int i = 0; i < sourceTablesModel.size(); i++) {
                sourceTables.add(sourceTablesModel.getElementAt(i));
            }
            sourceTableForTargetCombo.setAutoCompleteItems(sourceTables);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select or enter a valid table name.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void deleteSourceTable() {
        int selectedIndex = sourceTablesList.getSelectedIndex();
        if (selectedIndex >= 0) {
            String selectedTable = sourceTablesModel.getElementAt(selectedIndex);
            sourceTablesModel.remove(selectedIndex);
            
            // Delete from the data model
            wizard.removeSourceTable(selectedTable);
            
            // Update the source table for target combo
            List<String> sourceTables = new ArrayList<>();
            for (int i = 0; i < sourceTablesModel.size(); i++) {
                sourceTables.add(sourceTablesModel.getElementAt(i));
            }
            sourceTableForTargetCombo.setAutoCompleteItems(sourceTables);
        }
    }
    
    private void addTargetTable() {
        String sourceTableName = sourceTableForTargetCombo.getSelectedText();
        String targetTableName = targetTableComboBox.getSelectedText();
        DataSource dataSource = (DataSource) targetDataSourceCombo.getSelectedItem();
        
        if (sourceTableName != null && !sourceTableName.trim().isEmpty() && 
            targetTableName != null && !targetTableName.trim().isEmpty() && 
            dataSource != null) {
            // Check if the target table already exists in the list
            for (int i = 0; i < targetTablesModel.size(); i++) {
                if (targetTablesModel.getElementAt(i).equals(targetTableName)) {
                    JOptionPane.showMessageDialog(this, 
                        "This target table has already been added.", 
                        "Duplicate Table", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            
            // Check if source table exists
            boolean sourceExists = false;
            for (int i = 0; i < sourceTablesModel.size(); i++) {
                if (sourceTablesModel.getElementAt(i).equals(sourceTableName)) {
                    sourceExists = true;
                    break;
                }
            }
            
            if (sourceExists) {
                // Add the target table with data source info
                wizard.addTargetTable(sourceTableName, targetTableName, dataSource);
                targetTablesModel.addElement(targetTableName);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Source table '" + sourceTableName + "' does not exist. Please add it first.", 
                    "Invalid Source", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select or enter valid table names.", 
                "Input Error", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void deleteTargetTable() {
        int selectedIndex = targetTablesList.getSelectedIndex();
        if (selectedIndex >= 0) {
            String selectedTable = targetTablesModel.getElementAt(selectedIndex);
            targetTablesModel.remove(selectedIndex);
            
            // Delete from the data model
            wizard.removeTargetTable(selectedTable);
        }
    }
    
    public void refreshDataSources() {
        populateDataSources();
        
        // Update the source table for target combo with currently added tables
        List<String> sourceTables = new ArrayList<>();
        for (int i = 0; i < sourceTablesModel.size(); i++) {
            sourceTables.add(sourceTablesModel.getElementAt(i));
        }
        sourceTableForTargetCombo.setAutoCompleteItems(sourceTables);
    }
}