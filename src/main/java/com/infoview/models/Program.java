package com.infoview.models;

import java.util.List;

public class Program {
    private String name;
    private String path;
    private List<Data> data;

    public Program(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public Program(String name, String path, List<Data> data) {
        this.name = name;
        this.path = path;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Program{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", data=" + data +
                '}';
    }
}
