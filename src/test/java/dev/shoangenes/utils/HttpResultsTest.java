package dev.shoangenes.utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class HttpResultsTest {
    static HttpResults success;
    static HttpResults badRequest;
    static HttpResults forbidden;
    static HttpResults notFound;
    static HttpResults internalServerError;

    /**
     * Setup method to initialize HttpResults enum constants before all tests.
     */
    @BeforeAll
    public static void setup() {
        success = HttpResults.SUCCESS;
        badRequest = HttpResults.BAD_REQUEST;
        forbidden = HttpResults.FORBIDDEN;
        notFound = HttpResults.NOT_FOUND;
        internalServerError = HttpResults.INTERNAL_SERVER_ERROR;
    }

    /**
     * Test that the getStatusCode method returns the correct numerical code for each HttpResults enum constant.
     */
    @Test
    public void testGetStatusCode() {
        assertThat(success.getCode()).isEqualTo(200);
        assertThat(badRequest.getCode()).isEqualTo(400);
        assertThat(forbidden.getCode()).isEqualTo(403);
        assertThat(notFound.getCode()).isEqualTo(404);
        assertThat(internalServerError.getCode()).isEqualTo(500);
    }

    /**
     * Test that the getCode method returns the correct HttpResults enum constant for a valid status code.
     */
    @Test
    public void testFromStatusCodeValidCodes() {
        assertThat(HttpResults.fromCode(200)).isEqualTo(HttpResults.SUCCESS);
        assertThat(HttpResults.fromCode(400)).isEqualTo(HttpResults.BAD_REQUEST);
        assertThat(HttpResults.fromCode(403)).isEqualTo(HttpResults.FORBIDDEN);
        assertThat(HttpResults.fromCode(404)).isEqualTo(HttpResults.NOT_FOUND);
        assertThat(HttpResults.fromCode(500)).isEqualTo(HttpResults.INTERNAL_SERVER_ERROR);
    }

    /**
     * Test that the getCode method throws an IllegalArgumentException for an invalid status code.
     */
    @Test
    public void testFromStatusCodeInvalidCode() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> HttpResults.fromCode(999))
            .withMessage("No matching HttpResults for code: 999");
    }
}
