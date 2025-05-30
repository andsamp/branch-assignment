package com.andsamp.branch.assignment.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URI;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@JsonPropertyOrder({"login", "name", "avatar", "location", "email", "url", "created_at", "repos"})
public class GitHubUser {
    @JsonProperty("user_name")
    @JsonAlias("login")
    private String login;

    @JsonProperty("display_name")
    @JsonAlias("name")
    private String name;

    @JsonAlias("avatar_url")
    private URI avatar;

    @JsonProperty("geo_location")
    @JsonAlias("location")
    private String location;

    private String email;

    @JsonAlias("html_url")
    private URI url;

    @Getter(onMethod_ = {@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")})
    @Setter(onMethod_ = {@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")})
    @JsonProperty("created_at")
    private Date createdAt;

    @JsonAlias("public_repos")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private int publicRepoCount;

    @Setter
    private List<GitHubUserRepository> repos;
}
