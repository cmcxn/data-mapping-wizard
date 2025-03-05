package com.datamap.ui;

import com.datamap.model.TargetColumn;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

        inputPanel.add(new JLabel("Constant Value:"));
        constantValueField = new JTextField(20);
        inputPanel.add(constantValueField);

        inputPanel.add(new JLabel("Target Column:"));
        targetColumnCombo = new JComboBox<>();
        inputPanel.add(targetColumnCombo);

        // Mappings panel
        JPanel mappingsPanel = new JPanel(new BorderLayout());
        mappingsPanel.setBorder(BorderFactory.createTitledBorder("Constant Mappings"));

        mappingsList = new JList<>(mappingsModel);
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
        }
    }

    private void deleteMapping() {
        int selectedIndex = mappingsList.getSelectedIndex();
        if (selectedIndex >= 0) {
            mappingsModel.remove(selectedIndex);
            wizard.removeMapping(selectedIndex, "Constant");
        }
    }

    public void updateComponents() {
        targetColumnCombo.removeAllItems();

        Map<String, TargetColumn> targetColumns = wizard.getTargetColumns();

        for (String columnKey : targetColumns.keySet()) {
            targetColumnCombo.addItem(columnKey);
        }
    }
}