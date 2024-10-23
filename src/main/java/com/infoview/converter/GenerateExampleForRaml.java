package com.infoview.converter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class GenerateExampleForRaml {
    private static String programName;
    private static final String PARAMETER_VALUE="parmValue";
    private static final String STRUCTURE="STRUCTURE";
    private static final String PARAMETER_NAME="parameterName";
    private static final String AS400_PARAMETER="as400:parameter";
    private static final String PROGRAM_CALL_PROCESSOR_KEY="as400:program-call-processor";
    private static final String PROGRAM_NAME_AS_KAY="programName";
    private static final String AS400_PARAMETERS="as400:parameters";
    @Autowired
    RamlWriter ramlWriter;
    public  String convertToJsonFile(String convertPCMLtoAS400ProgramCallDefinitionFromString) {

        String s= convertPCMLtoAS400ProgramCallDefinitionFromString ;
        String s1 =s.substring(s.indexOf("<as400:program-call-processor"));
        s1.trim();
        System.out.println(s1);

        JSONObject xmlToJson = XML.toJSONObject(s1);
        programName  = (String) xmlToJson.getJSONObject(PROGRAM_CALL_PROCESSOR_KEY).get(PROGRAM_NAME_AS_KAY);
        JSONArray getActualJSONToParse= xmlToJson.getJSONObject(PROGRAM_CALL_PROCESSOR_KEY).getJSONObject(AS400_PARAMETERS).getJSONArray(AS400_PARAMETER);
        JSONArray jsonArray = new JSONArray(getActualJSONToParse.toString());
        return generateExampleForRaml(jsonArray);
    }

    private String generateExampleForRaml(JSONArray jsonArray) {
        HashMap<String, Object> results = new HashMap<>();
        HashMap<String, Object> resultResponse = new HashMap<>();
        List<String> matches = new ArrayList<String>();
        Matcher matcher = Pattern.compile("(?:\"dataType\":\"STRUCTURE\".*?\"parameterName\":\")(.*?)(?:\")").matcher(jsonArray.toString());
        int i = 0;
        while (matcher.find()) {
            for (int j = 0; j <= matcher.groupCount(); j++) {
                if(!matcher.group(j).contains("\"")) matches.add(matcher.group(j));
                i++;
            }
        }
        jsonArray.forEach(item -> {
            if(!item.toString().contains(STRUCTURE)){
                Matcher matcherItemName = Pattern.compile("(?:parameterName\":\")(.*?)(?:\")").matcher(item.toString());
                Matcher matcherItemValue = Pattern.compile("(?:parmValue\":\")(.*?)(?:\")").matcher(item.toString());
                Matcher matcherItemType = Pattern.compile("(?:dataType\":\")(.*?)(?:\")").matcher(item.toString());
                Matcher matcherItemUsage = Pattern.compile("(?:usage\":\")(.*?)(?:\")").matcher(item.toString());
                matcherItemName.find();
                matcherItemValue.find();
                matcherItemType.find();
                matcherItemUsage.find();
                try {
                    Object itemValue = generateParmValue(matcherItemValue.group(1).replace("#['", "").replace("']", ""), matcherItemType.group(1));
                    if(matcherItemUsage.group(1).equals("IN")) {
                        results.put(matcherItemName.group(1), itemValue);
                    } else if(matcherItemUsage.group(1).equals("OUT")){
                        resultResponse.put(matcherItemName.group(1), itemValue);
                    } else if(matcherItemUsage.group(1).equals("INOUT")){
                        results.put(matcherItemName.group(1), itemValue);
                        resultResponse.put(matcherItemName.group(1), itemValue);
                    }
                } catch (IllegalStateException ie){
                    System.out.println("Exception appeared: " + ie);
                    if(matcherItemUsage.group(1).equals("OUT")){
                        resultResponse.put(matcherItemName.group(1), generateParmValue(null, matcherItemType.group(1)));
                    }
                }
            } else {
                matches.forEach(nestedItem -> {
                    boolean errorFlag = false;
                    String matcherObjName = "(?:parameterName\":\"" + nestedItem + "\".*?)(\\[\\{.*?}\\])";
                    Matcher matcherItemName = Pattern.compile(matcherObjName).matcher(jsonArray.toString());
                    matcherItemName.find();
                    String matcherItemNameResult = matcherItemName.group(1);
                    String matcherObjDesc = "(.*?)(?:,\"as400:data-structure-elements\":\\{\"as400:parameter\":)";
                    Matcher matcherNestedRes = Pattern.compile(matcherObjDesc).matcher(matcherItemNameResult);
                    matcherNestedRes.find();
                    String nestedStr = null;
                    HashMap<String, Object> finalNestedHMap = new HashMap<>();
                    HashMap<String, Object> finalResponseNestedHMap = new HashMap<>();
                    try {
                        if (matcherNestedRes.group(1) != null) nestedStr = matcherNestedRes.group(1) + "}]";
                    } catch (Exception e) {
                        errorFlag = true;
                        System.out.println("Exception appeared: " + e);
                    } finally {
                        if(errorFlag){
                            nestedStr = matcherItemNameResult;
                        } else {
                            nestedStr = nestedStr;
                        }
                    }
                    new JSONArray(nestedStr).forEach(nestedJsonItem -> {
                        if(!((JSONObject) nestedJsonItem).get("dataType").equals(STRUCTURE)){
                            try {
                                if(((JSONObject) nestedJsonItem).get("usage").equals("IN")) {
                                    Object nestedItemValue = generateParmValueForNestedElements(
                                            ((JSONObject) nestedJsonItem).get("dataType").toString());
                                    finalNestedHMap.put(((JSONObject) nestedJsonItem).get(PARAMETER_NAME).toString(), nestedItemValue);
                                } else if(((JSONObject) nestedJsonItem).get("usage").equals("OUT")) {
                                    Object nestedItemValue = generateParmValueForNestedElements(
                                            ((JSONObject) nestedJsonItem).get("dataType").toString());
                                   finalResponseNestedHMap.put(((JSONObject) nestedJsonItem).get(PARAMETER_NAME).toString(), nestedItemValue);
                                } else if(((JSONObject) nestedJsonItem).get("usage").equals("INOUT")) {
                                    Object nestedItemValue = generateParmValueForNestedElements(
                                            ((JSONObject) nestedJsonItem).get("dataType").toString());
                                    finalNestedHMap.put(((JSONObject) nestedJsonItem).get(PARAMETER_NAME).toString(), nestedItemValue);
                                   finalResponseNestedHMap.put(((JSONObject) nestedJsonItem).get(PARAMETER_NAME).toString(), nestedItemValue);
                                }
                            } catch (IllegalStateException ie){
                                System.out.println("Exception appeared: " + ie);
                                if(((JSONObject) nestedJsonItem).get("usage").equals("OUT")){
                                    finalResponseNestedHMap.put(matcherItemName.group(1), generateParmValue(null, ((JSONObject) nestedJsonItem).get("dataType").toString()));
                                }
                            }
                        } else {
                            if(((JSONObject) nestedJsonItem).get("usage").equals("IN")) {
                                finalNestedHMap.put(((JSONObject) nestedJsonItem).get(PARAMETER_NAME).toString(), null);
                            } else if(((JSONObject) nestedJsonItem).get("usage").equals("OUT")){
                                finalResponseNestedHMap.put(((JSONObject) nestedJsonItem).get(PARAMETER_NAME).toString(), null);
                            } else if(((JSONObject) nestedJsonItem).get("usage").equals("INOUT")){
                                finalNestedHMap.put(((JSONObject) nestedJsonItem).get(PARAMETER_NAME).toString(), null);
                                finalResponseNestedHMap.put(((JSONObject) nestedJsonItem).get(PARAMETER_NAME).toString(), null);
                            }
                        }
                    });
                    results.put(nestedItem, finalNestedHMap);
                    resultResponse.put(nestedItem, finalResponseNestedHMap);
                });
                List<String> objToRemove = new ArrayList<>();
                List<String> objToRemoveResponse = new ArrayList<>();
                results.entrySet().forEach(res -> {
                    if(res.getValue().getClass().equals(HashMap.class)){
                        Map<String, Object> nm = (Map<String, Object>) res.getValue();
                        nm.entrySet().stream().forEach(nmItem -> {
                            if(nmItem.getValue() == null){
                                nm.put(nmItem.getKey(), results.get(nmItem.getKey()));
                                objToRemove.add(nmItem.getKey());
                            }
                        });
                    }
                });
                objToRemove.forEach(objRem -> {
                    results.remove(objRem);
                });
                resultResponse.entrySet().forEach(res -> {
                    if(res.getValue().getClass().equals(HashMap.class)){
                        Map<String, Object> nm = (Map<String, Object>) res.getValue();
                        nm.entrySet().stream().forEach(nmItem -> {
                            if(nmItem.getValue() == null){
                                nm.put(nmItem.getKey(), resultResponse.get(nmItem.getKey()));
                                objToRemoveResponse.add(nmItem.getKey());
                            }
                        });
                    }
                });
                objToRemoveResponse.forEach(objRem -> {
                    resultResponse.remove(objRem);
                });
            }
        });
        return ramlWriter.test(programName,results,resultResponse);
    }
    private Object generateParmValueForNestedElements(String dataType) {
        Object formattedValue = null;
        DecimalFormat df;
        switch(dataType){
            case "STRING":
                formattedValue=String.valueOf("XXXXXXXXXX");
                break;
            case "PACKED":
                formattedValue = Double.valueOf("0.00");
                break;
            case "INTEGER":
            case "BYTE":
                formattedValue = 0;
                break;
            case "FLOAT":
                formattedValue = Double.valueOf("0.0");
                break;
            case "ZONED":
                formattedValue = Double.valueOf("0.000");
                break;
            case "DATE":
                formattedValue = new Date();
                break;
            case "TIMESTAMP":
                formattedValue = new Timer();
                break;
            case "TIME":
                formattedValue = new Date().getTime();
                break;
            default:
                break;
        }
        return formattedValue;
    }
    private Object generateParmValue(String itemValue, String dataType) {
        Object formattedValue = null;
        DecimalFormat df;
        switch(dataType){
            case "STRING":
                if(itemValue != null) {
                    formattedValue = itemValue;
                } else {
                    formattedValue = "";
                }
                break;
            case "PACKED":
                if(itemValue != null) {
                    df = new DecimalFormat("#.##");
                    df.setRoundingMode(RoundingMode.FLOOR);
                    formattedValue = Double.valueOf(df.format(Double.valueOf(itemValue)).replace(",", "."));
                } else {
                    formattedValue = Double.valueOf("0.00");
                }
                break;
            case "INTEGER":
            case "BYTE":
                if(itemValue != null) {
                    formattedValue = Integer.valueOf(itemValue);
                } else {
                    formattedValue = 0;
                }
                break;
            case "FLOAT":
                if(itemValue != null) {
                    df = new DecimalFormat("#.#");
                    formattedValue = Double.valueOf(df.format(Double.valueOf(itemValue)).replace(",", "."));
                } else {
                    formattedValue = Double.valueOf("0.0");
                }
                break;
            case "ZONED":
                if(itemValue != null) {
                    df = new DecimalFormat("#.###");
                    formattedValue = Double.valueOf(df.format(Double.valueOf(itemValue)).replace(",", "."));
                } else {
                    formattedValue = Double.valueOf("0.000");
                }
                break;
            case "DATE":
                formattedValue = new Date();
                break;
            case "TIMESTAMP":
                formattedValue = new Timer();
                break;
            case "TIME":
                formattedValue = new Date().getTime();
                break;
            default:
                break;
        }
        return formattedValue;
    }
}

