package com.datamap.ui;

import com.datamap.model.SourceColumn;
import com.datamap.model.TargetColumn;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class ExternalConnectionPanel extends JPanel {
    private DataMapWizard wizard;
    private JComboBox<String> targetColumnCombo;
    private JComboBox<String> externalTableCombo;
    private JComboBox<String> externalColumnCombo;
    private JComboBox<String> externalIdColumnCombo;
    private JComboBox<String> sourceTableCombo;
    private JComboBox<String> sourceIdColumnCombo;
    private DefaultListModel<String> mappingsModel;
    private JList<String> mappingsList;

    public ExternalConnectionPanel(DataMapWizard wizard) {
        this.wizard = wizard;
        setLayout(new BorderLayout());

        mappingsModel = new DefaultListModel<>();

        initComponents();
    }

    private void initComponents() {
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));

        // Input panel
        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inputPanel.add(new JLabel("Target Column:"));
        targetColumnCombo = new JComboBox<>();
        inputPanel.add(targetColumnCombo);

        inputPanel.add(new JLabel("External Table:"));
        externalTableCombo = new JComboBox<>();
        inputPanel.add(externalTableCombo);

        inputPanel.add(new JLabel("External Column:"));
        externalColumnCombo = new JComboBox<>();
        inputPanel.add(externalColumnCombo);

        inputPanel.add(new JLabel("External ID Column:"));
        externalIdColumnCombo = new JComboBox<>();
        inputPanel.add(externalIdColumnCombo);

        inputPanel.add(new JLabel("Source Table:"));
        sourceTableCombo = new JComboBox<>();
        inputPanel.add(sourceTableCombo);

        inputPanel.add(new JLabel("Source ID Column:"));
        sourceIdColumnCombo = new JComboBox<>();
        inputPanel.add(sourceIdColumnCombo);

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

        contentPanel.add(inputPanel, BorderLayout.NORTH);
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

        externalTableCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateExternalColumnCombos();
            }
        });
    }

    private void addMapping() {
        String targetColumnKey = (String) targetColumnCombo.getSelectedItem();
        String externalTableName = (String) externalTableCombo.getSelectedItem();
        String externalColumnName = (String) externalColumnCombo.getSelectedItem();
        String externalIdColumnName = (String) externalIdColumnCombo.getSelectedItem();
        String sourceTableName = (String) sourceTableCombo.getSelectedItem();
        String sourceIdColumnName = (String) sourceIdColumnCombo.getSelectedItem();

        if (targetColumnKey != null && externalTableName != null && externalColumnName != null &&
                externalIdColumnName != null && sourceTableName != null && sourceIdColumnName != null) {

            String[] targetParts = targetColumnKey.split("\\.");

            if (targetParts.length == 2) {
                wizard.addExternalConnectionMapping(targetParts[0], targetParts[1],
                        externalTableName, externalColumnName,
                        externalIdColumnName, sourceTableName, sourceIdColumnName);

                mappingsModel.addElement(targetColumnKey + " <- " + externalTableName + "." +
                        externalColumnName + " (Join on " + sourceTableName + "." + sourceIdColumnName + ")");
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

    private void updateExternalColumnCombos() {
        externalColumnCombo.removeAllItems();
        externalIdColumnCombo.removeAllItems();

        String selectedExternalTable = (String) externalTableCombo.getSelectedItem();
        if (selectedExternalTable != null) {
            for (Map.Entry<String, SourceColumn> entry : wizard.getSourceColumns().entrySet()) {
                if (entry.getKey().startsWith(selectedExternalTable + ".")) {
                    String columnName = entry.getKey().substring(selectedExternalTable.length() + 1);
                    externalColumnCombo.addItem(columnName);
                    externalIdColumnCombo.addItem(columnName);
                }
            }
        }
    }

    public void updateComponents() {
        targetColumnCombo.removeAllItems();
        externalTableCombo.removeAllItems();
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
                externalTableCombo.addItem(tableName);
            }
        }

        // Initialize dependent comboboxes
        updateSourceIdColumnCombo();
        updateExternalColumnCombos();
    }
}