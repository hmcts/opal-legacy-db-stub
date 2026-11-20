package uk.gov.hmcts.opal.legacy.stub;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings("removal")
class HealthCheckTests {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    void healthCheckShouldReturnOk() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/health", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 ok");
    }

    @Test
    void livenessCheckShouldReturnOk() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/health/liveness", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Liveness probe should return 200");
    }

    @Test
    void readinessCheckShouldReturnOk() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/health/readiness", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Readiness probe should return 200");
    }
}
