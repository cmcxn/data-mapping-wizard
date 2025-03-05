package com.datamap.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddTablesPanel extends JPanel {
    private DataMapWizard wizard;
    private DefaultListModel<String> sourceTablesModel;
    private DefaultListModel<String> targetTablesModel;
    
    private JTextField sourceTableNameField;
    private JTextField targetTableNameField;
    private JComboBox<String> sourceTableCombo;
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
        
        JPanel sourceInputPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        sourceInputPanel.add(new JLabel("Source Table Name:"));
        sourceTableNameField = new JTextField(20);
        sourceInputPanel.add(sourceTableNameField);
        
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
        
        JPanel targetInputPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        targetInputPanel.add(new JLabel("Based on Source Table:"));
        sourceTableCombo = new JComboBox<>();
        targetInputPanel.add(sourceTableCombo);
        
        targetInputPanel.add(new JLabel("Target Table Name:"));
        targetTableNameField = new JTextField(20);
        targetInputPanel.add(targetTableNameField);
        
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
    }
    
    private void addSourceTable() {
        String tableName = sourceTableNameField.getText().trim();
        if (!tableName.isEmpty()) {
            wizard.addSourceTable(tableName);
            sourceTablesModel.addElement(tableName);
            sourceTableCombo.addItem(tableName);
            sourceTableNameField.setText("");
        }
    }
    
    private void deleteSourceTable() {
        int selectedIndex = sourceTablesList.getSelectedIndex();
        if (selectedIndex >= 0) {
            String selectedTable = sourceTablesModel.getElementAt(selectedIndex);
            sourceTablesModel.remove(selectedIndex);
            
            // Remove from the combo box
            for (int i = 0; i < sourceTableCombo.getItemCount(); i++) {
                if (sourceTableCombo.getItemAt(i).equals(selectedTable)) {
                    sourceTableCombo.removeItemAt(i);
                    break;
                }
            }
            
            // Delete from the data model
            wizard.removeSourceTable(selectedTable);
        }
    }
    
    private void addTargetTable() {
        String sourceTableName = (String) sourceTableCombo.getSelectedItem();
        String targetTableName = targetTableNameField.getText().trim();
        
        if (sourceTableName != null && !targetTableName.isEmpty()) {
            wizard.addTargetTable(sourceTableName, targetTableName);
            targetTablesModel.addElement(targetTableName);
            targetTableNameField.setText("");
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
}