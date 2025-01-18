package com.nguyen.server.interfaces;

public interface Note {

    /**
     * Gets the ID of the note.
     *
     * @return The unique identifier of the note.
     */
    String getId();

    /**
     * Gets the name of the note.
     *
     * @return The name or title of the note.
     */
    String getName();

    /**
     * Gets the content of the note.
     *
     * @return The content or body of the note.
     */
    String getContent();
}
