package com.datamap.util;

import com.datamap.model.DataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for managing database connections
 */
public class DatabaseConnectionManager {
    
    /**
     * Tests a database connection using the provided data source configuration
     * 
     * @param dataSource The data source configuration to test
     * @return true if connection is successful, false otherwise
     * @throws SQLException if there is a database error
     * @throws ClassNotFoundException if the JDBC driver class is not found
     */
    public static boolean testConnection(DataSource dataSource) throws SQLException, ClassNotFoundException {
        Connection connection = null;
        try {
            // Load the JDBC driver
            Class.forName(dataSource.getJdbcDriver());
            
            // Establish the connection
            connection = DriverManager.getConnection(
                dataSource.getJdbcUrl(),
                dataSource.getUsername(),
                dataSource.getPassword()
            );
            
            return connection.isValid(5); // Test if connection is valid within 5 seconds
        } finally {
            // Close the connection
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }
    }
    
    /**
     * Gets default JDBC driver for the given database type
     * 
     * @param dbType The database type ("postgres" or "mysql")
     * @return The default JDBC driver class name
     */
    public static String getDefaultJdbcDriver(String dbType) {
        if ("postgres".equals(dbType)) {
            return "org.postgresql.Driver";
        } else if ("mysql".equals(dbType)) {
            return "com.mysql.cj.jdbc.Driver";
        }
        return "";
    }
    
    /**
     * Gets default JDBC URL template for the given database type
     * 
     * @param dbType The database type ("postgres" or "mysql")
     * @return The default JDBC URL template
     */
    public static String getDefaultJdbcUrl(String dbType, String databaseName) {
        if ("postgres".equals(dbType)) {
            return "jdbc:postgresql://localhost:5432/" + databaseName;
        } else if ("mysql".equals(dbType)) {
            return "jdbc:mysql://localhost:3306/" + databaseName;
        }
        return "";
    }
}