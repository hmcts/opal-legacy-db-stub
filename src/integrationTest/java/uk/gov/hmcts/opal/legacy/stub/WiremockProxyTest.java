package uk.gov.hmcts.opal.legacy.stub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

class WiremockProxyTest extends IntegrationTestBase {

    private static final String PATH = "/test";
    private URI uri;

    @BeforeEach
    void createUri() throws URISyntaxException {
        uri = new URI(baseUrl + PATH);
    }

    @Test
    void forwardsGetRequests() {
        ResponseEntity<TestResponse> result = this.restTemplate.getForEntity(uri, TestResponse.class);

        assertThat(result.getBody().getResult()).isEqualTo("content");
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void forwardsPostRequests() {
        ResponseEntity<TestResponse> result = this.restTemplate.postForEntity(uri, "{}", TestResponse.class);

        assertThat(result.getBody().getResult()).isEqualTo("posted");
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void forwardsPutRequests() {
        ResponseEntity<TestResponse> result = this.restTemplate.exchange(uri, HttpMethod.PUT, null, TestResponse.class);

        assertThat(result.getBody().getResult()).isEqualTo("putted");
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void forwardsDeleteRequests() {
        ResponseEntity<TestResponse> result =
            this.restTemplate.exchange(uri, HttpMethod.DELETE, null, TestResponse.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    //False Positive
    @SuppressWarnings("PMD.TestClassWithoutTestCases")
    static class TestResponse {
        private String result;

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }
}
