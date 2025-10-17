package org.example.Repos;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.Model.Packing;
import org.example.Utils.ConnectionUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class PackingRepos {
    List<Packing> packingOptions;

    public PackingRepos() {
        packingOptions = new ArrayList<>();
        try (Connection conn = ConnectionUtils.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet set =  stmt.executeQuery("Select * from packing");

            while (set.next()) {
                Integer id = set.getInt("id");
                String type = set.getString("type");
                packingOptions.add(new Packing(id, type));
            }
        } catch (SQLException e) {
            System.out.println("Connection Error. Cant populate the packingOptions");
        }
    }

    public void printPackingOptions() {
        packingOptions.forEach((item) -> { System.out.println("Packing ID: " + item.getId() + " Type: " + item.getType()); });
    }

    public Packing getPacking(Integer id) {
        return  packingOptions.stream().filter(item -> Objects.equals(item.getId(), id)).findFirst().orElse(null);
    }
}
