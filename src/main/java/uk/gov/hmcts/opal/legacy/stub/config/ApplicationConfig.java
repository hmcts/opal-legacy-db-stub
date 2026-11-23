package uk.gov.hmcts.opal.legacy.stub.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
public class ApplicationConfig {

    @Bean(name = "httpClient")
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

    @Bean(name = "objectMapper")
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
