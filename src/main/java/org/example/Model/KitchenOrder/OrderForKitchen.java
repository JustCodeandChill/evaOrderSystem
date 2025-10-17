package org.example.Model.KitchenOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderForKitchen {
    private Integer id;
    private Integer orderId;
    private Integer statusId;
    private LocalDateTime timeStamp;
}
