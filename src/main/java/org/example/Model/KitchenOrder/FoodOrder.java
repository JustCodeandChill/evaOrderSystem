package org.example.Model.KitchenOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodOrder {
    private Integer foodId;
    private Integer quantity;
}
