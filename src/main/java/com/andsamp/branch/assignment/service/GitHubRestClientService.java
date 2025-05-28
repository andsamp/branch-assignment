package com.andsamp.branch.assignment.service;

import com.andsamp.branch.assignment.exception.GitHubClientException;
import com.andsamp.branch.assignment.exception.GitHubEntityNotFoundException;
import com.andsamp.branch.assignment.exception.RateLimitExceededException;
import com.andsamp.branch.assignment.model.GitHubUser;
import com.andsamp.branch.assignment.model.GitHubUserRepository;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class GitHubRestClientService {
    RestClient gitHubRestClient;
    int pageSize;

    @Autowired
    public GitHubRestClientService(
            RestClient.Builder restClientBuilder,
            @Value("${github.client.baseUrl:https://api.github.com/}") String baseUrl,
            @Value("${github.client.page_size}") int pageSize) {
        log.info("GitHubRestClientService:Constructor {} {}", baseUrl, pageSize);
        this.gitHubRestClient = restClientBuilder
                .baseUrl(baseUrl)
                .requestInterceptor((request, body, execution) -> {
                    // all this to prove that caching is indeed working.

                    log.debug("No cache hit; Request URL: {}", request.getURI());

                    ClientHttpResponse response = execution.execute(request, body);

                    log.info("Response Headers: {}", response.getHeaders()); // to keep track of rate limit.

                    return response;
                })
                .build();
        this.pageSize = pageSize;
    }

    @Cacheable("github-users")
    @Retryable(
            noRetryFor = {GitHubEntityNotFoundException.class, RateLimitExceededException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2.0, maxDelay = 1000))
    public GitHubUser getGitHubUser(String username) {
        // slowServiceWait();
        return this.gitHubRestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("users/{username}").build(username))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);

                    if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                        log.error(
                                "Unable to find user {}({}): {}: {}",
                                username,
                                request.getURI(),
                                response.getStatusCode(),
                                body);
                        throw new GitHubEntityNotFoundException(username + " not found");
                    }

                    String rateLimitRemaining = response.getHeaders().getFirst("x-ratelimit-remaining");

                    if ((response.getStatusCode() == HttpStatus.FORBIDDEN
                                    || response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS)
                            && "0".equals(rateLimitRemaining)) {
                        String rateLimitResetHeader = response.getHeaders().getFirst("x-ratelimit-reset");
                        assert rateLimitResetHeader != null;

                        LocalDateTime rateLimitReset =
                                LocalDateTime.ofEpochSecond(Long.parseLong(rateLimitResetHeader), 0, ZoneOffset.UTC);

                        log.error(
                                "Rate Limit exceeded when trying to retrieve user {}({}): Rate Limit Resets {}Z",
                                username,
                                request.getURI(),
                                rateLimitReset,
                                body);
                        throw new RateLimitExceededException("Rate Limit Exceeded when retrieving user " + username
                                + ". Rate Limit Resets: " + rateLimitReset);
                    }

                    log.error(
                            "Failed to retrieve user {}({}). Received {}({}): {}",
                            username,
                            request.getURI(),
                            response.getStatusCode(),
                            response.getStatusText(),
                            body);

                    throw new GitHubClientException(
                            "Failed to retrieve user " + username + "(" + request.getURI() + ")",
                            response.getStatusCode(),
                            body);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);

                    log.error(
                            "Failed to retrieve user {}({}). Received {}({}): {}",
                            username,
                            request.getURI(),
                            response.getStatusCode(),
                            response.getStatusText(),
                            body);

                    throw new GitHubClientException(
                            "Failed to retrieve user " + username + "(" + request.getURI() + ")",
                            response.getStatusCode(),
                            body);
                })
                .toEntity(GitHubUser.class)
                .getBody();
    }

    @Cacheable("github-user-repos")
    @Retryable(
            noRetryFor = {GitHubEntityNotFoundException.class, RateLimitExceededException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2.0, maxDelay = 1000))
    public GitHubUserRepository[] getGitHubUserRepos(String username, int page) {
        return this.gitHubRestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("users/{username}/repos")
                        .queryParam("per_page", this.pageSize)
                        .queryParam("page", page)
                        .queryParam("sort", "full_name")
                        .build(username))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);

                    if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                        log.error(
                                "Unable to find page {} of user {}'s repositories({}): {}",
                                page,
                                username,
                                request.getURI(),
                                body);
                        throw new GitHubEntityNotFoundException("Repositories not found for user " + username);
                    }

                    String rateLimitRemaining = response.getHeaders().getFirst("x-ratelimit-remaining");

                    if ((response.getStatusCode() == HttpStatus.FORBIDDEN
                                    || response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS)
                            && "0".equals(rateLimitRemaining)) {
                        String rateLimitResetHeader = response.getHeaders().getFirst("x-ratelimit-reset");
                        assert rateLimitResetHeader != null;

                        LocalDateTime rateLimitReset =
                                LocalDateTime.ofEpochSecond(Long.parseLong(rateLimitResetHeader), 0, ZoneOffset.UTC);

                        log.error(
                                "Rate Limit exceeded when trying to retrieve page {} of {}'s repos({}): Rate Limit Resets {}Z",
                                page,
                                username,
                                request.getURI(),
                                rateLimitReset,
                                body);
                        throw new RateLimitExceededException("Rate Limit Exceeded when retrieving user " + username
                                + ". Rate Limit Resets: " + rateLimitReset);
                    }

                    log.error(
                            "Failed to retrieve page {} of repositories for {}({}). Received {}({}): {}",
                            page,
                            username,
                            request.getURI(),
                            response.getStatusCode(),
                            response.getStatusText(),
                            body);

                    throw new GitHubClientException(
                            "Failed to retrieve page " + page + " of repositories for " + username,
                            response.getStatusCode(),
                            body);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);

                    log.error(
                            "Failed to retrieve page {} of repositories for {}({}). Received {}({}): {}",
                            page,
                            username,
                            request.getURI(),
                            response.getStatusCode(),
                            response.getStatusText(),
                            body);

                    throw new GitHubClientException(
                            "Failed to retrieve page " + page + " of repositories for " + username,
                            response.getStatusCode(),
                            body);
                })
                .toEntity(GitHubUserRepository[].class)
                .getBody();
    }

    //    private void slowServiceWait() {
    //        try {
    //            long time = 2000L;
    //            Thread.sleep(time);
    //        } catch (InterruptedException e) {
    //            throw new IllegalStateException(e);
    //        }
    //    }
}
