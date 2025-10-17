package org.example.Systems;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.Utils.ConnectionUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Data
@NoArgsConstructor
public class Supplier implements iSystem{


    @Override
    public void run() {

    }

    public void orderIngredient(Integer ingredientId, Integer quantity) {
        try (Connection conn = ConnectionUtils.getConnection()) {
            PreparedStatement preparedStatement = conn.prepareStatement("UPDATE ingredients SET quantity = quantity + ? WHERE id = ?");
            preparedStatement.setInt(1, quantity);
            preparedStatement.setInt(2, ingredientId);
            int rowAffected = preparedStatement.executeUpdate();
            if (rowAffected > 0) {
                System.out.println("Order more ingredient successfully.");
            } else {
                System.out.println("Failed to order more " + ingredientId);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
