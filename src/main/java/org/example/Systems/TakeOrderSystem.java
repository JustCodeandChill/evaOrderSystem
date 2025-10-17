package org.example.Systems;

import org.example.Controller.MainController;
import org.example.Model.KitchenOrder.FoodOrder;
import org.example.Model.Order;
import org.example.Model.Packing;
import org.example.Repos.PackingRepos;
import org.example.Repos.StatusRepo;
import org.example.Utils.ConnectionUtils;
import org.example.Utils.IdGenerators;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TakeOrderSystem implements iSystem{
    private Scanner scanner = new Scanner(System.in);
    private Order order;
    private PackingRepos packingRepos;
    private List<FoodOrder> currOrder;
    private KitchenSystem kitchenSystem;

    public TakeOrderSystem() {
        order = new Order();
        order.setId((int) IdGenerators.nextId());
        packingRepos = new PackingRepos();
        currOrder = new ArrayList<>();
        kitchenSystem = new KitchenSystem();
    }

    public void createOrder(){
        currOrder = new ArrayList<>();
    }

    @Override
    public void run() {
        boolean isOrdering = true;

        while (isOrdering) {
            // Assuming only add item, no delete item from items list yet
            System.out.println("------- Order System -------");
            System.out.println("1. View Menu");
            System.out.println("2. Add Item To Order");
            System.out.println("3. Choose Packing Type");
            System.out.println("4. View Current Order");
            System.out.println("5. Send Order to Kitchen");
            System.out.println("6. Go to Kitchen System");
            System.out.println("7. Go to Packing System");
            System.out.println("8. Go to Inventory System");
            System.out.println("Please enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1) {
                printMenu();
            } else if (choice == 2) {
                addItem();
            }  else if (choice == 3) {
                addPacking();
            } else if (choice == 4) {
                viewOrder();
            } else if (choice == 5) {
                sendToKitchen();
            } else if (choice == 6) {
                MainController.currentWorkingOnSystem = "kitchen";
                return;
            }  else if (choice == 7) {
                MainController.currentWorkingOnSystem = "packing";
                return;
            } else if (choice == 8) {
                MainController.currentWorkingOnSystem = "inventory";
                return;
            } else {
                isOrdering = true;
            }
        }

    }

    private void sendToKitchen() {
        // make sure the order is valid to send to kitchen
        if (order == null || order.getFoodOrders() == null || order.getFoodOrders().size() == 0) {
            System.out.println("There is no item to add to the Kitchen");
            return;
        }

        if (order.getPacking() == null) {
            Packing packing = new Packing();
            packing.setId(1);
            order.setPacking(packing);
        }

        try (Connection conn = ConnectionUtils.getConnection()) {
            conn.setAutoCommit(false);
            // general info to orders table
            String sql = "INSERT INTO orders (packing_id) VALUES (?)";
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, order.getPacking().getId());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            int orderId = -1;
            if (rs.next()) {
                orderId = rs.getInt(1);
            } else {
                throw new SQLException("Failed to retrieve generated order ID.");
            }

            // add list of food
            String sqlItem = "INSERT INTO order_items (order_id, food_id, quantity) VALUES (?, ?, ?)";
            PreparedStatement psItem = conn.prepareStatement(sqlItem);

            for (FoodOrder foodOrder : order.getFoodOrders()) {
                psItem.setInt(1, orderId);
                psItem.setInt(2, foodOrder.getFoodId());
                psItem.setInt(3, foodOrder.getQuantity());
                psItem.addBatch();
            }

            psItem.executeBatch();

            // add to status table
            String sqlStatus = "INSERT INTO order_status (order_id, status_id) VALUES (?, ?)";
            PreparedStatement psStatus = conn.prepareStatement(sqlStatus);
            psStatus.setInt(1, orderId);
            // status_id 2 means processing
            psStatus.setInt(2, StatusRepo.statuses.get("Processing"));
            psStatus.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console " + e.toString());
            throw new RuntimeException();
        }
        System.out.println("Sending Order to Kitchen. Thank you");

    }

    private void viewOrder() {
        System.out.println(order.toString());
    }

    private void addPacking() {
        if (currOrder == null) {
            System.out.println("Need to create order before set packing");
            createOrder();
            return;
        }

        packingRepos.printPackingOptions();
        // TODO validation logic
        System.out.println("Please enter your Packing Type: ");
        Integer choice =  scanner.nextInt();
        scanner.nextLine();

        // TODO validation logic
        Packing p = packingRepos.getPacking(choice);
        order.setPacking(p);

    }

    private void addItem() {
        if (order == null) {
            System.out.println("Need to create order before add item");
            createOrder();
            return;
        }

        System.out.println("Please enter the foodID:");
        int foodId = scanner.nextInt();
        scanner.nextLine();
        System.out.println("Please enter the quantity:");
        int quantity = scanner.nextInt();
        scanner.nextLine();

        // Check kitchen whether addable or not
        // if yes add to order, if no back to the loop
        boolean check = kitchenSystem.IsAbleToAddItem(foodId, quantity);
        if (check) {
            currOrder.stream()
                    .filter(item -> item.getFoodId() == foodId)
                    .findFirst()
                    .ifPresentOrElse(
                            existing -> existing.setQuantity(existing.getQuantity() + quantity),
                            () -> currOrder.add(new FoodOrder(foodId, quantity))
                    );
            System.out.println("Successfully added a new item" + currOrder);
            order.setFoodOrders(currOrder);
        } else {
            System.out.println("Unable to add food");
        }
    }

    private void printMenu() {
        System.out.println("Here are the food available right now");

        try (Connection connection = ConnectionUtils.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet set = statement.executeQuery("SELECT * FROM food");

            while (set.next()) {
                String name =  set.getString("name");
                System.out.println("Name: " + name + "_foodID: " + set.getString("id"));
            }
        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            throw new RuntimeException(e);
        }
    }
}
