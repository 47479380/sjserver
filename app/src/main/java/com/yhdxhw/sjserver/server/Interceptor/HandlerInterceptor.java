package com.yhdxhw.sjserver.server.Interceptor;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.util.IHandler;

public abstract   class HandlerInterceptor implements IHandler<IHTTPSession, Response> {


    public  Response after(IHTTPSession session,Response response){

        return response;
    }

    @Override
    public Response handle(IHTTPSession ihttpSession) {
        return null;
    }

}
