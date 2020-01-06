package com.evernote;

        import java.util.ArrayList;
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

public class EvernoteApi{

    private static final String AUTH_TOKEN = "your developer token";

    private UserStoreClient userStore;
    private NoteStoreClient noteStore;
    /**
     * Intialize UserStore and NoteStore clients. During this step, we
     * authenticate with the Evernote web service. All of this code is boilerplate
     * - you can copy it straight into your application.
     */
    public EvernoteApi(String token) throws Exception {
        // Set up the UserStore client and check that we can speak to the server
        EvernoteAuth evernoteAuth = new EvernoteAuth(EvernoteService.SANDBOX, token);
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

    public List<String> GetTodoList() throws Exception{
        Note todoListNote = findNoteByNoteTitleAndNotebookName(OrganizerConstants.SERVER_ORGANIZER_NOTEBOOK_NAME, OrganizerConstants.SERVER_ORGANIZER_TODOLIST_NOTE_TITLE);
        String content = todoListNote.getContent();
        parseTodoList(content);
        return null;
    }

    private List<String> parseTodoList(String enmlTodoList)
    {
        List<String> todos = new ArrayList<String>();
        while(enmlTodoList.contains("en-todo")) {
            String todoTag = enmlTodoList.substring(enmlTodoList.indexOf("/en-todo"), enmlTodoList.indexOf("</div"));
            todos.add(todoTag.replace("/en-todo>",""));
            enmlTodoList = enmlTodoList.substring(enmlTodoList.indexOf("</div")+3, enmlTodoList.length());
        }
        return todos;
    }

    public Note getNoteContent(String noteGuid) throws EDAMUserException, EDAMSystemException, TException, EDAMNotFoundException {
        return noteStore.getNote(
                noteGuid,
                true,
                true,
                true,
                true);
    }

    public Note findNoteByNoteTitleAndNotebookName(String notebookName, String noteTitle) throws Exception{
        List<Notebook> notebooks = noteStore.listNotebooks();
        for (Notebook notebook : notebooks) {
            if(notebook.getName().equals(notebookName)) {
                NoteFilter filter = new NoteFilter();
                filter.setNotebookGuid(notebook.getGuid());
                filter.setOrder(NoteSortOrder.CREATED.getValue());
                filter.setAscending(true);

                NoteList noteList = noteStore.findNotes(filter, 0, 100);
                List<Note> notes = noteList.getNotes();
                for (Note note : notes) {
                    if(note.getTitle().equals(noteTitle))
                    {
                        return getNoteContent(note.getGuid());
                    }
                }
            }
        }
        return null;
    }
}
