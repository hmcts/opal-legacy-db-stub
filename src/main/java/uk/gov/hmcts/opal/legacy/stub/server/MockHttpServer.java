package uk.gov.hmcts.opal.legacy.stub.server;

public interface MockHttpServer {

    void start();

    void stop();

    int httpPort();

    int httpsPort();
}
