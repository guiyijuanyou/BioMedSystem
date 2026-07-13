package com.cqutcm.biomed.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
public class SoapContractController {
    @GetMapping(value = "/api/soap/school-integration.wsdl", produces = MediaType.TEXT_XML_VALUE)
    public String wsdl() throws IOException {
        return resource("wsdl/school-integration.wsdl");
    }

    @GetMapping(value = "/api/soap/school-integration.xsd", produces = MediaType.TEXT_XML_VALUE)
    public String xsd() throws IOException {
        return resource("wsdl/school-integration.xsd");
    }

    private String resource(String path) throws IOException {
        return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
    }
}
