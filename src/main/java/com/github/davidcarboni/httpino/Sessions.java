package com.github.davidcarboni.httpino;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class provides a way to access multiple {@link Http} instances.
 * This is useful for using the same {@link Http} in multiple tests,
 * or for having separate instances for users that have different privilege levels.
 * Created by david on 08/04/2015.
 */
public class Sessions {

    /**
     * @see <a href="https://ria101.wordpress.com/2011/12/12/concurrenthashmap-avoid-a-common-misuse/"
     * >https://ria101.wordpress.com/2011/12/12/concurrenthashmap-avoid-a-common-misuse/</a>
     */
    static Map<String, com.github.davidcarboni.httpino.Http> https = java.util.Collections.synchronizedMap(new ConcurrentHashMap<String, com.github.davidcarboni.httpino.Http>(8, 0.9f, 1));


    /**
     * The default {@link HttpFactory} generates {@link Http} instances with Javascript enabled.
     */
    public static HttpFactory httpFactory = new HttpFactory() {
        @Override
        public com.github.davidcarboni.httpino.Http newHttp() {
            return new com.github.davidcarboni.httpino.Http();
        }
    };

    /**
     * If you want to use a different {@link Http} setup, implement this interface and assign it to the {@link #httpFactory} field.
     */
    public interface HttpFactory {
        com.github.davidcarboni.httpino.Http newHttp();
    }

    /**
     * @param name A string to identify a particular http.
     * @return An {@link Http} for the given name, creating it if necessary.
     */
    public static com.github.davidcarboni.httpino.Http get(String name) {
        com.github.davidcarboni.httpino.Http http = https.get(name);
        if (http == null) {
            https.put(name, http = new com.github.davidcarboni.httpino.Http());
        }
        return http;
    }

    /**
     * Convenience method to obtain a default {@link Http}.
     *
     * @return A {@link Http}. The same http will be returned each time.
     */
    public static com.github.davidcarboni.httpino.Http get() {
        return get("DEFAULT");
    }

    public static void quit() {
        for (com.github.davidcarboni.httpino.Http http : https.values()) {
            http.close();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        // Last-chance attempt to ensure everything is cleaned up:
        quit();
    }

}
