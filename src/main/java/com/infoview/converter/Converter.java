package com.infoview.converter;

import com.infoview.models.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Converter {

    private static final String PARAMETER_NAME_ATTR = "parameterName";
    private static final String PARAMETER_TYPE_ATTR = "dataType";
    private static final String PARAMETER_USAGE_ATTR = "usage";
    private static final String PARAMETER_LENGTH_ATTR = "length";
    private static final String PARAMETER_COUNT_ATTR = "count";
    private static final String PARAMETER_VALUE_ATTR = "parmValue";
    private static final String PARAMETER_DECIMAL_POSITION_ATTR = "decimalPositions";
    private static final String PROGRAM_LIBRARY_ATTR = "programLibrary";
    private static final String PROGRAM_NAME_ATTR = "programName";
    static String structName;

    public static Program getProgram(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element eElement = (Element) node;
            List<Data> parameters = getProgramParameters(node);
            return new Program(eElement.getAttribute("name"), eElement.getAttribute("path"), parameters);
        }
        return null;
    }

    public static String convertPCMLtoAS400ProgramCallDefinitionFromString(String xml) {
        try {
            InputStream is = new ByteArrayInputStream(xml.getBytes());
            DocumentBuilderFactory dbFactory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("struct");
            Node nProgram = doc.getElementsByTagName("program").item(0);

            Program program = Converter.getProgram(nProgram);
            Converter.initStructElements(nList, program);

            return Converter.createMuleProgramCallDefinition(program);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String convertPCMLtoAS400ProgramCallDefinitionFromFile(String path) {
        try {
            File inputFile = new File(path);
            DocumentBuilderFactory dbFactory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("struct");
            Node nProgram = doc.getElementsByTagName("program").item(0);

            Program program = Converter.getProgram(nProgram);
            Converter.initStructElements(nList, program);

            return Converter.createMuleProgramCallDefinition(program);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static List<Data> getProgramParameters(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element eElement = (Element) node;
            NodeList nodes = eElement.getElementsByTagName("data");
            List<Data> parameters = new ArrayList<>();
            for (int temp = 0; temp < nodes.getLength(); temp++) {
                Node nNode = nodes.item(temp);
                Element element = (Element) nNode;
                parameters.add(getParameter(element));
            }
            return parameters;
        }
        return Collections.emptyList();
    }

    private static Data getParameter(Element element) {
        Data data;
        String name = element.getAttribute("name");
        String usage = element.getAttribute("usage");
        String type = element.getAttribute("type");
        switch (type) {
            case "char":
            case "date":
            case "time":
            case "timestamp":
            case "float":
            case "byte":
            case "int":
                data = new CommonType(name, type, usage, element.getAttribute("length"));
                break;
            case "packed":
            case "zoned":
                data = new Packed(name, type, usage, element.getAttribute("length"), element.getAttribute("precision"));
                break;
            case "struct":
                data = new Struct(name, type, usage, element.getAttribute("struct"), element.getAttribute("count"));
                break;
            default:
                data = new Data(name, type, usage);
                break;
        }
        return data;
    }

    public static void initStructElements(NodeList structs, Program program) {
        for (Data element : program.getData()) {
            if (element.getType() == AS400Types.STRUCTURE) {
                Struct struct = (Struct) element;
                List<Data> data = getStructByName(structs, struct.getStructName());
                for (Data obj : data) {
                    if (obj.getType() == AS400Types.STRUCTURE) {
                        ((Struct) obj).setData(getStructByName(structs, ((Struct) obj).getStructName()));
                    }
                }
                ((Struct) element).setData(data);
            }
        }
    }

    private static List<Data> getStructByName(NodeList structs, String name) {
        List<Data> structElements = new ArrayList<>();
        for (int temp = 0; temp < structs.getLength(); temp++) {
            Node nNode = structs.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                if (eElement.getAttribute("name").equals(name)) {
                    structElements.addAll(getProgramParameters(nNode));
                }
            }
        }
        return structElements;
    }

    public static String createMuleProgramCallDefinition(Program program) {
        String heading1 = "<!--Dataweave Equation-->" +"\n";
        String heading2 = "\n"+"<!--AS400 Program Call Definition-->"+"\n" +"\n";
        String tag1 = "\n"+ "<ee:transform doc:name=\"Transform Message\" doc:id=\"DOC_ID\">" + "\n" + "<ee:variables>" + "\n";
        String tag3 ="_val>" + "\n"+"<![CDATA[%dw 2.0 output application/java\n";
        String openingbracket = "---\n" + "[{\n";
        String lasttag = "\n"+"}]]]>"+"\n"+"</ee:set-variable>"+"\n"+"</ee:variables>"+"\n"+"</ee:transform>"+ "\n";

       // "#[vars." + value.getName() + "_val ]"

        StringBuilder sb = new StringBuilder();
        StringBuilder sb_new = new StringBuilder();
      //  sb.append(heading1).append(tag1).append(tag2).append(tag3).append(openingbracket);
        for (Data data : program.getData()) {
            sb_new.append(NewCreateParameter(data));
        }
        String tag2 = "<ee:set-variable variableName=vars."+ Converter.structName;
        sb.append(heading1).append(tag1).append(tag2).append(tag3).append(openingbracket).append(sb_new);
        sb.append(lasttag).append(heading2);
        System.out.println(sb);


        try {
            DocumentBuilderFactory dbFactory =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder =
                    dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();
            // root element
            Element rootElement = doc.createElement("as400:program-call-processor");
//            rootElement.setAttribute("doc:name", "CHANGEME"); //prefix doc need to be declared before using it
            rootElement.setAttribute("config-ref", "AS400_Config");
            if (program.getPath().equals("")) {
                rootElement.setAttribute(PROGRAM_LIBRARY_ATTR, "CHANGEME");
                rootElement.setAttribute(PROGRAM_NAME_ATTR, "CHANGEME");
            } else {
                rootElement.setAttribute(PROGRAM_LIBRARY_ATTR, program.getPath().split("/")[2].split("\\.")[0]);
                rootElement.setAttribute(PROGRAM_NAME_ATTR, program.getPath().split("/")[3].split("\\.")[0]);
            }
            doc.appendChild(rootElement);
            Element parameters = doc.createElement("as400:parameters");
            for (Data data : program.getData()) {
                parameters.appendChild(createParameter(data, doc));
            }
            rootElement.appendChild(parameters);
            TransformerFactory transformerFactory =
                    TransformerFactory.newInstance();
            Transformer transformer =
                    transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); //tab with 4 spaces
            DOMSource source = new DOMSource(doc);
            StreamResult resultToFile = new StreamResult(new File("as400-program-parameters.xml"));
            StringWriter writer = new StringWriter();
            writer.append(sb);
            StreamResult result = new StreamResult(writer);
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(source, resultToFile);
            transformer.transform(source, result);
            return writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String NewCreateParameter(Data data) {
        String sb1 = null;
        if (data.getClass() == CommonType.class) {
            CommonType value = (CommonType) data;
            sb1 = value.getName() + ":"+getDefaultValue(value.getType(), value.getLength(), true)+ ","   + "\n";
        }
        if (data.getClass() == Packed.class) {
            Packed value = (Packed) data;
            sb1 = value.getName()+ ":"+  getDefaultValue(value.getType(), value.getLength(), true) + ","  + "\n";
        }
        if (data.getClass() == Struct.class) {
            Struct value = (Struct) data;
            structName=value.getName();
            sb1 = value.getName()+ ":" + "\n" + "{" + "\n" ;
            for (Data param : value.getData()) {
            sb1=sb1+NewCreateParameterForStruct(param);
            }
            sb1=sb1+"}";
        }
        return sb1;
    }

    private static String NewCreateParameterForStruct(Data data) {

        String sb2 = null;
        if (data.getClass() == CommonType.class) {
            CommonType value = (CommonType) data;
            sb2 =  value.getName() + ":" + getDefaultValue(value.getType(), value.getLength(), true) + "," + "\n";
        }
        if (data.getClass() == Packed.class) {
            Packed value = (Packed) data;
            sb2 = value.getName()+ ":" + getDefaultValue(value.getType(), value.getLength(), true) + ","  + "\n";
        }
        if (data.getClass() == Struct.class) {
            Struct value = (Struct) data;
            sb2 = value.getName() + ":"  + "\n" + "{" + "\n";
            for (Data param : value.getData()) {
               sb2=sb2+NewCreateParameterForStruct(param);
            }
            sb2 =sb2 + "}";
        }
        return sb2;
    }

    private static Element createParameter(Data data, Document doc) {

        Element parameter = doc.createElement("as400:parameter");
        if (data.getClass() == CommonType.class) {
            CommonType value = (CommonType) data;
            parameter.setAttribute(PARAMETER_NAME_ATTR, value.getName());
            parameter.setAttribute(PARAMETER_TYPE_ATTR, value.getType().name());
            parameter.setAttribute(PARAMETER_LENGTH_ATTR, value.getLength());
            parameter.setAttribute(PARAMETER_USAGE_ATTR, value.getUsage().name());
            if (value.getUsage() != AS400Usage.OUT)
                parameter.setAttribute(PARAMETER_VALUE_ATTR, "#['" + getDefaultValue(value.getType(), value.getLength(), false) + "']");
        }
        if (data.getClass() == Packed.class) {
            Packed value = (Packed) data;
            parameter.setAttribute(PARAMETER_NAME_ATTR, value.getName());
            parameter.setAttribute(PARAMETER_TYPE_ATTR, value.getType().name());
            parameter.setAttribute(PARAMETER_LENGTH_ATTR, value.getLength());
            parameter.setAttribute(PARAMETER_USAGE_ATTR, value.getUsage().name());
            parameter.setAttribute(PARAMETER_DECIMAL_POSITION_ATTR, value.getPrecision());
            if (value.getUsage()!=AS400Usage.OUT)
                parameter.setAttribute(PARAMETER_VALUE_ATTR, "#['" + getDefaultValue(value.getType(), value.getLength(), false) + "']");
        }
        if (data.getClass() == Struct.class) {
            Struct value = (Struct) data;
            parameter.setAttribute(PARAMETER_NAME_ATTR, value.getName());
            parameter.setAttribute(PARAMETER_TYPE_ATTR, value.getType().name());
            parameter.setAttribute(PARAMETER_USAGE_ATTR, value.getUsage().name());
            parameter.setAttribute(PARAMETER_COUNT_ATTR, value.getCount());
            parameter.setAttribute(PARAMETER_VALUE_ATTR, "#[vars." + value.getName() + "_val ]");
            Element structureElement = doc.createElement("as400:data-structure-elements");

            parameter.appendChild(structureElement);
            for (Data param : value.getData()) {
               structureElement.appendChild(createParameterForStruct(param, doc));
               //System.out.println(param);
                //System.out.println(value);
            }

        }
        return parameter;
    }

    private static Element createParameterForStruct(Data data, Document doc) {
        Element parameter = doc.createElement("as400:parameter");
        if (data.getClass() == CommonType.class) {
            CommonType value = (CommonType) data;
            parameter.setAttribute(PARAMETER_NAME_ATTR, value.getName());
            parameter.setAttribute(PARAMETER_TYPE_ATTR, value.getType().name());
            parameter.setAttribute(PARAMETER_LENGTH_ATTR, value.getLength());
            parameter.setAttribute(PARAMETER_USAGE_ATTR, value.getUsage().name());
        }
        if (data.getClass() == Packed.class) {
            Packed value = (Packed) data;
            parameter.setAttribute(PARAMETER_NAME_ATTR, value.getName());
            parameter.setAttribute(PARAMETER_TYPE_ATTR, value.getType().name());
            parameter.setAttribute(PARAMETER_LENGTH_ATTR, value.getLength());
            parameter.setAttribute(PARAMETER_USAGE_ATTR, value.getUsage().name());
            parameter.setAttribute(PARAMETER_DECIMAL_POSITION_ATTR, value.getPrecision());
        }
        if (data.getClass() == Struct.class) {
            Struct value = (Struct) data;
            parameter.setAttribute(PARAMETER_NAME_ATTR, value.getName());
            parameter.setAttribute(PARAMETER_TYPE_ATTR, value.getType().name());
            parameter.setAttribute(PARAMETER_USAGE_ATTR, value.getUsage().name());
            parameter.setAttribute(PARAMETER_COUNT_ATTR, value.getCount());
            parameter.setAttribute(PARAMETER_VALUE_ATTR, "#[vars." + value.getName() + "_val]");
            Element structureElement = doc.createElement("as400:data-structure-elements");
            parameter.appendChild(structureElement);
            for (Data param : value.getData()) {
                structureElement.appendChild(createParameterForStruct(param, doc));
            }
        }
        return parameter;
    }

    private static String getDefaultValue(AS400Types type, String length, Boolean isDataweave) {
        String value = "";
        switch (type) {
            case STRING:
                value = appendSign("X", Integer.parseInt(length), type);
                if(isDataweave)
                    value="'"+ value + "'";
                break;
            case PACKED:
            case ZONED:
            case INTEGER:
            case FLOAT:
            case BYTE:
                value = appendSign("9", Integer.parseInt(length), type);
                break;
            case DATE:
                value = "2017-01-01";
                if(isDataweave)
                    value="'"+ value + "'";
                break;
            case TIME:
                value = "08:10:20";
                if(isDataweave)
                    value="'"+ value + "'";
                break;
            case TIMESTAMP:
                value = "2017-04-20 19:11:47.670000";
                if(isDataweave)
                    value="'"+ value + "'";
                break;
            default:
                value = "change_val";
                break;
        }
        return value;
    }

    private static String appendSign(String sign, int length, AS400Types type) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if ((type.equals(AS400Types.PACKED) || type.equals(AS400Types.ZONED) || type.equals(AS400Types.FLOAT)) && i == 1) {
                stringBuilder.append(".");
            } else {
                stringBuilder.append(sign);
            }
        }
        return stringBuilder.toString();
    }
}