package com.yhdxhw.sjserver.server;

import android.annotation.SuppressLint;
import android.nfc.Tag;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yhdxhw.sjserver.nanohttpd.fileupload.NanoFileUpload;
import com.yhdxhw.sjserver.nanohttpd.router.RouterNanoHTTPD;
import com.yhdxhw.sjserver.server.model.FileInfo;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.IStatus;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Directory {
    private static final String TAG = "Directory";

    /**
     * 将Long类型的时间戳转换成String 类型的时间格式，时间格式为：yyyy-MM-dd HH:mm:ss
     */
    private static String convertTimeToString(Long time) {
         SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm");
       return simpleDateFormat.format(new Date(time));
    }

    private static String root;

    public static String getRoot() {
        return root;
    }

    public static void setRoot(String root) {
        Directory.root = root;
    }

    public static class dir extends RouterNanoHTTPD.GeneralHandler {

        public String getMimeType() {
            return "application/json";
        }

        @Override
        public Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {

            String path = null;
            try {
                path = Objects.requireNonNull(session.getParameters().get("path")).get(0);
            } catch (Exception e) {
                path = "/";
            }
            try {

                List<FileInfo> files = getFiles(path);
                Gson gson = new GsonBuilder().setDateFormat("MM-dd-yyyy hh:mm").create();
                return Response.newFixedLengthResponse(getStatus(), getMimeType(), gson.toJson(files));
            } catch (IOException e) {
                return Response.newFixedLengthResponse(getStatus(), getMimeType(), new Gson().toJson(new String[]{}));
            }
        }
        private List<FileInfo> getFiles(String path) throws IOException {

             List<FileInfo> infoList=new ArrayList<>();
            for (File file: Objects.requireNonNull(new File(root, path).listFiles())){
                if (file.isHidden()){
                    continue;
                }
                FileInfo fileInfo = new FileInfo();
                fileInfo.setDir(file.isDirectory());
                fileInfo.setName(file.getName());
                fileInfo.setSize(file.length());
                fileInfo.setDate(convertTimeToString(file.lastModified()));
                fileInfo.setAbsolutePath(ClearPath(file.getAbsolutePath()));
                infoList.add(fileInfo);
            }
            return infoList;
        }

        public static String ClearPath(String absolutePath) {

            return absolutePath.replaceAll("D:\\\\", "").replaceAll("\\\\", "/");
        }

        public IStatus getStatus() {
            return Status.OK;
        }
    }

    public static class uploadCheck extends RouterNanoHTTPD.GeneralHandler {

        public String getMimeType() {
            return "application/json";
        }

        @Override
        public Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {

            String path = session.getParameters().get("path").get(0);
            String name = session.getParameters().get("name").get(0);

            File file = new File(new File(root, path), name);
            ServerResponse serverResponse = new ServerResponse();

            if (file.exists()) {
                serverResponse.setStatus(-1);
                serverResponse.setMsg("当前目录已经存在该文件");
            } else {
                serverResponse.setStatus(1);
            }
            return Response.newFixedLengthResponse(Status.OK, "application/json", new GsonBuilder().create().toJson(serverResponse));

        }

        public IStatus getStatus() {
            return Status.OK;
        }
    }

    public static class download extends RouterNanoHTTPD.GeneralHandler {
        public String getMimeType() {
            return "application/octet-stream";
        }

        @Override
        public Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {

            try {
                String path = URLDecoder.decode(session.getParameters().get("path").get(0), "UTF-8");
                File file = new File(root, path);
                FileInputStream data = new FileInputStream(file);
                Response response = Response.newChunkedResponse(getStatus(), getMimeType(), data);

                response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(file.getName(), "UTF-8").replaceAll("\\+", "%20"));
                return response;
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                Response response = Response.newFixedLengthResponse(Status.REDIRECT, "", "");
                response.addHeader("Location", "https://www.baidu.com");
                return response;
            }


        }
    }

    public static class delete extends RouterNanoHTTPD.DefaultHandler {

        public String getMimeType() {
            return "application/json";
        }

        @Override
        public String getText() {
            return null;
        }

        @Override
        public IStatus getStatus() {
            return Status.OK;
        }

        @Override
        public Response delete(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            String path = session.getParameters().get("path").get(0);
            File file = new File(root, path);
            boolean b = FileUtils.deleteQuietly(file);
            ServerResponse src = new ServerResponse(b ? 1 : -1, b ? "删除成功" : "删除失败", "");
            return Response.newFixedLengthResponse(getStatus(), getMimeType(), new Gson().toJson(src));
        }
    }

    public static class rename extends RouterNanoHTTPD.DefaultHandler {

        public String getMimeType() {
            return "application/json";
        }

        @Override
        public String getText() {
            return null;
        }

        @Override
        public IStatus getStatus() {
            return Status.OK;
        }
        @Override
        public Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            String path = session.getParameters().get("path").get(0);
            String name = session.getParameters().get("name").get(0);
            String newName = session.getParameters().get("newName").get(0);
            File file = new File(new File(root, path), name);
            File newFile = new File(new File(root, path), newName);
            boolean renameTo = file.renameTo(newFile);
            ServerResponse serverResponse = new ServerResponse();
            if (renameTo) {
                serverResponse.setStatus(1);
                serverResponse.setMsg("修改成功");
                FileInfo fileInfo = new FileInfo();
                fileInfo.setDir(newFile.isDirectory());
                fileInfo.setName(newFile.getName());
                fileInfo.setSize(newFile.length());
                fileInfo.setDate(convertTimeToString(newFile.lastModified()));
                fileInfo.setAbsolutePath(dir.ClearPath(newFile.getAbsolutePath()));
                serverResponse.setData(fileInfo);
            } else {
                serverResponse.setStatus(-1);
                serverResponse.setMsg("修改失败");
            }
            return Response.newFixedLengthResponse(getStatus(), getMimeType(), new Gson().toJson(serverResponse));

        }
    }

    public static class create extends RouterNanoHTTPD.DefaultHandler {
        @Override
        public String getText() {
            return null;
        }

        @Override
        public String getMimeType() {
            return "application/json";
        }

        @Override
        public IStatus getStatus() {
            return Status.OK;
        }

        @Override
        public Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {

            Map<String, List<String>> parameters = session.getParameters();
            Map<String, String> body = new HashMap<>();

            try {
                session.parseBody(body);
            } catch (IOException | NanoHTTPD.ResponseException e) {
                e.printStackTrace();
            }
            String path = parameters.get("path").get(0);
            String name = parameters.get("name").get(0);
            File file = new File(new File(root, path), name);
            ServerResponse src = new ServerResponse();
            if (file.exists()) {
                src.setMsg("此位置已存在相同名称的目录");
                return Response.newFixedLengthResponse(Status.OK, "application/json", new GsonBuilder().create().toJson(src));
            }
            boolean mkdirs = file.mkdirs();
            if (mkdirs) {
                src.setStatus(1);
                src.setMsg("创建成功");
                FileInfo fileInfo = new FileInfo();
                fileInfo.setDir(file.isDirectory());
                fileInfo.setName(file.getName());
                fileInfo.setSize(file.length());
                fileInfo.setDate(convertTimeToString(file.lastModified()));
                fileInfo.setAbsolutePath(dir.ClearPath(file.getAbsolutePath()));
                src.setData(fileInfo);
            } else {
                src.setStatus(-1);
                src.setMsg("创建失败");
            }

            return Response.newFixedLengthResponse(Status.OK, "application/json", new GsonBuilder().create().toJson(src));
        }
    }

    public static class upload extends RouterNanoHTTPD.DefaultHandler {

        private final NanoFileUpload uploader;

        public upload() {
            uploader = new NanoFileUpload(new DiskFileItemFactory());
        }

        @Override
        public String getMimeType() {
            return null;
        }

        @Override
        public String getText() {
            return null;
        }

        @Override
        public IStatus getStatus() {
            return Status.OK;
        }

        @Override
        public Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {


            if (NanoFileUpload.isMultipartContent(session)) {
                ServerResponse serverResponse = new ServerResponse();

                try {
                    Map<String, List<FileItem>> stringListMap = uploader.parseParameterMap(session);

                    FileItem item = stringListMap.get("file").get(0);
                    try {

                        String path = URLDecoder.decode(session.getHeaders().get("path"), "UTF-8");
                        InputStream inputStream = item.getInputStream();
                        File file = new File(new File(root, path), item.getName());
                        if (file.exists()) {
                            serverResponse.setStatus(-1);
                            serverResponse.setMsg("当前目录已经存在该文件");
                            return Response.newFixedLengthResponse(Status.OK, "application/json", new GsonBuilder().create().toJson(serverResponse));

                        }
                        FileOutputStream output = new FileOutputStream(file);
                        IOUtils.copy(inputStream, output);
                        serverResponse.setStatus(1);
                        serverResponse.setMsg("上传成功");
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.setDir(file.isDirectory());
                        fileInfo.setName(file.getName());
                        fileInfo.setSize(file.length());
                        fileInfo.setDate(convertTimeToString(file.lastModified()));
                        fileInfo.setAbsolutePath(dir.ClearPath(file.getAbsolutePath()));
                        serverResponse.setData(fileInfo);
                        output.close();
                        return Response.newFixedLengthResponse(Status.OK, "application/json", new GsonBuilder().create().toJson(serverResponse));
                    } catch (IOException e) {
                        serverResponse.setStatus(-1);
                        serverResponse.setMsg("上传失败");
                        return Response.newFixedLengthResponse(Status.OK, "application/json", new GsonBuilder().create().toJson(serverResponse));

                    }
                } catch (FileUploadException e) {
                    serverResponse.setStatus(-1);
                    serverResponse.setMsg("上传失败");
                    return Response.newFixedLengthResponse(Status.OK, "application/json", new GsonBuilder().create().toJson(serverResponse));

                }

            }
            return super.post(uriResource, urlParams, session);
        }
    }
}
