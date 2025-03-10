package com.datamap.ui;

import com.datamap.model.SourceColumn;
import com.datamap.model.TargetColumn;
import com.datamap.model.mapping.ExternalConnection;
import com.datamap.model.mapping.LeftJoin;
import com.datamap.model.mapping.Mapping;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExternalConnectionPanel extends JPanel {
    private DataMapWizard wizard;
    private JComboBox<String> finalSelectColumnCombo;
    private JComboBox<String> whereIdColumnCombo;
    private JComboBox<String> whereSelectTableCombo; // New component
    private JComboBox<String> sourceIdColumnCombo;
    private JComboBox<String> targetColumnCombo;
    
    private JComboBox<String> leftColumnCombo;
    private JComboBox<String> rightColumnCombo;
    
    private JPanel joinsPanel;
    private List<JoinRow> joinRows;
    private int currentMappingIndex = -1;
    
    private JTable mappingsTable;
    private DefaultTableModel tableModel;
    
    public ExternalConnectionPanel(DataMapWizard wizard) {
        this.wizard = wizard;
        setLayout(new BorderLayout(10, 10));
        
        joinRows = new ArrayList<>();
        
        initComponents();
    }
    
    private void initComponents() {
        // Create main panel with padding
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Main input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Create External Connection Mapping"),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        // Base mapping section
        JPanel baseMappingPanel = new JPanel(new GridBagLayout());
        TitledBorder baseMappingBorder = BorderFactory.createTitledBorder("Base Mapping");
        baseMappingPanel.setBorder(BorderFactory.createCompoundBorder(
            baseMappingBorder,
            new EmptyBorder(5, 5, 5, 5)
        ));
        
        // Use GridBagLayout for better control
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 10);
        
        // Target column selector
        baseMappingPanel.add(new JLabel("Target Column:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        targetColumnCombo = new JComboBox<>();
        baseMappingPanel.add(targetColumnCombo, gbc);
        
        // Final select column
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        baseMappingPanel.add(new JLabel("Final Select Column:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        finalSelectColumnCombo = new JComboBox<>();
        baseMappingPanel.add(finalSelectColumnCombo, gbc);
        
        // Where ID column
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        baseMappingPanel.add(new JLabel("Where ID Column:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        whereIdColumnCombo = new JComboBox<>();
        baseMappingPanel.add(whereIdColumnCombo, gbc);
        
        // Where Select Table
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        baseMappingPanel.add(new JLabel("Where Select Table:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        whereSelectTableCombo = new JComboBox<>();
        baseMappingPanel.add(whereSelectTableCombo, gbc);
        
        // Source ID column
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        baseMappingPanel.add(new JLabel("Source ID Column:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        sourceIdColumnCombo = new JComboBox<>();
        baseMappingPanel.add(sourceIdColumnCombo, gbc);
        
        // LEFT JOINs section
        JPanel joinsContainer = new JPanel(new BorderLayout());
        TitledBorder joinsBorder = BorderFactory.createTitledBorder("LEFT JOINs (Optional)");
        joinsContainer.setBorder(BorderFactory.createCompoundBorder(
            joinsBorder,
            new EmptyBorder(5, 5, 5, 5)
        ));
        
        joinsPanel = new JPanel();
        joinsPanel.setLayout(new BoxLayout(joinsPanel, BoxLayout.Y_AXIS));
        
        JPanel joinControlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Join column selectors
        joinControlsPanel.add(new JLabel("Left Column:"));
        leftColumnCombo = new JComboBox<>();
        joinControlsPanel.add(leftColumnCombo);
        
        joinControlsPanel.add(new JLabel("Right Column:"));
        rightColumnCombo = new JComboBox<>();
        joinControlsPanel.add(rightColumnCombo);
        
        JButton addJoinButton = new JButton("Add LEFT JOIN");
        addJoinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addJoin();
            }
        });
        joinControlsPanel.add(addJoinButton);
        
        JScrollPane joinsScrollPane = new JScrollPane(joinsPanel);
        joinsScrollPane.setPreferredSize(new Dimension(500, 120));
        
        joinsContainer.add(joinControlsPanel, BorderLayout.NORTH);
        joinsContainer.add(joinsScrollPane, BorderLayout.CENTER);
        
        // Add mapping button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addMappingButton = new JButton("Add External Connection Mapping");
        addMappingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addMapping();
            }
        });
        buttonPanel.add(addMappingButton);
        
        // Add sections to the input panel
        inputPanel.add(baseMappingPanel);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        inputPanel.add(joinsContainer);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        inputPanel.add(buttonPanel);
        
        // Table to display mappings
        String[] columnNames = {"Target Column", "Details", "Actions"};
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
        
        // Set preferred column widths
        mappingsTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        mappingsTable.getColumnModel().getColumn(1).setPreferredWidth(400);
        mappingsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        
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
        scrollPane.setBorder(BorderFactory.createTitledBorder("Existing External Connection Mappings"));
        
        // Layout
        contentPanel.add(inputPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Header
        JLabel instructionLabel = new JLabel("Step 6: Create External Connection Mappings");
        instructionLabel.setFont(new Font(instructionLabel.getFont().getName(), Font.BOLD, 14));
        add(instructionLabel, BorderLayout.NORTH);
    }
    
    private void addJoin() {
        String leftColumnKey = (String) leftColumnCombo.getSelectedItem();
        String rightColumnKey = (String) rightColumnCombo.getSelectedItem();
        
        if (leftColumnKey != null && rightColumnKey != null) {
            // Create a join row UI component
            JoinRow joinRow = new JoinRow(leftColumnKey, rightColumnKey);
            joinRows.add(joinRow);
            joinsPanel.add(joinRow);
            joinsPanel.revalidate();
            joinsPanel.repaint();
        } else {
            JOptionPane.showMessageDialog(this, 
                    "Please select both left and right columns for the join", 
                    "Invalid Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void removeJoin(JoinRow joinRow) {
        joinRows.remove(joinRow);
        joinsPanel.remove(joinRow);
        joinsPanel.revalidate();
        joinsPanel.repaint();
    }
    
    private void clearJoinRows() {
        joinRows.clear();
        joinsPanel.removeAll();
        joinsPanel.revalidate();
        joinsPanel.repaint();
    }
    
    private void addMapping() {
        String targetColumnKey = (String) targetColumnCombo.getSelectedItem();
        String finalSelectColumnKey = (String) finalSelectColumnCombo.getSelectedItem();
        String whereIdColumnKey = (String) whereIdColumnCombo.getSelectedItem();
        String whereSelectTableKey = (String) whereSelectTableCombo.getSelectedItem();
        String sourceIdColumnKey = (String) sourceIdColumnCombo.getSelectedItem();
        
        if (targetColumnKey != null && finalSelectColumnKey != null && 
            whereIdColumnKey != null && sourceIdColumnKey != null &&
            whereSelectTableKey != null) {
            
            String[] targetParts = targetColumnKey.split("\\.");
            String[] finalSelectParts = finalSelectColumnKey.split("\\.");
            String[] whereIdParts = whereIdColumnKey.split("\\.");
            String[] whereSelectTableParts = whereSelectTableKey.split("\\.");
            String[] sourceIdParts = sourceIdColumnKey.split("\\.");
            
            if (targetParts.length == 2 && finalSelectParts.length == 2 && 
                whereIdParts.length == 2 && sourceIdParts.length == 2 &&
                whereSelectTableParts.length == 2) {
                
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
                
                // Add the base mapping
                int mappingIndex = wizard.addExternalConnectionMapping(
                        targetParts[0], targetParts[1],
                        finalSelectParts[0], finalSelectParts[1],
                        whereSelectTableParts[0], whereIdParts[1],
                        sourceIdParts[0], sourceIdParts[1]);
                
                if (mappingIndex >= 0) {
                    // Add LEFT JOINs if any
                    for (JoinRow joinRow : joinRows) {
                        String leftKey = joinRow.getLeftColumnKey();
                        String rightKey = joinRow.getRightColumnKey();
                        
                        String[] leftParts = leftKey.split("\\.");
                        String[] rightParts = rightKey.split("\\.");
                        
                        if (leftParts.length == 2 && rightParts.length == 2) {
                            wizard.addLeftJoinToExternalConnection(
                                    mappingIndex,
                                    leftParts[0], leftParts[1],
                                    rightParts[0], rightParts[1]);
                        }
                    }
                    
                    // Build the description text for the table
                    StringBuilder details = new StringBuilder();
                    details.append(finalSelectColumnKey)
                           .append(" WHERE ")
                           .append(whereIdColumnKey)
                           .append(" = ")
                           .append(sourceIdColumnKey);
                    
                    if (!joinRows.isEmpty()) {
                        details.append(" WITH LEFT JOINs: ");
                        for (int i = 0; i < joinRows.size(); i++) {
                            if (i > 0) details.append(", ");
                            details.append(joinRows.get(i).toString());
                        }
                    }
                    
                    // Add to table
                    Object[] rowData = {targetColumnKey, details.toString(), "Delete"};
                    tableModel.addRow(rowData);
                    
                    // Clear join rows for the next mapping
                    clearJoinRows();
                    
                    JOptionPane.showMessageDialog(this, 
                            "External connection mapping created successfully", 
                            "Mapping Created", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                    "Please fill in all required fields", 
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void deleteMapping(int row) {
        if (row >= 0 && row < tableModel.getRowCount()) {
            String targetCol = (String) tableModel.getValueAt(row, 0);
            
            // Ask for confirmation
            int option = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete the external connection mapping for " + targetCol + "?", 
                    "Confirm Deletion", 
                    JOptionPane.YES_NO_OPTION);
            
            if (option == JOptionPane.YES_OPTION) {
                // Find correct index in the wizard's mapping list
                int mappingIndex = findMappingIndexInWizard(row);
                if (mappingIndex >= 0) {
                    wizard.removeMapping(mappingIndex, "ExternalConnection");
                    tableModel.removeRow(row);
                    JOptionPane.showMessageDialog(this, 
                            "External connection mapping deleted successfully", 
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
            if (mapping instanceof ExternalConnection) {
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
        finalSelectColumnCombo.removeAllItems();
        whereIdColumnCombo.removeAllItems();
        whereSelectTableCombo.removeAllItems();
        sourceIdColumnCombo.removeAllItems();
        leftColumnCombo.removeAllItems();
        rightColumnCombo.removeAllItems();
        
        Map<String, SourceColumn> sourceColumns = wizard.getSourceColumns();
        Map<String, TargetColumn> targetColumns = wizard.getTargetColumns();
        
        for (String columnKey : sourceColumns.keySet()) {
            finalSelectColumnCombo.addItem(columnKey);
            whereIdColumnCombo.addItem(columnKey);
            whereSelectTableCombo.addItem(columnKey);
            sourceIdColumnCombo.addItem(columnKey);
            leftColumnCombo.addItem(columnKey);
            rightColumnCombo.addItem(columnKey);
        }
        
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
            if (mapping instanceof ExternalConnection) {
                ExternalConnection ecMapping = (ExternalConnection) mapping;
                
                String targetTable = ecMapping.getTargetColumn().getTable().getName();
                String targetColumn = ecMapping.getTargetColumn().getName();
                String finalSelectTable = ecMapping.getFinalSelectColumn().getTable().getName();
                String finalSelectColumn = ecMapping.getFinalSelectColumn().getName();
                String whereIdTable = ecMapping.getWhereIdColumn().getTable().getName();
                String whereIdColumn = ecMapping.getWhereIdColumn().getName();
                String sourceIdTable = ecMapping.getSourceIdColumn().getTable().getName();
                String sourceIdColumn = ecMapping.getSourceIdColumn().getName();
                
                String targetKey = targetTable + "." + targetColumn;
                String finalSelectKey = finalSelectTable + "." + finalSelectColumn;
                String whereIdKey = whereIdTable + "." + whereIdColumn;
                String sourceIdKey = sourceIdTable + "." + sourceIdColumn;
                
                // Build the description text for the table
                StringBuilder details = new StringBuilder();
                details.append(finalSelectKey)
                       .append(" WHERE ")
                       .append(whereIdKey)
                       .append(" = ")
                       .append(sourceIdKey);
                
                List<LeftJoin> joins = ecMapping.getJoins();
                if (!joins.isEmpty()) {
                    details.append(" WITH LEFT JOINs: ");
                    for (int i = 0; i < joins.size(); i++) {
                        LeftJoin join = joins.get(i);
                        if (i > 0) details.append(", ");
                        
                        String leftTable = join.getLeftColumn().getTable().getName();
                        String leftColumn = join.getLeftColumn().getName();
                        String rightTable = join.getRightColumn().getTable().getName();
                        String rightColumn = join.getRightColumn().getName();
                        
                        details.append(leftTable).append(".").append(leftColumn)
                               .append(" = ")
                               .append(rightTable).append(".").append(rightColumn);
                    }
                }
                
                Object[] rowData = {targetKey, details.toString(), "Delete"};
                tableModel.addRow(rowData);
            }
        }
    }
    
    // JoinRow component for displaying LEFT JOINs with delete button
    private class JoinRow extends JPanel {
        private String leftColumnKey;
        private String rightColumnKey;
        
        public JoinRow(String leftColumnKey, String rightColumnKey) {
            this.leftColumnKey = leftColumnKey;
            this.rightColumnKey = rightColumnKey;
            
            setLayout(new FlowLayout(FlowLayout.LEFT));
            
            JLabel joinLabel = new JLabel(leftColumnKey + " = " + rightColumnKey);
            add(joinLabel);
            
            JButton deleteButton = new JButton("X");
            deleteButton.setMargin(new Insets(1, 5, 1, 5));
            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    removeJoin(JoinRow.this);
                }
            });
            add(deleteButton);
        }
        
        public String getLeftColumnKey() {
            return leftColumnKey;
        }
        
        public String getRightColumnKey() {
            return rightColumnKey;
        }
        
        @Override
        public String toString() {
            return leftColumnKey + " = " + rightColumnKey;
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