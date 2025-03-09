package com.datamap.ui;

import com.datamap.model.SourceColumn;
import com.datamap.model.TargetColumn;
import com.datamap.model.TargetTable;
import com.datamap.model.mapping.Mapping;
import com.datamap.model.mapping.None;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class NoneMappingPanel extends JPanel {
    private DataMapWizard wizard;
    private JComboBox<String> sourceColumnCombo;
    private JComboBox<String> targetColumnCombo;
    private DefaultListModel<String> mappingsModel;
    private JList<String> mappingsList;

    public NoneMappingPanel(DataMapWizard wizard) {
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
        targetColumnCombo = new JComboBox<>();
        targetColumnCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateSourceColumnsForSelectedTarget();
                }
            }
        });
        inputPanel.add(targetColumnCombo);

        inputPanel.add(new JLabel("Source Column:"));
        sourceColumnCombo = new JComboBox<>();
        inputPanel.add(sourceColumnCombo);

        // Mappings panel
        JPanel mappingsPanel = new JPanel(new BorderLayout());
        mappingsPanel.setBorder(BorderFactory.createTitledBorder("Direct Mappings"));

        mappingsList = new JList<>(mappingsModel);
        // Enable multiple selection
        mappingsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(mappingsList);
        mappingsPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addMappingButton = new JButton("Add Direct Mapping");
        addMappingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addMapping();
            }
        });

        JButton deleteMappingButton = new JButton("Delete Direct Mapping");
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

        JLabel instructionLabel = new JLabel("Step 3: Create Direct Mappings (None Type)");
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

        if (sourceColumnKey != null && targetColumnKey != null) {
            String[] sourceParts = sourceColumnKey.split("\\.");
            String[] targetParts = targetColumnKey.split("\\.");

            if (sourceParts.length == 2 && targetParts.length == 2) {
                wizard.addNoneMapping(targetParts[0], targetParts[1], sourceParts[0], sourceParts[1]);
                mappingsModel.addElement(targetColumnKey + " <- " + sourceColumnKey);
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
            wizard.removeMapping(index, "None");
        }

        // Show notification if multiple items were deleted
        if (selectedIndices.length > 1) {
            JOptionPane.showMessageDialog(this,
                    selectedIndices.length + " direct mappings removed.",
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

        // Load existing None mappings from wizard
        refreshMappingsList();
    }

    /**
     * Refresh the mappings list based on wizard's current mappings
     */
    public void refreshMappingsList() {
        mappingsModel.clear();

        // Get all mappings from wizard
        List<Mapping> mappings = wizard.getMappings();

        // Add only None mappings to the list
        for (Mapping mapping : mappings) {
            if (mapping instanceof None) {
                None noneMapping = (None) mapping;
                String targetKey = noneMapping.getTargetColumn().getTable().getName() + "." +
                        noneMapping.getTargetColumn().getName();
                String sourceKey = noneMapping.getSourceColumn().getTable().getName() + "." +
                        noneMapping.getSourceColumn().getName();

                mappingsModel.addElement(targetKey + " <- " + sourceKey);
            }
        }
    }
}