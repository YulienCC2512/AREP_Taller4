package main.java.com.mycompany.server;

import main.java.com.mycompany.annotations.Request;
import main.java.com.mycompany.annotations.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class App {
    private static final Map<String, Map<String, BiConsumer<Request, Response>>> routes = new HashMap<>();

    public static void addRoute(String method, String path, BiConsumer<Request, Response> handler) {
        routes.computeIfAbsent(method.toUpperCase(), k -> new HashMap<>()).put(path, handler);
    }

    public static boolean hasRoute(String method, String path) {
        return routes.containsKey(method.toUpperCase()) && routes.get(method.toUpperCase()).containsKey(path);
    }

    public static void handleRoute(Request req, Response res) {
        BiConsumer<Request, Response> handler = routes
                .getOrDefault(req.getMethod().toUpperCase(), new HashMap<>())
                .get(req.getPath());

        if (handler != null) {
            handler.accept(req, res);
        } else {
            try {
                res.send(404, "text/plain", "404 Not Found");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
