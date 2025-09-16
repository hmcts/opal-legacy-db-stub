package uk.gov.hmcts.opal.legacy.stub.controllers;

import io.micrometer.core.instrument.util.IOUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
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
import java.nio.charset.StandardCharsets;
import java.util.Optional;
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
@Slf4j(topic = "WiremockRequestForwardingController")
public class WiremockRequestForwardingController {

    private static final String CATCH_ALL_PATH = "**";
    private static final Set<String> EXCLUDED_HEADERS = Set.of(
        "host", "connection", "accept-encoding", "content-length", "transfer-encoding", "upgrade"
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
        return forwardRequest(request, BodyPublishers.noBody(), HttpMethod.GET, null);
    }

    @PostMapping(CATCH_ALL_PATH)
    public ResponseEntity<byte[]> forwardPostRequests(HttpServletRequest request)
        throws IOException, InterruptedException {
        String requestBody = IOUtils.toString(request.getInputStream());
        return forwardRequest(request, BodyPublishers.ofString(requestBody), HttpMethod.POST, requestBody);
    }

    @PutMapping(CATCH_ALL_PATH)
    public ResponseEntity<byte[]> forwardPutRequests(HttpServletRequest request)
        throws IOException, InterruptedException {
        String requestBody = IOUtils.toString(request.getInputStream());
        return forwardRequest(request, BodyPublishers.ofString(requestBody), HttpMethod.PUT, requestBody);
    }

    @DeleteMapping(CATCH_ALL_PATH)
    public ResponseEntity<byte[]> forwardDeleteRequests(HttpServletRequest request)
        throws IOException, InterruptedException {
        return forwardRequest(request, BodyPublishers.noBody(), HttpMethod.DELETE, null);
    }

    /**
     * This deconstructs the original request URL and rebuilds it into a new request to target at
     * the Wiremock instance.
     */
    private ResponseEntity<byte[]> forwardRequest(
        HttpServletRequest request,
        BodyPublisher bodyPublisher,
        HttpMethod httpMethod,
        String requstBody) throws IOException, InterruptedException {

        String requestPath = new AntPathMatcher().extractPathWithinPattern(CATCH_ALL_PATH, request.getRequestURI());

        if (request.getQueryString() != null) {
            requestPath += "?" + request.getQueryString();
        }

        Builder requestBuilder = newBuilder(URI.create(getMockHttpServerUrl(requestPath)))
            .method(httpMethod.name(), bodyPublisher);

        log.info(":forwardRequest: request: {}", requestBuilder.build());
        Optional.ofNullable(requstBody).ifPresent(body -> log.info(":forwardRequest: body: {}", body));
        logRequestHeaders(request);

        transferRequestHeaders(request, requestBuilder);

        HttpResponse<byte[]> httpResponse =
                httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());

        log.info(":forwardRequest: response body: {}\n", new String(httpResponse.body(), StandardCharsets.UTF_8));


        return new ResponseEntity<>(
                httpResponse.body(),
                copyResponseHeaders(httpResponse),
                httpResponse.statusCode()
        );


    }

    private void logRequestHeaders(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder(":logRequestHeaders: ");
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            sb.append(MessageFormatter.format("[{} = {}] ", headerName, request.getHeader(headerName)).getMessage());
        });
        log.info(sb.toString());
    }

    private void transferRequestHeaders(HttpServletRequest request, Builder requestBuilder) {
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            if (!EXCLUDED_HEADERS.contains(headerName.toLowerCase(ENGLISH))) {
                requestBuilder.header(headerName, request.getHeader(headerName));
            }
        });
    }

    private MultiValueMap<String, String> copyResponseHeaders(HttpResponse<?> response) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        response.headers().map().forEach((key, values) -> {
            if (key != null && !"transfer-encoding".equalsIgnoreCase(key) && !key.startsWith(":")) {
                headers.addAll(key, values);
            }
        });
        return headers;
    }

    private String getMockHttpServerUrl(String requestPath) {
        return "http://" + mockHttpServerHost + ":" + mockHttpServer.portNumber() + requestPath;
    }
}
