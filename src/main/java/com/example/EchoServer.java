package com.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import redis.clients.jedis.Jedis;

public class EchoServer {
    
    private static boolean isRedisAvailable = false;
    // Default to localhost:36379 if not specified
    private static String redisHost = System.getenv("REDIS_HOST") != null ? System.getenv("REDIS_HOST") : "localhost";
    private static int redisPort = System.getenv("REDIS_PORT") != null ? Integer.parseInt(System.getenv("REDIS_PORT")) : 36379;

    public static void main(String[] args) throws IOException {
        int port = 8086;

        // Check Redis availability
        try (Jedis jedis = new Jedis(redisHost, redisPort)) {
            if ("PONG".equals(jedis.ping())) {
                 isRedisAvailable = true;
                 System.out.println("Redis is available on " + redisHost + ":" + redisPort);
            }
        } catch (Exception e) {
            System.err.println("Redis not available at " + redisHost + ":" + redisPort + " - " + e.getMessage());
            System.err.println("Continuing server startup without Redis.");
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new EchoHandler());
        server.setExecutor(null); // creates a default executor
        System.out.println("Starting server on port " + port);
        server.start();
    }

    static class EchoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            System.out.println("Received request: " + t.getRequestMethod() + " " + t.getRequestURI());

            if (isRedisAvailable) {
                try (Jedis jedis = new Jedis(redisHost, redisPort)) {
                    jedis.set("lastAccessTime", String.valueOf(System.currentTimeMillis()));
                } catch (Exception e) {
                    System.err.println("Failed to update Redis lastAccessTime: " + e.getMessage());
                }
            }

            // Read the request body
            InputStream is = t.getRequestBody();
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            
            // Prepare response
            String response = body;
            // If body is empty, return a simple greeting
            if (response.isEmpty()) {
                response = "Echo Server: Send a POST body to see it echoed back.\n";
            }

            // Send response headers
            t.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            
            // Send response body
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }
    }
}