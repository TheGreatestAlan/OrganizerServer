package com.nguyen.server.repository;

import com.nguyen.server.OrganizerRepositoryException;
import com.nguyen.server.interfaces.OrganizerRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;

import java.io.IOException;
import java.util.*;

public class GitFileSystemRepositoryDecorator implements OrganizerRepository {
    private final FileSystemRepository decoratedRepository;
    private final Repository repository;
    private final CredentialsProvider credentialsProvider;

    public GitFileSystemRepositoryDecorator(FileSystemRepository decoratedRepository, String repositoryPath, String githubUser, String githubToken) {
        this.decoratedRepository = decoratedRepository;
        try {
            this.repository = GitRepositoryUtils.buildRepository(repositoryPath);
            this.credentialsProvider = GitRepositoryUtils.createCredentialsProvider(githubUser, githubToken);
        } catch (IOException e) {
            throw new OrganizerRepositoryException("Repository initialization failed", e);
        }
    }

    @Override
    public List<String> getTodoList() {
        GitRepositoryUtils.syncWithRemote(repository, credentialsProvider);
        return decoratedRepository.getTodoList();
    }

    @Override
    public Map<String, List<String>> getOrganizerInventory() {
        GitRepositoryUtils.syncWithRemote(repository, credentialsProvider);
        return decoratedRepository.getOrganizerInventory();
    }

    @Override
    public List<String> getContainerById(String containerId) {
        GitRepositoryUtils.syncWithRemote(repository, credentialsProvider);
        return decoratedRepository.getContainerById(containerId);
    }

    @Override
    public List<String> getContainerLocation() {
        GitRepositoryUtils.syncWithRemote(repository, credentialsProvider);
        return decoratedRepository.getContainerLocation();
    }

    @Override
    public void saveOrganizerInventory(Map<String, List<String>> inventory) {
        GitRepositoryUtils.syncWithRemote(repository, credentialsProvider);
        decoratedRepository.saveOrganizerInventory(inventory);
        GitRepositoryUtils.commitAndPush(repository, credentialsProvider, "Update inventory: " + new Date());
    }

    @Override
    public void addOrganizerInventory(String item, String containerId) {
        GitRepositoryUtils.syncWithRemote(repository, credentialsProvider);
        decoratedRepository.addOrganizerInventory(item, containerId);
    }

    @Override
    public void deleteOrganizerInventory(String item, String containerId) {
        GitRepositoryUtils.syncWithRemote(repository, credentialsProvider);
        decoratedRepository.deleteOrganizerInventory(item, containerId);
        GitRepositoryUtils.commitAndPush(repository, credentialsProvider, "Remove item: " + item + " from " + containerId);
    }
}