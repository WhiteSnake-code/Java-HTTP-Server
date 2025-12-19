package org.whitesnake.httpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HTTPServer {

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(8080)) {
            System.out.println("HTTP Server started on port 8080");

            while (true) {
                try (Socket client = server.accept()) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    String firstLine = reader.readLine();
                    if (firstLine == null) continue;

                    String method = firstLine.split(" ")[0];
                    String resource = firstLine.split(" ")[1];

                    // --- 1. PROCESAR FORMULARIO (POST /submit) ---
                    if (method.equals("POST") && resource.equals("/submit")) {
                        handlePostRequest(client, reader);
                        continue; 
                    }

                    // --- 2. SERVIR ARCHIVOS ESTÁTICOS (GET) ---
                    handleGetRequest(client, resource);
                } catch (IOException e) {
                    System.err.println("Error processing request: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handlePostRequest(Socket client, BufferedReader reader) throws IOException {
        int contentLength = 0;
        String line;

        // Leer cabeceras para obtener el tamaño del cuerpo (body)
        while (!(line = reader.readLine()).isEmpty()) {
            if (line.startsWith("Content-Length:")) {
                contentLength = Integer.parseInt(line.split(":")[1].trim());
            }
        }

        // Leer el cuerpo del formulario
        char[] bodyChars = new char[contentLength];
        reader.read(bodyChars, 0, contentLength);
        String formData = new String(bodyChars);

        // Parsear los datos (firstName=John&lastName=Doe...)
        Map<String, String> params = new HashMap<>();
        for (String pair : formData.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                // Decodificar caracteres especiales como @ o espacios
                String value = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                params.put(kv[0], value);
            }
        }

        // Cargar el template result.html desde resources
        InputStream is = HTTPServer.class.getClassLoader().getResourceAsStream("result.html");
        if (is != null) {
            String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            // Reemplazar las variables {{}} por los datos reales
            html = html.replace("{{firstName}}", params.getOrDefault("firstName", ""))
                       .replace("{{lastName}}", params.getOrDefault("lastName", ""))
                       .replace("{{email}}", params.getOrDefault("email", ""))
                       .replace("{{message}}", params.getOrDefault("message", ""));

            sendResponse(client, html.getBytes(StandardCharsets.UTF_8), "text/html");
        }
    }

    private static void handleGetRequest(Socket client, String resource) throws IOException {
        // Si piden la raíz, enviamos el index
        String fileName = resource.equals("/") ? "index.html" : resource.substring(1);

        // El ClassLoader busca el archivo directamente en src/main/resources
        InputStream is = HTTPServer.class.getClassLoader().getResourceAsStream(fileName);

        if (is != null) {
            byte[] content = is.readAllBytes();
            String contentType = fileName.endsWith(".css") ? "text/css" : "text/html";
            sendResponse(client, content, contentType);
        } else {
            String error404 = "HTTP/1.1 404 Not Found\r\n\r\nFile Not Found";
            client.getOutputStream().write(error404.getBytes());
        }
    }

    private static void sendResponse(Socket client, byte[] content, String contentType) throws IOException {
        String header = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + contentType + "; charset=UTF-8\r\n" +
                        "Content-Length: " + content.length + "\r\n" +
                        "\r\n";

        OutputStream out = client.getOutputStream();
        out.write(header.getBytes(StandardCharsets.UTF_8));
        out.write(content);
        out.flush();
    }
}