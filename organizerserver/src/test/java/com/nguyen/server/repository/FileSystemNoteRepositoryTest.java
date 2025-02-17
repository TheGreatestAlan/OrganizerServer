package com.nguyen.server.repository;

import com.nguyen.server.interfaces.Note;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FileSystemNoteRepositoryTest {

    private static final String TEST_DIRECTORY = "test-notes";
    private FileSystemNoteRepository noteRepository;

    @BeforeEach
    void setupTestDirectory() throws IOException {
        // Clean up the test directory if it exists
        Path testDir = Paths.get(TEST_DIRECTORY);
        if (Files.exists(testDir)) {
            Files.walk(testDir)
                    .sorted(Comparator.reverseOrder()) // Reverse order to delete files before directories
                    .map(Path::toFile)
                    .forEach(file -> {
                        if (!file.delete()) {
                            System.err.println("Failed to delete: " + file.getAbsolutePath());
                        }
                    });
        }
        // Recreate the test directory
        Files.createDirectories(testDir);

        // Initialize the repository
        noteRepository = new FileSystemNoteRepository(TEST_DIRECTORY);
    }

    @Test
    void testAddOrUpdateNoteAndGetNoteByName() {
        // Arrange
        String noteName = "TestNote";
        String noteContent = "This is a test note.";

        // Act
        noteRepository.addOrUpdateNote(noteName, noteContent);
        Optional<Note> retrievedNote = noteRepository.getNoteByName(noteName);

        // Assert
        assertTrue(retrievedNote.isPresent(), "Note should be present");
        assertEquals(noteName, retrievedNote.get().getName(), "Note name should match");
        assertEquals(noteContent, retrievedNote.get().getContent(), "Note content should match");
    }

    @Test
    void testGetAllNotes() {
        // Arrange
        noteRepository.addOrUpdateNote("Note1", "Content for Note1");
        noteRepository.addOrUpdateNote("Note2", "Content for Note2");

        // Act
        List<Note> allNotes = noteRepository.getAllNotes();

        // Assert
        assertEquals(2, allNotes.size(), "There should be 2 notes");
        assertTrue(allNotes.stream().anyMatch(note -> note.getName().equals("Note1")), "Note1 should exist");
        assertTrue(allNotes.stream().anyMatch(note -> note.getName().equals("Note2")), "Note2 should exist");
    }

    @Test
    void testSearchNotes() {
        // Arrange
        noteRepository.addOrUpdateNote("ShoppingList", "Buy milk, eggs, and bread.");
        noteRepository.addOrUpdateNote("WorkoutPlan", "Run 5km, do push-ups, and stretch.");

        // Act
        List<Note> searchResults = noteRepository.searchNotes("milk");

        // Assert
        assertEquals(1, searchResults.size(), "There should be 1 note matching the keyword");
        assertEquals("ShoppingList", searchResults.get(0).getName(), "Matching note name should be ShoppingList");
    }

    @Test
    void testListAllTitles() {
        // Arrange
        noteRepository.addOrUpdateNote("Title1", "Content1");
        noteRepository.addOrUpdateNote("Title2", "Content2");

        // Act
        List<String> titles = noteRepository.listAllTitles();

        // Assert
        assertEquals(2, titles.size(), "There should be 2 titles");
        assertTrue(titles.contains("Title1"), "Title1 should exist");
        assertTrue(titles.contains("Title2"), "Title2 should exist");
    }

    @Test
    void testDeleteNoteByName() {
        // Arrange
        String noteName = "NoteToDelete";
        noteRepository.addOrUpdateNote(noteName, "This note will be deleted.");

        // Act
        noteRepository.deleteNoteByName(noteName);
        Optional<Note> retrievedNote = noteRepository.getNoteByName(noteName);

        // Assert
        assertFalse(retrievedNote.isPresent(), "Note should be deleted");
    }

    @Test
    void testListNotesFromSubdirectories() throws IOException {
        // Arrange: Create files in subdirectories
        Path subDir1 = Paths.get(TEST_DIRECTORY, "subdir1");
        Path subDir2 = Paths.get(TEST_DIRECTORY, "subdir1", "subdir2");
        Files.createDirectories(subDir1);
        Files.createDirectories(subDir2);

        noteRepository.addOrUpdateNote("note1", "Content of root note");
        noteRepository.addOrUpdateNote("subdir1/note2", "Content of note in subdir1");
        noteRepository.addOrUpdateNote("subdir1/subdir2/note3", "Content of note in subdir2");

        // Act
        List<Note> allNotes = noteRepository.getAllNotes();

        // Assert
        assertEquals(3, allNotes.size(), "There should be 3 notes in total");
        assertTrue(allNotes.stream().anyMatch(note -> note.getName().equals("note1")), "note1 should exist in root");
        assertTrue(allNotes.stream().anyMatch(note -> note.getName().equals("subdir1/note2")), "note2 should exist in subdir1");
        assertTrue(allNotes.stream().anyMatch(note -> note.getName().equals("subdir1/subdir2/note3")), "note3 should exist in subdir2");
    }

    @Test
    void testIgnoreNonMarkdownFiles() throws IOException {
        // Arrange: Add a non-markdown file
        Path nonMarkdownFile = Paths.get(TEST_DIRECTORY, "nonMarkdown.txt");
        Files.writeString(nonMarkdownFile, "This should be ignored.");

        noteRepository.addOrUpdateNote("ValidNote", "This is a valid markdown note.");

        // Act
        List<Note> allNotes = noteRepository.getAllNotes();

        // Assert
        assertEquals(1, allNotes.size(), "Only markdown files should be listed");
        assertTrue(allNotes.stream().anyMatch(note -> note.getName().equals("ValidNote")), "ValidNote should exist");
    }
}
