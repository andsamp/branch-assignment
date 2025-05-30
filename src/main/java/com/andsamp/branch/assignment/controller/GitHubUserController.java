package com.andsamp.branch.assignment.controller;

import com.andsamp.branch.assignment.model.GitHubUser;
import com.andsamp.branch.assignment.service.GitHubUserDetailService;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Validated
@Slf4j
public class GitHubUserController {

    private final GitHubUserDetailService gitHubUserDetailService;

    @Autowired
    public GitHubUserController(GitHubUserDetailService gitHubUserDetailServiceRestClientImpl) {
        this.gitHubUserDetailService = gitHubUserDetailServiceRestClientImpl;
    }

    @GetMapping("/{username}")
    public GitHubUser getUser(
            @PathVariable
                    @Pattern(
                            regexp = "^(?i)[a-z0-9](?:[a-z0-9]|-(?=[a-z0-9])){0,38}$",
                            message =
                                    "Invalid username. Usernames for user accounts on GitHub can only contain alphanumeric characters and dashes ( - ). Maximum of 39 Characters.")
                    String username) {

        return this.gitHubUserDetailService.getGitHubUser(username);
    }
}
