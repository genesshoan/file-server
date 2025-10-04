package dev.shoangenes.utils;

public enum HttpResults {
    SUCCESS(200, "OK"),
    BAD_REQUEST(400, "Bad Request"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    UNKNOWN(520, "Unknown Error");

    private final int code;
    private final String message;

    // Constructor
    HttpResults(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Get the numerical HTTP status code.
     */
    public int getCode() {
        return code;
    }

    /**
     * Get the descriptive message associated with the HTTP status code.
     */
    public String getMessage() {
        return message;
    }

    public static HttpResults fromCode(int code) {
        for (HttpResults r : values()) {
            if (r.getCode() == code) {
                return r;
            }
        }
        return UNKNOWN;
    }
}
