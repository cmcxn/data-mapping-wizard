package com.datamap.ui;

import com.datamap.model.DataSource;
import com.datamap.model.SourceColumn;
import com.datamap.model.SourceTable;
import com.datamap.model.TargetColumn;
import com.datamap.model.TargetTable;
import com.datamap.model.Table;
import com.datamap.model.mapping.*;
import com.datamap.util.JsonConfig;
import com.datamap.util.JsonConfig.Configuration;
import com.datamap.util.JsonConfig.TableConfig;
import com.datamap.util.JsonConfig.MappingConfig;
import com.datamap.util.JsonConfig.LeftJoinConfig;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class GenerateCodePanel extends JPanel {
    private DataMapWizard wizard;
    private JTextArea codeArea;
    private JButton copyButton;
    private JButton saveButton;
    private JButton regenerateButton;

    public GenerateCodePanel(DataMapWizard wizard) {
        this.wizard = wizard;
        setLayout(new BorderLayout(10, 10));

        initComponents();
    }

    private void initComponents() {
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Code area with syntax highlighting
        codeArea = new JTextArea();
        codeArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        codeArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(codeArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Generated Code"));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        regenerateButton = new JButton("Regenerate Code");
        regenerateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCode();
                JOptionPane.showMessageDialog(GenerateCodePanel.this,
                        "Code has been regenerated based on the current configuration.",
                        "Code Regenerated", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        copyButton = new JButton("Copy to Clipboard");
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyToClipboard();
            }
        });

        saveButton = new JButton("Save Code to File");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCodeToFile();
            }
        });

        JButton saveConfigButton = new JButton("Save Configuration");
        saveConfigButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveConfiguration();
            }
        });

        JButton loadConfigButton = new JButton("Load Configuration");
        loadConfigButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadConfiguration();
            }
        });

        buttonPanel.add(regenerateButton);
        buttonPanel.add(copyButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(saveConfigButton);
        buttonPanel.add(loadConfigButton);

        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);

        JLabel instructionLabel = new JLabel("Step 7: Generate and Save Code");
        instructionLabel.setFont(new Font(instructionLabel.getFont().getName(), Font.BOLD, 14));
        add(instructionLabel, BorderLayout.NORTH);
    }

    public void updateCode() {
        String code = wizard.generateCode();
        codeArea.setText(code);
    }

    private void copyToClipboard() {
        StringSelection stringSelection = new StringSelection(codeArea.getText());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        JOptionPane.showMessageDialog(this,
                "Code copied to clipboard successfully!",
                "Clipboard", JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveCodeToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Code");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Java Files", "java"));

        String classname = null;
        for (TargetTable targetTable : wizard.getTargetTables().values()) {
            classname = targetTable.getTable().getName();
            //classname 首字母大写
            classname = classname.substring(0, 1).toUpperCase() + classname.substring(1);
            break;
        }
