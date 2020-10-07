package com.yhdxhw.sjserver.server.Interceptor;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;

import java.util.Map;

import static org.nanohttpd.protocols.http.response.Response.newFixedLengthResponse;

public class CORSInterceptor extends HandlerInterceptor {


    // explicitly relax visibility to package for tests purposes
    public final static String DEFAULT_ALLOWED_HEADERS = "origin,accept,content-type,cache-control,x-requested-with,path";

    public final static String ACCESS_CONTROL_ALLOW_HEADER_PROPERTY_NAME = "AccessControlAllowHeader";
    private final static String ALLOWED_METHODS = "GET, POST, PUT, DELETE, OPTIONS, HEAD";

    private final static int MAX_AGE = 42 * 60 * 60;

    private String calculateAllowHeaders(Map<String, String> queryHeaders) {
        // here we should use the given asked headers
        // but NanoHttpd uses a Map whereas it is possible for requester to send
        // several time the same header
        // let's just use default values for this version
        return System.getProperty(ACCESS_CONTROL_ALLOW_HEADER_PROPERTY_NAME, DEFAULT_ALLOWED_HEADERS);
    }
    protected Response addCORSHeaders(Map<String, String> queryHeaders, Response resp, String cors) {
        resp.addHeader("Access-Control-Allow-Origin", cors);
        resp.addHeader("Access-Control-Allow-Headers", calculateAllowHeaders(queryHeaders));
        resp.addHeader("Access-Control-Allow-Credentials", "true");
        resp.addHeader("Access-Control-Allow-Methods", ALLOWED_METHODS);
        resp.addHeader("Access-Control-Max-Age", "" + MAX_AGE);

        return resp;
    }
    @Override
    public Response after(IHTTPSession session,Response response) {

        return addCORSHeaders(session.getHeaders(), response, "*");
    }

}
