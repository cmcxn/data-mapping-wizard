package com.datamap.model;

/**
 * Model class for database data source configuration
 */
public class DataSource {
    private String name;
    private String dbType;
    private String jdbcDriver;
    private String jdbcUrl;
    private String username;
    private String password;
    private String databaseName;

    public DataSource() {
    }

    public DataSource(String name, String dbType, String jdbcDriver, String jdbcUrl, 
                      String username, String password, String databaseName) {
        this.name = name;
        this.dbType = dbType;
        this.jdbcDriver = jdbcDriver;
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.databaseName = databaseName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getJdbcDriver() {
        return jdbcDriver;
    }

    public void setJdbcDriver(String jdbcDriver) {
        this.jdbcDriver = jdbcDriver;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
    
    @Override
    public String toString() {
        return name + " (" + dbType + ")";
    }
}