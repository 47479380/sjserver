package com.yhdxhw.sjserver.server.Interceptor;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RequestInterceptor extends HandlerInterceptor {

    @Override
    public Response handle(IHTTPSession ihttpSession) {
        ArrayList<String> arrayList = new ArrayList<>();
        Map<String, List<String>> parameters = ihttpSession.getParameters();
          for (String k:parameters.keySet()){
              if (parameters.get(k)==null){

                  parameters.put(k, arrayList);
              }
          }


        return super.handle(ihttpSession);
    }
}
