package com.github.davidcarboni.httpino;

import org.junit.Test;

import static org.junit.Assert.*;

public class HostTest {

    @Test
    public void toStringUsesOriginalUrl() {
        Host host = new Host("http://localhost:8080/api/");
        assertEquals("http://localhost:8080/api/", host.toString());
    }

    @Test
    public void equalsAndHashCodeMatchSameUrl() {
        Host first = new Host("http://example.com/service/");
        Host second = new Host("http://example.com/service/");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
