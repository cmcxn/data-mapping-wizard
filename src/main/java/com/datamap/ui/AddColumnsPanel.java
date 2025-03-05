package com.datamap.ui;

import com.datamap.model.SourceTable;
import com.datamap.model.TargetTable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class AddColumnsPanel extends JPanel {
    private DataMapWizard wizard;
    private JComboBox<String> sourceTableCombo;
    private JComboBox<String> targetTableCombo;
    private JTextField sourceColumnField;
    private JTextField targetColumnField;
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
        sourceInputPanel.add(sourceTableCombo);
        
        sourceInputPanel.add(new JLabel("Column Name:"));
        sourceColumnField = new JTextField(20);
        sourceInputPanel.add(sourceColumnField);
        
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
        targetInputPanel.add(targetTableCombo);
        
        targetInputPanel.add(new JLabel("Column Name:"));
        targetColumnField = new JTextField(20);
        targetInputPanel.add(targetColumnField);
        
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
    
    private void addSourceColumn() {
        String tableName = (String) sourceTableCombo.getSelectedItem();
        String columnName = sourceColumnField.getText().trim();
        
        if (tableName != null && !columnName.isEmpty()) {
            wizard.addSourceColumn(tableName, columnName);
            sourceColumnsModel.addElement(tableName + "." + columnName);
            sourceColumnField.setText("");
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
        String columnName = targetColumnField.getText().trim();
        
        if (tableName != null && !columnName.isEmpty()) {
            wizard.addTargetColumn(tableName, columnName);
            targetColumnsModel.addElement(tableName + "." + columnName);
            targetColumnField.setText("");
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
    }
}