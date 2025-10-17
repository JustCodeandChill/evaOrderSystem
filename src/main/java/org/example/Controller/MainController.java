package org.example.Controller;

import lombok.*;
import org.example.Repos.StatusRepo;
import org.example.Systems.InventorySystem;
import org.example.Systems.KitchenSystem;
import org.example.Systems.PackingSystem;
import org.example.Systems.TakeOrderSystem;

import java.util.logging.Level;
import java.util.logging.Logger;


@Data
@Getter
@Setter
public class MainController {
    public static String currentWorkingOnSystem;
    public TakeOrderSystem takeOrderSystem;
    public KitchenSystem kitchenSystem;
    public InventorySystem inventorySystem;
    public PackingSystem packingSystem;

    public MainController() {
        takeOrderSystem = new TakeOrderSystem();
        kitchenSystem = new KitchenSystem();
        inventorySystem = new InventorySystem();
        packingSystem = new PackingSystem();
    }

    public void control() {
        System.out.println("Run control");

        if (currentWorkingOnSystem.equals("order")) {
            takeOrderSystem.run();
        } else if (currentWorkingOnSystem.equals("kitchen")) {
            kitchenSystem.run();
        } else if (currentWorkingOnSystem.equals("packing")) {
            packingSystem.run();
        } else if (currentWorkingOnSystem.equals("inventory")) {
            inventorySystem.run();
        }
    }

    public void run() {
        try {
            while (true) {
                control();
            }
        } catch (Exception ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
