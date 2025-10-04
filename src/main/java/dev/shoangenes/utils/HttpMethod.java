package dev.shoangenes.utils;

public enum HttpMethod {
    // --- Enum Values ---

    PUT(1),
    GET_BY_ID(2),
    GET_BY_NAME(3),
    DELETE_BY_ID(4),
    DELETE_BY_NAME(5);

    // --- Fields ---

    private final int code;

    // --- Constructor ---

    HttpMethod(int code) { this.code = code; }

    // --- Getters ---

    /**
     * Get the numerical code associated with the HTTP method.
     */
    public int getCode() { return code; }

    // --- Static Methods ---

    /**
     * Get the HttpMethod enum constant corresponding to the given code.
     *
     * @param code The numerical code of the HTTP method.
     * @return The corresponding HttpMethod enum constant.
     * @throws IllegalArgumentException if the code does not correspond to any HttpMethod.
     */
    public static HttpMethod fromCode(int code) {
        for (HttpMethod m : values()) {
            if (m.code == code) return m;
        }
        throw new IllegalArgumentException("Unknown method code: " + code);
    }
}