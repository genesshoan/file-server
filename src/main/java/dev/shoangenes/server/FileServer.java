package dev.shoangenes.server;

import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import dev.shoangenes.utils.LoggerUtil;
import dev.shoangenes.utils.ServerProperties;

public class FileServer implements AutoCloseable{
    private FileManager fileManager;
    private final ExecutorService executor;
    private final ServerSocket serverSocket;
    private boolean running;

    private static final Logger logger = LoggerUtil.getLogger(FileServer.class);

    /**
     * Private constructor to initialize the FileServer.
     * Reads configuration from ServerProperties and sets up the server socket and thread pool.
     */
    private FileServer() throws IOException {
        logger.info("Initializing File Server...");

        String host = ServerProperties.getInstance().getHost();
        int port = ServerProperties.getInstance().getPort();
        int maxThreads = ServerProperties.getInstance().getMaxConnections();

        executor = Executors.newFixedThreadPool(maxThreads);
        serverSocket =  new ServerSocket(port, 50, InetAddress.getByName(host));
        running = true;

        logger.info("File Server initialized on " + host + ":" + port);
    }

    /**
     * Starts the server to accept incoming connections.
     * This method blocks and runs indefinitely until the server is closed.
     */
    private void startServer() {
        logger.info("Server started!");
        acceptConnections();
    }

    /**
     * Accepts incoming client connections and delegates them to the thread pool for handling.
     */
    private void acceptConnections() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                executor.submit(new RequestHandler(clientSocket));
            } catch (IOException e) {
                logger.severe("Error accepting connection: " + e);
            }
        }
    }

    /**
     * Main method to start the FileServer.
     * Ensures proper resource management and logs server status.
     */
    public static void main(String[] args) {
        try (FileServer server = new FileServer()) {
            server.startServer();
        } catch (IOException e) {
            logger.severe("Fatal error starting server: " + e.getMessage());
            System.exit(1);
        } finally {
            logger.info("Server stopped");
        }
    }

    /**
     * Closes the server, stops accepting new connections, and shuts down the thread pool.
     * Ensures all resources are released properly.
     */
    @Override
    public void close() {
        logger.info("Closing File Server...");
        running = false;

        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.severe("Error closing server: " + e);
            }
        }

        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                        logger.warning("Executor did not terminate");
                    }
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        logger.info("Server stopped");
    }
}
