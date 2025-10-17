package org.example.Utils;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionUtils {
    static MysqlDataSource dataSource = new MysqlDataSource();
    static Connection connection;
    static {
        dataSource.setServerName("127.0.0.1");
        dataSource.setPort(3307);
        dataSource.setUser("root");
        dataSource.setPassword("admin");
        dataSource.setDatabaseName("orderSystem");
    }

    public static Connection getConnection(){
        try {
            // Always create a fresh connection
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get connection", e);
        }
    }


}
