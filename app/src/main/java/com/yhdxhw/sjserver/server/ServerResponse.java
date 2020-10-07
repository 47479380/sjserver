package com.yhdxhw.sjserver.server;

public class ServerResponse {
//    1是成功
//    -1是失败
    private int status;
    private String msg;
    private Object data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public ServerResponse() {
        this.status=0;
    }

    public ServerResponse(int status, String msg, Object data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    public ServerResponse(int status, String msg){
        this.status=status;
        this.msg=msg;
    }
    public ServerResponse(String msg, Object data) {
        this.msg = msg;
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
