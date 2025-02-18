package com.nguyen.server.repository;

import com.nguyen.server.OrganizerRepositoryException;
import com.nguyen.server.interfaces.Note;
import com.nguyen.server.interfaces.NoteRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;

import java.io.IOException;
import java.util.*;

public class GitFileSystemNoteRepositoryDecorator implements NoteRepository{
    private final NoteRepository decoratedRepository;
    private final Repository gitRepository;
    private final CredentialsProvider credentialsProvider;

    public GitFileSystemNoteRepositoryDecorator(NoteRepository decoratedRepository, String repositoryPath, String gitUser, String gitToken) {
        this.decoratedRepository = decoratedRepository;
        try {
            this.gitRepository = GitRepositoryUtils.buildRepository(repositoryPath);
            this.credentialsProvider = GitRepositoryUtils.createTokenCredentialsProvider(gitUser, gitToken);
        } catch (IOException e) {
            throw new OrganizerRepositoryException("Failed to initialize Git repository", e);
        }
    }

    @Override
    public Optional<Note> getNoteByName(String name) {
        GitRepositoryUtils.syncWithRemote(gitRepository, credentialsProvider);
        return decoratedRepository.getNoteByName(name);
    }

    @Override
    public List<Note> getAllNotes() {
        GitRepositoryUtils.syncWithRemote(gitRepository, credentialsProvider);
        return decoratedRepository.getAllNotes();
    }

    @Override
    public List<Note> searchNotes(String keyword) {
        GitRepositoryUtils.syncWithRemote(gitRepository, credentialsProvider);
        return decoratedRepository.searchNotes(keyword);
    }

    @Override
    public List<String> listAllTitles() {
        GitRepositoryUtils.syncWithRemote(gitRepository, credentialsProvider);
        return decoratedRepository.listAllTitles();
    }

}