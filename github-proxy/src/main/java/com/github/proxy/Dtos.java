package com.github.proxy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
class GithubRepoDto {

    @JsonProperty("name")
    String name;

    @JsonProperty("owner")
    GithubOwnerDto owner;

    @JsonProperty("fork")
    boolean fork;

    String getName() { return name; }
    GithubOwnerDto getOwner() { return owner; }
    boolean isFork() { return fork; }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class GithubOwnerDto {

    @JsonProperty("login")
    String login;

    String getLogin() { return login; }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class GithubBranchDto {

    @JsonProperty("name")
    String name;

    @JsonProperty("commit")
    GithubCommitDto commit;

    String getName() { return name; }
    GithubCommitDto getCommit() { return commit; }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class GithubCommitDto {

    @JsonProperty("sha")
    String sha;

    String getSha() { return sha; }
}
