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
import com.andsamp.branch.assignment.model.GitHubUserDetails;
import com.andsamp.branch.assignment.model.GitHubUserRepository;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@TestPropertySource(properties = {"github.client.page_size=100"})
public class GitHubUserDetailServiceRestClientImplTests {
    @MockitoBean
    GitHubRestClientService gitHubRestClientService;

    @Autowired
    GitHubUserDetailServiceRestClientImpl gitHubUserService;

    @Test
    void getUserDetails_shouldReturnWith0Repos() {
        String username = "samiam";
        GitHubUser gitHubUser = new GitHubUser(
                "samiam",
                "ava://tar.url",
                "u://r.l",
                "name",
                "there",
                "superoldemailaddress@aol.com",
                new Date(1748406464214L),
                0);
        GitHubUserDetails expected = new GitHubUserDetails(
                "samiam",
                "name",
                "ava://tar.url",
                "there",
                "superoldemailaddress@aol.com",
                "u://r.l",
                new Date(1748406464214L),
                new ArrayList<>());

        when(gitHubRestClientService.getGitHubUser(username)).thenReturn(gitHubUser);

        GitHubUserDetails actual = gitHubUserService.getGitHubUserDetails(username);

        assertEquals(expected, actual);

        verify(gitHubRestClientService, times(1)).getGitHubUser(username);
        verify(gitHubRestClientService, never()).getGitHubUserRepos(anyString(), anyInt());
    }

    @Test
    void getUserDetails_shouldReturnWithRepos() throws MalformedURLException {
        String username = "samiam";
        GitHubUser gitHubUser = new GitHubUser(
                "samiam",
                "ava://tar.url",
                "u://r.l",
                "name",
                "there",
                "superoldemailaddress@aol.com",
                new Date(1748406464214L),
                200);

        GitHubUserRepository repo = new GitHubUserRepository("repo-1", "https://github/samiam/repo-1");
        GitHubUserDetails expected = new GitHubUserDetails(
                "samiam",
                "name",
                "ava://tar.url",
                "there",
                "superoldemailaddress@aol.com",
                "u://r.l",
                new Date(1748406464214L),
                Arrays.asList(repo, repo));

        when(gitHubRestClientService.getGitHubUser(username)).thenReturn(gitHubUser);
        GitHubUserRepository[] repos = {};
        when(gitHubRestClientService.getGitHubUserRepos(anyString(), anyInt())).thenReturn(repos);

        GitHubUserDetails actual = gitHubUserService.getGitHubUserDetails(username);

        //        assertEquals(expected, actual);

        verify(gitHubRestClientService, times(1)).getGitHubUser(username);
        verify(gitHubRestClientService, times(1)).getGitHubUserRepos(username, 1);
        verify(gitHubRestClientService, times(1)).getGitHubUserRepos(username, 2);
    }

    @Test
    void getUserDetails_shouldThrowOnGitHubEntityNotFoundExceptionFromGetGitHubUser() {
        String username = "test";

        when(gitHubRestClientService.getGitHubUser(username))
                .thenThrow(new GitHubEntityNotFoundException("Unable to find user. Who took my glasses?"));

        assertThrows(GitHubEntityNotFoundException.class, () -> gitHubUserService.getGitHubUserDetails(username));

        verify(gitHubRestClientService, times(1)).getGitHubUser(username);
        verify(gitHubRestClientService, never()).getGitHubUserRepos(anyString(), anyInt());
    }
}
