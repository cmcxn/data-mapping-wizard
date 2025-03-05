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
    private JComboBox<String> sourceTableCombo;
    private JComboBox<String> sourceIdColumnCombo;
    private JComboBox<String> externalTableCombo;
    private JComboBox<String> externalColumnCombo;
    private JComboBox<String> externalIdColumnCombo;
    private JComboBox<String> targetColumnCombo;
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

        inputPanel.add(new JLabel("Source Table:"));
        sourceTableCombo = new JComboBox<>();
        sourceTableCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSourceIdColumns();
            }
        });
        inputPanel.add(sourceTableCombo);

        inputPanel.add(new JLabel("Source ID Column:"));
        sourceIdColumnCombo = new JComboBox<>();
        inputPanel.add(sourceIdColumnCombo);

        inputPanel.add(new JLabel("External Table:"));
        externalTableCombo = new JComboBox<>();
        externalTableCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateExternalColumns();
            }
        });
        inputPanel.add(externalTableCombo);

        inputPanel.add(new JLabel("External Source Column:"));
        externalColumnCombo = new JComboBox<>();
        inputPanel.add(externalColumnCombo);

        inputPanel.add(new JLabel("External ID Column:"));
        externalIdColumnCombo = new JComboBox<>();
        inputPanel.add(externalIdColumnCombo);

        inputPanel.add(new JLabel("Target Column:"));
        targetColumnCombo = new JComboBox<>();
        inputPanel.add(targetColumnCombo);

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
    }

    private void updateSourceIdColumns() {
        sourceIdColumnCombo.removeAllItems();

        String selectedTable = (String) sourceTableCombo.getSelectedItem();
        if (selectedTable != null) {
            Map<String, SourceColumn> sourceColumns = wizard.getSourceColumns();
            for (String columnKey : sourceColumns.keySet()) {
                if (columnKey.startsWith(selectedTable + ".")) {
                    sourceIdColumnCombo.addItem(columnKey.split("\\.")[1]);
                }
            }
        }
    }

    private void updateExternalColumns() {
        externalColumnCombo.removeAllItems();
        externalIdColumnCombo.removeAllItems();

        String selectedTable = (String) externalTableCombo.getSelectedItem();
        if (selectedTable != null) {
            Map<String, SourceColumn> sourceColumns = wizard.getSourceColumns();
            for (String columnKey : sourceColumns.keySet()) {
                if (columnKey.startsWith(selectedTable + ".")) {
                    String columnName = columnKey.split("\\.")[1];
                    externalColumnCombo.addItem(columnName);
                    externalIdColumnCombo.addItem(columnName);
                }
            }
        }
    }

    private void addMapping() {
        String sourceTable = (String) sourceTableCombo.getSelectedItem();
        String sourceIdColumn = (String) sourceIdColumnCombo.getSelectedItem();
        String externalTable = (String) externalTableCombo.getSelectedItem();
        String externalColumn = (String) externalColumnCombo.getSelectedItem();
        String externalIdColumn = (String) externalIdColumnCombo.getSelectedItem();
        String targetColumnKey = (String) targetColumnCombo.getSelectedItem();

        if (sourceTable != null && sourceIdColumn != null && externalTable != null &&
                externalColumn != null && externalIdColumn != null && targetColumnKey != null) {

            String[] targetParts = targetColumnKey.split("\\.");

            if (targetParts.length == 2) {
                wizard.addExternalConnectionMapping(
                        targetParts[0], targetParts[1],
                        externalTable, externalColumn,
                        externalIdColumn, sourceTable, sourceIdColumn
                );

                mappingsModel.addElement(targetColumnKey + " <- ExternalConnection(" +
                        externalTable + "." + externalColumn + " where " +
                        externalTable + "." + externalIdColumn + " = " +
                        sourceTable + "." + sourceIdColumn + ")");
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

    public void updateComponents() {
        sourceTableCombo.removeAllItems();
        externalTableCombo.removeAllItems();
        targetColumnCombo.removeAllItems();

        Map<String, SourceColumn> sourceColumns = wizard.getSourceColumns();
        Map<String, TargetColumn> targetColumns = wizard.getTargetColumns();

        // Add unique source tables
        for (String columnKey : sourceColumns.keySet()) {
            String tableName = columnKey.split("\\.")[0];
            boolean found = false;

            for (int i = 0; i < sourceTableCombo.getItemCount(); i++) {
                if (sourceTableCombo.getItemAt(i).equals(tableName)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                sourceTableCombo.addItem(tableName);
                externalTableCombo.addItem(tableName);
            }
        }

        // Add target columns
        for (String columnKey : targetColumns.keySet()) {
            targetColumnCombo.addItem(columnKey);
        }

        updateSourceIdColumns();
        updateExternalColumns();
    }
}