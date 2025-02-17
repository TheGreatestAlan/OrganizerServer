package com.nguyen.server.repository;

import com.nguyen.server.OrganizerRepositoryException;
import com.nguyen.server.interfaces.OrganizerRepository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GitFileSystemRepositoryDecorator implements OrganizerRepository {
    private final FileSystemRepository decoratedRepository;
    private final Repository repository;
    private final CredentialsProvider credentialsProvider;

    public GitFileSystemRepositoryDecorator(FileSystemRepository decoratedRepository,
                                            String repositoryPath, String githubUser, String githubToken) {
        this.decoratedRepository = decoratedRepository;
        try {
            this.repository = buildRepository(repositoryPath);
            this.credentialsProvider = createCredentialsProvider(githubUser, githubToken);
        } catch (IOException e) {
            throw new OrganizerRepositoryException("Repository initialization failed", e);
        }
    }

    private CredentialsProvider createCredentialsProvider(String gitUser, String githubToken) {
        if (gitUser == null || githubToken == null) {
            throw new OrganizerRepositoryException(
                    "GitHub credentials not configured. Set GITHUB_USERNAME and GITHUB_TOKEN environment variables."
            );
        }

        return new UsernamePasswordCredentialsProvider(gitUser, githubToken);
    }

    private void syncWithRemote() {
        try (Git git = new Git(repository)) {
            git.pull()
                    .setCredentialsProvider(credentialsProvider)
                    .call();
        } catch (Exception e) {
            throw new OrganizerRepositoryException("Sync failed", e);
        }
    }

    private void commitAndPush(String message) {
        try (Git git = new Git(repository)) {
            git.add().addFilepattern(".").call();
            git.commit()
                    .setAuthor("Organizer System", "system@organizer")
                    .setMessage(message)
                    .call();

            git.push()
                    .setCredentialsProvider(credentialsProvider)
                    .call();
        } catch (Exception e) {
            throw new OrganizerRepositoryException("Push failed", e);
        }
    }

    @Override
    public List<String> getTodoList() {
        syncWithRemote();
        return decoratedRepository.getTodoList();
    }

    @Override
    public Map<String, List<String>> getOrganizerInventory() {
        syncWithRemote();
        return decoratedRepository.getOrganizerInventory();
    }

    @Override
    public List<String> getContainerById(String containerId) {
        syncWithRemote();
        return decoratedRepository.getContainerById(containerId);
    }

    @Override
    public List<String> getContainerLocation() {
        syncWithRemote();
        return decoratedRepository.getContainerLocation();
    }

    @Override
    public void saveOrganizerInventory(Map<String, List<String>> inventory) {
        syncWithRemote();
        decoratedRepository.saveOrganizerInventory(inventory);
        commitAndPush("Update inventory: " + new Date());
    }

    @Override
    public void addOrganizerInventory(String item, String containerId) {
        syncWithRemote();
        decoratedRepository.addOrganizerInventory(item, containerId);
    }

    @Override
    public void deleteOrganizerInventory(String item, String containerId) {
        syncWithRemote();
        decoratedRepository.deleteOrganizerInventory(item, containerId);
        commitAndPush("Remove item: " + item + " from " + containerId);
    }

    private Repository buildRepository(String repositoryPath) throws IOException {
        return new FileRepositoryBuilder()
                .setGitDir(new File(repositoryPath, ".git"))
                .build();
    }
}