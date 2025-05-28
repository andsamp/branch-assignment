package com.andsamp.branch.assignment.service;

import com.andsamp.branch.assignment.model.GitHubUserDetails;

public interface GitHubUserDetailService {

    /**
     * Retrieves basic GitHub user profile info including a list of all public repositories.
     *
     * @param username
     * @return found user details
     */
    GitHubUserDetails getGitHubUserDetails(String username);
}
