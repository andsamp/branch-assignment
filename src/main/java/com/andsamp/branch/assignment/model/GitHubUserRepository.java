package com.andsamp.branch.assignment.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GitHubUserRepository {
    private String name;

    @Setter(onMethod = @__(@JsonSetter(value = "html_url")))
    @Getter(onMethod = @__(@JsonGetter(value = "url")))
    private String url;
}
