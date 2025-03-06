package com.datamap.util;

import com.datamap.model.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnectionManager {
    
    /**
     * Get a database connection from a data source
     * 
     * @param dataSource The data source connection info
     * @return The database connection
     * @throws ClassNotFoundException If the JDBC driver is not found
     * @throws SQLException If a database error occurs
     */
    public static Connection getConnection(DataSource dataSource) throws ClassNotFoundException, SQLException {
        // Load the JDBC driver
        Class.forName(dataSource.getJdbcDriver());
        
        // Create a connection
        return DriverManager.getConnection(
            dataSource.getJdbcUrl(),
            dataSource.getUsername(),
            dataSource.getPassword()
        );
    }
    
    /**
     * Test a database connection
     * 
     * @param dataSource The data source to test
     * @return true if connection is successful, false otherwise
     * @throws ClassNotFoundException If the JDBC driver is not found
     * @throws SQLException If a database error occurs
     */
    public static boolean testConnection(DataSource dataSource) throws ClassNotFoundException, SQLException {
        Connection conn = null;
        try {
            conn = getConnection(dataSource);
            return conn != null && !conn.isClosed();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    /**
     * Get the default JDBC driver for a database type
     * 
     * @param dbType The database type (postgres, mysql)
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
     * Get the default JDBC URL for a database type and name
     * 
     * @param dbType The database type (postgres, mysql)
     * @param dbName The database name
     * @return The default JDBC URL
     */
    public static String getDefaultJdbcUrl(String dbType, String dbName) {
        if ("postgres".equals(dbType)) {
            return "jdbc:postgresql://localhost:5432/" + dbName;
        } else if ("mysql".equals(dbType)) {
            return "jdbc:mysql://localhost:3306/" + dbName + "?useSSL=false";
        }
        return "";
    }
    
    /**
     * Get a list of tables from a database connection
     * 
     * @param conn The database connection
     * @return A list of table names
     * @throws SQLException If a database error occurs
     */
    public static List<String> getTables(Connection conn) throws SQLException {
        List<String> tables = new ArrayList<>();
        
        DatabaseMetaData metaData = conn.getMetaData();
        String[] types = {"TABLE"};
        
        try (ResultSet rs = metaData.getTables(null, null, "%", types)) {
            while (rs.next()) {
                // Skip system tables
                String tableName = rs.getString("TABLE_NAME");
                String tableSchema = rs.getString("TABLE_SCHEM");
                
                // Skip system schemas in PostgreSQL
                if (tableSchema != null && 
                    ("pg_catalog".equals(tableSchema) || "information_schema".equals(tableSchema))) {
                    continue;
                }
                
                tables.add(tableName);
            }
        }
        
        return tables;
    }
    
    /**
     * Get a list of columns from a table
     * 
     * @param conn The database connection
     * @param tableName The table name
     * @return A list of column names
     * @throws SQLException If a database error occurs
     */
    public static List<String> getColumns(Connection conn, String tableName) throws SQLException {
        List<String> columns = new ArrayList<>();
        
        DatabaseMetaData metaData = conn.getMetaData();
        
        try (ResultSet rs = metaData.getColumns(null, null, tableName, "%")) {
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                columns.add(columnName);
            }
        }
        
        return columns;
    }
}