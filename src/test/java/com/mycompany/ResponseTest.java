package test.java.com.mycompany;

import main.java.com.mycompany.annotations.Response;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    @Test
    void testSendResponse() throws Exception {
        ByteArrayOutputStream rawOut = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(rawOut, true);

        Response response = new Response(writer, rawOut);
        response.send(200, "text/plain", "Hola");

        String result = rawOut.toString();
        assertTrue(result.contains("200 OK"));
        assertTrue(result.contains("Hola"));
    }
}
