package com.datamap.ui;

import com.datamap.model.SourceColumn;
import com.datamap.model.TargetColumn;
import com.datamap.model.mapping.LeftJoin;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExternalConnectionPanel extends JPanel {
    private DataMapWizard wizard;
    private JComboBox<String> targetColumnCombo;
    private JComboBox<String> finalSelectTableCombo;
    private JComboBox<String> finalSelectColumnCombo;
    private JComboBox<String> whereSelectTableCombo;
    private JComboBox<String> whereIdColumnCombo;
    private JComboBox<String> sourceTableCombo;
    private JComboBox<String> sourceIdColumnCombo;
    private DefaultListModel<String> mappingsModel;
    private JList<String> mappingsList;
    
    // Components for LEFT JOIN support
    private JPanel joinsPanel;
    private List<JoinRow> joinRows = new ArrayList<>();

    public ExternalConnectionPanel(DataMapWizard wizard) {
        this.wizard = wizard;
        setLayout(new BorderLayout());

        mappingsModel = new DefaultListModel<>();

        initComponents();
    }

    private void initComponents() {
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));

        // Input panel
        JPanel inputPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inputPanel.add(new JLabel("Target Column:"));
        targetColumnCombo = new JComboBox<>();
        inputPanel.add(targetColumnCombo);

        inputPanel.add(new JLabel("Final Select Table:"));
        finalSelectTableCombo = new JComboBox<>();
        inputPanel.add(finalSelectTableCombo);

        inputPanel.add(new JLabel("Final Select Column:"));
        finalSelectColumnCombo = new JComboBox<>();
        inputPanel.add(finalSelectColumnCombo);
        
        inputPanel.add(new JLabel("Where Select Table:"));
        whereSelectTableCombo = new JComboBox<>();
        inputPanel.add(whereSelectTableCombo);

        inputPanel.add(new JLabel("Where ID Column:"));
        whereIdColumnCombo = new JComboBox<>();
        inputPanel.add(whereIdColumnCombo);

        inputPanel.add(new JLabel("Source Table:"));
        sourceTableCombo = new JComboBox<>();
        inputPanel.add(sourceTableCombo);

        inputPanel.add(new JLabel("Source ID Column:"));
        sourceIdColumnCombo = new JComboBox<>();
        inputPanel.add(sourceIdColumnCombo);

        // LEFT JOIN panel
        JPanel joinsPanelContainer = new JPanel(new BorderLayout(5, 5));
        joinsPanelContainer.setBorder(BorderFactory.createTitledBorder("LEFT JOIN Relationships"));
        
        joinsPanel = new JPanel();
        joinsPanel.setLayout(new BoxLayout(joinsPanel, BoxLayout.Y_AXIS));
        JScrollPane joinsScrollPane = new JScrollPane(joinsPanel);
        joinsScrollPane.setPreferredSize(new Dimension(500, 150));
        
        JPanel joinButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addJoinButton = new JButton("Add LEFT JOIN");
        addJoinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addJoinRow();
            }
        });
        joinButtonPanel.add(addJoinButton);
        
        joinsPanelContainer.add(joinsScrollPane, BorderLayout.CENTER);
        joinsPanelContainer.add(joinButtonPanel, BorderLayout.SOUTH);

        // Mappings panel
        JPanel mappingsPanel = new JPanel(new BorderLayout());
        mappingsPanel.setBorder(BorderFactory.createTitledBorder("External Connection Mappings"));

        mappingsList = new JList<>(mappingsModel);
        JScrollPane scrollPane = new JScrollPane(mappingsList);
        mappingsPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addMappingButton = new JButton("Add External Connection");
        addMappingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addMapping();
            }
        });

        JButton deleteMappingButton = new JButton("Delete External Connection");
        deleteMappingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteMapping();
            }
        });

        buttonPanel.add(addMappingButton);
        buttonPanel.add(deleteMappingButton);

        // Main content assembly
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(inputPanel, BorderLayout.NORTH);
        topPanel.add(joinsPanelContainer, BorderLayout.CENTER);
        
        contentPanel.add(topPanel, BorderLayout.NORTH);
        contentPanel.add(mappingsPanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);

        JLabel instructionLabel = new JLabel("Step 6: Create External Connection Mappings");
        instructionLabel.setFont(new Font(instructionLabel.getFont().getName(), Font.BOLD, 14));
        add(instructionLabel, BorderLayout.NORTH);

        // Add listeners to update the dependent comboboxes
        sourceTableCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSourceIdColumnCombo();
            }
        });

        finalSelectTableCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateFinalSelectColumnCombo();
            }
        });
        
        whereSelectTableCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateWhereIdColumnCombo();
            }
        });
    }
    
    private void addJoinRow() {
        JoinRow joinRow = new JoinRow();
        joinRows.add(joinRow);
        joinsPanel.add(joinRow);
        joinsPanel.revalidate();
        joinsPanel.repaint();
    }

    private void addMapping() {
        String targetColumnKey = (String) targetColumnCombo.getSelectedItem();
        String finalSelectTableName = (String) finalSelectTableCombo.getSelectedItem();
        String finalSelectColumnName = (String) finalSelectColumnCombo.getSelectedItem();
        String whereSelectTableName = (String) whereSelectTableCombo.getSelectedItem();
        String whereIdColumnName = (String) whereIdColumnCombo.getSelectedItem();
        String sourceTableName = (String) sourceTableCombo.getSelectedItem();
        String sourceIdColumnName = (String) sourceIdColumnCombo.getSelectedItem();

        if (targetColumnKey != null && finalSelectTableName != null && finalSelectColumnName != null &&
                whereSelectTableName != null && whereIdColumnName != null && 
                sourceTableName != null && sourceIdColumnName != null) {

            String[] targetParts = targetColumnKey.split("\\.");

            if (targetParts.length == 2) {
                int mappingIndex = wizard.addExternalConnectionMapping(targetParts[0], targetParts[1],
                        finalSelectTableName, finalSelectColumnName,
                        whereSelectTableName, whereIdColumnName, 
                        sourceTableName, sourceIdColumnName);
                
                // Add LEFT JOIN relationships if any
                for (JoinRow joinRow : joinRows) {
                    String leftTableName = (String) joinRow.getLeftTableCombo().getSelectedItem();
                    String leftColumnName = (String) joinRow.getLeftColumnCombo().getSelectedItem();
                    String rightTableName = (String) joinRow.getRightTableCombo().getSelectedItem();
                    String rightColumnName = (String) joinRow.getRightColumnCombo().getSelectedItem();
                    
                    if (leftTableName != null && leftColumnName != null && 
                        rightTableName != null && rightColumnName != null) {
                        wizard.addLeftJoinToExternalConnection(mappingIndex, leftTableName, leftColumnName, 
                                                              rightTableName, rightColumnName);
                    }
                }

                // Create display string for the mapping
                StringBuilder mappingDisplay = new StringBuilder();
                mappingDisplay.append(targetColumnKey)
                             .append(" <- ")
                             .append(finalSelectTableName)
                             .append(".")
                             .append(finalSelectColumnName)
                             .append(" (Where ")
                             .append(whereSelectTableName)
                             .append(".")
                             .append(whereIdColumnName)
                             .append(" = ")
                             .append(sourceTableName)
                             .append(".")
                             .append(sourceIdColumnName)
                             .append(")");
                
                // Add LEFT JOIN info if any
                if (!joinRows.isEmpty()) {
                    mappingDisplay.append(" with LEFT JOINs: ");
                    for (int i = 0; i < joinRows.size(); i++) {
                        JoinRow joinRow = joinRows.get(i);
                        String leftTableName = (String) joinRow.getLeftTableCombo().getSelectedItem();
                        String leftColumnName = (String) joinRow.getLeftColumnCombo().getSelectedItem();
                        String rightTableName = (String) joinRow.getRightTableCombo().getSelectedItem();
                        String rightColumnName = (String) joinRow.getRightColumnCombo().getSelectedItem();
                        
                        if (i > 0) {
                            mappingDisplay.append(", ");
                        }
                        mappingDisplay.append(leftTableName)
                                     .append(".")
                                     .append(leftColumnName)
                                     .append(" = ")
                                     .append(rightTableName)
                                     .append(".")
                                     .append(rightColumnName);
                    }
                }
                
                mappingsModel.addElement(mappingDisplay.toString());
                
                // Clear join rows after adding the mapping
                clearJoinRows();
            }
        }
    }

    private void deleteMapping() {
        int selectedIndex = mappingsList.getSelectedIndex();
        if (selectedIndex >= 0) {
            mappingsModel.remove(selectedIndex);
            wizard.removeMapping(selectedIndex, "ExternalConnection");
        }
    }
    
    private void clearJoinRows() {
        joinRows.clear();
        joinsPanel.removeAll();
        joinsPanel.revalidate();
        joinsPanel.repaint();
    }

    private void updateSourceIdColumnCombo() {
        sourceIdColumnCombo.removeAllItems();

        String selectedSourceTable = (String) sourceTableCombo.getSelectedItem();
        if (selectedSourceTable != null) {
            for (Map.Entry<String, SourceColumn> entry : wizard.getSourceColumns().entrySet()) {
                if (entry.getKey().startsWith(selectedSourceTable + ".")) {
                    String columnName = entry.getKey().substring(selectedSourceTable.length() + 1);
                    sourceIdColumnCombo.addItem(columnName);
                }
            }
        }
    }

    private void updateFinalSelectColumnCombo() {
        finalSelectColumnCombo.removeAllItems();

        String selectedFinalSelectTable = (String) finalSelectTableCombo.getSelectedItem();
        if (selectedFinalSelectTable != null) {
            for (Map.Entry<String, SourceColumn> entry : wizard.getSourceColumns().entrySet()) {
                if (entry.getKey().startsWith(selectedFinalSelectTable + ".")) {
                    String columnName = entry.getKey().substring(selectedFinalSelectTable.length() + 1);
                    finalSelectColumnCombo.addItem(columnName);
                }
            }
        }
    }
    
    private void updateWhereIdColumnCombo() {
        whereIdColumnCombo.removeAllItems();

        String selectedWhereSelectTable = (String) whereSelectTableCombo.getSelectedItem();
        if (selectedWhereSelectTable != null) {
            for (Map.Entry<String, SourceColumn> entry : wizard.getSourceColumns().entrySet()) {
                if (entry.getKey().startsWith(selectedWhereSelectTable + ".")) {
                    String columnName = entry.getKey().substring(selectedWhereSelectTable.length() + 1);
                    whereIdColumnCombo.addItem(columnName);
                }
            }
        }
    }

    public void updateComponents() {
        targetColumnCombo.removeAllItems();
        finalSelectTableCombo.removeAllItems();
        whereSelectTableCombo.removeAllItems();
        sourceTableCombo.removeAllItems();

        Map<String, TargetColumn> targetColumns = wizard.getTargetColumns();
        Map<String, SourceColumn> sourceColumns = wizard.getSourceColumns();

        // Populate target columns
        for (String columnKey : targetColumns.keySet()) {
            targetColumnCombo.addItem(columnKey);
        }

        // Get unique source table names
        for (SourceColumn column : sourceColumns.values()) {
            String tableName = column.getTable().getName();
            boolean exists = false;

            for (int i = 0; i < sourceTableCombo.getItemCount(); i++) {
                if (sourceTableCombo.getItemAt(i).equals(tableName)) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                sourceTableCombo.addItem(tableName);
                finalSelectTableCombo.addItem(tableName);
                whereSelectTableCombo.addItem(tableName);
            }
        }

        // Initialize dependent comboboxes
        updateSourceIdColumnCombo();
        updateFinalSelectColumnCombo();
        updateWhereIdColumnCombo();
        
        // Clear join rows
        clearJoinRows();
    }
    
    // Inner class for LEFT JOIN row
    private class JoinRow extends JPanel {
        private JComboBox<String> leftTableCombo;
        private JComboBox<String> leftColumnCombo;
        private JComboBox<String> rightTableCombo;
        private JComboBox<String> rightColumnCombo;
        private JButton removeButton;
        
        public JoinRow() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
            
            leftTableCombo = new JComboBox<>();
            leftColumnCombo = new JComboBox<>();
            rightTableCombo = new JComboBox<>();
            rightColumnCombo = new JComboBox<>();
            removeButton = new JButton("X");
            
            // Populate table combos with all source tables
            for (int i = 0; i < sourceTableCombo.getItemCount(); i++) {
                String tableName = sourceTableCombo.getItemAt(i);
                leftTableCombo.addItem(tableName);
                rightTableCombo.addItem(tableName);
            }
            
            // Add listeners to update column combos when tables are selected
            leftTableCombo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateLeftColumnCombo();
                }
            });
            
            rightTableCombo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateRightColumnCombo();
                }
            });
            
            // Add listener for remove button
            removeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    joinRows.remove(JoinRow.this);
                    joinsPanel.remove(JoinRow.this);
                    joinsPanel.revalidate();
                    joinsPanel.repaint();
                }
            });
            
            // Initialize column combos
            updateLeftColumnCombo();
            updateRightColumnCombo();
            
            // Add components to panel
            add(new JLabel("Left Table:"));
            add(leftTableCombo);
            add(new JLabel("Left Column:"));
            add(leftColumnCombo);
            add(new JLabel("="));
            add(new JLabel("Right Table:"));
            add(rightTableCombo);
            add(new JLabel("Right Column:"));
            add(rightColumnCombo);
            add(removeButton);
            
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        }
        
        private void updateLeftColumnCombo() {
            leftColumnCombo.removeAllItems();
            String selectedLeftTable = (String) leftTableCombo.getSelectedItem();
            if (selectedLeftTable != null) {
                for (Map.Entry<String, SourceColumn> entry : wizard.getSourceColumns().entrySet()) {
                    if (entry.getKey().startsWith(selectedLeftTable + ".")) {
                        String columnName = entry.getKey().substring(selectedLeftTable.length() + 1);
                        leftColumnCombo.addItem(columnName);
                    }
                }
            }
        }
        
        private void updateRightColumnCombo() {
            rightColumnCombo.removeAllItems();
            String selectedRightTable = (String) rightTableCombo.getSelectedItem();
            if (selectedRightTable != null) {
                for (Map.Entry<String, SourceColumn> entry : wizard.getSourceColumns().entrySet()) {
                    if (entry.getKey().startsWith(selectedRightTable + ".")) {
                        String columnName = entry.getKey().substring(selectedRightTable.length() + 1);
                        rightColumnCombo.addItem(columnName);
                    }
                }
            }
        }
        
        public JComboBox<String> getLeftTableCombo() {
            return leftTableCombo;
        }
        
        public JComboBox<String> getLeftColumnCombo() {
            return leftColumnCombo;
        }
        
        public JComboBox<String> getRightTableCombo() {
            return rightTableCombo;
        }
        
        public JComboBox<String> getRightColumnCombo() {
            return rightColumnCombo;
        }
    }
}