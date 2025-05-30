package com.andsamp.branch.assignment.service;

import com.andsamp.branch.assignment.model.GitHubUser;

public interface GitHubUserDetailService {

    /**
     * Retrieves basic GitHub user profile info including a list of all public repositories.
     *
     * @param username
     * @return found user details
     */
    GitHubUser getGitHubUser(String username);
}
