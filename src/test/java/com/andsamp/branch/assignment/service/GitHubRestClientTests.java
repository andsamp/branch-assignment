package com.andsamp.branch.assignment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withForbiddenRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

import com.andsamp.branch.assignment.exception.GitHubClientException;
import com.andsamp.branch.assignment.exception.GitHubEntityNotFoundException;
import com.andsamp.branch.assignment.exception.RateLimitExceededException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;

@RestClientTest(GitHubRestClientService.class)
@TestPropertySource(properties = {"github.client.page_size=100"})
public class GitHubRestClientTests {

    @Autowired
    GitHubRestClientService gitHubRestClientService;

    @Autowired
    MockRestServiceServer mockServer;

    @Test
    void getGitHubUser_encounters500() {
        String username = "someone";

        mockServer
                .expect(ExpectedCount.times(3), requestTo("https://api.github.com/users/" + username))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError().body("{}"));

        GitHubClientException thrown =
                assertThrows(GitHubClientException.class, () -> gitHubRestClientService.getGitHubUser(username));
        assertEquals(500, thrown.getStatusCode().value());
    }

    @Test
    void getGitHubUser_encounters404() {
        String username = "someone";

        mockServer
                .expect(requestTo("https://api.github.com/users/" + username))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withResourceNotFound().body("{}"));

        assertThrows(GitHubEntityNotFoundException.class, () -> gitHubRestClientService.getGitHubUser(username));
    }

    @Test
    void getGitHubUser_encounters403() {
        String username = "someone";
        HttpHeaders rateLimitExceededHeaders = new HttpHeaders();
        rateLimitExceededHeaders.set("x-ratelimit-reset", "1748405746");
        rateLimitExceededHeaders.set("x-ratelimit-remaining", "0");

        mockServer
                .expect(requestTo("https://api.github.com/users/" + username))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withForbiddenRequest().body("{}").headers(rateLimitExceededHeaders));

        assertThrows(RateLimitExceededException.class, () -> gitHubRestClientService.getGitHubUser(username));
    }

    @Test
    void getGitHubUser_encounters401() {
        String username = "someone";

        mockServer
                .expect(ExpectedCount.times(3), requestTo("https://api.github.com/users/" + username))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withUnauthorizedRequest().body("{}"));

        GitHubClientException thrown =
                assertThrows(GitHubClientException.class, () -> gitHubRestClientService.getGitHubUser(username));
        assertEquals(401, thrown.getStatusCode().value());
    }

    @Test
    void getGitHubUserRepos_encounters500() {
        String username = "someone";
        int page = 1;

        mockServer
                .expect(
                        ExpectedCount.times(3),
                        requestTo("https://api.github.com/users/" + username + "/repos?per_page=100&page=" + page
                                + "&sort=full_name"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError().body("{}"));

        GitHubClientException thrown = assertThrows(
                GitHubClientException.class, () -> gitHubRestClientService.getGitHubUserRepos(username, page));
        assertEquals(500, thrown.getStatusCode().value());
    }

    @Test
    void getGitHubUserRepos_encounters404() {
        String username = "someone";
        int page = 42;

        mockServer
                .expect(requestTo("https://api.github.com/users/" + username + "/repos?per_page=100&page=" + page
                        + "&sort=full_name"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withResourceNotFound().body("{}"));

        assertThrows(
                GitHubEntityNotFoundException.class, () -> gitHubRestClientService.getGitHubUserRepos(username, page));
    }

    @Test
    void getGitHubUserRepos_encounters403() {
        String username = "someone";
        int page = 27;
        HttpHeaders rateLimitExceededHeaders = new HttpHeaders();
        rateLimitExceededHeaders.set("x-ratelimit-reset", "1748405746");
        rateLimitExceededHeaders.set("x-ratelimit-remaining", "0");

        mockServer
                .expect(requestTo("https://api.github.com/users/" + username + "/repos?per_page=100&page=" + page
                        + "&sort=full_name"))
                .andRespond(withForbiddenRequest().body("{}").headers(rateLimitExceededHeaders));

        assertThrows(
                RateLimitExceededException.class, () -> gitHubRestClientService.getGitHubUserRepos(username, page));
    }

    @Test
    void getGitHubUserRepos_encounters401() {
        String username = "someone";
        int page = 1;

        mockServer
                .expect(
                        ExpectedCount.times(3),
                        requestTo("https://api.github.com/users/" + username + "/repos?per_page=100&page=" + page
                                + "&sort=full_name"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withUnauthorizedRequest().body("{}"));

        GitHubClientException thrown = assertThrows(
                GitHubClientException.class, () -> gitHubRestClientService.getGitHubUserRepos(username, page));
        assertEquals(401, thrown.getStatusCode().value());
    }
}
