package com.github.davidcarboni.httpino;

import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * Represents a host on which we'll want to make API calls.
 */
public class Host {

    URI url;

    public Host(String baseUrl)  {
        try {
            URIBuilder uriBuilder = new URIBuilder(baseUrl);
            url = uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Er, reealy? baseUrl: "+baseUrl);
        }
    }

    @Override
    public String toString() {
        // Gracefully convert the url to a String:
        return Objects.toString(url);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj==this || (obj!=null && this.getClass().isAssignableFrom(obj.getClass()) && obj.toString().equals(toString()));
    }
}
