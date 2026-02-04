package com.github.davidcarboni.httpino;

import org.junit.Test;

import static org.junit.Assert.*;

public class SessionsTest {

    @Test
    public void getReturnsSameInstanceForName() {
        Http first = Sessions.get("A");
        Http second = Sessions.get("A");

        assertSame(first, second);
    }

    @Test
    public void getReturnsDefaultInstance() {
        Http first = Sessions.get();
        Http second = Sessions.get();

        assertSame(first, second);
    }
}
