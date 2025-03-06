package com.datamap.ui;

import com.datamap.model.*;
import com.datamap.model.mapping.*;
import com.datamap.util.JsonConfig;
import com.datamap.util.JsonConfig.Configuration;
import com.datamap.util.JsonConfig.TableConfig;
import com.datamap.util.JsonConfig.MappingConfig;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GenerateCodePanel extends JPanel {
    private DataMapWizard wizard;
    private JTextArea codeTextArea;

    public GenerateCodePanel(DataMapWizard wizard) {
        this.wizard = wizard;
        setLayout(new BorderLayout());

        initComponents();
    }

    private void initComponents() {
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        codeTextArea = new JTextArea();
        codeTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(codeTextArea);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton copyButton = new JButton("Copy to Clipboard");
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyToClipboard();
            }
        });

        JButton regenerateButton = new JButton("Regenerate Code");
        regenerateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCode();
            }
        });

        buttonPanel.add(copyButton);
        buttonPanel.add(regenerateButton);

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);

        JLabel instructionLabel = new JLabel("Generated Code:");
        instructionLabel.setFont(new Font(instructionLabel.getFont().getName(), Font.BOLD, 14));
        add(instructionLabel, BorderLayout.NORTH);
    }

    public void updateCode() {
        String code = wizard.generateCode();
        codeTextArea.setText(code);
    }

    private void copyToClipboard() {
        String code = codeTextArea.getText();
        StringSelection stringSelection = new StringSelection(code);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        JOptionPane.showMessageDialog(this, "Code copied to clipboard!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public void saveConfiguration() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Configuration");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files (*.json)", "json"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            // Add .json extension if not present
            if (!fileToSave.getName().toLowerCase().endsWith(".json")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".json");
            }

            try {
                JsonConfig.saveToFile(fileToSave, wizard.getSourceTables(),
                        wizard.getTargetTables(), wizard.getMappings());
                JOptionPane.showMessageDialog(this,
                        "Configuration saved to " + fileToSave.getName(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error saving configuration: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    public void loadConfiguration() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Configuration");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files (*.json)", "json"));

        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToLoad = fileChooser.getSelectedFile();

            try {
                Configuration config = JsonConfig.loadFromFile(fileToLoad);

                // Reset current wizard state
                wizard.resetData();

                // First create source tables
                for (TableConfig tableConfig : config.getSourceTables()) {
                    String[] columnsArray = tableConfig.getColumns().toArray(new String[0]);
                    wizard.addSourceTable(tableConfig.getName(), columnsArray);

                    // Add columns manually to trigger proper internal state updates
                    for (String column : tableConfig.getColumns()) {
                        wizard.addSourceColumn(tableConfig.getName(), column);
                    }
                }

                // Then create target tables (which depend on source tables)
                for (TableConfig tableConfig : config.getTargetTables()) {
                    String[] columnsArray = tableConfig.getColumns().toArray(new String[0]);
                    wizard.addTargetTable(tableConfig.getSourceTableName(), tableConfig.getName(), columnsArray);

                    // Add columns manually to trigger proper internal state updates
                    for (String column : tableConfig.getColumns()) {
                        wizard.addTargetColumn(tableConfig.getName(), column);
                    }
                }

                // Finally create mappings
                for (MappingConfig mappingConfig : config.getMappings()) {
                    if ("None".equals(mappingConfig.getType())) {
                        wizard.addNoneMapping(
                                mappingConfig.getTargetTable(),
                                mappingConfig.getTargetColumn(),
                                mappingConfig.getSourceTable(),
                                mappingConfig.getSourceColumn()
                        );
                    } else if ("Dict".equals(mappingConfig.getType())) {
                        wizard.addDictMapping(
                                mappingConfig.getTargetTable(),
                                mappingConfig.getTargetColumn(),
                                mappingConfig.getDictType(),
                                mappingConfig.getSourceTable(),
                                mappingConfig.getSourceColumn()
                        );
                    } else if ("Constant".equals(mappingConfig.getType())) {
                        wizard.addConstantMapping(
                                mappingConfig.getTargetTable(),
                                mappingConfig.getTargetColumn(),
                                mappingConfig.getConstantValue()
                        );
                    } else if ("ExternalConnection".equals(mappingConfig.getType())) {
                        wizard.addExternalConnectionMapping(
                                mappingConfig.getTargetTable(),
                                mappingConfig.getTargetColumn(),
                                mappingConfig.getExternalTable(),
                                mappingConfig.getExternalColumn(),
                                mappingConfig.getExternalIdColumn(),
                                mappingConfig.getSourceIdTable(),
                                mappingConfig.getSourceIdColumn()
                        );
                    }
                }

                // Refresh all panels to show the loaded data
                wizard.refreshAllPanelUIs();

                // Update code after loading
                updateCode();

                JOptionPane.showMessageDialog(this,
                        "Configuration loaded from " + fileToLoad.getName(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error loading configuration: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
}