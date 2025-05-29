package com.andsamp.branch.assignment.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.net.URI;
import java.util.Date;

public record GitHubUser(
        String login,
        @JsonAlias("avatar_url") URI avatarUrl,
        @JsonAlias("html_url") URI url,
        String name,
        String location,
        String email,
        @JsonAlias("created_at") Date createdAt,
        @JsonAlias("public_repos") int publicRepoCount) {}
