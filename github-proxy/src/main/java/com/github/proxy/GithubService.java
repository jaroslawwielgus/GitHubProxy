package com.github.proxy;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
class GithubService {

    private final GithubClient githubClient;

    GithubService(GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    List<RepositoryResponse> getNonForkRepositories(String username) {
        return githubClient.fetchRepositories(username).stream()
                .filter(repo -> !repo.isFork())
                .map(repo -> new RepositoryResponse(
                        repo.getName(),
                        repo.getOwner().getLogin(),
                        fetchBranches(username, repo.getName())
                ))
                .toList();
    }

    private List<BranchResponse> fetchBranches(String username, String repoName) {
        return githubClient.fetchBranches(username, repoName).stream()
                .map(branch -> new BranchResponse(branch.getName(), branch.getCommit().getSha()))
                .toList();
    }
}
