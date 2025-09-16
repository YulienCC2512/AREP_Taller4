package main.java.com.mycompany.server;

import main.java.com.mycompany.annotations.GetMapping;
import main.java.com.mycompany.annotations.Request;
import main.java.com.mycompany.annotations.Response;
import main.java.com.mycompany.annotations.RestController;
import main.java.com.mycompany.server.App;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.*;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HttpServer - versión refactor inicial
 */
public class HttpServer {

    private static final Logger LOGGER = Logger.getLogger(HttpServer.class.getName());


    private final int port;
    private String staticFilesDir = "resources/public";

    private final ExecutorService threadPool;
    private final int maxThreads;

    private ServerSocket serverSocket;


    public HttpServer(int port, int maxThreads) {
        this.port = port;
        this.maxThreads = maxThreads > 0 ? maxThreads : 10;
        this.threadPool = Executors.newFixedThreadPool(this.maxThreads);


    }


    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "35000"));
        int maxThreads = Integer.parseInt(System.getenv().getOrDefault("MAX_THREADS", "10"));
        HttpServer server = new HttpServer(port, maxThreads);


        if (args != null && args.length > 0) {
            server.loadComponents(args);
        }

        // start
        try {
            server.start();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "No se pudo iniciar el servidor: " + e.getMessage(), e);
        }
    }

    public static void loadComponents(String[] args) {
        try {
            Class<?> c = Class.forName(args[0]);
            if (c.isAnnotationPresent(RestController.class)) {
                Object instance = c.getDeclaredConstructor().newInstance(); // crear el controlador

                Method[] methods = c.getDeclaredMethods();
                for (Method m : methods) {
                    if (m.isAnnotationPresent(GetMapping.class)) {
                        String mapping = m.getAnnotation(GetMapping.class).mapping();

                        // Registrar en App
                        App.addRoute("GET", mapping, (req, res) -> {
                            try {
                                Object result = m.invoke(instance, req, res);
                                if (result != null) {
                                    res.send(200, "text/plain", result.toString());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                try {
                                    res.send(500, "text/plain", "Internal Server Error");
                                } catch (Exception ignored) {}
                            }
                        });
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void registerControllers(String[] controllers) {
        for (String controller : controllers) {
            try {
                Class<?> c = Class.forName(controller);

                if (c.isAnnotationPresent(RestController.class)) {
                    Object instance = c.getDeclaredConstructor().newInstance();

                    for (Method m : c.getDeclaredMethods()) {
                        if (m.isAnnotationPresent(GetMapping.class)) {
                            String mapping = m.getAnnotation(GetMapping.class).mapping();

                            App.addRoute("GET", mapping, (req, res) -> {
                                try {
                                    Object result = m.invoke(instance, req, res);
                                    if (result != null) {
                                        res.send(200, "text/plain", result.toString());
                                    }
                                } catch (Exception e) {
                                    try {
                                        res.send(500, "text/plain", "Internal Server Error");
                                    } catch (IOException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                }
                            });
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    // Inicia el servidor y el loop de aceptación
    public void start() throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        try {
            serverSocket = new ServerSocket(port);
            LOGGER.info("Server started on port: " + port + " (threads: " + maxThreads + ")");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not start server on port: " + port, e);
            throw e;
        }

        while (!serverSocket.isClosed()) {
            try {
                Socket client = serverSocket.accept();
                threadPool.submit(() -> {
                    try {
                        handleClient(client);
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Error handling client: " + ex.getMessage(), ex);
                        try {
                            client.close();
                        } catch (IOException ignored) {}
                    }
                });
            } catch (SocketException se) {
                // socket cerrado como parte del shutdown
                break;
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Accept failed: " + e.getMessage(), e);
            }
        }
    }


    public void shutdown() {
        LOGGER.info("Shutting down server...");
        // cerrar server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error closing server socket", e);
        }

        // shutdown pool
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        LOGGER.info("Server shutdown complete.");
    }


    private void handleClient(Socket clientSocket) {
        String remote = clientSocket.getRemoteSocketAddress().toString();
        LOGGER.info("Handling client: " + remote);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream rawOut = clientSocket.getOutputStream()) {

            Request request = Request.build(in);
            Response response = new Response(new PrintWriter(rawOut, true), rawOut);

            try {
                if (App.hasRoute(request.getMethod(), request.getPath())) {
                    App.handleRoute(request, response);
                } else {
                    serveStaticFile(request.getPath(), response);
                }
                if (request.getPath().startsWith("/app/getTask")) {
                    String name = request.getQueryParams().getOrDefault("name", "desconocido");
                    response.send(200, "application/json", "{ \"task\": \"Tarea registrada para " + name + "\" }");
                } else if (request.getPath().equals("/app/postTask") && request.getMethod().equals("POST")) {
                    response.send(200, "application/json", "{ \"status\": \"Post realizado con éxito\" }");
                } else {
                    serveStaticFile(request.getPath(), response);
                }

            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error invoking route: " + ex.getMessage(), ex);
                response.send(500, "text/plain", "500 Internal Server Error");
            }


        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Client handling error: " + e.getMessage(), e);
        } finally {
            try {
                if (!clientSocket.isClosed()) clientSocket.close();
            } catch (IOException ignored) {}
            LOGGER.info("Finished handling client: " + remote);
        }



    }


    private static final String STATIC_DIR = "src/main/public";

    public void serveStaticFile(String path, Response response) {
        try {
            if (path == null || path.equals("/")) {
                path = "/index.html";
            }

            File file = new File("src/main/java/resources/public"+ path);

            if (!file.exists() || file.isDirectory()) {
                response.send(404, "text/html", "<h1>404 Not Found</h1>");
                return;
            }

            byte[] fileData = Files.readAllBytes(file.toPath());
            String contentType = typeOfFile(file.getName());

            response.send(200, contentType, fileData);
        } catch (IOException e) {
            try {
                response.send(500, "index/html", "<h1>500 Internal Server Error</h1>");
            } catch (IOException ignored) {}
        }
    }





    private String typeOfFile(String fileName) {
        if (fileName.endsWith(".html") || fileName.endsWith(".htm")) return "text/html";
        if (fileName.endsWith(".css")) return "text/css";
        if (fileName.endsWith(".js")) return "application/javascript";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".gif")) return "image/gif";
        return "application/octet-stream";
    }

    public void setStaticFilesDir(String staticFilesDir) {
        this.staticFilesDir = staticFilesDir;
    }
}
