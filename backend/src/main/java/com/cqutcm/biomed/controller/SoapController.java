package com.cqutcm.biomed.controller;

import com.cqutcm.biomed.entity.Herb;
import com.cqutcm.biomed.mapper.HerbMapper;
import com.cqutcm.biomed.service.AuthenticationRequiredException;
import com.cqutcm.biomed.service.AuthorizationDeniedException;
import com.cqutcm.biomed.service.IntegrationClientService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
public class SoapController {
    static final String SOAP_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/";
    static final String SERVICE_NAMESPACE = "http://cqutcm.com/biomed/school/v1";
    static final String SOAP_ACTION = SERVICE_NAMESPACE + "/queryGrowthData";
    private static final String SOAP_PATH = "/api/soap/school";

    private final HerbMapper herbMapper;
    private final IntegrationClientService integrationClientService;

    public SoapController(HerbMapper herbMapper, IntegrationClientService integrationClientService) {
        this.herbMapper = herbMapper;
        this.integrationClientService = integrationClientService;
    }

    @PostMapping(
            value = SOAP_PATH,
            consumes = {MediaType.TEXT_XML_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.ALL_VALUE},
            produces = MediaType.TEXT_XML_VALUE
    )
    public ResponseEntity<String> querySchoolData(
            @RequestBody String xml,
            @RequestHeader(value = "SOAPAction", required = false) String soapAction,
            @RequestHeader(value = "X-Integration-Client", required = false) String clientCode,
            @RequestHeader(value = "X-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-Nonce", required = false) String nonce,
            @RequestHeader(value = "X-Signature", required = false) String signature,
            HttpServletRequest request
    ) {
        try {
            integrationClientService.authenticate(
                    clientCode, timestamp, nonce, signature,
                    request.getMethod(), SOAP_PATH, xml, request.getRemoteAddr());
            validateSoapAction(soapAction);
            String keyword = parseKeyword(xml);
            List<Herb> items = herbMapper.findAll().stream()
                    .filter(herb -> matches(herb, keyword))
                    .toList();
            return soapResponse(HttpStatus.OK, successEnvelope(items));
        } catch (AuthenticationRequiredException ex) {
            return fault(HttpStatus.UNAUTHORIZED, "soap:Client.Authentication", ex.getMessage());
        } catch (AuthorizationDeniedException ex) {
            return fault(HttpStatus.FORBIDDEN, "soap:Client.Authorization", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return fault(HttpStatus.BAD_REQUEST, "soap:Client", ex.getMessage());
        } catch (Exception ex) {
            return fault(HttpStatus.INTERNAL_SERVER_ERROR, "soap:Server", "internal SOAP service error");
        }
    }

    private void validateSoapAction(String soapAction) {
        String normalized = soapAction == null ? "" : soapAction.trim();
        if (normalized.startsWith("\"") && normalized.endsWith("\"") && normalized.length() >= 2) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        if (!SOAP_ACTION.equals(normalized)) {
            throw new IllegalArgumentException("unsupported SOAPAction");
        }
    }

    private String parseKeyword(String xml) {
        try {
            DocumentBuilderFactory factory = secureDocumentBuilderFactory();
            Document document = factory.newDocumentBuilder().parse(
                    new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            Element envelope = document.getDocumentElement();
            requireElement(envelope, SOAP_NAMESPACE, "Envelope", "SOAP Envelope is required");

            Element body = firstChildElement(envelope, SOAP_NAMESPACE, "Body");
            if (body == null) {
                throw new IllegalArgumentException("SOAP Body is required");
            }
            Element operation = firstElementChild(body);
            requireElement(operation, SERVICE_NAMESPACE, "queryGrowthDataRequest",
                    "queryGrowthDataRequest is required");
            Element keyword = firstChildElement(operation, SERVICE_NAMESPACE, "keyword");
            String value = keyword == null ? "" : keyword.getTextContent().trim();
            if (value.length() > 100) {
                throw new IllegalArgumentException("keyword cannot exceed 100 characters");
            }
            return value;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid SOAP XML request");
        }
    }

    private boolean matches(Herb herb, String keyword) {
        return keyword.isBlank()
                || contains(herb.getName(), keyword)
                || contains(herb.getDistrict(), keyword);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.contains(keyword);
    }

    private String successEnvelope(List<Herb> herbs) {
        StringBuilder xml = new StringBuilder("""
                <?xml version="1.0" encoding="UTF-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
                               xmlns:sch="http://cqutcm.com/biomed/school/v1">
                  <soap:Body>
                    <sch:queryGrowthDataResponse>
                      <sch:items>
                """);
        for (Herb herb : herbs) {
            xml.append("        <sch:item>\n");
            appendElement(xml, "id", herb.getId());
            appendElement(xml, "name", herb.getName());
            appendElement(xml, "district", herb.getDistrict());
            appendElement(xml, "longitude", herb.getLongitude());
            appendElement(xml, "latitude", herb.getLatitude());
            appendElement(xml, "scale", herb.getScaleDesc());
            appendElement(xml, "environment", herb.getEnvironment());
            appendElement(xml, "traceCode", herb.getTraceCode());
            xml.append("        </sch:item>\n");
        }
        xml.append("""
                      </sch:items>
                    </sch:queryGrowthDataResponse>
                  </soap:Body>
                </soap:Envelope>
                """);
        return xml.toString();
    }

    private void appendElement(StringBuilder xml, String name, Object value) {
        if (value != null) {
            xml.append("          <sch:").append(name).append(">")
                    .append(escapeXml(String.valueOf(value)))
                    .append("</sch:").append(name).append(">\n");
        }
    }

    private ResponseEntity<String> fault(HttpStatus status, String faultCode, String message) {
        String body = """
                <?xml version="1.0" encoding="UTF-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <soap:Fault>
                      <faultcode>%s</faultcode>
                      <faultstring>%s</faultstring>
                    </soap:Fault>
                  </soap:Body>
                </soap:Envelope>
                """.formatted(escapeXml(faultCode), escapeXml(message));
        return soapResponse(status, body);
    }

    private ResponseEntity<String> soapResponse(HttpStatus status, String body) {
        return ResponseEntity.status(status)
                .contentType(MediaType.parseMediaType("text/xml;charset=UTF-8"))
                .header("X-Content-Type-Options", "nosniff")
                .body(body);
    }

    private void requireElement(Element element, String namespace, String localName, String message) {
        if (element == null
                || !namespace.equals(element.getNamespaceURI())
                || !localName.equals(element.getLocalName())) {
            throw new IllegalArgumentException(message);
        }
    }

    private Element firstChildElement(Element parent, String namespace, String localName) {
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element element
                    && namespace.equals(element.getNamespaceURI())
                    && localName.equals(element.getLocalName())) {
                return element;
            }
        }
        return null;
    }

    private Element firstElementChild(Element parent) {
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element element) {
                return element;
            }
        }
        return null;
    }

    private static DocumentBuilderFactory secureDocumentBuilderFactory()
            throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory;
    }

    private String escapeXml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
