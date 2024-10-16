package com.infoview;

import com.infoview.converter.RamlWriter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;


@SpringBootApplication
public class Main {
    public static void main(String[] args) throws IOException {
        /*RamlWriter ramlWriter = new RamlWriter();
        ramlWriter.generateRaml();*/
        SpringApplication.run(Main.class, args);
    }
}

