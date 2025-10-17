package org.example.Repos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.Model.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class StatusRepo {
    public static Map<String, Integer> statuses;

    static {
        statuses = new HashMap<>();
        statuses.put("Processing", 2);
        statuses.put("Ready", 3);
        statuses.put("Pending", 1);
        statuses.put("Delivered", 4);
        statuses.put("Cancelled", 5);
    }
}
