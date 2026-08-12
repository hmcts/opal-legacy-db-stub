package uk.gov.hmcts.opal.legacy.stub.wiremock;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WiremockV2VersionTest {

    private static final String OVER_LONG_MAX_VALUE = "9223372036854775808";
    private static final Path WIREMOCK_ROOT = Path.of("wiremock");
    private static final Path MAPPINGS_ROOT = WIREMOCK_ROOT.resolve("mappings/legacy");
    private static final Path BODY_ROOT = WIREMOCK_ROOT.resolve("__files");
    private static final Pattern BODY_FILE_NAME_PATTERN = Pattern.compile("\"bodyFileName\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern VERSION_ELEMENT_PATTERN = Pattern.compile(
        "<(version|account_version|creditor_account_version)>[^<]+</\\1>"
    );

    @Test
    void v2MappingsUseV2BodiesForVersionedResponses() throws IOException {
        List<String> failures = Files.walk(MAPPINGS_ROOT)
            .filter(path -> path.getFileName().toString().endsWith("_v2.json"))
            .filter(WiremockV2VersionTest::usesVersionedNonV2Body)
            .map(Path::toString)
            .toList();

        assertTrue(failures.isEmpty(), "v2 mappings share version-bearing v1 bodies: " + String.join(", ", failures));
    }

    @Test
    void v2BodiesUseVersionsAboveLongMax() throws IOException {
        List<String> failures = Files.walk(BODY_ROOT)
            .filter(path -> path.getFileName().toString().endsWith("_v2.xml"))
            .filter(WiremockV2VersionTest::hasWrongVersionValue)
            .map(Path::toString)
            .toList();

        assertTrue(failures.isEmpty(), "v2 bodies contain fixed-width version values: " + String.join(", ", failures));
    }

    @Test
    void creditorResponsesHaveVersionedV2Bodies() throws IOException {
        assertV2BodyHasOverLongVersion("legacy/MajorCreditor/getMajorCreditorAtAGlance_v2.xml");
        assertV2BodyHasOverLongVersion("legacy/MajorCreditor/getMajorCreditorHeaderSummary_v2.xml");
        assertV2BodyHasOverLongVersion("legacy/MinorCreditor/getMinorCreditorAtAGlance_v2.xml");
        assertV2BodyHasOverLongVersion("legacy/MinorCreditor/getMinorCreditorHeaderSummary_v2.xml");
        assertV2BodyHasOverLongVersion("legacy/MinorCreditor/getMinorCreditorAccount_v2.xml");
    }

    private static boolean usesVersionedNonV2Body(Path mappingPath) {
        try {
            return bodyFileNames(mappingPath).stream()
                .filter(bodyFileName -> !bodyFileName.endsWith("_v2.xml"))
                .map(BODY_ROOT::resolve)
                .anyMatch(WiremockV2VersionTest::containsVersionElement);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<String> bodyFileNames(Path mappingPath) throws IOException {
        return BODY_FILE_NAME_PATTERN.matcher(Files.readString(mappingPath))
            .results()
            .map(result -> result.group(1))
            .toList();
    }

    private static boolean containsVersionElement(Path bodyPath) {
        try {
            return Files.exists(bodyPath) && VERSION_ELEMENT_PATTERN.matcher(Files.readString(bodyPath)).find();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static boolean hasWrongVersionValue(Path bodyPath) {
        try {
            String wrongValues = VERSION_ELEMENT_PATTERN.matcher(Files.readString(bodyPath))
                .results()
                .map(MatchResult::group)
                .filter(element -> !element.contains(OVER_LONG_MAX_VALUE))
                .collect(joining(", "));

            return !wrongValues.isEmpty();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void assertV2BodyHasOverLongVersion(String bodyFileName) throws IOException {
        String body = Files.readString(BODY_ROOT.resolve(bodyFileName));
        assertTrue(body.contains(OVER_LONG_MAX_VALUE), bodyFileName + " should contain an over-long version value");
        assertFalse(hasWrongVersionValue(BODY_ROOT.resolve(bodyFileName)),
                    bodyFileName + " should not contain fixed-width version values");
    }
}
