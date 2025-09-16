package main.java.com.mycompany;

import main.java.com.mycompany.server.HttpServer;


public class MicroSpringBoot {
    public static void main(String[] args) {
        try {
            String[] controllers = {
                    "main.java.com.mycompany.controllers.HelloController"
            };

            HttpServer server = new HttpServer(35000, 5);
            server.registerControllers(controllers);
            server.start();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error starting MicroSpringBoot: " + e.getMessage());
        }
    }
}

