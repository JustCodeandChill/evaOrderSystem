package org.example.Systems;

import lombok.Data;
import org.example.Controller.MainController;
import org.example.Model.KitchenOrder.OrderForKitchen;
import org.example.Repos.OrderRepo;
import org.example.Repos.StatusRepo;
import org.example.Utils.ConnectionUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Data
public class KitchenSystem implements iSystem{
    InventorySystem inventorySystem;
    Queue<OrderForKitchen> startQueue;
    Queue<OrderForKitchen> endQueue;
    OrderRepo orderRepo;
    private Map<Integer, LocalDateTime> cookingStartTimes = new HashMap<>();
    private final static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private Scanner scanner = new Scanner(System.in);

    public KitchenSystem() {
        inventorySystem = new InventorySystem();
        orderRepo = new OrderRepo();
        orderRepo.pullOrderToCook();
        orderRepo.pullReadyOrders();
        startQueue = orderRepo.getStartQueue();
        endQueue = orderRepo.getEndQueue();
        cookFood();
    }

    @Override
    public void run() {
        // Kitchen is cooking food all the time


        while (true) {
            System.out.println("------------- Welcome to KitchenSystem ---------------");
            System.out.println("1. Check the Waiting Food Order List");
            System.out.println("2. Check the Finished Food Order List");
            System.out.println("3. Back to Order System");
            System.out.println("4. Go to Packing System");

            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1) {
                printWaitingList();
            } else if (choice == 2) {
                printReadyList();
            }else if (choice == 3) {
                MainController.currentWorkingOnSystem = "order";
                return;
            } else if (choice == 4) {
                MainController.currentWorkingOnSystem = "packing";
                return;
            }
        }
    }

    private void cookFood() {
        scheduler.scheduleAtFixedRate(this::cookFoodByKitchen, 1, 10, TimeUnit.SECONDS);
    }

    private void cookFoodByKitchen() {
        int DEFAULT_COOKING_TIME = 5;
        syncOrdersWaitingList();

        for (OrderForKitchen order : new ArrayList<>(startQueue)) {
            // If order just started cooking, set start time and mark Pending
            if (!cookingStartTimes.containsKey(order.getOrderId())) {
                try (Connection connection = ConnectionUtils.getConnection()) {
                    PreparedStatement ps = connection.prepareStatement(
                            "UPDATE order_status SET status_id = ? WHERE id = ?"
                    );
                    ps.setInt(1, StatusRepo.statuses.get("Pending")); // Cooking
                    ps.setInt(2, order.getId());
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                cookingStartTimes.put(order.getOrderId(), LocalDateTime.now());
                System.out.println("Started cooking order " + order.getOrderId());
            }

            // Check if cooking finished
            LocalDateTime startTime = cookingStartTimes.get(order.getOrderId());
            if (LocalDateTime.now().isAfter(startTime.plusSeconds(DEFAULT_COOKING_TIME))) {
                try (Connection connection = ConnectionUtils.getConnection()) {
                    PreparedStatement ps = connection.prepareStatement(
                            "UPDATE order_status SET status_id = ? WHERE id = ?"
                    );
                    ps.setInt(1, StatusRepo.statuses.get("Ready"));
                    ps.setInt(2, order.getId());
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                startQueue.remove(order);
                cookingStartTimes.remove(order.getOrderId());
                System.out.println("Order " + order.getOrderId() + " is Ready!");
            }
        }
    }

    private void printReadyList() {
        syncOrdersReadyList();
        System.out.println("Here is the list of food that ready " + endQueue);
    }

    private void printWaitingList() {
        syncOrdersWaitingList();
        System.out.println("waiting list is " + startQueue);
    }


    public boolean IsAbleToAddItem(int foodId, int quantity) {
        // check with inventory to see food can be added
        return inventorySystem.isAbleToAddItem(foodId, quantity);
    }

    private void syncOrdersWaitingList() {
        orderRepo.pullOrderToCook();
        startQueue = orderRepo.getStartQueue();
    }

    private void syncOrdersReadyList() {
        orderRepo.pullReadyOrders();
        endQueue = orderRepo.getEndQueue();
    }

}
