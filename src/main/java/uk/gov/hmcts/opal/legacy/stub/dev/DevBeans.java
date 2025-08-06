package uk.gov.hmcts.opal.legacy.stub.dev;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.Function;

import static java.time.ZoneOffset.UTC;
import static java.util.Collections.list;

@Configuration
@SuppressWarnings({
    "PMD.UnusedPrivateMethod",
    "HideUtilityClassConstructor",
})
public class DevBeans {
    public static final boolean ENABLED = true;

    static {
        if (ENABLED) {
            TimeZone.setDefault(TimeZone.getTimeZone(UTC));
        }
    }
    @Component
    @Slf4j
    @RequiredArgsConstructor
    public static class LoggingFilterBean extends GenericFilterBean {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response,
                             FilterChain chain) throws IOException, ServletException {
            if (!ENABLED) {
                return;
            }

            ContentCachingRequestWrapper requestWrapper = requestWrapper(request);
            ContentCachingResponseWrapper responseWrapper = responseWrapper(response);

            chain.doFilter(requestWrapper, responseWrapper);

            logRequest(requestWrapper);
            logResponse(responseWrapper);
        }

        private void logRequest(ContentCachingRequestWrapper request) {
            StringBuilder builder = new StringBuilder();
            builder.append(headersToString(list(request.getHeaderNames()), request::getHeader));
            builder.append(new String(request.getContentAsByteArray()));
            log.info("Request: {} {}: {}?{}", request.getMethod(), request.getRequestURI(),
                Optional.ofNullable(request.getQueryString()).orElse(""), builder);
        }

        private void logResponse(ContentCachingResponseWrapper response) throws IOException {
            StringBuilder builder = new StringBuilder();
            builder.append(headersToString(response.getHeaderNames(), response::getHeader));
            builder.append("%s=%s".formatted("content-type", response.getContentType())).append('\n');
            builder.append(new String(response.getContentAsByteArray()));
            log.info("Response: {}", builder);
            response.copyBodyToResponse();
        }

        private String headersToString(Collection<String> headerNames, Function<String, String> headerValueResolver) {
            StringBuilder builder = new StringBuilder();
            for (String headerName : headerNames) {
                String header = headerValueResolver.apply(headerName);
                builder.append("%s=%s".formatted(headerName, header)).append('\n');
            }
            return builder.toString();
        }

        private ContentCachingRequestWrapper requestWrapper(ServletRequest request) {
            if (request instanceof ContentCachingRequestWrapper requestWrapper) {
                return requestWrapper;
            }
            return new ContentCachingRequestWrapper((HttpServletRequest) request);
        }

        private ContentCachingResponseWrapper responseWrapper(ServletResponse response) {
            if (response instanceof ContentCachingResponseWrapper responseWrapper) {
                return responseWrapper;
            }
            return new ContentCachingResponseWrapper((HttpServletResponse) response);
        }


    }
}
