package com.nguyen.server.repository;

import com.nguyen.server.OrganizerRepositoryException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;

public class GitRepositoryUtils {

    public static Repository buildRepository(String repositoryPath) throws IOException {
        return new FileRepositoryBuilder()
                .setGitDir(new File(repositoryPath, ".git"))
                .build();
    }

    public static CredentialsProvider createCredentialsProvider(String gitUser, String githubToken) {
        if (gitUser == null || githubToken == null) {
            throw new OrganizerRepositoryException(
                    "GitHub credentials not configured. Set GITHUB_USERNAME and GITHUB_TOKEN environment variables."
            );
        }
        return new UsernamePasswordCredentialsProvider(gitUser, githubToken);
    }

    public static CredentialsProvider createTokenCredentialsProvider(String username, String token) {
        if (token == null || username == null) {
            throw new OrganizerRepositoryException("no username or token passed in");
        }
        return new UsernamePasswordCredentialsProvider(username, token);
    }

    public static void syncWithRemote(Repository repository, CredentialsProvider credentialsProvider) {
        try (Git git = new Git(repository)) {
            git.pull()
                    .setCredentialsProvider(credentialsProvider)
                    .setRebase(true)
                    .call();
        } catch (Exception e) {
            throw new OrganizerRepositoryException("Sync failed", e);
        }
    }

    public static void commitAndPush(Repository repository, CredentialsProvider credentialsProvider, String message) {
        try (Git git = new Git(repository)) {
            git.add().addFilepattern(".").call();
            git.commit()
                    .setAuthor("System", "system@organizer")
                    .setMessage(message)
                    .call();
            git.push()
                    .setCredentialsProvider(credentialsProvider)
                    .call();
        } catch (Exception e) {
            throw new OrganizerRepositoryException("Push failed", e);
        }
    }
}