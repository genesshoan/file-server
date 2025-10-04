package dev.shoangenes.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class HttpRequest {
    // --- Fields ---

    private static final int MAGIC_NUMBER = 0x52455153;
    private HttpMethod method;
    private String fileName;
    private int id;
    private byte[] data;

    // --- Constructors ---

    /**
     * Private constructor to enforce use of static factory methods.
     * @param method The HTTP method for the request.
     */
    private HttpRequest(HttpMethod method) {
        this.method = method;
        this.fileName = null;
        this.id = -1;
        this.data = null;
    }

    // --- Getters ---

    public HttpMethod getMethod() { return method; }
    public String getFileName() { return fileName; }
    public int getId() { return id; }
    public byte[] getData() { return data; }

    // --- Static Factory Methods ---

    /**
     * Creates a PUT request with the specified file name and data.
     * @param fileName The name of the file to upload.
     * @param data The byte array representing the file data.
     * @return A new HttpRequest instance for a PUT operation.
     */
    public static HttpRequest put(String fileName, byte[] data) {
        HttpRequest req = new HttpRequest(HttpMethod.PUT);
        req.fileName = fileName;
        req.data = data;
        return req;
    }

    /**
     * Creates a GET request to retrieve a file by its ID.
     * @param id The ID of the file to retrieve.
     * @return A new HttpRequest instance for a GET operation by ID.
     */
    public static HttpRequest getById(int id) {
        HttpRequest req = new HttpRequest(HttpMethod.GET_BY_ID);
        req.id = id;
        return req;
    }

    /**
     * Creates a GET request to retrieve a file by its name.
     * @param fileName The name of the file to retrieve.
     * @return A new HttpRequest instance for a GET operation by name.
     */
    public static HttpRequest getByName(String fileName) {
        HttpRequest req = new HttpRequest(HttpMethod.GET_BY_NAME);
        req.fileName = fileName;
        return req;
    }

    /**
     * Creates a DELETE request to remove a file by its ID.
     * @param id The ID of the file to delete.
     * @return A new HttpRequest instance for a DELETE operation by ID.
     */
    public static HttpRequest deleteById(int id) {
        HttpRequest req = new HttpRequest(HttpMethod.DELETE_BY_ID);
        req.id = id;
        return req;
    }

    /**
     * Creates a DELETE request to remove a file by its name.
     * @param fileName The name of the file to delete.
     * @return A new HttpRequest instance for a DELETE operation by name.
     */
    public static HttpRequest deleteByName(String fileName) {
        HttpRequest req = new HttpRequest(HttpMethod.DELETE_BY_NAME);
        req.fileName = fileName;
        return req;
    }

    // --- Serialization Method ---

    /**
     * Serializes the HttpRequest object to a DataOutputStream.
     * @param output The DataOutputStream to write the serialized data to.
     * @throws IOException If an I/O error occurs.
     */
    public void writeTo(DataOutputStream output) throws IOException {
        output.writeInt(MAGIC_NUMBER);
        output.writeInt(method.getCode());

        switch (method) {
            case PUT -> {
                output.writeUTF(fileName);
                output.writeInt(data.length);
                output.write(data);
            }
            case GET_BY_NAME, DELETE_BY_NAME -> output.writeUTF(fileName);
            case GET_BY_ID, DELETE_BY_ID -> output.writeInt(id);
        }

        output.flush();
    }

    // --- Deserialization Method ---

    /**
     * Deserializes an HttpRequest object from a DataInputStream.
     * @param input The DataInputStream to read the serialized data from.
     * @return A new HttpRequest instance reconstructed from the stream.
     * @throws IOException If an I/O error occurs or if the magic number is invalid.
     */
    public static HttpRequest readFrom(DataInputStream input) throws IOException {
        int magic = input.readInt();
        if (magic != MAGIC_NUMBER) throw new IOException("Invalid magic number: " + magic);

        int methodCode = input.readInt();
        HttpMethod method = HttpMethod.fromCode(methodCode);
        HttpRequest req = new HttpRequest(method);

        switch (method) {
            case PUT -> {
                req.fileName = input.readUTF();
                int length = input.readInt();
                req.data = new byte[length];
                input.readFully(req.data);
            }
            case GET_BY_NAME, DELETE_BY_NAME -> req.fileName = input.readUTF();
            case GET_BY_ID, DELETE_BY_ID -> req.id = input.readInt();
        }

        return req;
    }
}
