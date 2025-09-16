package main.java.com.mycompany.annotations;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class Request {
    private String method;                // GET, POST, etc.
    private String path;                  // /api/users
    private String httpVersion;           // HTTP/1.1
    private Map<String, String> headers;  // headers del request
    private Map<String, String> queryParams; // parámetros en la URL
    private String body;                  // cuerpo del request

    public Request(String method, String path, String httpVersion,
                   Map<String, String> headers, Map<String, String> queryParams,
                   String body) {
        this.method = method;
        this.path = path;
        this.httpVersion = httpVersion;
        this.headers = headers;
        this.queryParams = queryParams;
        this.body = body;
    }

    public static Request build(BufferedReader in) throws IOException, URISyntaxException {
        // Primera línea: "GET /path?x=1 HTTP/1.1"
        String requestLine = in.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Empty request");
        }

        String[] parts = requestLine.split(" ");
        String method = parts[0];
        String fullPath = parts[1];
        String httpVersion = parts.length > 2 ? parts[2] : "HTTP/1.1";

        // Parsear URI para separar path y queryParams
        URI uri = new URI(fullPath);
        String path = uri.getPath();
        Map<String, String> queryParams = parseQueryParams(uri.getQuery());

        // Leer headers
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            int colonIndex = line.indexOf(":");
            if (colonIndex > 0) {
                String key = line.substring(0, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();
                headers.put(key, value);
            }
        }

        // Leer body si existe
        StringBuilder body = new StringBuilder();
        if (headers.containsKey("Content-Length")) {
            int contentLength = Integer.parseInt(headers.get("Content-Length"));
            for (int i = 0; i < contentLength; i++) {
                body.append((char) in.read());
            }
        }

        return new Request(method, path, httpVersion, headers, queryParams, body.toString());
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                String key = keyValue[0];
                String value = keyValue.length > 1 ? keyValue[1] : "";
                params.put(key, value);
            }
        }
        return params;
    }

    // Getters
    public String getMethod() { return method; }
    public String getPath() { return path; }
    public String getHttpVersion() { return httpVersion; }
    public Map<String, String> getHeaders() { return headers; }
    public String getHeader(String name) { return headers.getOrDefault(name, null); }
    public Map<String, String> getQueryParams() { return queryParams; }
    public String getQueryParam(String name) { return queryParams.getOrDefault(name, null); }
    public String getBody() { return body; }
}
