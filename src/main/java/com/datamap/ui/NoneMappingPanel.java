package com.datamap.ui;

import com.datamap.model.SourceColumn;
import com.datamap.model.TargetColumn;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

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
        
        inputPanel.add(new JLabel("Source Column:"));
        sourceColumnCombo = new JComboBox<>();
        inputPanel.add(sourceColumnCombo);
        
        inputPanel.add(new JLabel("Target Column:"));
        targetColumnCombo = new JComboBox<>();
        inputPanel.add(targetColumnCombo);
        
        // Mappings panel
        JPanel mappingsPanel = new JPanel(new BorderLayout());
        mappingsPanel.setBorder(BorderFactory.createTitledBorder("Direct Mappings"));
        
        mappingsList = new JList<>(mappingsModel);
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
        int selectedIndex = mappingsList.getSelectedIndex();
        if (selectedIndex >= 0) {
            String mapping = mappingsModel.getElementAt(selectedIndex);
            mappingsModel.remove(selectedIndex);
            
            // Delete from the data model
            String[] parts = mapping.split(" <- ");
            if (parts.length == 2) {
                wizard.removeMapping(selectedIndex, "None");
            }
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