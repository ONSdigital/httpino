package com.github.davidcarboni.httpino;

import org.junit.Test;

import static org.junit.Assert.*;

public class ResponseTest {

    @Test
    public void exposesStatusAndBody() {
        Response<String> response = new Response<>(200, "OK", "payload");

        assertEquals(200, response.getStatusCode());
        assertEquals("OK", response.getReasonPhrase());
        assertEquals("payload", response.getBody());
    }

    @Test
    public void toStringHandlesNullBody() {
        Response<String> response = new Response<>(204, "No Content", null);

        assertTrue(response.toString().contains("204 No Content"));
        assertTrue(response.toString().contains("[no body]"));
    }
}
