package uk.gov.hmcts.opal.legacy.stub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
public class ApplicationConfig {

    @Bean(name = "httpClient")
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }
}
