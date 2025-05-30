package com.andsamp.branch.assignment.service;

import com.andsamp.branch.assignment.model.GitHubUser;
import com.andsamp.branch.assignment.model.GitHubUserRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GitHubUserDetailServiceRestClientImpl implements GitHubUserDetailService {
    GitHubRestClientService gitHubRestClientService;
    int pageSize;

    @Autowired
    public GitHubUserDetailServiceRestClientImpl(
            GitHubRestClientService gitHubRestClientService, @Value("${github.client.page_size}") int pageSize) {
        this.gitHubRestClientService = gitHubRestClientService;
        this.pageSize = pageSize;
    }

    @Override
    public GitHubUser getGitHubUser(String username) {
        GitHubUser user = this.gitHubRestClientService.getGitHubUser(username);
        List<GitHubUserRepository> userRepositories =
                this.getAllPublicRepositoriesForUser(username, user.getPublicRepoCount());

        user.setRepos(userRepositories);

        return user;
    }

    private List<GitHubUserRepository> getAllPublicRepositoriesForUser(String username, int totalPublicRepos) {
        List<GitHubUserRepository> userRepositories = new ArrayList<>();
        int currentPage = 1;

        while (totalPublicRepos > 0) {
            userRepositories.addAll(
                    Arrays.asList(this.gitHubRestClientService.getGitHubUserRepos(username, currentPage)));

            totalPublicRepos -= this.pageSize;
            currentPage++;
        }

        return userRepositories;
    }
}
