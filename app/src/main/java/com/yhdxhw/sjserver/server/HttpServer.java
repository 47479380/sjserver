package com.yhdxhw.sjserver.server;

import com.yhdxhw.sjserver.nanohttpd.router.RouterNanoHTTPD;
import com.yhdxhw.sjserver.server.Interceptor.CORSInterceptor;
import com.yhdxhw.sjserver.server.Interceptor.HandlerInterceptor;
import com.yhdxhw.sjserver.server.Interceptor.RequestInterceptor;


import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.IStatus;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;
import org.nanohttpd.util.IHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class HttpServer extends RouterNanoHTTPD {


    protected static Map<String,Class<?>> httpHandlers=new HashMap<>();
    private static final String TAG = "HttpServer";
    public static String staticPath;
    public String rootPath;
    private String hostname="";
    private int port=8080;

    public HttpServer( int port) {
        this("0.0.0.0",port);
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public HttpServer( String hostname,int port) {
        super(hostname, port);
        addMappings();
        this.addHTTPInterceptor(new CORSInterceptor());
        this.addHTTPInterceptor(new RequestInterceptor());
    }

    public HttpServer() {
    this(8090);

    }


    @Override
    public void addMappings() {
//        super.addMappings();
        addRoute("/list",Directory.dir.class,rootPath);
        addRoute("/download",Directory.download.class);
        addRoute("/delete",Directory.delete.class);
        addRoute("/create",Directory.create.class);
        addRoute("/upload",Directory.upload.class);
        addRoute("/uploadcheck",Directory.uploadCheck.class);
        addRoute("/rename",Directory.rename.class);


       this.setNotFoundHandler(NotFoundHandler.class);

    }

    @Override
    public void addRoute(String url, Class<?> handler, Object... initParameter) {
        httpHandlers.put(url,handler);
        super.addRoute(url, handler, initParameter);
    }

    public   static class NotFoundHandler extends DefaultHandler {

        public String getText() {
            return "<html><body><h3>Error 404: the requested page doesn't exist.</h3></body></html>";
        }

        @Override
        public String getMimeType() {
            return "text/html";
        }

        @Override
        public IStatus getStatus() {
            return Status.NOT_FOUND;
        }

        @Override
        public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            String uri = session.getUri();
//            if (httpHandlers.containsKey(uri)){
//                return super.get(uriResource, urlParams, session);
//            }
            File file = new File(staticPath, uri);
            if (uri.equals("/")||!file.exists()){
                file=
                        new File(staticPath, "index.html");
            }


                try {

                    String fileName = file.getName();
                    return Response.newChunkedResponse(Status.OK,mimeTypes().get(fileName.substring(fileName.lastIndexOf(".")+1)),new FileInputStream(file));
                } catch (FileNotFoundException e) {
                    return super.get(uriResource, urlParams, session);
                }
        }
    }
    @Override
    public Response handle(IHTTPSession session) {

        Response response = super.handle(session);
        for (IHandler iHandler:this.interceptors){
            if (iHandler instanceof HandlerInterceptor){
              response=  ((HandlerInterceptor) iHandler).after(session,response);
            }
        }
        return response;
    }
}
