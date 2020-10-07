package com.yhdxhw.sjserver.server.model;

public class FileInfo {
   private String name;
   private Long size;
   private String date;
   private boolean isDir;
   private String absolutePath;

    public FileInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isDir() {
        return isDir;
    }

    public void setDir(boolean dir) {
        isDir = dir;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }
}
