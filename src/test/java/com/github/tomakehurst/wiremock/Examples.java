package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class Examples extends AcceptanceTestBase {

    @Test
    public void exactUrlOnly() {
        stubFor(get(urlEqualTo("/some/thing"))
                .willReturn(aResponse()
                    .withHeader("Content-Type", "text/plain")
                    .withBody("Hello world!")));

        assertThat(testClient.get("/some/thing").statusCode(), is(200));
        assertThat(testClient.get("/some/thing/else").statusCode(), is(404));
    }

    @Test
    public void urlRegexMatch() {
        stubFor(put(urlMatching("/thing/matching/[0-9]+"))
                .willReturn(aResponse().withStatus(200)));
    }

    @Test
    public void headerMatching() {
        stubFor(post(urlEqualTo("/with/headers"))
                .withHeader("Content-Type", equalTo("text/xml"))
                .withHeader("Accept", matching("text/.*"))
                .withHeader("etag", notMatching("abcd.*"))
                .withHeader("etag", containing("2134"))
                .willReturn(aResponse().withStatus(200)));
    }

    @Test
    public void bodyMatching() {
        stubFor(post(urlEqualTo("/with/body"))
                .withRequestBody(matching("<status>OK</status>"))
                .withRequestBody(notMatching("<status>ERROR</status>"))
                .willReturn(aResponse().withStatus(200)));
    }

    @Test
    public void priorities() {

        //Catch-all case
        stubFor(get(urlMatching("/api/.*")).atPriority(5)
                .willReturn(aResponse().withStatus(401)));

        //Specific case
        stubFor(get(urlEqualTo("/api/specific-resource")).atPriority(1) //1 is highest
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Resource state")));
    }

    @Test
    public void responseHeaders() {
        stubFor(get(urlEqualTo("/whatever"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Etag", "b13894794wb")));
    }

    @Test
    public void bodyFile() {
        stubFor(get(urlEqualTo("/body-file"))
                .willReturn(aResponse()
                        .withBodyFile("path/to/myfile.xml")));
    }

    @Test
    public void binaryBody() {
        stubFor(get(urlEqualTo("/binary-body"))
                .willReturn(aResponse()
                        .withBody(new byte[] { 1, 2, 3, 4 })));
    }

    @Test(expected=Exception.class)
    public void verifyAtLeastOnce() {
        verify(postRequestedFor(urlEqualTo("/verify/this"))
                .withHeader("Content-Type", equalTo("text/xml")));

        verify(3, postRequestedFor(urlEqualTo("/3/of/these")));
    }

    @Test
    public void findingRequests() {
        List<LoggedRequest> requests = findAll(putRequestedFor(urlMatching("/api/.*")));
    }

    @Test
    public void proxying() {
        stubFor(get(urlMatching("/other/service/.*"))
                .willReturn(aResponse().proxiedFrom("http://otherhost.com/approot")));
    }

    @Test
    public void proxyIntercept() {
        // Low priority catch-all proxies to otherhost.com by default
        stubFor(get(urlMatching(".*")).atPriority(10)
                .willReturn(aResponse().proxiedFrom("http://otherhost.com")));


        // High priority stub will send a Service Unavailable response
        // if the specified URL is requested
        stubFor(get(urlEqualTo("/api/override/123")).atPriority(1)
                .willReturn(aResponse().withStatus(503)));
    }

}