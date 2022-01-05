package com.evernote;

import com.evernote.edam.type.SharedNotebook;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.OrganizerConstants;
import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;
import com.evernote.clients.ClientFactory;
import com.evernote.clients.NoteStoreClient;
import com.evernote.clients.UserStoreClient;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.*;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Notebook;
import com.evernote.thrift.TException;
import com.google.inject.Inject;
import com.interfaces.Evernoteable;
import com.OrganizerServerConfiguration;
import java.util.stream.Collectors;

public class EvernoteApi implements Evernoteable {

  private UserStoreClient userStore;
  private NoteStoreClient noteStore;

  /**
   * Intialize UserStore and NoteStore clients. During this step, we authenticate with the Evernote
   * web service. All of this code is boilerplate - you can copy it straight into your application.
   */
  @Inject
  public EvernoteApi(OrganizerServerConfiguration configuration) throws Exception {
    this(configuration.getEvernoteToken());
  }

  public EvernoteApi(String token) throws Exception {
    // Set up the UserStore client and check that we can speak to the server
    EvernoteAuth evernoteAuth = new EvernoteAuth(EvernoteService.PRODUCTION, token);
    ClientFactory factory = new ClientFactory(evernoteAuth);
    userStore = factory.createUserStoreClient();

    boolean versionOk = userStore.checkVersion("Evernote EvernoteApi (Java)",
        com.evernote.edam.userstore.Constants.EDAM_VERSION_MAJOR,
        com.evernote.edam.userstore.Constants.EDAM_VERSION_MINOR);
    if (!versionOk) {
      System.err.println("Incompatible Evernote client protocol version");
      System.exit(1);
    }

    // Set up the NoteStore client
    noteStore = factory.createNoteStoreClient();
  }

  public List<String> GetTodoList() {
    Note todoListNote = null;
    try {
      todoListNote = findNoteByNoteTitleAndNotebookName(
          OrganizerConstants.SERVER_ORGANIZER_NOTEBOOK_NAME,
          OrganizerConstants.SERVER_ORGANIZER_TODOLIST_NOTE_TITLE);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    String content = todoListNote.getContent();
    return parseTodoList(content);
  }

  private List<String> parseTodoList(String enmlTodoList) {
    List<String> todos = new ArrayList<String>();
    while (enmlTodoList.contains("en-todo")) {
      String todoTag = enmlTodoList
          .substring(enmlTodoList.indexOf("/en-todo"), enmlTodoList.indexOf("</div"));
      todos.add(todoTag.replace("/en-todo>", ""));
      enmlTodoList = enmlTodoList
          .substring(enmlTodoList.indexOf("</div") + 3, enmlTodoList.length());
    }
    return todos;
  }


  public List<String> parseNote(String divXml) {
    List<String> divs = new ArrayList();
    return
        Arrays.stream(
            divXml
                .substring(divXml.indexOf("<div>"), divXml.lastIndexOf("</div>"))
                .replace("</div>", "")
                .split("<div>")
        )
            .filter(divContent -> !divContent.startsWith("<"))
            .filter(divContent -> !divContent.isEmpty())
            .collect(Collectors.toList());
  }

  public Note getNoteContent(String noteGuid) {
    try {
      return noteStore.getNote(
          noteGuid,
          true,
          true,
          true,
          true);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Note findNoteByNoteTitleAndNotebookName(String notebookName, String noteTitle) {
    List<Notebook> notebooks = listNoteBooks();
    for (Notebook notebook : notebooks) {
      if (notebook.getName().equals(notebookName)) {
        NoteFilter filter = new NoteFilter();
        filter.setNotebookGuid(notebook.getGuid());
        filter.setOrder(NoteSortOrder.CREATED.getValue());
        filter.setAscending(true);

        NoteList noteList = findNotes(filter, 0, 100);
        List<Note> notes = noteList.getNotes();
        for (Note note : notes) {
          if (note.getTitle().equals(noteTitle)) {
            return getNoteContent(note.getGuid());
          }
        }
      }
    }
    return null;
  }

  public Note getItemLocation() {
    try {
      NoteFilter noteFilter = new NoteFilter();

      String guid = listNoteBooks().stream()
          .filter(notebook -> notebook.getName().equalsIgnoreCase("organizer")).collect(
              Collectors.toList()).get(0).getGuid();
      noteFilter.setNotebookGuid(guid);
      NoteList noteList = noteStore.findNotes(noteFilter, 0, 100);
      return noteStore.getNote(noteList.getNotes().get(0).getGuid(), true, false, false, false);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public List<Notebook> listAllNoteBooks() {
    return listNoteBooks();
  }

  public List<SharedNotebook> listAllSharedNoteBooks() {
    return listSharedNoteBooks();
  }

  private List<SharedNotebook> listSharedNoteBooks() {
    try {
      return noteStore.listSharedNotebooks();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private List<Notebook> listNoteBooks() {
    try {
      return noteStore.listNotebooks();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private NoteList findNotes(NoteFilter noteFilter, int offset, int maxNotes) {
    try {
      return noteStore.findNotes(noteFilter, offset, maxNotes);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
