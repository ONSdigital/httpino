package com.github.davidcarboni.httpino;

import org.apache.hc.core5.http.Header;
import org.junit.Test;

import static org.junit.Assert.*;

public class HttpTest {

    @Test
    public void addHeaderStoresHeader() {
        Http http = new Http();

        http.addHeader("X-Test", "123");

        assertEquals(1, http.headers.size());
        Header header = http.headers.get(0);
        assertEquals("X-Test", header.getName());
        assertEquals("123", header.getValue());
        http.close();
    }
}
