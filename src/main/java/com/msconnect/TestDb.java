package com.msconnect;

import java.sql.*;

public class TestDb {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://thomas.proxy.rlwy.net:36107/railway";
        String user = "postgres";
        String password = "ZzPpVwBzXtMNhrkeaGKlWBffFfxbGqAg";

        System.out.println("Testing connection to: " + url);
        System.out.println("Username: " + user);
        System.out.println("Password length: " + password.length());

        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("✅ Driver loaded successfully");

            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                System.out.println("✅ Connected to database!");
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT 1 as result");
                rs.next();
                System.out.println("✅ Query executed! Result: " + rs.getInt("result"));
            }
        } catch (ClassNotFoundException e) {
            System.out.println("❌ PostgreSQL driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ SQL Error: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
        }
    }
}