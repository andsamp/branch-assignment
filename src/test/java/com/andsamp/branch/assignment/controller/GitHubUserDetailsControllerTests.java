package com.andsamp.branch.assignment.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.andsamp.branch.assignment.exception.GitHubClientException;
import com.andsamp.branch.assignment.exception.GitHubEntityNotFoundException;
import com.andsamp.branch.assignment.exception.RateLimitExceededException;
import com.andsamp.branch.assignment.model.GitHubUser;
import com.andsamp.branch.assignment.model.GitHubUserRepository;
import com.andsamp.branch.assignment.service.GitHubUserDetailService;
import java.net.URI;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GitHubUserController.class)
public class GitHubUserDetailsControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    GitHubUserDetailService gitHubUserDetailService;

    @Test
    void getUser_shouldFailValidation() throws Exception {
        this.mockMvc
                .perform(get("/users/-asdf-"))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(
                        content()
                                .string(
                                        containsString(
                                                "Usernames for user accounts on GitHub can only contain alphanumeric characters and dashes ( - ). Maximum of 39 Characters.")));

        verify(gitHubUserDetailService, never()).getGitHubUser(anyString());
    }

    @Test
    void getUser_shouldReturn429OnRateLimitExceededException() throws Exception {
        String username = "octocat";

        when(gitHubUserDetailService.getGitHubUser(username))
                .thenThrow(new RateLimitExceededException("RateLimitExceededException"));

        this.mockMvc
                .perform(get("/users/" + username))
                .andDo(print())
                .andExpect(status().is(429))
                .andExpect(content().string(containsString("RateLimitExceededException")));

        verify(gitHubUserDetailService, times(1)).getGitHubUser(username);
    }

    @Test
    void getUser_shouldReturn404OnGitHubEntityNotFoundException() throws Exception {
        String username = "octocat";

        when(gitHubUserDetailService.getGitHubUser(username))
                .thenThrow(new GitHubEntityNotFoundException(
                        "Unable to find user octocat", new Exception("I am the root cause of evil.")));

        this.mockMvc
                .perform(get("/users/" + username))
                .andDo(print())
                .andExpect(status().is(404))
                .andExpect(content().string(containsString("Unable to find user octocat")));

        verify(gitHubUserDetailService, times(1)).getGitHubUser(username);
    }

    @Test
    void getUser_shouldReturnStatusFromGitHubClientException() throws Exception {
        String username = "octocat";

        when(gitHubUserDetailService.getGitHubUser(username))
                .thenThrow(
                        new GitHubClientException("Failed to fetch the thing.", HttpStatus.INTERNAL_SERVER_ERROR, ""));

        this.mockMvc
                .perform(get("/users/" + username))
                .andDo(print())
                .andExpect(status().is(500))
                .andExpect(content().string(containsString("Failed to fetch the thing.")));

        verify(gitHubUserDetailService, times(1)).getGitHubUser(username);
    }

    @Test
    void getUser_shouldReturn200OnGitHubEntityFound() throws Exception {
        String username = "octocat";
        GitHubUserRepository repo = new GitHubUserRepository("name", new URI("https://github.com/octocat/repo-1"));

        GitHubUser gitHubUser = new GitHubUser(
                "login",
                "name",
                new URI("https://localhost:8080/avatar.jpg"),
                "here",
                "b@c.a",
                new URI("http://localhost:8080/url"),
                new Date(1748406464214L),
                200,
                List.of(repo));

        when(gitHubUserDetailService.getGitHubUser(username)).thenReturn(gitHubUser);

        this.mockMvc
                .perform(get("/users/" + username))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        content()
                                .json(
                                        """
{
  "user_name" : "login",
  "display_name" : "name",
  "avatar" : "https://localhost:8080/avatar.jpg",
  "geo_location" : "here",
  "email" : "b@c.a",
  "url" : "http://localhost:8080/url",
  "created_at" : "2025-05-28 04:27:44",
  "repos" : [ {
    "name" : "name",
    "url" : "https://github.com/octocat/repo-1"
  } ]
}"""));

        verify(gitHubUserDetailService, times(1)).getGitHubUser(username);
    }
}
