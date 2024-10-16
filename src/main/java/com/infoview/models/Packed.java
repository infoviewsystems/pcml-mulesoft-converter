package com.infoview.models;

public class Packed extends Data{
    private String length;
    private String precision;

    public Packed(String name, String type, String usage, String length, String precision) {
        super(name, type, usage);
        this.length = length;
        this.precision = precision;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getPrecision() {
        return precision;
    }

    public void setPrecision(String precision) {
        this.precision = precision;
    }

    @Override
    public String toString() {
        return "Packed{" +
                "name='" + getName() + '\'' +
                ", type='" + getType() + '\'' +
                ", usage='" + getUsage() + '\'' +
                ", length='" + length + '\'' +
                ", precision='" + precision + '\'' +
                '}';
    }
}
