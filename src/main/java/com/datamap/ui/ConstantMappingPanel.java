package com.datamap.ui;

import com.datamap.model.TargetColumn;
import com.datamap.model.mapping.Constant;
import com.datamap.model.mapping.Mapping;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

public class ConstantMappingPanel extends JPanel {
    private DataMapWizard wizard;
    private JComboBox<String> targetColumnCombo;
    private JTextField constantValueField;
    private JTable mappingsTable;
    private DefaultTableModel tableModel;
    
    public ConstantMappingPanel(DataMapWizard wizard) {
        this.wizard = wizard;
        setLayout(new BorderLayout(10, 10));
        
        initComponents();
    }
    
    private void initComponents() {
        // Create main panel with padding
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Input panel for mapping creation
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Create Constant Mapping"),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        // Use GridBagLayout for better control
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 10);
        
        // Target column selector
        inputPanel.add(new JLabel("Target Column:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        targetColumnCombo = new JComboBox<>();
        inputPanel.add(targetColumnCombo, gbc);
        
        // Constant value field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        inputPanel.add(new JLabel("Constant Value:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        constantValueField = new JTextField(20);
        inputPanel.add(constantValueField, gbc);
        
        // Add button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(15, 5, 5, 5);
        JButton addButton = new JButton("Add Constant Mapping");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addMapping();
            }
        });
        inputPanel.add(addButton, gbc);
        
        // Table to display mappings
        String[] columnNames = {"Target Column", "Constant Value", "Actions"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make only the action column editable
                return column == 2;
            }
        };
        mappingsTable = new JTable(tableModel);
        mappingsTable.getColumnModel().getColumn(2).setCellRenderer(new ButtonRenderer());
        mappingsTable.getColumnModel().getColumn(2).setCellEditor(new ButtonEditor(new JCheckBox(), "Delete"));
        
        // Listen for button clicks in the table
        mappingsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int column = mappingsTable.getColumnModel().getColumnIndexAtX(e.getX());
                int row = e.getY() / mappingsTable.getRowHeight();
                
                if (row < mappingsTable.getRowCount() && row >= 0 && column == 2) {
                    deleteMapping(row);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(mappingsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Existing Constant Mappings"));
        
        // Layout
        contentPanel.add(inputPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Header
        JLabel instructionLabel = new JLabel("Step 5: Create Constant Mappings");
        instructionLabel.setFont(new Font(instructionLabel.getFont().getName(), Font.BOLD, 14));
        add(instructionLabel, BorderLayout.NORTH);
    }
    
    private void addMapping() {
        String targetColumnKey = (String) targetColumnCombo.getSelectedItem();
        String constantValue = constantValueField.getText();
        
        if (targetColumnKey != null && !constantValue.isEmpty()) {
            String[] targetParts = targetColumnKey.split("\\.");
            
            if (targetParts.length == 2) {
                // Check if this mapping already exists
                int existingMappingIndex = findExistingMapping(targetParts[0], targetParts[1]);
                if (existingMappingIndex >= 0) {
                    int option = JOptionPane.showConfirmDialog(this, 
                            "A mapping for this target column already exists. Replace it?", 
                            "Mapping Already Exists", 
                            JOptionPane.YES_NO_OPTION);
                    
                    if (option == JOptionPane.YES_OPTION) {
                        deleteMapping(existingMappingIndex);
                    } else {
                        return;
                    }
                }
                
                // Add the mapping to the wizard
                wizard.addConstantMapping(targetParts[0], targetParts[1], constantValue);
                
                // Add to table
                Object[] rowData = {targetColumnKey, constantValue, "Delete"};
                tableModel.addRow(rowData);
                
                // Clear constant value field for next entry
                constantValueField.setText("");
                
                JOptionPane.showMessageDialog(this, 
                        "Constant mapping created successfully: " + targetColumnKey + " <- Constant(\"" + constantValue + "\")", 
                        "Mapping Created", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                    "Please select a target column and enter a constant value", 
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void deleteMapping(int row) {
        if (row >= 0 && row < tableModel.getRowCount()) {
            String targetCol = (String) tableModel.getValueAt(row, 0);
            
            // Ask for confirmation
            int option = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete the constant mapping for " + targetCol + "?", 
                    "Confirm Deletion", 
                    JOptionPane.YES_NO_OPTION);
            
            if (option == JOptionPane.YES_OPTION) {
                // Find correct index in the wizard's mapping list
                int mappingIndex = findMappingIndexInWizard(row);
                if (mappingIndex >= 0) {
                    wizard.removeMapping(mappingIndex, "Constant");
                    tableModel.removeRow(row);
                    JOptionPane.showMessageDialog(this, 
                            "Constant mapping deleted successfully", 
                            "Mapping Deleted", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }
    
    private int findExistingMapping(String tableName, String columnName) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String targetCol = (String) tableModel.getValueAt(i, 0);
            String[] targetParts = targetCol.split("\\.");
            
            if (targetParts.length == 2 && 
                targetParts[0].equals(tableName) && 
                targetParts[1].equals(columnName)) {
                return i;
            }
        }
        return -1;
    }
    
    private int findMappingIndexInWizard(int tableRow) {
        if (tableRow < 0 || tableRow >= tableModel.getRowCount()) return -1;
        
        String targetCol = (String) tableModel.getValueAt(tableRow, 0);
        String[] targetParts = targetCol.split("\\.");
        
        if (targetParts.length != 2) return -1;
        
        // Find the actual index in the wizard's mapping list
        List<Mapping> allMappings = wizard.getMappings();
        for (int i = 0; i < allMappings.size(); i++) {
            Mapping mapping = allMappings.get(i);
            if (mapping instanceof Constant) {
                String mappingTableName = mapping.getTargetColumn().getTable().getName();
                String mappingColumnName = mapping.getTargetColumn().getName();
                
                if (mappingTableName.equals(targetParts[0]) && 
                    mappingColumnName.equals(targetParts[1])) {
                    return i;
                }
            }
        }
        
        return -1;
    }
    
    public void updateComponents() {
        targetColumnCombo.removeAllItems();
        
        Map<String, TargetColumn> targetColumns = wizard.getTargetColumns();
        
        for (String columnKey : targetColumns.keySet()) {
            targetColumnCombo.addItem(columnKey);
        }
        
        refreshMappingsTable();
    }
    
    private void refreshMappingsTable() {
        // Clear existing rows
        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }
        
        // Populate with current mappings
        List<Mapping> mappings = wizard.getMappings();
        for (Mapping mapping : mappings) {
            if (mapping instanceof Constant) {
                Constant constantMapping = (Constant) mapping;
                
                String targetTable = constantMapping.getTargetColumn().getTable().getName();
                String targetColumn = constantMapping.getTargetColumn().getName();
                String constantValue = constantMapping.getConstantValue();
                
                String targetKey = targetTable + "." + targetColumn;
                
                Object[] rowData = {targetKey, constantValue, "Delete"};
                tableModel.addRow(rowData);
            }
        }
    }
    
    // Custom renderer for the button column
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "Delete" : value.toString());
            return this;
        }
    }
    
    // Custom editor for the button column
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        
        public ButtonEditor(JCheckBox checkBox, String label) {
            super(checkBox);
            this.label = label;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            button.setText(label);
            isPushed = true;
            return button;
        }
        
        @Override
        public Object getCellEditorValue() {
            isPushed = false;
            return label;
        }
        
        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
}