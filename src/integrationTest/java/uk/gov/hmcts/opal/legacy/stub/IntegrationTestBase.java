package uk.gov.hmcts.opal.legacy.stub;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTestBase {

    @LocalServerPort
    int port;

    protected RestTemplate restTemplate;

    protected String baseUrl = "http://localhost";

    @BeforeEach
    public void setUp() {
        baseUrl = baseUrl + ":" + port;
        restTemplate = new RestTemplate();
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        messageConverters.add(new MappingJackson2HttpMessageConverter(new ObjectMapper()));
        restTemplate.setMessageConverters(messageConverters);
    }

}
