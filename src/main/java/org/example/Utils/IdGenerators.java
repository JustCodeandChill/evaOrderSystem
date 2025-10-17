package org.example.Utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicLong;

public class IdGenerators {
    private static final AtomicLong counter = new AtomicLong(0);

    // initialize from database
    public static void initializeFromDatabase(Connection connection, String tableName) {
        try (Statement stmt = connection.createStatement()) {
            // Get the maximum id from the table
            String query = "SELECT IFNULL(MAX(id), 0) FROM " + "orders";
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                long lastId = rs.getLong(1);
                counter.set(lastId); // Start counting from last ID
                System.out.println("Initialized ID counter from DB: " + lastId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize ID generator from database", e);
        }
    }

    public static long nextId() {
        return counter.incrementAndGet();
    }
}
