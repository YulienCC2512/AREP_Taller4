package main.java.com.mycompany.annotations;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class Response {
    private PrintWriter writer;
    private OutputStream rawOut;
    private boolean headersSent = false;

    public Response(PrintWriter writer, OutputStream rawOut) {
        this.writer = writer;
        this.rawOut = rawOut;
    }

    public void send(int status, String contentType, String body) throws IOException {
        byte[] bodyBytes = body.getBytes("UTF-8");
        send(status, contentType, bodyBytes);
    }

    public void send(int status, String contentType, byte[] body) throws IOException {
        if (!headersSent) {
            writer.printf("HTTP/1.1 %d %s\r\n", status, getStatusMessage(status));
            writer.printf("Content-Type: %s\r\n", contentType);
            writer.printf("Content-Length: %d\r\n", body.length);
            writer.printf("Connection: close\r\n");
            writer.print("\r\n");
            writer.flush();
            headersSent = true;
        }

        rawOut.write(body);
        rawOut.flush();
    }

    public void setHeader(String name, String value) {
        if (!headersSent) {
            writer.printf("%s: %s\r\n", name, value);
        }
    }

    public void close() throws IOException {
        writer.close();
        rawOut.close();
    }

    private String getStatusMessage(int status) {
        switch (status) {
            case 200: return "OK";
            case 201: return "Created";
            case 400: return "Bad Request";
            case 401: return "Unauthorized";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 500: return "Internal Server Error";
            default: return "";
        }
    }
}
