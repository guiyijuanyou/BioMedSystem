package com.cqutcm.biomed.controller;

import com.cqutcm.biomed.entity.Herb;
import com.cqutcm.biomed.mapper.HerbMapper;
import com.cqutcm.biomed.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SoapController {
    private final HerbMapper herbMapper;
    private final ObjectMapper objectMapper;
    private final AuthService authService;

    public SoapController(HerbMapper herbMapper, ObjectMapper objectMapper, AuthService authService) {
        this.herbMapper = herbMapper;
        this.objectMapper = objectMapper;
        this.authService = authService;
    }

    @PostMapping(value = "/api/soap/school",
            consumes = {MediaType.TEXT_XML_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.ALL_VALUE},
            produces = MediaType.TEXT_XML_VALUE)
    public String querySchoolData(@RequestBody String xml,
                                   @RequestHeader(value = "Authorization", required = false) String authorization)
            throws Exception {
        // 认证检查
        authService.requireActor(authorization);

        String keyword = extractXmlValueSafe(xml, "keyword");
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

    /**
     * 安全地解析 XML 中指定标签的值，使用 DocumentBuilderFactory 并禁用外部实体（防 XXE）。
     */
    private String extractXmlValueSafe(String xml, String tagName) {
        try {
            var factory = createSecureDocumentBuilderFactory();
            var builder = factory.newDocumentBuilder();
            var doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            var nodes = doc.getElementsByTagName(tagName);
            if (nodes.getLength() > 0) {
                return nodes.item(0).getTextContent().trim();
            }
        } catch (Exception e) {
            // 解析失败返回空，不暴露内部错误
        }
        return "";
    }

    /** 创建安全的 DocumentBuilderFactory，禁用 DTD 和外部实体 */
    private static DocumentBuilderFactory createSecureDocumentBuilderFactory()
            throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory;
    }

    private String escapeXml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
