package org.example.Systems;

public class OrderSupplierTask implements Runnable {
    Supplier supplier;
    int ingredientId;

    public OrderSupplierTask(int ingredientId) {
        supplier = new Supplier();
        this.ingredientId = ingredientId;
    }

    public void orderMoreIngredientFromSupplier(int ingredientId) {
        int DEFAULT_QUANTITY = 100;
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }

        supplier.orderIngredient(ingredientId, DEFAULT_QUANTITY);
    }

    @Override
    public void run() {
        orderMoreIngredientFromSupplier(this.ingredientId);
    }
}
