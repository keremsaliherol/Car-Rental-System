package com.kerem.ordersystem.carrentalsystem.util;

import com.kerem.ordersystem.carrentalsystem.database.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to fix missing user-customer links
 */
public class UserCustomerLinkFixer {
    
    public static class LinkResult {
        public final boolean success;
        public final String message;
        public final int linksFixed;
        
        public LinkResult(boolean success, String message, int linksFixed) {
            this.success = success;
            this.message = message;
            this.linksFixed = linksFixed;
        }
    }
    
    /**
     * Fix all missing user-customer links by matching usernames with customer names
     */
    public static LinkResult fixAllUserCustomerLinks() {
        int linksFixed = 0;
        List<String> messages = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            
            // First, try to disable RLS policies that might interfere
            try {
                disableRLSPolicies(conn);
            } catch (Exception rlsError) {
                System.err.println("⚠️ Warning: Could not disable RLS policies: " + rlsError.getMessage());
                // Continue anyway - might still work
            }
            
            // Get all users with Customer role (RoleID = 2) that don't have linked customers
            String getUsersSql = """
                SELECT u.UserID, u.Username 
                FROM Users u 
                WHERE u.RoleID = 2 
                AND NOT EXISTS (
                    SELECT 1 FROM Customers c WHERE c.UserID = u.UserID
                )
                """;
            
            try (PreparedStatement getUsersStmt = conn.prepareStatement(getUsersSql);
                 ResultSet usersRs = getUsersStmt.executeQuery()) {
                
                while (usersRs.next()) {
                    int userId = usersRs.getInt("UserID");
                    String username = usersRs.getString("Username");
                    
                    // Try multiple matching strategies to find the customer
                    String[] searchPatterns = {
                        "%" + username.toLowerCase() + "%",           // Contains username
                        username.toLowerCase() + "%",                 // Starts with username  
                        "%" + username.toLowerCase(),                 // Ends with username
                        username.toLowerCase()                        // Exact match
                    };
                    
                    boolean customerFound = false;
                    
                    for (String pattern : searchPatterns) {
                        if (customerFound) break;
                        
                        String findCustomerSql = """
                            SELECT CustomerID, FullName 
                            FROM Customers 
                            WHERE LOWER(FullName) LIKE ? 
                            AND UserID IS NULL
                            """;
                        
                        try (PreparedStatement findStmt = conn.prepareStatement(findCustomerSql)) {
                            findStmt.setString(1, pattern);
                            ResultSet customerRs = findStmt.executeQuery();
                            
                            if (customerRs.next()) {
                                int customerId = customerRs.getInt("CustomerID");
                                String customerName = customerRs.getString("FullName");
                                
                                // Link them
                                String linkSql = "UPDATE Customers SET UserID = ? WHERE CustomerID = ?";
                                try (PreparedStatement linkStmt = conn.prepareStatement(linkSql)) {
                                    linkStmt.setInt(1, userId);
                                    linkStmt.setInt(2, customerId);
                                    
                                    int rowsUpdated = linkStmt.executeUpdate();
                                    if (rowsUpdated > 0) {
                                        linksFixed++;
                                        String msg = "✅ Linked: " + customerName + " → " + username + " (pattern: " + pattern + ")";
                                        messages.add(msg);
                                        System.out.println(msg);
                                        customerFound = true;
                                    }
                                }
                            }
                        }
                    }
                    
                    if (!customerFound) {
                        String msg = "⚠️ No matching customer found for user: " + username;
                        messages.add(msg);
                        System.out.println(msg);
                    }
                }
            }
            
            // Re-enable RLS policies
            try {
                enableRLSPolicies(conn);
            } catch (Exception rlsError) {
                System.err.println("⚠️ Warning: Could not re-enable RLS policies: " + rlsError.getMessage());
            }
            
            String finalMessage = "Fixed " + linksFixed + " user-customer links.\n" + 
                                String.join("\n", messages);
            
            return new LinkResult(true, finalMessage, linksFixed);
            
        } catch (SQLException e) {
            String errorMsg = "❌ Error fixing user-customer links: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            return new LinkResult(false, errorMsg, linksFixed);
        }
    }
    
    /**
     * Temporarily disable RLS policies that might interfere with user-customer linking
     */
    private static void disableRLSPolicies(Connection conn) throws SQLException {
        System.out.println("🔧 Attempting to disable RLS policies...");
        
        // Check if RLS policies exist and disable them
        String checkPolicySql = """
            SELECT p.name AS policy_name
            FROM sys.security_policies p
            JOIN sys.security_predicates pr ON p.object_id = pr.object_id
            JOIN sys.tables t ON pr.target_object_id = t.object_id
            WHERE t.name IN ('Customers', 'Rentals') AND p.is_enabled = 1
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(checkPolicySql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String policyName = rs.getString("policy_name");
                try {
                    String disableSql = "ALTER SECURITY POLICY [" + policyName + "] WITH (STATE = OFF)";
                    try (PreparedStatement disableStmt = conn.prepareStatement(disableSql)) {
                        disableStmt.executeUpdate();
                        System.out.println("✅ Disabled RLS policy: " + policyName);
                    }
                } catch (SQLException e) {
                    System.err.println("⚠️ Could not disable policy " + policyName + ": " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Re-enable RLS policies after user-customer linking
     */
    private static void enableRLSPolicies(Connection conn) throws SQLException {
        System.out.println("🔧 Attempting to re-enable RLS policies...");
        
        // Check if RLS policies exist and enable them
        String checkPolicySql = """
            SELECT p.name AS policy_name
            FROM sys.security_policies p
            JOIN sys.security_predicates pr ON p.object_id = pr.object_id
            JOIN sys.tables t ON pr.target_object_id = t.object_id
            WHERE t.name IN ('Customers', 'Rentals') AND p.is_enabled = 0
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(checkPolicySql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String policyName = rs.getString("policy_name");
                try {
                    String enableSql = "ALTER SECURITY POLICY [" + policyName + "] WITH (STATE = ON)";
                    try (PreparedStatement enableStmt = conn.prepareStatement(enableSql)) {
                        enableStmt.executeUpdate();
                        System.out.println("✅ Re-enabled RLS policy: " + policyName);
                    }
                } catch (SQLException e) {
                    System.err.println("⚠️ Could not re-enable policy " + policyName + ": " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Get diagnostic information about user-customer links
     */
    public static String getDiagnosticInfo() {
        StringBuilder info = new StringBuilder();
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            
            // Count users without customers
            String unlinkedUsersSql = """
                SELECT COUNT(*) as count 
                FROM Users u 
                WHERE u.RoleID = 2 
                AND NOT EXISTS (SELECT 1 FROM Customers c WHERE c.UserID = u.UserID)
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(unlinkedUsersSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    info.append("👤 Users without customers: ").append(rs.getInt("count")).append("\n");
                }
            }
            
            // Count customers without users
            String unlinkedCustomersSql = "SELECT COUNT(*) as count FROM Customers WHERE UserID IS NULL";
            try (PreparedStatement stmt = conn.prepareStatement(unlinkedCustomersSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    info.append("🏷️ Customers without users: ").append(rs.getInt("count")).append("\n");
                }
            }
            
            // Count rentals by unlinked customers
            String unlinkedRentalsSql = """
                SELECT COUNT(*) as count 
                FROM Rentals r 
                JOIN Customers c ON r.CustomerID = c.CustomerID 
                WHERE c.UserID IS NULL
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(unlinkedRentalsSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    info.append("📋 Rentals by unlinked customers: ").append(rs.getInt("count")).append("\n");
                }
            }
            
            // List specific unlinked customers with rentals
            String detailsSql = """
                SELECT c.CustomerID, c.FullName, COUNT(r.RentalID) as rental_count
                FROM Customers c
                LEFT JOIN Rentals r ON c.CustomerID = r.CustomerID
                WHERE c.UserID IS NULL
                GROUP BY c.CustomerID, c.FullName
                HAVING COUNT(r.RentalID) > 0
                ORDER BY rental_count DESC
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(detailsSql);
                 ResultSet rs = stmt.executeQuery()) {
                
                info.append("\n🔍 Unlinked customers with rentals:\n");
                while (rs.next()) {
                    info.append("  • ").append(rs.getString("FullName"))
                        .append(" (ID: ").append(rs.getInt("CustomerID"))
                        .append(", Rentals: ").append(rs.getInt("rental_count"))
                        .append(")\n");
                }
            }
            
        } catch (SQLException e) {
            info.append("❌ Error getting diagnostic info: ").append(e.getMessage());
        }
        
        return info.toString();
    }
} 