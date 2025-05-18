package com.datamap.ui;

import com.datamap.model.TargetColumn;
import com.datamap.model.mapping.Constant;
import com.datamap.model.mapping.Mapping;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConstantMappingPanel extends JPanel {
    private DataMapWizard wizard;
    private JComboBox<String> targetColumnCombo;
    private JTextField constantValueField;
    private DefaultListModel<String> mappingsModel;
    private JList<String> mappingsList;

    public ConstantMappingPanel(DataMapWizard wizard) {
        this.wizard = wizard;
        setLayout(new BorderLayout());

        mappingsModel = new DefaultListModel<>();

        initComponents();
    }

    private void initComponents() {
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));

        // Input panel
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inputPanel.add(new JLabel("Target Column:"));
        targetColumnCombo = new SortedJXComboBox<>();
        inputPanel.add(targetColumnCombo);

        inputPanel.add(new JLabel("Constant Value:"));
        constantValueField = new JTextField(20);
        inputPanel.add(constantValueField);

        // Mappings panel
        JPanel mappingsPanel = new JPanel(new BorderLayout());
        mappingsPanel.setBorder(BorderFactory.createTitledBorder("Constant Mappings"));

        mappingsList = new JList<>(mappingsModel);
        // Enable multiple selection
        mappingsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(mappingsList);
        mappingsPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addMappingButton = new JButton("Add Constant Mapping");
        addMappingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addMapping();
            }
        });

        JButton deleteMappingButton = new JButton("Delete Constant Mapping");
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

        JLabel instructionLabel = new JLabel("Step 5: Create Constant Mappings");
        instructionLabel.setFont(new Font(instructionLabel.getFont().getName(), Font.BOLD, 14));
        add(instructionLabel, BorderLayout.NORTH);
    }

    private void addMapping() {
        String targetColumnKey = (String) targetColumnCombo.getSelectedItem();
        String constantValue = constantValueField.getText().trim();

        if (targetColumnKey != null && !constantValue.isEmpty()) {
            String[] targetParts = targetColumnKey.split("\\.");

            if (targetParts.length == 2) {
                wizard.addConstantMapping(targetParts[0], targetParts[1], constantValue);
                mappingsModel.addElement(targetColumnKey + " <- Constant(\"" + constantValue + "\")");
                constantValueField.setText("");
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a target column and enter a constant value.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteMapping() {
        // Get all selected indices
        int[] selectedIndices = mappingsList.getSelectedIndices();
        if (selectedIndices.length == 0) return;

        // Create list of mappings to remove
        List<String> mappingsToRemove = new ArrayList<>();
        for (int index : selectedIndices) {
            mappingsToRemove.add(mappingsModel.getElementAt(index));
        }

        // Remove mappings in reverse order to avoid index shifting
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            int index = selectedIndices[i];
            mappingsModel.remove(index);
            wizard.removeMapping(index, "Constant");
        }

        // Show notification if multiple items were deleted
        if (selectedIndices.length > 1) {
            JOptionPane.showMessageDialog(this,
                    selectedIndices.length + " constant mappings removed.",
                    "Mappings Removed", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void updateComponents() {
        targetColumnCombo.removeAllItems();
        mappingsModel.clear();

        // Add target columns
        Map<String, TargetColumn> targetColumns = wizard.getTargetColumns();
        for (String columnKey : targetColumns.keySet()) {
            targetColumnCombo.addItem(columnKey);
        }

        // Load existing Constant mappings from wizard
        refreshMappingsList();
    }

    /**
     * Refresh the mappings list based on wizard's current mappings
     */
    public void refreshMappingsList() {
        mappingsModel.clear();

        // Get all mappings from wizard
        List<Mapping> mappings = wizard.getMappings();

        // Add only Constant mappings to the list
        for (Mapping mapping : mappings) {
            if (mapping instanceof Constant) {
                Constant constantMapping = (Constant) mapping;
                String targetKey = constantMapping.getTargetColumn().getTable().getName() + "." +
                        constantMapping.getTargetColumn().getName();

                mappingsModel.addElement(targetKey + " <- Constant(\"" + constantMapping.getConstantValue() + "\")");
            }
        }
    }
}