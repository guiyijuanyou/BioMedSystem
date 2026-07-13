package com.cqutcm.biomed.controller;

import com.cqutcm.biomed.entity.Herb;
import com.cqutcm.biomed.mapper.HerbMapper;
import com.cqutcm.biomed.service.AuthenticationRequiredException;
import com.cqutcm.biomed.service.IntegrationClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SoapControllerTest {
    private static final String REQUEST = """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
                           xmlns:sch="http://cqutcm.com/biomed/school/v1">
              <soap:Body>
                <sch:queryGrowthDataRequest>
                  <sch:keyword>黄连</sch:keyword>
                </sch:queryGrowthDataRequest>
              </soap:Body>
            </soap:Envelope>
            """;

    @Mock
    private HerbMapper herbMapper;
    @Mock
    private IntegrationClientService integrationClientService;

    private SoapController controller;
    private MockHttpServletRequest servletRequest;

    @BeforeEach
    void setUp() {
        controller = new SoapController(herbMapper, integrationClientService);
        servletRequest = new MockHttpServletRequest("POST", "/api/soap/school");
        servletRequest.setRemoteAddr("127.0.0.1");
    }

    @Test
    void returnsContractShapedSoapResponse() throws Exception {
        when(herbMapper.findAll()).thenReturn(List.of(herb()));

        ResponseEntity<String> response = invoke(SoapController.SOAP_ACTION);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("<sch:queryGrowthDataResponse>"));
        assertTrue(response.getBody().contains("<sch:name>黄连</sch:name>"));
        assertTrue(response.getBody().contains("<sch:longitude>108.25</sch:longitude>"));
        parseXml(response.getBody());
    }

    @Test
    void unsupportedSoapActionReturnsClientFault() throws Exception {
        ResponseEntity<String> response = invoke("wrong-action");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("<soap:Fault>"));
        assertTrue(response.getBody().contains("unsupported SOAPAction"));
        parseXml(response.getBody());
    }

    @Test
    void missingNamespaceReturnsClientFault() throws Exception {
        String invalid = """
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body><queryGrowthDataRequest><keyword>黄连</keyword></queryGrowthDataRequest></soap:Body>
                </soap:Envelope>
                """;

        ResponseEntity<String> response = controller.querySchoolData(
                invalid, SoapController.SOAP_ACTION, "client", "timestamp", "nonce-1234567890",
                "signature", servletRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("queryGrowthDataRequest is required"));
        parseXml(response.getBody());
    }

    @Test
    void authenticationFailureReturnsSoapFaultInsteadOfJson() throws Exception {
        when(integrationClientService.authenticate(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString()))
                .thenThrow(new AuthenticationRequiredException("invalid integration signature"));

        ResponseEntity<String> response = invoke(SoapController.SOAP_ACTION);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().contains("soap:Client.Authentication"));
        assertTrue(response.getBody().contains("invalid integration signature"));
        parseXml(response.getBody());
    }

    private ResponseEntity<String> invoke(String soapAction) {
        return controller.querySchoolData(
                REQUEST, soapAction, "client", "timestamp", "nonce-1234567890",
                "signature", servletRequest);
    }

    private Herb herb() {
        Herb herb = new Herb();
        herb.setId("herb-1");
        herb.setName("黄连");
        herb.setDistrict("石柱县");
        herb.setLongitude(new BigDecimal("108.25"));
        herb.setLatitude(new BigDecimal("30.10"));
        herb.setTraceCode("TRACE-001");
        return herb;
    }

    private void parseXml(String xml) throws Exception {
        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
