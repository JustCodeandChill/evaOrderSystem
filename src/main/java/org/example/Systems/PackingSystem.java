package org.example.Systems;

import lombok.Data;
import org.example.Controller.MainController;
import org.example.Model.KitchenOrder.FoodOrder;
import org.example.Model.KitchenOrder.OrderForKitchen;
import org.example.Repos.FoodRepo;
import org.example.Repos.OrderRepo;
import org.example.Repos.StatusRepo;
import org.example.Utils.ConnectionUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

@Data
public class PackingSystem implements iSystem {
    private Scanner scanner = new Scanner(System.in);
    Queue<OrderForKitchen> readyOrder;
    OrderRepo orderRepo;
    FoodRepo foodRepo;

    public PackingSystem() {
        orderRepo = new OrderRepo();
    }

    @Override
    public void run() {

        while (true) {
            System.out.println(" ----------------- Packing system ------------- ");
            System.out.println("1. Check Ready to Packing Orders");
            System.out.println("2. View Specific Order");
            System.out.println("3. View Packing Decision");
            System.out.println("4. Pay Money and Process Forward");

            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1) {
                printReadyList();
            } else if (choice == 2) {
                checkOrderByOrderId();
            } else if (choice == 3) {
                checkPacking();
            } else if (choice == 4) {
                payAndProceed();
            }
        }
    }

    private void payAndProceed() {
        System.out.println("Please enter the order id you want to pay");
        int orderId = scanner.nextInt();
        scanner.nextLine();
        System.out.println("Your total cost is " + getTotalCost(orderId));

        try (Connection connection = ConnectionUtils.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE order_status SET status_id = ? WHERE order_id = ?"
            );
            ps.setInt(1, StatusRepo.statuses.get("Delivered"));
            ps.setInt(2, orderId);
            int rowAffected = ps.executeUpdate();

            if (rowAffected > 0) {
                System.out.printf("You successfully paid the order with order id %d\n", orderId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void checkPacking() {
        System.out.println("Please enter the order id you want to check");
        int orderId = scanner.nextInt();
        scanner.nextLine();
        String packing = "";

        try (Connection c = ConnectionUtils.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT p.type FROM orders o LEFT JOIN packing p ON o.packing_id = p.id  WHERE o.id = ?");
            ps.setInt(1, orderId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                packing = rs.getString("type");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Your preferred packing is " + packing);

        switch (packing) {
            case "Delivery":
                System.out.println("Your order will be sent to the delivery system to deliver after you make payment");
                break;
            case  "Take Out":
                System.out.println("Your order is ready for you to pick up");
                break;
            case "Dine in":
                System.out.println("Your food is already bring to the table");
                break;
            default:
                System.out.println("No option");
                break;
        }
    }

    private void checkOrderByOrderId() {
        System.out.println("Please enter the order id you want to check");
        int orderId = scanner.nextInt();
        scanner.nextLine();

        List<FoodOrder> foods = new ArrayList<>();
        try (Connection c = ConnectionUtils.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM order_items WHERE order_id = ?");
            ps.setInt(1, orderId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int foodId = rs.getInt("food_id");
                int quantity = rs.getInt("quantity");
                FoodOrder food = new FoodOrder();
                food.setFoodId(foodId);
                food.setQuantity(quantity);
                foods.add(food);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Display
        System.out.println("Here are the total view of Food order with id " + orderId);
        int totalCost = 0;
        for (FoodOrder food : foods) {
            System.out.println( "Food Name: " + FoodRepo.getFoodName(food.getFoodId()));
            System.out.println( "Ordered Quantity: " + food.getQuantity());
            int cost = FoodRepo.getFoodCost(food.getFoodId()) * food.getQuantity();
            System.out.println( "Partial Cost: " + cost);
            totalCost += cost;
        }

        System.out.println("Total Cost: " + totalCost);
    }

    private void printReadyList() {
        orderRepo.pullReadyOrders();
        readyOrder = orderRepo.getEndQueue();
        System.out.println("Ready to Packing Order " + readyOrder);
    }

    private int getTotalCost(int orderId) {
        List<FoodOrder> foods = new ArrayList<>();
        try (Connection c = ConnectionUtils.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM order_items WHERE order_id = ?");
            ps.setInt(1, orderId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int foodId = rs.getInt("food_id");
                int quantity = rs.getInt("quantity");
                FoodOrder food = new FoodOrder();
                food.setFoodId(foodId);
                food.setQuantity(quantity);
                foods.add(food);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        int totalCost =foods.stream().mapToInt(FoodOrder::getQuantity).sum();
        System.out.println("Total Cost: " + totalCost);
        return totalCost;
    }


//    private void checkout() {
//        System.out.println("--------------- Enter your card information ---------------");
//        System.out.println("The sample card number is 123, the card CVV: 123, or ('4111111111111111', '123')");
//        String cardNumber = "";
//        String cardCVV = "";
//
//        boolean isValidCard = false;
//        while (!isValidCard) {
//            System.out.println("Enter your card number:");
//            cardNumber = scanner.nextLine();
//            System.out.println("Enter your card CVV:");
//            cardCVV = scanner.nextLine();
//            boolean isValidCardNumber = isValidCreditCard(cardNumber);
//            boolean isValidCVV =  isValidCvvFormat(cardCVV);
//            isValidCard = isValidCardNumber && isValidCVV;
//            if (!isValidCard) {
//                System.out.println("Invalid card number or cvv format. Please try again.");
//            }
//        }
//
//
//        try (Connection connection = ConnectionUtils.getConnection()) {
//            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM card WHERE card_number = ?");
//            preparedStatement.setString(1, cardNumber);
//            ResultSet resultSet = preparedStatement.executeQuery();
//
//            if (resultSet.next()) {
//                String number = resultSet.getString("card_number");
//                String cvv = resultSet.getString("card_cvv");
//
//                if (cvv.equalsIgnoreCase(cardCVV)) {
//                    System.out.println("You have successfully checked out your card.");
//                    // update database
//
//                    // redirect to homepage
//                    MainController.currentWorkingOnSystem = "";
//                }
//
//            } else {
//                System.out.println("Invalid card number or CVV. Unable to authenticate");
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//
//    }

}
