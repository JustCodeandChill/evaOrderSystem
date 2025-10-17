package org.example;

import org.example.Controller.MainController;

public class Main {
    public static void main(String[] args) {
        // order
        MainController mainController = new MainController();
        mainController.currentWorkingOnSystem = "order";
        mainController.run();
    }
}