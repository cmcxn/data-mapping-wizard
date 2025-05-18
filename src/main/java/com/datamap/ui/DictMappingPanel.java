package com.datamap.ui;

import com.datamap.model.SourceColumn;
import com.datamap.model.TargetColumn;
import com.datamap.model.TargetTable;
import com.datamap.model.mapping.Dict;
import com.datamap.model.mapping.Mapping;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

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

        inputPanel.add(new JLabel("Target Column:"));
        targetColumnCombo = new SortedJXComboBox<>();
        targetColumnCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateSourceColumnsForSelectedTarget();
                }
            }
        });
        inputPanel.add(targetColumnCombo);

        inputPanel.add(new JLabel("Dictionary Type:"));
        dictTypeField = new JTextField(20);
        inputPanel.add(dictTypeField);

        inputPanel.add(new JLabel("Source Column:"));
        sourceColumnCombo = new SortedJXComboBox<>();
        inputPanel.add(sourceColumnCombo);

        // Mappings panel
        JPanel mappingsPanel = new JPanel(new BorderLayout());
        mappingsPanel.setBorder(BorderFactory.createTitledBorder("Dictionary Mappings"));

        mappingsList = new JList<>(mappingsModel);
        // Enable multiple selection
        mappingsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
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

    private void updateSourceColumnsForSelectedTarget() {
        String targetColumnKey = (String) targetColumnCombo.getSelectedItem();
        if (targetColumnKey == null) return;

        // Clear the source column combo box
        sourceColumnCombo.removeAllItems();

        // Parse the target column key to get the table name
        String[] targetParts = targetColumnKey.split("\\.");
        if (targetParts.length != 2) return;

        String targetTableName = targetParts[0];

        // Find the corresponding target table
        TargetTable targetTable = wizard.getTargetTables().get(targetTableName);
        if (targetTable == null) return;

        // Get the source table associated with this target table
        String sourceTableName = targetTable.getSourceTable().getName();

        // Add only source columns from the associated source table
        Map<String, SourceColumn> sourceColumns = wizard.getSourceColumns();
        for (Map.Entry<String, SourceColumn> entry : sourceColumns.entrySet()) {
            String columnKey = entry.getKey();
            if (columnKey.startsWith(sourceTableName + ".")) {
                sourceColumnCombo.addItem(columnKey);
            }
        }
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
            wizard.removeMapping(index, "Dict");
        }

        // Show notification if multiple items were deleted
        if (selectedIndices.length > 1) {
            JOptionPane.showMessageDialog(this,
                    selectedIndices.length + " dictionary mappings removed.",
                    "Mappings Removed", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void updateComponents() {
        targetColumnCombo.removeAllItems();
        sourceColumnCombo.removeAllItems();
        mappingsModel.clear();

        // Add target columns
        Map<String, TargetColumn> targetColumns = wizard.getTargetColumns();
        for (String columnKey : targetColumns.keySet()) {
            targetColumnCombo.addItem(columnKey);
        }

        // Update source columns based on selected target (if any)
        if (targetColumnCombo.getItemCount() > 0) {
            updateSourceColumnsForSelectedTarget();
        }

        // Load existing Dict mappings from wizard
        refreshMappingsList();
    }

    /**
     * Refresh the mappings list based on wizard's current mappings
     */
    public void refreshMappingsList() {
        mappingsModel.clear();

        // Get all mappings from wizard
        List<Mapping> mappings = wizard.getMappings();

        // Add only Dict mappings to the list
        for (Mapping mapping : mappings) {
            if (mapping instanceof Dict) {
                Dict dictMapping = (Dict) mapping;
                String targetKey = dictMapping.getTargetColumn().getTable().getName() + "." +
                        dictMapping.getTargetColumn().getName();
                String sourceKey = dictMapping.getSourceColumn().getTable().getName() + "." +
                        dictMapping.getSourceColumn().getName();

                mappingsModel.addElement(targetKey + " <- Dict(" + dictMapping.getDictType() + ") <- " + sourceKey);
            }
        }
    }
}