// 设置默认文件名
        fileChooser.setSelectedFile(new File(classname+".java"));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            // Add .java extension if not present
            if (!fileToSave.getName().toLowerCase().endsWith(".java")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".java");
            }

            try {
                java.nio.file.Files.write(fileToSave.toPath(),
                        codeArea.getText().getBytes());
                JOptionPane.showMessageDialog(this,
                        "Code saved successfully to " + fileToSave.getName(),
                        "Save Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error saving file: " + ex.getMessage(),
                        "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void saveConfiguration() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Configuration");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files", "json"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            // Add .json extension if not present
            if (!fileToSave.getName().toLowerCase().endsWith(".json")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".json");
            }

            try {
                JsonConfig.saveToFile(fileToSave,
                        wizard.getSourceTables(),
                        wizard.getTargetTables(),
                        wizard.getMappings());

                JOptionPane.showMessageDialog(this,
                        "Configuration saved successfully to " + fileToSave.getName(),
                        "Save Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error saving configuration: " + ex.getMessage(),
                        "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void loadConfiguration() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Configuration");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files", "json"));

        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToLoad = fileChooser.getSelectedFile();

            try {
                Configuration config = JsonConfig.loadFromFile(fileToLoad);
                applyConfiguration(config);

                JOptionPane.showMessageDialog(this,
                        "Configuration loaded successfully from " + fileToLoad.getName(),
                        "Load Successful", JOptionPane.INFORMATION_MESSAGE);

                // Refresh all UI panels to show the loaded data
                wizard.refreshAllPanelUIs();

                // Update code area
                updateCode();

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error loading configuration: " + ex.getMessage(),
                        "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void applyConfiguration(Configuration config) {
        // Reset all existing data
        wizard.resetData();

        // First create all source tables
        for (TableConfig tableConfig : config.getSourceTables()) {
            // Create source table
            DataSource dataSource = new DataSource();
            dataSource.setName(tableConfig.getDataSourceName());
            wizard.addSourceTable(tableConfig.getName(),dataSource, tableConfig.getColumns().toArray(new String[0]));

            // Add columns
            for (String column : tableConfig.getColumns()) {
                wizard.addSourceColumn(tableConfig.getName(), column);
            }
        }

        // Then create target tables
        for (TableConfig tableConfig : config.getTargetTables()) {
            // Create target table
            DataSource dataSource = new DataSource();
            dataSource.setName(tableConfig.getDataSourceName());
            wizard.addTargetTable(tableConfig.getSourceTableName(), tableConfig.getName(),dataSource,
                    tableConfig.getColumns().toArray(new String[0]));

            // Add columns
            for (String column : tableConfig.getColumns()) {
                wizard.addTargetColumn(tableConfig.getName(), column);
            }
        }

        // Finally create mappings
        for (MappingConfig mappingConfig : config.getMappings()) {
            if (mappingConfig.getType().equals("None")) {
                wizard.addNoneMapping(
                        mappingConfig.getTargetTable(),
                        mappingConfig.getTargetColumn(),
                        mappingConfig.getSourceTable(),
                        mappingConfig.getSourceColumn()
                );
            } else if (mappingConfig.getType().equals("Dict")) {
                wizard.addDictMapping(
                        mappingConfig.getTargetTable(),
                        mappingConfig.getTargetColumn(),
                        mappingConfig.getDictType(),
                        mappingConfig.getSourceTable(),
                        mappingConfig.getSourceColumn()
                );
            } else if (mappingConfig.getType().equals("Constant")) {
                wizard.addConstantMapping(
                        mappingConfig.getTargetTable(),
                        mappingConfig.getTargetColumn(),
                        mappingConfig.getConstantValue()
                );
            } else if (mappingConfig.getType().equals("ExternalConnection")) {
                String finalSelectTable = mappingConfig.getFinalSelectTable();
                String finalSelectColumn = mappingConfig.getFinalSelectColumn();
                String finalIdColumn = mappingConfig.getWhereIdColumn(); // Use the new getter for backward compatibility
                String sourceIdTable = mappingConfig.getSourceIdTable();
                String sourceIdColumn = mappingConfig.getSourceIdColumn();

                // Get whereSelectTable, defaulting to the same table as whereIdColumn for backward compatibility
                String whereSelectTable = mappingConfig.getWhereSelectTable();
                if (whereSelectTable == null || whereSelectTable.isEmpty()) {
                    whereSelectTable = finalSelectTable; // Default to the same table as before
                }

                // Add the base external connection mapping with the new parameter
                int mappingIndex = wizard.addExternalConnectionMapping(
                        mappingConfig.getTargetTable(),
                        mappingConfig.getTargetColumn(),
                        finalSelectTable,
                        finalSelectColumn,
                        whereSelectTable,
                        finalIdColumn,
                        sourceIdTable,
                        sourceIdColumn
                );

                // Process LEFT JOINs
                if (mappingIndex != -1) {
                    for (LeftJoinConfig joinConfig : mappingConfig.getLeftJoins()) {
                        wizard.addLeftJoinToExternalConnection(
                                mappingIndex,
                                joinConfig.getLeftTable(),
                                joinConfig.getLeftColumn(),
                                joinConfig.getRightTable(),
                                joinConfig.getRightColumn()
                        );
                    }
                }
            }
        }
    }
}