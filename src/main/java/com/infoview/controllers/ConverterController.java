package com.infoview.controllers;
import com.infoview.converter.Converter;
import com.infoview.converter.GenerateExampleForRaml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping(value = "/api")
@CrossOrigin(origins = "*")
public class ConverterController {
    @Autowired
    GenerateExampleForRaml generateExampleForRaml;
    @GetMapping("/test")
    public String test(){
        return "TEST";
    }
    @PostMapping("/from-string")
    public String getProgramDefinitionFromString(@RequestBody String xml) {
        return Converter.convertPCMLtoAS400ProgramCallDefinitionFromString(xml);
    }
    @PostMapping("/from-string-to-raml")
    public String getRamlFromString(@RequestBody String xml) {
        return generateExampleForRaml.convertToJsonFile(xml);
    }
}
