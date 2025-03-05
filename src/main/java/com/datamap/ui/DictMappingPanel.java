package com.datamap.ui;

import com.datamap.model.SourceColumn;
import com.datamap.model.TargetColumn;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class DictMappingPanel extends JPanel {
    private DataMapWizard wizard;
    private JComboBox<String> sourceColumnCombo;
    private JComboBox<String> targetColumnCombo;
    private JTextField dictTypeField;
    private DefaultListModel<String> mappingsModel;
    private JList<String> mappingsList;

    public DictMappingPanel(DataMapWizard wizard) {
        this.wizard = wizard;
        setLayout(new BorderLayout());

        mappingsModel = new DefaultListModel<>();

        initComponents();
    }

    private void initComponents() {
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));

        // Input panel
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inputPanel.add(new JLabel("Source Column:"));
        sourceColumnCombo = new JComboBox<>();
        inputPanel.add(sourceColumnCombo);

        inputPanel.add(new JLabel("Dictionary Type:"));
        dictTypeField = new JTextField(20);
        inputPanel.add(dictTypeField);

        inputPanel.add(new JLabel("Target Column:"));
        targetColumnCombo = new JComboBox<>();
        inputPanel.add(targetColumnCombo);

        // Mappings panel
        JPanel mappingsPanel = new JPanel(new BorderLayout());
        mappingsPanel.setBorder(BorderFactory.createTitledBorder("Dictionary Mappings"));

        mappingsList = new JList<>(mappingsModel);
        JScrollPane scrollPane = new JScrollPane(mappingsList);
        mappingsPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addMappingButton = new JButton("Add Dictionary Mapping");
        addMappingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addMapping();
            }
        });

        JButton deleteMappingButton = new JButton("Delete Dictionary Mapping");
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

        JLabel instructionLabel = new JLabel("Step 4: Create Dictionary Mappings (Dict Type)");
        instructionLabel.setFont(new Font(instructionLabel.getFont().getName(), Font.BOLD, 14));
        add(instructionLabel, BorderLayout.NORTH);
    }

    private void addMapping() {
        String sourceColumnKey = (String) sourceColumnCombo.getSelectedItem();
        String targetColumnKey = (String) targetColumnCombo.getSelectedItem();
        String dictType = dictTypeField.getText().trim();

        if (sourceColumnKey != null && targetColumnKey != null && !dictType.isEmpty()) {
            String[] sourceParts = sourceColumnKey.split("\\.");
            String[] targetParts = targetColumnKey.split("\\.");

            if (sourceParts.length == 2 && targetParts.length == 2) {
                wizard.addDictMapping(targetParts[0], targetParts[1], dictType, sourceParts[0], sourceParts[1]);
                mappingsModel.addElement(targetColumnKey + " <- Dict(" + dictType + ") <- " + sourceColumnKey);
                dictTypeField.setText("");
            }
        }
    }

    private void deleteMapping() {
        int selectedIndex = mappingsList.getSelectedIndex();
        if (selectedIndex >= 0) {
            mappingsModel.remove(selectedIndex);
            wizard.removeMapping(selectedIndex, "Dict");
        }
    }

    public void updateComponents() {
        sourceColumnCombo.removeAllItems();
        targetColumnCombo.removeAllItems();

        Map<String, SourceColumn> sourceColumns = wizard.getSourceColumns();
        Map<String, TargetColumn> targetColumns = wizard.getTargetColumns();

        for (String columnKey : sourceColumns.keySet()) {
            sourceColumnCombo.addItem(columnKey);
        }

        for (String columnKey : targetColumns.keySet()) {
            targetColumnCombo.addItem(columnKey);
        }
    }
}