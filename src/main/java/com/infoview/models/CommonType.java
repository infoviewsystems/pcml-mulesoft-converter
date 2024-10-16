package com.infoview.models;

public class CommonType extends Data{
    private String length;

    public CommonType(String name, String type, String usage, String length) {
        super(name, type, usage);
        this.length = length;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    @Override
    public String toString() {
        return "Char{" +
                "name='" + getName() + '\'' +
                ", type='" + getType() + '\'' +
                ", usage='" + getUsage() + '\'' +
                ", length='" + length + '\'' +
                '}';
    }
}
