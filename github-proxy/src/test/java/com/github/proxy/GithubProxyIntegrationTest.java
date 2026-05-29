package com.github.proxy;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock
@AutoConfigureTestRestTemplate
class GithubProxyIntegrationTest {

    @InjectWireMock
    WireMockServer wireMockServer;

    @Autowired
    TestRestTemplate restTemplate;

    @DynamicPropertySource
    static void overrideGithubApiBaseUrl(DynamicPropertyRegistry registry) {
        registry.add("github.api.base-url", () -> "http://localhost:${wiremock.server.port}");
    }

    @Test
    void shouldReturnNonForkRepositoriesWithBranches() {
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/users/octocat/repos"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {"name": "hello-world", "owner": {"login": "octocat"}, "fork": false},
                                  {"name": "forked-repo", "owner": {"login": "octocat"}, "fork": true}
                                ]
                                """)));

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/repos/octocat/hello-world/branches"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {"name": "main", "commit": {"sha": "abc123"}},
                                  {"name": "develop", "commit": {"sha": "def456"}}
                                ]
                                """)));

        ResponseEntity<RepositoryResponse[]> response = restTemplate.getForEntity(
                "/api/users/octocat/repositories", RepositoryResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        RepositoryResponse[] repos = response.getBody();
        assertThat(repos).hasSize(1);
        assertThat(repos[0].repositoryName()).isEqualTo("hello-world");
        assertThat(repos[0].ownerLogin()).isEqualTo("octocat");
        assertThat(repos[0].branches()).hasSize(2);
        assertThat(repos[0].branches().get(0).name()).isEqualTo("main");
        assertThat(repos[0].branches().get(0).lastCommitSha()).isEqualTo("abc123");
        assertThat(repos[0].branches().get(1).name()).isEqualTo("develop");
        assertThat(repos[0].branches().get(1).lastCommitSha()).isEqualTo("def456");
    }

    @Test
    void shouldReturnEmptyListWhenAllRepositoriesAreForks() {
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/users/forkonly/repos"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {"name": "forked-a", "owner": {"login": "forkonly"}, "fork": true},
                                  {"name": "forked-b", "owner": {"login": "forkonly"}, "fork": true}
                                ]
                                """)));

        ResponseEntity<RepositoryResponse[]> response = restTemplate.getForEntity(
                "/api/users/forkonly/repositories", RepositoryResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void shouldReturn404WithErrorBodyForNonExistingUser() {
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/users/ghost-user/repos"))
                .willReturn(WireMock.aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"message": "Not Found"}
                                """)));

        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                "/api/users/ghost-user/repositories", ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().message()).contains("ghost-user");
    }
}
