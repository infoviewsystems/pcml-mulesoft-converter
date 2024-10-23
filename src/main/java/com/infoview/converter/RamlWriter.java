package com.infoview.converter;
import org.json.JSONObject;
import org.raml.emitter.RamlEmitter;
import org.raml.model.*;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class RamlWriter {
    Raml raml = new Raml();
    private final RamlEmitter ramlEmitter = new RamlEmitter();
    public String test(String programName, Map<String,Object> exampleMapInternalForRequest, Map<String,Object> exampleMapInternalForResponse){
        JSONObject exampleJsonInternalForRequest = new JSONObject(exampleMapInternalForRequest);
        JSONObject exampleJsonInternalForResponse = new JSONObject(exampleMapInternalForResponse);
        String str= " API";
        raml.setTitle(programName.concat(str));
        raml.setVersion("1.0");
        Resource resource= new Resource();
        Action action = new Action();
        Response response =new Response();
        MimeType mimeTypeForRequest=new MimeType();
        MimeType mimeTypeForResponse=new MimeType();

        action.setDescription("This is for AS400 program call API" );

        Map<String, Resource> mapToSetResource = new LinkedHashMap<>();
        Map<ActionType,Action> mapToSetAction = new LinkedHashMap<>();
        mapToSetAction.put(ActionType.POST,new Action());
        resource.setActions(mapToSetAction);
        mapToSetResource.put("/".concat(programName), resource);
        raml.setResources(mapToSetResource);

        Map<String,MimeType> mapToSetResMimeType = new LinkedHashMap<>();
        if(!exampleMapInternalForRequest.isEmpty()){
            mimeTypeForResponse.setExample(exampleJsonInternalForRequest.toString());
        }else{
            mimeTypeForResponse.setExample("{message: no input params}");
        }
        mapToSetResMimeType.put("application/json",mimeTypeForRequest);
        response.setBody(mapToSetResMimeType);

        Map<String,MimeType> mapToSetReqMimeType = new LinkedHashMap<>();
        if(!exampleMapInternalForResponse.isEmpty()){
            mimeTypeForRequest.setExample(exampleJsonInternalForResponse.toString());
        }else{
            mimeTypeForRequest.setExample("{message: no output params}");
        }
        mapToSetReqMimeType.put("application/json",mimeTypeForResponse);
        action.setBody(mapToSetReqMimeType);

        Map<String,Response> mapToSetResponse = new LinkedHashMap<>();
        mapToSetResponse.put("200",response);
        action.setResponses(mapToSetResponse);
        mapToSetAction.put(ActionType.POST,action);
        resource.setActions(mapToSetAction);
        mapToSetResource.put("/".concat(programName), resource);
        raml.setResources(mapToSetResource);
        return ramlEmitter.dump(raml);
    }
    public Raml getRaml() {
        return raml;
    }

    public void setRaml(Raml raml) {
        this.raml = raml;
    }
}
