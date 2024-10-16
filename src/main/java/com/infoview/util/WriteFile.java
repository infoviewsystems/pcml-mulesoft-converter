package com.infoview.util;

import org.json.JSONObject;
import org.springframework.stereotype.Component;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Component
public class WriteFile {

    public void writingOutputToFile(Map exampleMapInternalForRequest, Map exampleMapInternalForResponse){
        FileWriter fInputWriter = null;
        FileWriter fOutputWriter = null;
        try{
           /* Path path =Paths.get(getClass().getResource("/").getPath()).toAbsolutePath();
            System.out.println(path);*/

            fInputWriter = new FileWriter(
                    "as400-input-params.json");
               //     "C:\\Users\\isipl\\projects\\PCML-Converter-Project\\pcml-converter\\src\\main\\resources\\as400-input-params.json");
            fInputWriter.write(String.valueOf(new JSONObject(exampleMapInternalForRequest)));
            fInputWriter.close();

            fOutputWriter = new FileWriter(
                    "as400-output-params.json");
                    //"C:\\Users\\isipl\\projects\\PCML-Converter-Project\\pcml-converter\\src\\main\\resources\\as400-output-params.json");
            fOutputWriter.write(String.valueOf(new JSONObject(exampleMapInternalForResponse)));
            fOutputWriter.close();
        } catch (IOException e) {
        e.printStackTrace();
          }
    }
}
