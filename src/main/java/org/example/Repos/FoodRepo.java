package org.example.Repos;

import lombok.Data;
import org.example.Utils.ConnectionUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Data
public final class FoodRepo {
    private static final Map<Integer, String> foodNames = new HashMap<>();
    private static final Map<Integer, Integer> foodCosts = new HashMap<>();

    static {
        buildMaps();
    }

    // Private constructor to prevent unnecessary instantiation
    private FoodRepo() {}

    public static void buildMaps() {
        foodNames.clear();
        foodCosts.clear();
        try (Connection c = ConnectionUtils.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM food");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                Integer foodId = rs.getInt("id");
                String foodName = rs.getString("name");
                Integer priceForOne = rs.getInt("price");

                foodNames.put(foodId, foodName);
                foodCosts.put(foodId, priceForOne);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Optional: helper methods to get values
    public static String getFoodName(int id) {
        return foodNames.get(id);
    }

    public static Integer getFoodCost(int id) {
        return foodCosts.get(id);
    }
}
