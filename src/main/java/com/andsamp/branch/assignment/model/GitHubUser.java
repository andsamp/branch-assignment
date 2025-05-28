package com.andsamp.branch.assignment.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.Date;

public record GitHubUser(
        String login,
        @JsonAlias("avatar_url") String avatarUrl,
        @JsonAlias("html_url") String url,
        String name,
        String location,
        String email,
        @JsonAlias("created_at") Date createdAt,
        @JsonAlias("public_repos") int publicRepoCount) {}
