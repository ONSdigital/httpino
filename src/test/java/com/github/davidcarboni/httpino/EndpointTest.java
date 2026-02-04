package com.github.davidcarboni.httpino;

import org.junit.Test;

import static org.junit.Assert.*;

public class EndpointTest {

    @Test
    public void buildsUrlFromHostAndPath() {
        Endpoint endpoint = new Endpoint("http://localhost:8080/api/", "/users");
        assertEquals("http://localhost:8080/api/users", endpoint.url().toString());
    }

    @Test
    public void addsPathSegmentsAndParametersWithoutMutatingOriginal() {
        Endpoint base = new Endpoint("http://localhost:8080/api/", "/users");
        Endpoint configured = base.addPathSegment("42").setParameter("active", true);

        assertEquals("http://localhost:8080/api/users", base.url().toString());
        assertEquals("http://localhost:8080/api/users/42?active=true", configured.url().toString());
    }

    @Test
    public void equalsUsesUrlString() {
        Endpoint first = new Endpoint("http://localhost:8080/api/", "/users");
        Endpoint second = new Endpoint(new Host("http://localhost:8080/api/"), "/users");

        assertEquals(first, second);
    }
}
