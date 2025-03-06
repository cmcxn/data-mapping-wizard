package com.datamap.util;

import com.datamap.model.DataSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for managing data source configurations
 */
public class DataSourceConfig {
    
    /**
     * Saves the list of data sources to a JSON file
     * 
     * @param file The file to save to
     * @param dataSources The list of data sources to save
     * @throws IOException if there is an error writing to the file
     */
    public static void saveToFile(File file, List<DataSource> dataSources) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(file, dataSources);
    }
    
    /**
     * Loads a list of data sources from a JSON file
     * 
     * @param file The file to load from
     * @return The list of data sources
     * @throws IOException if there is an error reading from the file
     */
    public static List<DataSource> loadFromFile(File file) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(file, 
            objectMapper.getTypeFactory().constructCollectionType(List.class, DataSource.class));
    }
    
    /**
     * Creates a default list with sample data sources
     * 
     * @return A list with default data sources
     */
    public static List<DataSource> createDefaultDataSources() {
        List<DataSource> dataSources = new ArrayList<>();
        
        // Add a sample PostgreSQL data source
        DataSource postgresDs = new DataSource(
            "Sample PostgreSQL",
            "postgres",
            "org.postgresql.Driver",
            "jdbc:postgresql://localhost:5432/sample_db",
            "postgres",
            "password",
            "sample_db"
        );
        dataSources.add(postgresDs);
        
        // Add a sample MySQL data source
        DataSource mysqlDs = new DataSource(
            "Sample MySQL",
            "mysql",
            "com.mysql.cj.jdbc.Driver",
            "jdbc:mysql://localhost:3306/sample_db",
            "root",
            "password",
            "sample_db"
        );
        dataSources.add(mysqlDs);
        
        return dataSources;
    }
}