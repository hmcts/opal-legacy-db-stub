package uk.gov.hmcts.opal.legacy.stub.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@Configuration
public class ApplicationConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean(name = "httpClient")
    public HttpClient httpClient() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    // Trust all client certificates
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    // Trust all server certificates
                }
            }
        };
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new SecureRandom());

        SSLParameters sslParameters = new SSLParameters();
        sslParameters.setEndpointIdentificationAlgorithm(""); // Disable endpoint ID check (hostname verification)

        return HttpClient.newBuilder()
                .sslContext(sslContext)
                .sslParameters(sslParameters)
                .build();
    }

    @Bean(name = "objectMapper")
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
