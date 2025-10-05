package dev.shoangenes.server;

import dev.shoangenes.utils.HttpMethod;
import dev.shoangenes.utils.HttpRequest;
import dev.shoangenes.utils.LoggerUtil;
import dev.shoangenes.utils.HttpResponse;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

public class RequestHandler implements Runnable {
    // --- Instance Variables ---
    private final Socket socket;
    private final FileManager fileManager = FileManager.getInstance();

    // --- Logger ---

    private static final Logger logger = LoggerUtil.getLogger(RequestHandler.class);

    // --- Constructor ---

    public RequestHandler(Socket socket) {
        this.socket = socket;
    }

    /**
     * Handles incoming client requests.
     * Supports PUT, GET (BY_ID, BY_NAME), and DELETE (BY_ID, BY_NAME) methods.
     * Sends appropriate HTTP-like responses back to the client.
     */
    @Override
    public void run() {
        logger.info("Client connected: " + socket.getInetAddress() + ":" + socket.getPort());
        try (socket;
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
            logger.info("Connection accepted from " + socket.getInetAddress() + ":" + socket.getPort());

            HttpRequest request = HttpRequest.readFrom(input);
            logger.info("Received request: " + request);

            HttpResponse response = processRequest(request, input);

            sendResponse(output, response);
        } catch (Exception e) {
            logger.severe("Error handling request: " + e.getMessage());
        }
        finally {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (Exception e) {
                logger.severe("Error closing socket: " + e.getMessage());
            }
            logger.info("Client disconnected: " + socket.getInetAddress() + ":" + socket.getPort());
        }
    }

    /**
     * Processes the client request and returns an appropriate HttpResponse.
     *
     * @param request The request string from the client.
     * @param input   The DataInputStream to read additional data if needed.
     * @return An HttpResponse object representing the result of the request.
     */
    private HttpResponse processRequest(HttpRequest request, DataInputStream input) {
        HttpMethod method = request.getMethod();

        switch (method) {
            case PUT -> {
                String fileName = request.getFileName();

                byte[] fileContent = request.getData();

                if (fileContent == null) {
                    return HttpResponse.serverError();
                }

                return handlePut(fileName, fileContent);
            }
            case GET_BY_ID -> {
                return handleGetById(request.getId());
            }
            case GET_BY_NAME -> {
                return handleGetByName(request.getFileName());
            }
            case DELETE_BY_ID -> {
                return handleDeleteById(request.getId());
            }
            case DELETE_BY_NAME -> {
                return handleDeleteByName(request.getFileName());
            }
            default -> {
                logger.info("Unsupported method: " + method);
                return HttpResponse.serverError();
            }
        }
    }

    // --- Handler Methods ---

    /**
     * Handles DELETE requests by file ID.
     * @param id The ID of the file to delete.
     * @return An HttpResponse indicating success or failure.
     */
    private HttpResponse handleDeleteById(int id) {
        boolean success = fileManager.deleteFileById(id);

        if (!success) {
            logger.warning("Could not delete file with ID: " + id);
            return HttpResponse.notFound();
        }

        logger.info("File deleted with ID: " + id);
        return HttpResponse.success();
    }

    /**
     * Handles DELETE requests by file name.
     * @param fileName The name of the file to delete.
     * @return An HttpResponse indicating success or failure.
     */
    private HttpResponse handleDeleteByName(String fileName) {
        boolean success = fileManager.deleteFileByName(fileName);

        if (!success) {
            logger.warning("Could not delete file with name: " + fileName);
            return HttpResponse.notFound();
        }

        logger.info("File deleted with name: " + fileName);
        return HttpResponse.success();
    }

    /**
     * Handles PUT requests to save a file.
     * @param fileName The name of the file to save.
     * @param fileContent The content of the file as a byte array.
     * @return An HttpResponse indicating success and the file ID, or failure.
     */
    private HttpResponse handlePut(String fileName, byte[] fileContent) {
        try {
            int id = fileManager.saveFile(fileName, fileContent);

            if (id == -1) {
                logger.warning("Could not save file: " + fileName);
                return HttpResponse.forbidden();
            }

            logger.info("File saved with ID: " + id);
            return HttpResponse.successWithId(id);
        } catch (Exception e) {
            logger.severe("Error saving file: " + e.getMessage());
            return HttpResponse.serverError();
        }
    }

    /**
     * Handles GET requests by file ID.
     * @param id The ID of the file to retrieve.
     * @return An HttpResponse containing the file content, or indicating failure.
     */
    private HttpResponse handleGetById(int id) {
        byte[] content = fileManager.getFileById(id);

        if (content == null) {
            logger.warning("File not found with ID: " + id);
            return HttpResponse.notFound();
        }

        logger.info("File sent with ID: " + id);
        return HttpResponse.successWithContent(content);
    }

    /**
     * Handles GET requests by file name.
     * @param fileName The name of the file to retrieve.
     * @return An HttpResponse containing the file content, or indicating failure.
     */
    private HttpResponse handleGetByName(String fileName) {
        byte[] content = fileManager.getFileByName(fileName);

        if (content == null) {
            logger.warning("File not found with name: " + fileName);
            return HttpResponse.notFound();
        }

        logger.info("File sent with name: " + fileName);
        return HttpResponse.successWithContent(content);
    }

    /**
     * Sends the HttpResponse back to the client.
     * Handles both text and binary content appropriately.
     * @param output The DataOutputStream to write to.
     * @param response The HttpResponse to send.
     */
    private void sendResponse(DataOutputStream output, HttpResponse response) {
        try {
            response.writeTo(output);
        } catch (IOException e) {
            logger.severe("Error sending response: " + e.getMessage());
        }
    }
}
