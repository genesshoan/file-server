package dev.shoangenes.client;

import java.net.*;
import java.io.*;
import java.util.logging.Logger;

import dev.shoangenes.utils.*;

public class ClientConnection implements AutoCloseable {
    // --- Fields ---

    private final Socket socket;
    private final DataInputStream input;
    private final DataOutputStream output;
    private final boolean connected;

    // --- Logger ---

    private static final Logger logger = LoggerUtil.getLogger(ClientConnection.class);

    // --- Constructor ---

    /**
     * Initializes a new ClientConnection instance and connects to the server.
     * @throws IOException if an I/O error occurs when creating the socket or streams.
     */
    public ClientConnection() throws IOException {
        int port = ServerProperties.getInstance().getPort();
        String host = ServerProperties.getInstance().getHost();

        socket = new Socket(InetAddress.getByName(host), port);
        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
        connected = true;
    }

    // --- Getters ---

    /**
     * Checks if the client is currently connected to the server.
     * @return true if connected, false otherwise.
     */
    public boolean isConnected() {
        return connected;
    }

    // --- Public Methods ---

    /**
     * Sends an HTTP GET request to the server.
     * @throws IOException if an I/O error occurs while sending the request.
     */
    public void sendPutRequest(String fileName, byte[] data) throws IOException {
        checkConnection();
        HttpRequest.put(fileName, data).writeTo(output);
    }

    /**
     * Sends an HTTP GET request to the server.
     * @throws IOException if an I/O error occurs while sending the request.
     */
    public void sendGetByIdRequest(int id) throws IOException {
        checkConnection();
        HttpRequest request = HttpRequest.getById(id);
        request.writeTo(output);
    }

    /**
     * Sends an HTTP GET request to the server.
     * @throws IOException if an I/O error occurs while sending the request.
     */
    public void sendGetByNameRequest(String name) throws IOException {
        checkConnection();
        HttpRequest request = HttpRequest.getByName(name);
        request.writeTo(output);
    }

    /**
     * Sends an HTTP DELETE request to the server.
     * @throws IOException if an I/O error occurs while sending the request.
     */
    public void sendDeleteByIdRequest(int id) throws IOException {
        checkConnection();
        HttpRequest request = HttpRequest.deleteById(id);
        request.writeTo(output);
    }

    /**
     * Sends an HTTP DELETE request to the server.
     * @throws IOException if an I/O error occurs while sending the request.
     */
    public void sendDeleteByNameRequest(String name) throws IOException {
        checkConnection();
        HttpRequest request = HttpRequest.deleteByName(name);
        request.writeTo(output);
    }

    // --- Helpers ---

    /**
     * Receives an HTTP response from the server.
     * @return The received HttpResponse object.
     * @throws IOException if an I/O error occurs while reading the response.
     */
    private HttpResponse receiveResponse() throws IOException{
        return HttpResponse.readFrom(input);
    }

    /**
     * Checks if the client is connected to the server.
     * @throws IOException if the client is not connected.
     */
    private void checkConnection() throws IOException {
        if (!connected) throw new IOException("Not connected to the server.");
    }

    // --- AutoCloseable Implementation ---

    @Override
    public void close() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            logger.severe("Error closing client resources: " + e.getMessage());
        }
    }
}
