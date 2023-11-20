package uk.gov.hmcts.opal.legacy.stub.config;

import java.security.SecureRandom;

public final class MockDatabase {

    private static final SecureRandom RANDOM = new SecureRandom();

    private MockDatabase() {

    }

    public static int getAccountNumber() {
        return RANDOM.nextInt();
    }
}
