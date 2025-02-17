package com.nguyen.server.repository;

import com.nguyen.server.interfaces.Note;
import com.nguyen.server.interfaces.NoteRepository;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class FileSystemNoteRepository implements NoteRepository {

    private final Path notesDirectory;

    public FileSystemNoteRepository(String notesDirectoryPath) {
        this.notesDirectory = Paths.get(notesDirectoryPath);
        if (!Files.isDirectory(this.notesDirectory)) {
            throw new IllegalArgumentException("The path must be a valid directory: " + notesDirectoryPath);
        }
    }

    @Override
    public Optional<Note> getNoteByName(String name) {
        Path notePath = notesDirectory.resolve(name.replace("/", FileSystems.getDefault().getSeparator()) + ".md");
        if (isValidNoteFile(notePath)) {
            return readFile(notePath)
                    .map(content -> new FileSystemNote(name, content));
        }
        return Optional.empty();
    }

    @Override
    public List<Note> getAllNotes() {
        try {
            return Files.walk(notesDirectory)
                    .filter(Files::isRegularFile)
                    .filter(this::isValidNoteFile)
                    .map(this::tryReadNoteFromFile)
                    .flatMap(Optional::stream) // Filter out Optional.empty() notes
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to list notes in directory tree: " + notesDirectory, e);
        }
    }

    @Override
    public List<Note> searchNotes(String keyword) {
        return getAllNotes().stream()
                .filter(note -> note.getName().contains(keyword) || note.getContent().contains(keyword))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listAllTitles() {
        return getAllNotes().stream()
                .map(Note::getName)
                .collect(Collectors.toList());
    }

    private Optional<Note> tryReadNoteFromFile(Path path) {
        String relativePath = notesDirectory.relativize(path).toString().replace(FileSystems.getDefault().getSeparator(), "/");
        String name = relativePath.replace(".md", "");
        return readFile(path).map(content -> new FileSystemNote(name, content));
    }

    private Optional<String> readFile(Path path) {
        try {
            return Optional.of(Files.readString(path));
        } catch (IOException e) {
            System.err.println("Failed to read file: " + path + " - " + e.getMessage());
            return Optional.empty();
        }
    }

    private void writeFile(Path path, String content) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to file: " + path, e);
        }
    }

    public void addOrUpdateNote(String name, String content) {
        Path notePath = notesDirectory.resolve(name.replace("/", FileSystems.getDefault().getSeparator()) + ".md");
        writeFile(notePath, content);
    }

    public void deleteNoteByName(String name) {
        Path notePath = notesDirectory.resolve(name.replace("/", FileSystems.getDefault().getSeparator()) + ".md");
        try {
            Files.deleteIfExists(notePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete note: " + notePath, e);
        }
    }

    /**
     * Validates whether the file is a valid note (only `.md` files).
     *
     * @param path The file path to check.
     * @return True if the file is a valid `.md` note; false otherwise.
     */
    private boolean isValidNoteFile(Path path) {
        return path.toString().endsWith(".md");
    }

    /**
     * Inner class to represent a note loaded from the file system.
     */
    private static class FileSystemNote implements Note {
        private final String name;
        private final String content;

        public FileSystemNote(String name, String content) {
            this.name = name;
            this.content = content;
        }

        @Override
        public String getId() {
            // Use the full name (including subdirectory path) as the ID.
            return name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getContent() {
            return content;
        }
    }
}
