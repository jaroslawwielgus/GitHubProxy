package com.github.proxy;

import java.util.List;

record RepositoryResponse(String repositoryName, String ownerLogin, List<BranchResponse> branches) {}
