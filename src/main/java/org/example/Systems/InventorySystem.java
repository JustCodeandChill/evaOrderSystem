package org.example.Systems;

import lombok.Data;
import org.example.Controller.MainController;
import org.example.Model.KitchenOrder.Ingredient;
import org.example.Utils.ConnectionUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@Data
public class InventorySystem implements iSystem {
    private Map<Ingredient, Integer> ingredients;
    Map<Integer, Map<Ingredient, Integer>> costTable;
    Supplier supplier;
    private Scanner scanner = new Scanner(System.in);

    public InventorySystem() {
        supplier = new Supplier();
        ingredients = new HashMap<>();
        costTable = new HashMap<>();
        buildInventory();
        buildCostTable();
    }

    public void buildInventory() {
        try (Connection c = ConnectionUtils.getConnection()) {
            Statement s = c.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM ingredients");

            while (rs.next()) {
                Integer ingredientId = rs.getInt("id");
                Integer quantity = rs.getInt("quantity");
                String name = rs.getString("name");
                Ingredient ingredient = new Ingredient(ingredientId, name);
                ingredients.put(ingredient, quantity);
            }

        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        System.out.println(" ----- In the Inventory System -----");
        System.out.println("1. Check your inventory");
        System.out.println("2. Back to order system");

        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 1) {
            checkInventory();
        } else if (choice == 2) {
            MainController.currentWorkingOnSystem = "";
        }
    }

    private void checkInventory() {
        for (Ingredient ingredient : ingredients.keySet()) {
            String name =  ingredient.getName();
            int quantity = ingredients.get(ingredient);
            System.out.println("name: " + name + ", quantity: " + quantity);
        }
    }

    public boolean checkInventoryLimit() {
        int FLOOR_QUANTITY = 10;
        // if any ingredient has less than floorQuantity, not accept order and order some from supplier
        for (Map.Entry<Ingredient, Integer> entry : ingredients.entrySet()) {
            int quantity = entry.getValue();
            Ingredient ingredient = entry.getKey();

            if (quantity <= FLOOR_QUANTITY) {
                System.out.println("Inventory is running low on " + ingredient.getName());
                System.out.println("Need to order more from Supplier");
                // Spin a deamon thread to order low ingredient from Supplier
                orderMoreIngredientFromSupplier(ingredient.getId());
                return false;
            }
        }
        return true;
    }

    public void orderMoreIngredientFromSupplier(int ingredientId) {
        Runnable task = new OrderSupplierTask(ingredientId);

        Thread thread = new Thread(task);
        thread.setDaemon(true);


        try {
            thread.start();
            thread.join();
            buildInventory();
        } catch (InterruptedException e) {
            System.out.println("Interrupted in daemon thread");
        }

    }

    public boolean isAbleToAddItem(int foodId, int quantityOrdered) {
        Map<Ingredient, Integer> foodMap = costTable.get(foodId);
        // if inventory is less than 90%, not accepting any order
        if (!checkInventoryLimit()) {
            System.out.println("Inventory is running low on " + foodId + " and cannot make this order");
            return false;
        }

        for (Map.Entry<Ingredient, Integer> entry : foodMap.entrySet()) {
            Ingredient ingredient = entry.getKey();
            Integer quantity = entry.getValue();
            // check ingredient exist in inventory, no ingredient = no cook
            if (ingredients.containsKey(ingredient)
                    // check quantity of ingredient in inventory is sufficent enough to make
                    && quantity * quantityOrdered < ingredients.get(ingredient)
            ) {

                return true;
            }
        }

        System.out.println("Need this much ingredient to add " + foodMap);

        return false;
    }

    public void buildCostTable() {
        Ingredient tomato = new Ingredient(1, "Tomato");
        Ingredient cheese = new Ingredient(2, "Cheese");
        Ingredient lettuce = new Ingredient(3, "Lettuce");
        Ingredient rice = new Ingredient(5, "Rice");
        Ingredient beefPatty = new Ingredient(4, "Beef Patty");
        // for burger
        Map<Ingredient, Integer> burgerCostMap = new HashMap<>();
        burgerCostMap.put(tomato, 2);
        burgerCostMap.put(beefPatty, 1);
        burgerCostMap.put(cheese, 2);
        int burgerFoodId = 1;
        costTable.put(burgerFoodId, burgerCostMap);

        // for salad
        Map<Ingredient, Integer> pizzaCostMap = new HashMap<>();
        pizzaCostMap.put(tomato, 3);
        pizzaCostMap.put(lettuce, 4);
        pizzaCostMap.put(cheese, 1);
        int pizzaFoodId = 2;
        costTable.put(pizzaFoodId, pizzaCostMap);

        // for salad
        Map<Ingredient, Integer> saladCostMap = new HashMap<>();
        burgerCostMap.put(tomato, 3);
        burgerCostMap.put(lettuce, 4);
        burgerCostMap.put(cheese, 1);
        int saladFoodId = 3;
        costTable.put(saladFoodId, burgerCostMap);

        costTable.put(4, burgerCostMap);
        costTable.put(5, burgerCostMap);
    }
}
