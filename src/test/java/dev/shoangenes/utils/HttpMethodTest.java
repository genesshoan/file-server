package dev.shoangenes.utils;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class HttpMethodTest {
    /**
     * Test that the getCode method returns the correct numerical code for each HttpMethod enum constant.
     */
    @Test
    public void testFromCodeValidCodes() {
        assertThat(HttpMethod.fromCode(1)).isEqualTo(HttpMethod.PUT);
        assertThat(HttpMethod.fromCode(2)).isEqualTo(HttpMethod.GET_BY_ID);
        assertThat(HttpMethod.fromCode(3)).isEqualTo(HttpMethod.GET_BY_NAME);
        assertThat(HttpMethod.fromCode(4)).isEqualTo(HttpMethod.DELETE_BY_ID);
        assertThat(HttpMethod.fromCode(5)).isEqualTo(HttpMethod.DELETE_BY_NAME);
    }

    /**
     * Test that the fromCode method throws an IllegalArgumentException for an invalid code.
     */
    @Test
    public void testFromCodeInvalidCode() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> HttpMethod.fromCode(999))
                .withMessage("Unknown method code: 999");
    }
}
