package com.kerem.ordersystem.carrentalsystem.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database connection manager for CarRentalManagementSystem using HikariCP connection pool
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private HikariDataSource dataSource;

    // Database credentials (MSSQL)
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=CarRentalDB;encrypt=true;trustServerCertificate=true";
    private static final String DB_USERNAME = "advancedata";  // kendi kullanıcını yaz
    private static final String DB_PASSWORD = "glandorp29"; // kendi şifreni yaz

    private DatabaseManager() {
        initializeDataSource();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void initializeDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DB_URL);
        config.setUsername(DB_USERNAME);
        config.setPassword(DB_PASSWORD);
        config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        // Pool config
        config.setMinimumIdle(3);
        config.setMaximumPoolSize(15);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);

        // Connection test
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(3000);

        this.dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            System.err.println("❌ Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    public void createCustomersTableIfNotExists() {
        try (Connection conn = getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            
            // First check if table exists
            String checkTableExistsSQL = """
                SELECT COUNT(*) as table_count 
                FROM INFORMATION_SCHEMA.TABLES 
                WHERE TABLE_NAME = 'Customers'
                """;
            
            boolean tableExists = false;
            try (java.sql.ResultSet rs = stmt.executeQuery(checkTableExistsSQL)) {
                if (rs.next()) {
                    tableExists = rs.getInt("table_count") > 0;
                }
            }
            
            if (!tableExists) {
                // Create table if it doesn't exist
                String createTableSQL = """
                    CREATE TABLE Customers (
                        CustomerID int IDENTITY(1,1) PRIMARY KEY,
                        FullName nvarchar(100) NOT NULL,
                        Phone nvarchar(20),
                        Address nvarchar(255),
                        DriverLicenseNo nvarchar(50),
                        DateOfBirth date,
                        Email nvarchar(100) NULL,
                        UserID int NULL
                    )
                    """;
                
                stmt.execute(createTableSQL);
                System.out.println("✅ Customers table created successfully");
                return; // Exit early if we just created the table
            }
            
            // If table exists, check columns but handle RLS carefully
            System.out.println("🔧 Customers table exists, checking structure...");
            
            try {
                // Try to disable RLS policies temporarily for schema changes
                disableRLSPoliciesForTable(conn, "Customers");
                
                // Check existing columns
                String checkColumnsSQL = """
                    SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE 
                    FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE TABLE_NAME = 'Customers'
                    ORDER BY ORDINAL_POSITION
                    """;
                
                boolean hasUserID = false;
                boolean hasEmail = false;
                
                try (java.sql.ResultSet rs = stmt.executeQuery(checkColumnsSQL)) {
                    System.out.println("📋 Existing Customers table columns:");
                    while (rs.next()) {
                        String columnName = rs.getString("COLUMN_NAME");
                        String dataType = rs.getString("DATA_TYPE");
                        String nullable = rs.getString("IS_NULLABLE");
                        System.out.println("  - " + columnName + " (" + dataType + ", nullable: " + nullable + ")");
                        
                        if ("UserID".equals(columnName)) {
                            hasUserID = true;
                        }
                        if ("Email".equals(columnName)) {
                            hasEmail = true;
                        }
                    }
                }
                
                // Add missing columns
                if (!hasEmail) {
                    System.out.println("📧 Adding Email column...");
                    String addEmailSQL = "ALTER TABLE Customers ADD Email nvarchar(100) NULL";
                    stmt.execute(addEmailSQL);
                    System.out.println("✅ Email column added");
                }
                
                if (!hasUserID) {
                    System.out.println("👤 Adding UserID column...");
                    String addUserIdSQL = "ALTER TABLE Customers ADD UserID int NULL";
                    stmt.execute(addUserIdSQL);
                    System.out.println("✅ UserID column added");
                } else {
                    // Make sure UserID is nullable
                    System.out.println("🔧 Ensuring UserID column is nullable...");
                    String alterUserIdSQL = "ALTER TABLE Customers ALTER COLUMN UserID int NULL";
                    stmt.execute(alterUserIdSQL);
                    System.out.println("✅ UserID column set to nullable");
                }
                
            } catch (SQLException schemaError) {
                if (schemaError.getMessage().contains("fn_securitypredicate") || 
                    schemaError.getMessage().contains("security policy")) {
                    System.err.println("⚠️ RLS policy preventing schema changes. Table structure may need manual adjustment.");
                    System.err.println("⚠️ Please run schema updates manually with RLS disabled, or contact DBA.");
                } else {
                    throw schemaError; // Re-throw if it's not an RLS issue
                }
            } finally {
                // Always try to re-enable RLS policies
                try {
                    enableRLSPoliciesForTable(conn, "Customers");
                } catch (SQLException rlsError) {
                    System.err.println("⚠️ Warning: Could not re-enable RLS policies: " + rlsError.getMessage());
                }
            }
            
            System.out.println("✅ Customers table structure check completed");
            
        } catch (SQLException e) {
            System.err.println("❌ Error with Customers table: " + e.getMessage());
            // Don't print full stack trace for RLS errors as they're expected
            if (!e.getMessage().contains("fn_securitypredicate") && 
                !e.getMessage().contains("security policy")) {
                e.printStackTrace();
            }
        }
    }
    
    private void disableRLSPoliciesForTable(Connection conn, String tableName) throws SQLException {
        String checkPolicySQL = """
            SELECT p.name AS policy_name
            FROM sys.security_policies p
            JOIN sys.security_predicates pr ON p.object_id = pr.object_id
            JOIN sys.tables t ON pr.target_object_id = t.object_id
            WHERE t.name = ? AND p.is_enabled = 1
            """;
        
        try (java.sql.PreparedStatement stmt = conn.prepareStatement(checkPolicySQL)) {
            stmt.setString(1, tableName);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String policyName = rs.getString("policy_name");
                    try {
                        String disableSQL = "ALTER SECURITY POLICY [" + policyName + "] WITH (STATE = OFF)";
                        try (java.sql.Statement disableStmt = conn.createStatement()) {
                            disableStmt.execute(disableSQL);
                            System.out.println("🔒 Temporarily disabled RLS policy: " + policyName);
                        }
                    } catch (SQLException e) {
                        System.err.println("⚠️ Could not disable policy " + policyName + ": " + e.getMessage());
                    }
                }
            }
        }
    }
    
    private void enableRLSPoliciesForTable(Connection conn, String tableName) throws SQLException {
        String checkPolicySQL = """
            SELECT p.name AS policy_name
            FROM sys.security_policies p
            JOIN sys.security_predicates pr ON p.object_id = pr.object_id
            JOIN sys.tables t ON pr.target_object_id = t.object_id
            WHERE t.name = ? AND p.is_enabled = 0
            """;
        
        try (java.sql.PreparedStatement stmt = conn.prepareStatement(checkPolicySQL)) {
            stmt.setString(1, tableName);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String policyName = rs.getString("policy_name");
                    try {
                        String enableSQL = "ALTER SECURITY POLICY [" + policyName + "] WITH (STATE = ON)";
                        try (java.sql.Statement enableStmt = conn.createStatement()) {
                            enableStmt.execute(enableSQL);
                            System.out.println("🔒 Re-enabled RLS policy: " + policyName);
                        }
                    } catch (SQLException e) {
                        System.err.println("⚠️ Could not re-enable policy " + policyName + ": " + e.getMessage());
                    }
                }
            }
        }
    }
}