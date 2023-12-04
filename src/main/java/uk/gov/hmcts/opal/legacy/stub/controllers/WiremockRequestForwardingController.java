package uk.gov.hmcts.opal.legacy.stub.controllers;

import io.micrometer.core.instrument.util.IOUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.legacy.stub.server.MockHttpServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Set;

import static java.net.http.HttpRequest.BodyPublisher;
import static java.net.http.HttpRequest.BodyPublishers;
import static java.net.http.HttpRequest.Builder;
import static java.net.http.HttpRequest.newBuilder;
import static java.util.Locale.ENGLISH;

/**
 * Stand up a REST controller that captures all incoming REST requests and forwards them to the Wiremock instance.
 */
@RestController
@RequestMapping("/")
@SuppressWarnings("PMD.LawOfDemeter")
public class WiremockRequestForwardingController {

    private static final String CATCH_ALL_PATH = "**";
    private static final Set<String> EXCLUDED_HEADERS = Set.of(
            "host", "connection", "accept-encoding", "content-length"
    );

    @Value("${wiremock.server.host}")
    private String mockHttpServerHost;

    private final HttpClient httpClient;
    private final MockHttpServer mockHttpServer;

    public WiremockRequestForwardingController(HttpClient httpClient, MockHttpServer mockHttpServer) {
        this.httpClient = httpClient;
        this.mockHttpServer = mockHttpServer;
    }

    @GetMapping(CATCH_ALL_PATH)
    public ResponseEntity<byte[]> forwardGetRequests(HttpServletRequest request)
            throws IOException, InterruptedException {
        return forwardRequest(request, BodyPublishers.noBody(), HttpMethod.GET);
    }

    @PostMapping(CATCH_ALL_PATH)
    public ResponseEntity<byte[]> forwardPostRequests(HttpServletRequest request)
            throws IOException, InterruptedException {
        var requestBody = IOUtils.toString(request.getInputStream());
        return forwardRequest(request, BodyPublishers.ofString(requestBody), HttpMethod.POST);
    }

    @PutMapping(CATCH_ALL_PATH)
    public ResponseEntity<byte[]> forwardPutRequests(HttpServletRequest request)
            throws IOException, InterruptedException {
        var requestBody = IOUtils.toString(request.getInputStream());
        return forwardRequest(request, BodyPublishers.ofString(requestBody), HttpMethod.PUT);
    }

    @DeleteMapping(CATCH_ALL_PATH)
    public ResponseEntity<byte[]> forwardDeleteRequests(HttpServletRequest request)
            throws IOException, InterruptedException {
        return forwardRequest(request, BodyPublishers.noBody(), HttpMethod.DELETE);
    }

    /**
     * This deconstructs the original request URL and rebuilds it into a new request to target at
     * the Wiremock instance.
     */
    private ResponseEntity<byte[]> forwardRequest(
            HttpServletRequest request,
            BodyPublisher bodyPublisher,
            HttpMethod httpMethod) throws IOException, InterruptedException {

        var requestPath = new AntPathMatcher().extractPathWithinPattern(CATCH_ALL_PATH, request.getRequestURI());

        if (request.getQueryString() != null) {
            requestPath += "?" + request.getQueryString();
        }

        var requestBuilder = newBuilder(URI.create(getMockHttpServerUrl(requestPath)))
                .method(httpMethod.name(), bodyPublisher);

        transferRequestHeaders(request, requestBuilder);

        var httpResponse = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

        return new ResponseEntity<>(
                httpResponse.body().getBytes(),
                copyResponseHeaders(httpResponse),
                httpResponse.statusCode()
        );

    }

    private void transferRequestHeaders(HttpServletRequest request, Builder requestBuilder) {
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            if (!EXCLUDED_HEADERS.contains(headerName.toLowerCase(ENGLISH))) {
                requestBuilder.header(headerName, request.getHeader(headerName));
            }
        });
    }

    private HttpHeaders copyResponseHeaders(HttpResponse<?> response) {
        HttpHeaders headers = new HttpHeaders();
        response.headers().map().forEach(headers::addAll);
        return headers;
    }

    private String getMockHttpServerUrl(String requestPath) {
        return "http://" + mockHttpServerHost + ":" + mockHttpServer.portNumber() + requestPath;
    }
}
