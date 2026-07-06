package com.cqutcm.biomed.controller;

import com.cqutcm.biomed.service.GenericRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SoapController {
    private final GenericRecordService service;
    private final ObjectMapper objectMapper;

    public SoapController(GenericRecordService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/api/soap/school", consumes = {MediaType.TEXT_XML_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.ALL_VALUE}, produces = MediaType.TEXT_XML_VALUE)
    public String querySchoolData(@RequestBody String xml) throws Exception {
        String keyword = extractXmlValue(xml, "keyword");
        String payload = objectMapper.writeValueAsString(Map.of("items", service.queryHerbs(keyword)));
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <queryGrowthDataResponse>
                      <result>%s</result>
                    </queryGrowthDataResponse>
                  </soap:Body>
                </soap:Envelope>
                """.formatted(escapeXml(payload));
    }

    private String extractXmlValue(String xml, String name) {
        int open = xml.indexOf("<" + name + ">");
        int close = xml.indexOf("</" + name + ">");
        if (open >= 0 && close > open) {
            return xml.substring(open + name.length() + 2, close).trim();
        }
        return "";
    }

    private String escapeXml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}

