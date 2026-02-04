package com.github.davidcarboni.httpino;

/**
 * A data record to hold the essentials of an API response.
 * Created by david on 26/03/2015.
 */
public class Response<T> {

    public int statusCode;
    public T body;
    public String reasonPhrase;


    public Response(int statusCode, String reasonPhrase, T body) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.body = body;
    }

    @Override
    public String toString() {
        return statusCode + " " + reasonPhrase + (body==null?"\n[no body]": "\nbody:\n" + body) ;
    }
}
