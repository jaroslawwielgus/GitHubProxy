package com.github.proxy;

class GithubUserNotFoundException extends RuntimeException {

    GithubUserNotFoundException(String username) {
        super("Github user '%s' not found".formatted(username));
    }
}
