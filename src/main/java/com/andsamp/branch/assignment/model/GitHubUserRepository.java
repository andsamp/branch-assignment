package com.andsamp.branch.assignment.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.net.URI;
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
@JsonPropertyOrder({"name", "url"})
public class GitHubUserRepository {
    private String name;

    @Setter(onMethod = @__(@JsonSetter(value = "html_url")))
    @Getter(onMethod = @__(@JsonGetter(value = "url")))
    private URI url;
}
