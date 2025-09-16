package test.java.com.mycompany;

import main.java.com.mycompany.server.HttpServer;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HttpServerIntegrationTest {

    private static HttpServer server;
    private static Thread serverThread;

    @BeforeAll
    static void startServer() {
        server = new HttpServer(35000, 5);
        server.loadComponents(new String[]{"com.mycompany.controllers.TaskController"});

        serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.start();

        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
    }

    @AfterAll
    static void stopServer() {
        server.shutdown();
    }
}
