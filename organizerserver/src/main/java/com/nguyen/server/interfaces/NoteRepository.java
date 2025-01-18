package com.nguyen.server.interfaces;

import java.util.List;
import java.util.Optional;

public interface NoteRepository {

    /**
     * Fetches a note by its name.
     *
     * @param name The name of the note to retrieve.
     * @return An Optional containing the note if found, or an empty Optional if not.
     */
    Optional<Note> getNoteByName(String name);

    /**
     * Fetches all notes.
     *
     * @return A list of all notes.
     */
    List<Note> getAllNotes();

    /**
     * Searches for notes that contain the specified keyword in their name or content.
     *
     * @param keyword The keyword to search for.
     * @return A list of notes that match the search criteria.
     */
    List<Note> searchNotes(String keyword);

    /**
     * Lists the titles of all notes.
     *
     * @return A list of note titles.
     */
    List<String> listAllTitles();
}
