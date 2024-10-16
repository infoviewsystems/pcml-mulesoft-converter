package com.infoview.models;

public class Data {
    private String name;
    private AS400Types type;
    private AS400Usage usage;

    public Data(String name, String type, String usage) {
        this.name = name;
        this.type = toAS400Type(type);
        this.usage = toAS400usageT(usage);
    }

    public AS400Usage toAS400usageT(String PCMLUsage){
        AS400Usage AS400usage;
        switch (PCMLUsage){
            case "input":
                AS400usage = AS400Usage.IN;
                break;
            case "output":
                AS400usage = AS400Usage.OUT;
                break;
            case "inputoutput":
            case "inherit":
                AS400usage = AS400Usage.INOUT;
                break;
            default:
                AS400usage = AS400Usage.CHANGE_USAGE;
                break;
        }
        return AS400usage;
    }


    public static AS400Types toAS400Type(String PCMLType){
        AS400Types AS400type;
        switch (PCMLType){
            case "char":
                AS400type = AS400Types.STRING;
                break;
            case "int":
                AS400type = AS400Types.INTEGER;
                break;
            case "packed":
                AS400type = AS400Types.PACKED;
                break;
            case "zoned":
                AS400type = AS400Types.ZONED;
                break;
            case "float":
                AS400type = AS400Types.FLOAT;
                break;
            case "byte":
                AS400type = AS400Types.BYTE;
                break;
            case "struct":
                AS400type = AS400Types.STRUCTURE;
                break;
            case "date":
                AS400type = AS400Types.DATE;
                break;
            case "time":
                AS400type = AS400Types.TIME;
                break;
            case "timestamp":
                AS400type = AS400Types.TIMESTAMP;
                break;
            default:
                AS400type = AS400Types.CHANGE_TYPE;
                break;
        }
        return AS400type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AS400Types getType() {
        return type;
    }

    public void setType(AS400Types type) {
        this.type = type;
    }

    public AS400Usage getUsage() {
        return usage;
    }

    public void setUsage(AS400Usage usage) {
        this.usage = usage;
    }

    @Override
    public String toString() {
        return "Data{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", usage='" + usage + '\'' +
                '}';
    }
}
