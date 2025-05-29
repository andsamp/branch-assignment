package com.andsamp.branch.assignment.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Date;
import java.util.List;

public record GitHubUserDetails(
        @JsonProperty("user_name") String login,
        @JsonProperty("display_name") String name,
        URI avatar,
        @JsonProperty("geo_location") String location,
        String email,
        URI url,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss") @JsonProperty("created_at")
                Date createdAt,
        List<GitHubUserRepository> repos) {}
