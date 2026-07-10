package com.cqutcm.biomed.controller;

import com.cqutcm.biomed.entity.Herb;
import com.cqutcm.biomed.mapper.HerbMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SoapController {
    private final HerbMapper herbMapper;
    private final ObjectMapper objectMapper;

    public SoapController(HerbMapper herbMapper, ObjectMapper objectMapper) {
        this.herbMapper = herbMapper;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/api/soap/school", consumes = {MediaType.TEXT_XML_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.ALL_VALUE}, produces = MediaType.TEXT_XML_VALUE)
    public String querySchoolData(@RequestBody String xml) throws Exception {
        String keyword = extractXmlValue(xml, "keyword");
        String safeKeyword = keyword == null ? "" : keyword.trim();
        List<Herb> herbs = herbMapper.findAll();
        List<Map<String, Object>> items = herbs.stream()
                .filter(h -> safeKeyword.isBlank()
                        || (h.getName() != null && h.getName().contains(safeKeyword))
                        || (h.getDistrict() != null && h.getDistrict().contains(safeKeyword)))
                .map(h -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", h.getId());
                    row.put("name", h.getName());
                    row.put("district", h.getDistrict());
                    row.put("longitude", h.getLongitude());
                    row.put("latitude", h.getLatitude());
                    row.put("scale", h.getScaleDesc());
                    row.put("environment", h.getEnvironment());
                    row.put("traceCode", h.getTraceCode());
                    return row;
                }).toList();
        String payload = objectMapper.writeValueAsString(Map.of("items", items));
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
