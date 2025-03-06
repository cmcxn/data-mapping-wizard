package com.datamap.ui;

import com.datamap.model.DataSource;
import com.datamap.util.DataSourceConfig;
import com.datamap.util.DatabaseConnectionManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConfigPanel extends JPanel {
    private DataMapWizard wizard;
    private DefaultListModel<DataSource> dataSourcesListModel;
    private JList<DataSource> dataSourcesList;
    
    private JTextField nameField;
    private JComboBox<String> dbTypeCombo;
    private JTextField jdbcDriverField;
    private JTextField jdbcUrlField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField databaseNameField;
    
    private List<DataSource> dataSources;
    
    public DatabaseConfigPanel(DataMapWizard wizard) {
        this.wizard = wizard;
        this.dataSources = DataSourceConfig.createDefaultDataSources();
        setLayout(new BorderLayout());
        
        dataSourcesListModel = new DefaultListModel<>();
        updateDataSourcesModel();
        
        initComponents();
    }
    
    private void updateDataSourcesModel() {
        dataSourcesListModel.clear();
        for (DataSource ds : dataSources) {
            dataSourcesListModel.addElement(ds);
        }
    }
    
    private void initComponents() {
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        
        // Left panel - list of data sources
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Data Sources"));
        
        dataSourcesList = new JList<>(dataSourcesListModel);
        dataSourcesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dataSourcesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                displaySelectedDataSource();
            }
        });
        
        JScrollPane listScrollPane = new JScrollPane(dataSourcesList);
        leftPanel.add(listScrollPane, BorderLayout.CENTER);
        
        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("New");
        addButton.addActionListener(e -> addNewDataSource());
        
        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(e -> removeDataSource());
        
        leftButtonPanel.add(addButton);
        leftButtonPanel.add(removeButton);
        leftPanel.add(leftButtonPanel, BorderLayout.SOUTH);
        
        // Right panel - data source details
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Data Source Configuration"));
        
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        
        formPanel.add(new JLabel("Name:"));
        nameField = new JTextField(20);
        formPanel.add(nameField);
        
        formPanel.add(new JLabel("Database Type:"));
        dbTypeCombo = new JComboBox<>(new String[]{"postgres", "mysql"});
        dbTypeCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateFieldsForDbType();
                }
            }
        });
        formPanel.add(dbTypeCombo);
        
        formPanel.add(new JLabel("JDBC Driver:"));
        jdbcDriverField = new JTextField(20);
        formPanel.add(jdbcDriverField);
        
        formPanel.add(new JLabel("Database Name:"));
        databaseNameField = new JTextField(20);
        databaseNameField.addActionListener(e -> updateJdbcUrl());
        formPanel.add(databaseNameField);
        
        formPanel.add(new JLabel("JDBC URL:"));
        jdbcUrlField = new JTextField(20);
        formPanel.add(jdbcUrlField);
        
        formPanel.add(new JLabel("Username:"));
        usernameField = new JTextField(20);
        formPanel.add(usernameField);
        
        formPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField(20);
        formPanel.add(passwordField);
        
        rightPanel.add(formPanel, BorderLayout.NORTH);
        
        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener(e -> saveCurrentDataSource());
        
        JButton testButton = new JButton("Test Connection");
        testButton.addActionListener(e -> testConnection());
        
        rightButtonPanel.add(testButton);
        rightButtonPanel.add(saveButton);
        rightPanel.add(rightButtonPanel, BorderLayout.SOUTH);
        
        // Add panels to content panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(250);
        contentPanel.add(splitPane, BorderLayout.CENTER);
        
        // File action panel
        JPanel fileActionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton saveConfigButton = new JButton("Save to File");
        saveConfigButton.addActionListener(e -> saveToFile());
        
        JButton loadConfigButton = new JButton("Load from File");
        loadConfigButton.addActionListener(e -> loadFromFile());
        
        fileActionPanel.add(saveConfigButton);
        fileActionPanel.add(loadConfigButton);
        contentPanel.add(fileActionPanel, BorderLayout.SOUTH);
        
        // Main panel layout
        add(contentPanel, BorderLayout.CENTER);
        
        JLabel instructionLabel = new JLabel("Step 0: Setup Database Connections");
        instructionLabel.setFont(new Font(instructionLabel.getFont().getName(), Font.BOLD, 14));
        add(instructionLabel, BorderLayout.NORTH);
        
        // If we have data sources, select the first one
        if (!dataSources.isEmpty()) {
            dataSourcesList.setSelectedIndex(0);
        }
    }
    
    private void displaySelectedDataSource() {
        int index = dataSourcesList.getSelectedIndex();
        if (index >= 0 && index < dataSources.size()) {
            DataSource ds = dataSources.get(index);
            nameField.setText(ds.getName());
            dbTypeCombo.setSelectedItem(ds.getDbType());
            jdbcDriverField.setText(ds.getJdbcDriver());
            jdbcUrlField.setText(ds.getJdbcUrl());
            usernameField.setText(ds.getUsername());
            passwordField.setText(ds.getPassword());
            databaseNameField.setText(ds.getDatabaseName());
        } else {
            clearForm();
        }
    }
    
    private void clearForm() {
        nameField.setText("");
        dbTypeCombo.setSelectedIndex(0);
        jdbcDriverField.setText("");
        jdbcUrlField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        databaseNameField.setText("");
    }
    
    private void addNewDataSource() {
        DataSource newDs = new DataSource();
        newDs.setName("New Data Source");
        newDs.setDbType((String) dbTypeCombo.getSelectedItem());
        newDs.setJdbcDriver(DatabaseConnectionManager.getDefaultJdbcDriver(newDs.getDbType()));
        newDs.setDatabaseName("mydb");
        newDs.setJdbcUrl(DatabaseConnectionManager.getDefaultJdbcUrl(newDs.getDbType(), newDs.getDatabaseName()));
        newDs.setUsername("user");
        newDs.setPassword("password");
        
        dataSources.add(newDs);
        updateDataSourcesModel();
        dataSourcesList.setSelectedIndex(dataSources.size() - 1);
    }
    
    private void removeDataSource() {
        int index = dataSourcesList.getSelectedIndex();
        if (index >= 0) {
            dataSources.remove(index);
            updateDataSourcesModel();
            
            if (dataSources.isEmpty()) {
                clearForm();
            } else {
                dataSourcesList.setSelectedIndex(Math.min(index, dataSources.size() - 1));
            }
        }
    }
    
    private void saveCurrentDataSource() {
        int index = dataSourcesList.getSelectedIndex();
        if (index >= 0 && index < dataSources.size()) {
            DataSource ds = dataSources.get(index);
            
            ds.setName(nameField.getText());
            ds.setDbType((String) dbTypeCombo.getSelectedItem());
            ds.setJdbcDriver(jdbcDriverField.getText());
            ds.setJdbcUrl(jdbcUrlField.getText());
            ds.setUsername(usernameField.getText());
            ds.setPassword(new String(passwordField.getPassword()));
            ds.setDatabaseName(databaseNameField.getText());
            
            updateDataSourcesModel();
            dataSourcesList.setSelectedIndex(index);
            JOptionPane.showMessageDialog(this, "Data source saved", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void updateFieldsForDbType() {
        String dbType = (String) dbTypeCombo.getSelectedItem();
        String dbName = databaseNameField.getText();
        if (dbName == null || dbName.trim().isEmpty()) {
            dbName = "mydb";
            databaseNameField.setText(dbName);
        }
        
        jdbcDriverField.setText(DatabaseConnectionManager.getDefaultJdbcDriver(dbType));
        jdbcUrlField.setText(DatabaseConnectionManager.getDefaultJdbcUrl(dbType, dbName));
    }
    
    private void updateJdbcUrl() {
        String dbType = (String) dbTypeCombo.getSelectedItem();
        String dbName = databaseNameField.getText();
        jdbcUrlField.setText(DatabaseConnectionManager.getDefaultJdbcUrl(dbType, dbName));
    }
    
    private void testConnection() {
        // Create a temporary data source with current form values
        DataSource ds = new DataSource();
        ds.setName(nameField.getText());
        ds.setDbType((String) dbTypeCombo.getSelectedItem());
        ds.setJdbcDriver(jdbcDriverField.getText());
        ds.setJdbcUrl(jdbcUrlField.getText());
        ds.setUsername(usernameField.getText());
        ds.setPassword(new String(passwordField.getPassword()));
        
        try {
            boolean success = DatabaseConnectionManager.testConnection(ds);
            if (success) {
                JOptionPane.showMessageDialog(this, 
                    "Connection successful!", "Test Connection", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Connection failed.", "Test Connection", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(this, 
                "JDBC Driver not found: " + ex.getMessage(), 
                "Test Connection", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "SQL Exception: " + ex.getMessage(), 
                "Test Connection", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Data Sources Configuration");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files (*.json)", "json"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            // Add .json extension if not present
            if (!fileToSave.getName().toLowerCase().endsWith(".json")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".json");
            }

            try {
                DataSourceConfig.saveToFile(fileToSave, dataSources);
                JOptionPane.showMessageDialog(this,
                        "Data sources saved to " + fileToSave.getName(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error saving data sources: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    private void loadFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Data Sources Configuration");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files (*.json)", "json"));

        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToLoad = fileChooser.getSelectedFile();

            try {
                List<DataSource> loadedSources = DataSourceConfig.loadFromFile(fileToLoad);
                dataSources = new ArrayList<>(loadedSources);
                updateDataSourcesModel();
                
                if (!dataSources.isEmpty()) {
                    dataSourcesList.setSelectedIndex(0);
                } else {
                    clearForm();
                }
                
                JOptionPane.showMessageDialog(this,
                        "Data sources loaded from " + fileToLoad.getName(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error loading data sources: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Returns the currently configured data sources
     * 
     * @return List of data sources
     */
    public List<DataSource> getDataSources() {
        return dataSources;
    }
    
    /**
     * Sets the data sources list and updates the UI
     * 
     * @param dataSources The list of data sources
     */
    public void setDataSources(List<DataSource> dataSources) {
        this.dataSources = dataSources;
        updateDataSourcesModel();
        if (!dataSources.isEmpty()) {
            dataSourcesList.setSelectedIndex(0);
        } else {
            clearForm();
        }
    }
}