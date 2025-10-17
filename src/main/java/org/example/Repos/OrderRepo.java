package org.example.Repos;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.Model.KitchenOrder.OrderForKitchen;
import org.example.Utils.ConnectionUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

@Data
public class OrderRepo {
    Queue<OrderForKitchen> startQueue;
    Queue<OrderForKitchen> endQueue;
    Set<OrderForKitchen> orderProcessingSet;
    Set<OrderForKitchen> orderReadySet;

    public OrderRepo() {
        orderProcessingSet = new HashSet<>();
        orderReadySet = new HashSet<>();
        Comparator<OrderForKitchen> comparator = Comparator.comparing(OrderForKitchen::getTimeStamp);
        startQueue = new PriorityQueue<>(comparator);
        endQueue =  new PriorityQueue<>(comparator);
    }

    public void pullOrderToCook() {
        orderProcessingSet.clear();          // clear old objects
        startQueue.clear();

        try (Connection conn = ConnectionUtils.getConnection()) {
            PreparedStatement statement = conn.prepareStatement("SELECT * FROM order_status WHERE status_id = ?");
            statement.setInt(1,StatusRepo.statuses.get("Processing"));

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Integer id = rs.getInt("id");
                Integer orderId = rs.getInt("order_id");

                // Check so we not adding duplicate order when pulling data from dtbs
                boolean isDuplicated = orderProcessingSet.stream().anyMatch((OrderForKitchen o) -> Objects.equals(o.getOrderId(), orderId));
                if (isDuplicated) continue;

                Integer statusId = rs.getInt("status_id");
                LocalDateTime orderTimeStamp = rs.getTimestamp("updated_at").toLocalDateTime();
                OrderForKitchen orderForKitchen = new OrderForKitchen();
                orderForKitchen.setId(id);
                orderForKitchen.setOrderId(orderId);
                orderForKitchen.setStatusId(statusId);
                orderForKitchen.setTimeStamp(orderTimeStamp);
                orderProcessingSet.add(orderForKitchen);
            }
            startQueue.clear();
            startQueue.addAll(orderProcessingSet);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void pullReadyOrders() {
        orderReadySet.clear();          // clear old objects
        endQueue.clear();
        try (Connection conn = ConnectionUtils.getConnection()) {
            PreparedStatement statement = conn.prepareStatement("SELECT * FROM order_status WHERE status_id = ?");
            statement.setInt(1,StatusRepo.statuses.get("Ready"));

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Integer id = rs.getInt("id");
                Integer orderId = rs.getInt("order_id");
                boolean isDuplicated = orderReadySet.stream().anyMatch((OrderForKitchen o) -> Objects.equals(o.getOrderId(), orderId));
                if (isDuplicated) continue;

                Integer statusId = rs.getInt("status_id");
                LocalDateTime orderTimeStamp = rs.getTimestamp("updated_at").toLocalDateTime();
                OrderForKitchen orderForKitchen = new OrderForKitchen();
                orderForKitchen.setId(id);
                orderForKitchen.setOrderId(orderId);
                orderForKitchen.setStatusId(statusId);
                orderForKitchen.setTimeStamp(orderTimeStamp);

                orderReadySet.add(orderForKitchen);
            }
            endQueue.clear();
            endQueue.addAll(orderReadySet);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
