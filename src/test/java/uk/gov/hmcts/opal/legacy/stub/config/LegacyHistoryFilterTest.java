package uk.gov.hmcts.opal.legacy.stub.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

class LegacyHistoryFilterTest {

    private WireMockServer server;

    @BeforeEach
    void startServer() {
        server = new WireMockServer(WireMockConfiguration.options().dynamicPort()
            .extensions(new LegacyHistoryFilter()));
        server.start();
        server.stubFor(post(urlEqualTo("/history")).willReturn(aResponse()
            .withTransformers("legacy-history-filters").withBody("""
                <response><version>7</version><history_items>
                  <history_items_element><posted_details><posted_date>2026-01-01T12:00:00</posted_date></posted_details>
                    <type>Financial</type></history_items_element>
                  <history_items_element><posted_details><posted_date>2026-01-02T00:00:00</posted_date></posted_details>
                    <type>Note</type></history_items_element>
                  <history_items_element><posted_details><posted_date>2026-01-03T23:59:59</posted_date></posted_details>
                    <type>Financial</type></history_items_element>
                  <history_items_element><posted_details><posted_date>2026-01-04T00:00:00</posted_date></posted_details>
                    <type>Note</type></history_items_element>
                </history_items></response>
                """)));
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }

    @Test
    void datesIncludeBothBoundaries() throws IOException, InterruptedException {
        String body = request("{\"from_date\":\"2026-01-02\",\"to_date\":\"2026-01-03\"}");
        assertThat(body).contains("2026-01-02T00:00:00", "2026-01-03T23:59:59")
            .doesNotContain("2026-01-01T12:00:00", "2026-01-04T00:00:00");
    }

    @Test
    void itemTypesCombineWithDates() throws IOException, InterruptedException {
        String body = request("""
            {"from_date":"2026-01-02","to_date":"2026-01-03","item_types":["note"]}
            """);
        assertThat(body).contains("2026-01-02T00:00:00", "<type>Note</type>")
            .doesNotContain("<type>Financial</type>", "2026-01-04T00:00:00");
    }

    @Test
    void absentFiltersPreserveAllHistory() throws IOException, InterruptedException {
        assertThat(request("{}")).contains("2026-01-01T12:00:00", "2026-01-02T00:00:00",
            "2026-01-03T23:59:59", "2026-01-04T00:00:00");
    }

    @Test
    void noMatchingHistoryPreservesResponseVersion() throws IOException, InterruptedException {
        assertThat(request("{\"from_date\":\"2026-02-01\"}"))
            .contains("<version>7</version>").doesNotContain("<history_items_element>");
    }

    private String request(String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(server.baseUrl() + "/history"))
            .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build();
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isEqualTo(200);
            return response.body();
        }
    }
}
