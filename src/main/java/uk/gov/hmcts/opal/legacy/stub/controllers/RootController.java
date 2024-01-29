package uk.gov.hmcts.opal.legacy.stub.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

/**
 * Default endpoints per application.
 */
@RestController
public class RootController {

    /**
     * Root GET endpoint.
     *
     * <p>Azure application service has a hidden feature of making requests to root endpoint when
     * "Always On" is turned on.
     * This is the endpoint to deal with that and therefore silence the unnecessary 404s as a response code.
     *
     * @return Welcome message from the service.
     */
    @GetMapping("/")
    public ResponseEntity<String> welcome() {
        return ok().contentType(MediaType.TEXT_HTML).body("""
            <!DOCTYPE html>
            <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>opal-legacy-db-stub Server</title>
                </head>
                <body>
                    <h2>Welcome to the opal-legacy-db-stub Server. This is an API server.</h2>
                    <p>You can explore these endpoints to learn how Wiremock can be used to simulate an API:</p>
                    <ul>
                        <li><a href="hello" target="_blank">/hello</a> - This url uses a Wiremock <i>mappings</i> file
                        (found in the <i>mappings</i> directory) called <i>hello.json</i> that specifies both a mapping
                        url path to match, and an actual response body (which could be text, HTML, JSON, etc).</li>
                        <li><a href="poc_file.html" target="_blank">/file.html</a> - This url returns a file found in the
                        <i>__files</i> directory that is returned directly, with no mapping process performed by
                        Wiremock.</li>
                        <li><a href="template/123456/abcde" target="_blank">/template/xxxxx/yyyyy</a> - This url uses a
                        Wiremock <i>mappings</i> file (found in the <i>mappings</i> directory) called
                        <i>poc_template.json</i> that uses both <a href='https://wiremock.org/docs/request-matching/'
                        target='_blank'>request matching</a> and the <a href="https://handlebarsjs.com/guide/"
                        target="_blank">Handlebars templating</a> engine to extract data from the request and
                        populate it into the response.</li>
                        <li><a href="account/1" target="_blank">/account/1</a> - This url uses a Wiremock
                        <i>mappings</i> file called <i>poc_handlebars.json</i> that takes part of the url path to
                        derive a filename in the <i>__files</i> directory that gets served up as the response
                        body. Try also <a href="account/2" target="_blank">/account/2</a> that responds with
                        a different body file</li>
                    </ul>
                </body>
            </html>
       """);
    }
}
