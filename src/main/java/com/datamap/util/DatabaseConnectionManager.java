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
     * Get a list of views from a database connection
     *
     * @param conn The database connection
     * @return A list of view names with "VIEW: " prefix
     * @throws SQLException If a database error occurs
     */
    public static List<String> getViews(Connection conn) throws SQLException {
        List<String> views = new ArrayList<>();

        DatabaseMetaData metaData = conn.getMetaData();
        String[] types = {"VIEW"};

        try (ResultSet rs = metaData.getTables(null, null, "%", types)) {
            while (rs.next()) {
                String viewName = rs.getString("TABLE_NAME");
                String viewSchema = rs.getString("TABLE_SCHEM");

                // Skip system schemas in PostgreSQL
                if (viewSchema != null &&
                        ("pg_catalog".equals(viewSchema) || "information_schema".equals(viewSchema))) {
                    continue;
                }

                // Add "VIEW: " prefix to distinguish from tables
                views.add(viewName);
            }
        }

        return views;
    }

    /**
     * Get both tables and views from a database connection
     *
     * @param conn The database connection
     * @return A combined list of table and view names (views are prefixed with "VIEW: ")
     * @throws SQLException If a database error occurs
     */
    public static List<String> getTablesAndViews(Connection conn) throws SQLException {
        List<String> tablesAndViews = new ArrayList<>();

        // Get tables
        tablesAndViews.addAll(getTables(conn));

        // Get views
        tablesAndViews.addAll(getViews(conn));

        return tablesAndViews;
    }

    /**
     * Check if a database object name represents a view
     *
     * @param name The name to check (may have "VIEW: " prefix)
     * @return true if the name represents a view
     */
    public static boolean isView(String name) {
        return name != null && name.startsWith("VIEW: ");
    }

    /**
     * Get the clean name without the "VIEW: " prefix
     *
     * @param name The name that may have a "VIEW: " prefix
     * @return The name without the prefix
     */
    public static String getCleanName(String name) {
        return name != null && name.startsWith("VIEW: ") ? name.substring(6) : name;
    }

    /**
     * Get a list of columns from a table or view
     *
     * @param conn The database connection
     * @param tableOrViewName The table or view name (may have "VIEW: " prefix)
     * @return A list of column names
     * @throws SQLException If a database error occurs
     */
    public static List<String> getColumns(Connection conn, String tableOrViewName) throws SQLException {
        List<String> columns = new ArrayList<>();

        // Remove "VIEW: " prefix if present
        String cleanName = getCleanName(tableOrViewName);

        // First try using DatabaseMetaData
        try {
            DatabaseMetaData metaData = conn.getMetaData();

            try (ResultSet rs = metaData.getColumns(null, null, cleanName, "%")) {
                while (rs.next()) {
                    String columnName = rs.getString("COLUMN_NAME");
                    columns.add(columnName);
                }
            }
        } catch (SQLException e) {
            // Some databases might have issues with metadata for views
            // Log the error but continue with alternative approach
            System.err.println("Metadata approach failed: " + e.getMessage());
        }

        // If no columns found, try using a SELECT statement
        if (columns.isEmpty()) {
            try {
                // Use a dummy query to get column metadata
                try (Statement stmt = conn.createStatement()) {
                    // Limit to 0 rows to avoid unnecessary data transfer
                    String query = "SELECT * FROM " + cleanName + " WHERE 1=0";
                    try (ResultSet rs = stmt.executeQuery(query)) {
                        ResultSetMetaData rsmd = rs.getMetaData();

                        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                            columns.add(rsmd.getColumnName(i));
                        }
                    }
                }
            } catch (SQLException e) {
                // If this also fails, throw the exception
                throw new SQLException("Failed to get columns for " + tableOrViewName + ": " + e.getMessage(), e);
            }
        }

        return columns;
    }
}