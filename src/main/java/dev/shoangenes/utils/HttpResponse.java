package dev.shoangenes.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;

public class HttpResponse {
    private static final int MAGIC_NUMBER = 0x474F4B55;
    private HttpResults status;
    private int id;
    private byte[] binaryContent;
    private boolean hasBinaryContent;
    private long crc32;

    /**
     * Constructor for HttpResponse
     * @param status the HTTP status of the response
     */
    private HttpResponse(HttpResults status) {
        this.status = status;
        this.id = -1;
        this.binaryContent = null;
        this.hasBinaryContent = false;
        this.crc32 = 0;
    }

    /**
     * Static factory methods for common responses
     */
    public static HttpResponse success() {
        return new HttpResponse(HttpResults.SUCCESS);
    }

    /**
     * Creates a success response with an ID
     * @param id the ID to include in the response
     * @return HttpResponse with status SUCCESS and the given ID
     */
    public static HttpResponse successWithId(int id) {
        HttpResponse response = success();
        response.id = id;
        return response;
    }

    /**
     * Creates a success response with binary content
     * @param content the binary content to include in the response
     * @return HttpResponse with status SUCCESS and the given binary content
     */
    public static HttpResponse successWithContent(byte[] content) {
        HttpResponse response = success();
        response.hasBinaryContent = true;
        response.binaryContent = content;
        return response;
    }

    /** Static factory methods for error responses */

    /**
     * Creates a bad request response
     * @return HttpResponse with status BAD_REQUEST
     */
    public static HttpResponse notFound() {
        return new HttpResponse(HttpResults.NOT_FOUND);
    }

    /**
     * Creates a forbidden response
     * @return HttpResponse with status FORBIDDEN
     */
    public static HttpResponse forbidden() {
        return new HttpResponse(HttpResults.FORBIDDEN);
    }

    /**
     * Creates an internal server error response
     * @return HttpResponse with status INTERNAL_SERVER_ERROR
     */
    public static HttpResponse serverError() {
        return new HttpResponse(HttpResults.INTERNAL_SERVER_ERROR);
    }

    /**
     * Builds the text response to be sent over the network
     * @return the text representation of the response
     */
    public String buildTextResponse() {
        String result = status.getCode() + "";
        if (id != -1) {
            result += " " + id;
        }
        return result;
    }

    /** Getters */

    /**
     * Gets the binary content of the response
     * @return the binary content, or null if none exists
     */
    public byte[] getBinaryContent() {
        return binaryContent;
    }

    /**
     * Gets the status code as a string
     * @return the status code
     */
    public int getStatusCode() {
        return status.getCode();
    }

    /**
     * Gets the HTTP status of the response
     * @return the HTTP status
     */
    public HttpResults getStatus() {
        return status;
    }

    /**
     * Checks if the response has binary content
     * @return true if binary content exists, false otherwise
     */
    public boolean hasBinaryContent() {
        return hasBinaryContent;
    }

    /**
     * Computes the CRC32 checksum of the binary content
     */
    private void computeCrc32() {
        if (hasBinaryContent) {
            CRC32 crc32 = new CRC32();
            crc32.update(binaryContent);
            this.crc32 = crc32.getValue();
        }
    }

    /**
     * Writes the response to a DataOutputStream
     * @param output the DataOutputStream to write to
     * @throws IOException if an I/O error occurs
     */
    public void writeTo(DataOutputStream output) throws IOException {
        output.writeInt(MAGIC_NUMBER);
        output.writeInt(getStatusCode());
        output.writeInt(id);

        output.writeBoolean(hasBinaryContent);
        if (hasBinaryContent) {
            computeCrc32();
            output.writeInt(binaryContent.length);
            output.write(binaryContent);
            output.writeLong(crc32);
        }
    }

    /**
     * Reads a response from a DataInputStream
     * @param input the DataInputStream to read from
     * @return the HttpResponse read from the stream
     * @throws IOException if an I/O error occurs or if the magic number is invalid
     */
    public static HttpResponse readFrom(DataInputStream input) throws IOException {
        int magic = input.readInt();
        if (magic != MAGIC_NUMBER) {
            throw new IOException("Invalid magic number: " + magic);
        }

        int statusCode = input.readInt();
        HttpResults status = HttpResults.fromCode(statusCode);
        HttpResponse response = new HttpResponse(status);

        response.id = input.readInt();
        response.hasBinaryContent = input.readBoolean();
        if (response.hasBinaryContent) {
            int contentLength = input.readInt();
            response.binaryContent = new byte[contentLength];
            input.readFully(response.binaryContent);
            response.crc32 = input.readLong();
        }

        if (!response.verifyCrc32()) {
            throw new IOException("CRC32 checksum mismatch");
        }

        return response;
    }

    /**
     * Verifies the CRC32 checksum of the binary content
     * @return true if the checksum matches, false otherwise
     */
    public boolean verifyCrc32() {
        if (!hasBinaryContent) return true;
        CRC32 crc = new CRC32();
        crc.update(binaryContent);
        return crc.getValue() == this.crc32;
    }
}
