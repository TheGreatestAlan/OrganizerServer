package com.nguyen.server.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nguyen.server.interfaces.Note;

public class NoteDto {

    @JsonProperty("id")
    private final String id;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("content")
    private final String content;

    public NoteDto(String id, String name, String content) {
        this.id = id;
        this.name = name;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public static NoteDto from(Note note) {
        return new NoteDto(note.getId(), note.getName(), note.getContent());
    }
}
