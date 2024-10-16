package com.infoview.models;

import java.util.List;

public class Struct extends Data{
    private String structName;
    private List<Data> data;
    private String count;

    public Struct(String name, String type, String usage, String structName,String count){
        super(name, type, usage);
        this.count = count;
        this.structName = structName;
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getStructName() {
        return structName;
    }

    public void setStructName(String structName) {
        this.structName = structName;
    }

    @Override
    public String toString() {
        return "Struct{" +
                "structName='" + structName + '\'' +
                ", data=" + data +
                ", count='" + count + '\'' +
                '}';
    }
}
