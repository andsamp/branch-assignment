package com.andsamp.branch.assignment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.andsamp.branch.assignment.exception.GitHubEntityNotFoundException;
import com.andsamp.branch.assignment.model.GitHubUser;
import com.andsamp.branch.assignment.model.GitHubUserRepository;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@TestPropertySource(properties = {"github.client.page_size=100"})
@Slf4j
public class GitHubUserDetailServiceRestClientImplTests {
    @MockitoBean
    GitHubRestClientService gitHubRestClientService;

    @Autowired
    GitHubUserDetailServiceRestClientImpl gitHubUserService;

    @Test
    void getUserDetails_shouldReturnWith0Repos() throws URISyntaxException {
        String username = "samiam";
        GitHubUser gitHubUser = new GitHubUser(
                "samiam",
                "name",
                new URI("https://localhost:8080/avatar.jpg"),
                "there",
                "superoldemailaddress@aol.com",
                new URI("https://localhost:8080/url"),
                new Date(1748406464214L),
                0,
                null);
        GitHubUser expected = new GitHubUser(
                "samiam",
                "name",
                new URI("https://localhost:8080/avatar.jpg"),
                "there",
                "superoldemailaddress@aol.com",
                new URI("https://localhost:8080/url"),
                new Date(1748406464214L),
                0,
                new ArrayList<>());

        when(gitHubRestClientService.getGitHubUser(username)).thenReturn(gitHubUser);

        GitHubUser actual = gitHubUserService.getGitHubUser(username);

        assertEquals(expected, actual);

        verify(gitHubRestClientService, times(1)).getGitHubUser(username);
        verify(gitHubRestClientService, never()).getGitHubUserRepos(anyString(), anyInt());
    }

    @Test
    void getUserDetails_shouldReturnWithRepos() throws MalformedURLException, URISyntaxException {
        String username = "samiam";
        GitHubUser gitHubUser = new GitHubUser(
                "samiam",
                "name",
                new URI("https://localhost:8080/avatar.jpg"),
                "there",
                "superoldemailaddress@aol.com",
                new URI("https://localhost:8080/url"),
                new Date(1748406464214L),
                200,
                null);

        GitHubUserRepository repo = new GitHubUserRepository("repo-1", new URI("https://github.com/samiam/repo-1"));
        GitHubUser expected = new GitHubUser(
                "samiam",
                "name",
                new URI("https://localhost:8080/avatar.jpg"),
                "there",
                "superoldemailaddress@aol.com",
                new URI("https://localhost:8080/url"),
                new Date(1748406464214L),
                200,
                Arrays.asList(repo, repo));

        when(gitHubRestClientService.getGitHubUser(username)).thenReturn(gitHubUser);
        GitHubUserRepository[] repos = {repo};
        when(gitHubRestClientService.getGitHubUserRepos(anyString(), anyInt())).thenReturn(repos);

        GitHubUser actual = gitHubUserService.getGitHubUser(username);

        assertEquals(expected, actual);

        verify(gitHubRestClientService, times(1)).getGitHubUser(username);
        verify(gitHubRestClientService, times(1)).getGitHubUserRepos(username, 1);
        verify(gitHubRestClientService, times(1)).getGitHubUserRepos(username, 2);
    }

    @Test
    void getUserDetails_shouldThrowOnGitHubEntityNotFoundExceptionFromGetGitHubUser() {
        String username = "test";

        when(gitHubRestClientService.getGitHubUser(username))
                .thenThrow(new GitHubEntityNotFoundException("Unable to find user. Who took my glasses?"));

        assertThrows(GitHubEntityNotFoundException.class, () -> gitHubUserService.getGitHubUser(username));

        verify(gitHubRestClientService, times(1)).getGitHubUser(username);
        verify(gitHubRestClientService, never()).getGitHubUserRepos(anyString(), anyInt());
    }
}
