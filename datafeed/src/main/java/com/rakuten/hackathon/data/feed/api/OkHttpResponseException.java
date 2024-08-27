package com.rakuten.hackathon.data.feed.api;

import okhttp3.Response;

import java.io.IOException;

public class OkHttpResponseException extends IOException {

    private static final long serialVersionUID = 4979059537835555322L;

    private Response response;

    public OkHttpResponseException(Response response) {
        this.response = response;
    }

    public OkHttpResponseException(Response response, String message) {
        super(message);
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }
}
