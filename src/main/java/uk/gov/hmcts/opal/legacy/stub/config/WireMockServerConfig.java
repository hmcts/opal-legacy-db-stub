package uk.gov.hmcts.opal.legacy.stub.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.extension.TemplateHelperProviderExtension;
import com.github.tomakehurst.wiremock.extension.TemplateModelDataProviderExtension;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import wiremock.com.github.jknack.handlebars.Helper;

import java.io.File;
import java.util.Map;

@Configuration
public class WireMockServerConfig {

    private static final Logger LOG = LoggerFactory.getLogger(WireMockServerConfig.class);
    private static final String MAPPINGS_DIRECTORY_NAME = "/mappings";

    private final String mappingsPath;

    @Autowired
    public WireMockServerConfig(@Value("${wiremock.server.mappings-path}") String mappingsPath) {
        this.mappingsPath = mappingsPath;
    }

    @Bean
    public WireMockServer wireMockServer() {
        LOG.info("WireMock mappings file path: {}", mappingsPath);

        WireMockServer wireMockServer = new WireMockServer(getWireMockConfig());

        LOG.info("Stubs registered with wiremock");
        wireMockServer.getStubMappings().forEach(w -> LOG.info("\nRequest : {}, \nResponse: {}", w.getRequest(),
            w.getResponse()));

        return wireMockServer;
    }

    private WireMockConfiguration getWireMockConfig() {
        File mappingDirectory = new File(mappingsPath + MAPPINGS_DIRECTORY_NAME);
        LOG.info("Mappings directory path: {}", mappingDirectory.getAbsolutePath());

        WireMockConfiguration config = WireMockConfiguration.wireMockConfig().stubCorsEnabled(false)
            .dynamicHttpsPort().dynamicPort().globalTemplating(true).extensions(bespokeBehaviourExtensions());

        if (mappingDirectory.isDirectory()) {
            return config.usingFilesUnderDirectory(mappingsPath);
        } else {
            LOG.info("using classpath resources to resolve mappings");
            return config.usingFilesUnderClasspath(mappingsPath);
        }
    }

    public static Extension[] bespokeBehaviourExtensions() {
        return new Extension[]{new StringLengthExtension(), new CustomDataExtension()};
    }

    private static final class StringLengthExtension implements TemplateHelperProviderExtension {
        @Override
        public String getName() {
            return "custom-helpers";
        }

        @Override
        public Map<String, Helper<?>> provideTemplateHelpers() {
            Helper<Object> helper = (context, options) -> context.toString().length();
            return Map.of("string-length", helper);
        }
    }

    private static final class CustomDataExtension implements TemplateModelDataProviderExtension {


        @Override
        public Map<String, Object> provideTemplateModelData(ServeEvent serveEvent) {
            return Map.of("dbData", Map.of("accountId", MockDatabase.getAccountNumber()));
        }

        @Override
        public String getName() {
            return "custom-model-data";
        }
    }
}
