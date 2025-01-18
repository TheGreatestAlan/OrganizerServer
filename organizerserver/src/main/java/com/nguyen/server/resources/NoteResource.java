package com.nguyen.server.resources;

import com.nguyen.server.interfaces.NoteRepository;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/notes")
@Produces(MediaType.APPLICATION_JSON)
public class NoteResource {

    private final NoteRepository noteRepository;

    public NoteResource(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    /**
     * Gets a list of all notes or a specific note by name.
     *
     * @param name Optional name of the note to retrieve.
     * @return A JSON response containing the note or the list of notes.
     */
    @GET
    public Response getNotes(@QueryParam("name") Optional<String> name) {
        try {
            if (name.isPresent()) {
                return noteRepository.getNoteByName(name.get())
                        .map(note -> Response.ok(NoteDto.from(note)).build())
                        .orElse(Response.status(Response.Status.NOT_FOUND)
                                .entity("Note not found").build());
            } else {
                return Response.ok(noteRepository.getAllNotes().stream()
                                .map(NoteDto::from)
                                .collect(Collectors.toList()))
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error fetching notes").build();
        }
    }

    /**
     * Searches for notes containing the specified keyword.
     *
     * @param keyword The keyword to search for.
     * @return A JSON response containing the matching notes.
     */
    @GET
    @Path("/search")
    public Response searchNotes(@QueryParam("keyword") String keyword) {
        try {
            if (keyword == null || keyword.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Keyword is required for searching").build();
            }
            return Response.ok(noteRepository.searchNotes(keyword).stream()
                            .map(NoteDto::from)
                            .collect(Collectors.toList()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error searching notes").build();
        }
    }

    /**
     * Lists the titles of all notes.
     *
     * @return A JSON response containing the list of note titles.
     */
    @GET
    @Path("/titles")
    public Response listAllTitles() {
        try {
            return Response.ok(noteRepository.listAllTitles()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error fetching note titles").build();
        }
    }
}
