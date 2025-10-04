package dev.shoangenes.utils;

public class HttpResponse {
    private int statusCode;
    private int id;
    private byte[] binaryContent;
    private boolean hasBinaryContent;

    /**
     * Private constructor to enforce the use of static factory methods.
     */
    private HttpResponse() {
        statusCode = 0;
        id = -1;
        binaryContent = null;
        hasBinaryContent = false;
    }

    /**
     * Creates a success response with status code 200.
     * @return HttpResponse instance representing a successful operation.
     */
    public static HttpResponse success() {
        HttpResponse response = new HttpResponse();
        response.statusCode = 200;

        return response;
    }

    /**
     * Creates a success response with status code 200 and an associated ID.
     * @param id The ID to include in the response.
     * @return HttpResponse instance representing a successful operation with an ID.
     */
    public static HttpResponse successWithId(int id) {
        HttpResponse response = success();
        response.id = id;
        return response;
    }

    /**
     * Creates a success response with status code 200 and binary content.
     * @param content The binary content to include in the response.
     * @return HttpResponse instance representing a successful operation with binary content.
     */
    public static HttpResponse successWithContent(byte[] content) {
        HttpResponse response = success();
        response.hasBinaryContent = true;
        response.binaryContent = content;
        return response;
    }

    /**
     * Creates a not found response with status code 404.
     * @return HttpResponse instance representing a not found error.
     */
    public static HttpResponse notFound() {
        HttpResponse response = new HttpResponse();
        response.statusCode = 404;

        return response;
    }

    /**
     * Creates a forbidden response with status code 403.
     * @return HttpResponse instance representing a forbidden error.
     */
    public static HttpResponse forbidden() {
        HttpResponse response = new HttpResponse();
        response.statusCode = 403;

        return response;
    }

    /**
     * Creates a server error response with status code 500.
     * @return HttpResponse instance representing a server error.
     */
    public static HttpResponse serverError() {
        HttpResponse response = new HttpResponse();
        response.statusCode = 500;

        return response;
    }

    /**
     * Builds a text representation of the response.
     * @return A string containing the status code and, if applicable, the ID.
     */
    public String buildTextResponse() {
        String result = statusCode + "";
        if (id != -1) {
            result += " " + id;
        }

        return result;
    }

    /**
     * Gets the binary content of the response.
     * @return A byte array containing the binary content, or null if there is none.
     */
    public byte[] getBinaryContent() {
        return binaryContent;
    }

    /**
     * Gets the status code of the response as a string.
     * @return A string representation of the status code.
     */
    public String getStatusCode() {
        return String.valueOf(statusCode);
    }

    /**
     * Indicates whether the response contains binary content.
     * @return true if the response has binary content, false otherwise.
     */
    public boolean hasBinaryContent() {
        return hasBinaryContent;
    }
}
